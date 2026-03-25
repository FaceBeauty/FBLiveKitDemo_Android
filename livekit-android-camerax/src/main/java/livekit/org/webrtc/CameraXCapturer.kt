/*
 * Copyright 2024-2025 LiveKit, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package livekit.org.webrtc

import android.content.Context
import android.hardware.camera2.CameraManager
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import androidx.camera.core.UseCase
import androidx.lifecycle.LifecycleOwner
import com.nimo.facebeauty.FBEffect
import com.nimo.facebeauty.model.FBRotationEnum
import io.livekit.android.room.track.video.CameraCapturerWithSize
import io.livekit.android.room.track.video.CameraEventsDispatchHandler
import io.livekit.android.util.FlowObservable
import io.livekit.android.util.flow
import io.livekit.android.util.flowDelegate
import kotlinx.coroutines.flow.StateFlow

@ExperimentalCamera2Interop
internal class CameraXCapturer(
    enumerator: CameraXEnumerator,
    private val lifecycleOwner: LifecycleOwner,
    cameraName: String?,
    eventsHandler: CameraVideoCapturer.CameraEventsHandler?,
    private val useCases: Array<out UseCase> = emptyArray(),
    //todo --facebeauty--start--3
    protected var init: Boolean = false,
    //todo --facebeauty--end--3
) : CameraCapturer(cameraName, eventsHandler, enumerator) {

    @FlowObservable
    @get:FlowObservable
    var currentCamera by flowDelegate<Camera?>(null)

    override fun createCameraSession(
        createSessionCallback: CameraSession.CreateSessionCallback,
        events: CameraSession.Events,
        applicationContext: Context,
        surfaceTextureHelper: SurfaceTextureHelper,
        cameraName: String,
        width: Int,
        height: Int,
        framerate: Int,
    ) {
        CameraXSession(
            object : CameraSession.CreateSessionCallback {
                override fun onDone(session: CameraSession) {
                    createSessionCallback.onDone(session)
                    currentCamera = (session as CameraXSession).camera
                    //todo --facebeauty--start--5
                    FBEffect.shareInstance().releaseTextureOESRenderer()
                    init=false
                    //todo --facebeauty--end--5
                }

                override fun onFailure(failureType: CameraSession.FailureType, error: String) {
                    createSessionCallback.onFailure(failureType, error)
                }
            },
            object : CameraSession.Events {
                override fun onCameraOpening() {
                    events.onCameraOpening()
                }

                override fun onCameraError(session: CameraSession, error: String) {
                    events.onCameraError(session, error)
                }

                override fun onCameraDisconnected(session: CameraSession) {
                    events.onCameraDisconnected(session)
                }

                override fun onCameraClosed(session: CameraSession) {
                    events.onCameraClosed(session)
                }

                override fun onFrameCaptured(session: CameraSession, frame: VideoFrame) {
                    //todo --facebeauty--start--4
                    if (!init) {
                        init = FBEffect.shareInstance().initTextureOESRenderer(
                            frame.rotatedHeight,
                            frame.rotatedWidth,
                            FBRotationEnum.FBRotationClockwise270,
                            true,//Is it a front-facing camera.The default value is true.front-facing camera
                            5,
                        )
                        FBEffect.shareInstance().setFaceDetectionDistanceLevel(2)
                    }

                    val textureBuffer = frame.buffer as? VideoFrame.TextureBuffer ?: run {
                        events.onFrameCaptured(session, frame)
                        return
                    }

                    FBEffect.shareInstance().setFilter(0, "biaozhun", 100)//Set filter
                    FBEffect.shareInstance().setReshape(21,100)//Set the V-shaped face to 100.
                    FBEffect.shareInstance().setReshape(22,100)//The face-slimming setting is 100.
                    FBEffect.shareInstance().setReshape(20,100)//The face-slimming setting is 100
                    FBEffect.shareInstance().setARItem(0,"sticker_effect_apple")//Set Apple stickers.
                    val processedId = FBEffect.shareInstance().processTextureOES(
                        textureBuffer.textureId,
                    )

                    val originalImpl = textureBuffer as TextureBufferImpl
                    val processedBuffer = TextureBufferImpl(
                        textureBuffer.width,
                        textureBuffer.height,
                        VideoFrame.TextureBuffer.Type.RGB,
                        processedId,
                        textureBuffer.transformMatrix,
                        surfaceTextureHelper.handler,
                        originalImpl.yuvConverter,
                        null as Runnable?,
                    )

                    val processedFrame = VideoFrame(processedBuffer, frame.rotation, frame.timestampNs)
                    events.onFrameCaptured(session, processedFrame)
                    //todo --facebeauty--end--4
//                    processedFrame.release()
                }
            },
            applicationContext,
            lifecycleOwner,
            surfaceTextureHelper,
            cameraName,
            width,
            height,
            framerate,
            useCases,
        )
    }
}

@ExperimentalCamera2Interop
internal class CameraXCapturerWithSize(
    internal val capturer: CameraXCapturer,
    private val cameraManager: CameraManager,
    private val deviceName: String?,
    cameraEventsDispatchHandler: CameraEventsDispatchHandler,
) : CameraCapturerWithSize(cameraEventsDispatchHandler), CameraVideoCapturer by capturer {
    override fun findCaptureFormat(width: Int, height: Int): Size {
        return CameraXHelper.findClosestCaptureFormat(cameraManager, deviceName, width, height)
    }
}

/**
 * Gets the [androidx.camera.core.Camera] from the VideoCapturer if it's using CameraX.
 */
@OptIn(ExperimentalCamera2Interop::class)
fun VideoCapturer.getCameraX(): StateFlow<Camera?>? {
    val actualCapturer = if (this is CameraXCapturerWithSize) {
        this.capturer
    } else {
        this
    }

    if (actualCapturer is CameraXCapturer) {
        return actualCapturer::currentCamera.flow
    }
    return null
}
