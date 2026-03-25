package com.nimo.fb_effect.adapter;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hwangjr.rxbus.RxBus;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.dispatcher.DownloadDispatcher;
import com.liulishuo.okdownload.core.listener.DownloadListener2;
import com.nimo.facebeauty.FBEffect;
import com.nimo.facebeauty.model.FBItemEnum;
import com.nimo.fb_effect.R;
import com.nimo.fb_effect.model.FBDownloadState;
import com.nimo.fb_effect.model.FBEventAction;
import com.nimo.fb_effect.model.GestureConfig;
import com.nimo.fb_effect.utils.FBSelectedPosition;
import com.nimo.fb_effect.utils.FBUnZip;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 贴纸Item的适配器
 */
public class GestureAdapter extends RecyclerView.Adapter<FBStickerViewHolder> {

    private final int ITEM_TYPE_ONE = 1;

    private final int ITEM_TYPE_TWO = 2;

    private int selectedPosition = FBSelectedPosition.POSITION_GESTURE;
    private int lastPosition;

    private final List<GestureConfig.Gesture> gestureList;

    private Handler handler = new Handler();

    private Map<String, String> downloadingGestures = new ConcurrentHashMap<>();

    public GestureAdapter(List<GestureConfig.Gesture> gestureList) {
        this.gestureList = gestureList;
        DownloadDispatcher.setMaxParallelRunningCount(5);
    }

    @Override
    public int getItemViewType(int position) {
        // if (position == 0) {
        //     return ITEM_TYPE_ONE;
        // } else {
            return ITEM_TYPE_TWO;
        // }
    }

    @NonNull
    @Override
    public FBStickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fb_sticker, parent, false);
        return new FBStickerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FBStickerViewHolder holder, int position) {

        final GestureConfig.Gesture fbGesture = gestureList.get(holder.getAdapterPosition());

        selectedPosition = FBSelectedPosition.POSITION_GESTURE;
        if (selectedPosition == position) {
            holder.itemView.setSelected(true);
        } else {
            holder.itemView.setSelected(false);
        }

        //显示封面
        if (fbGesture == GestureConfig.Gesture.NO_GESTURE) {
            holder.thumbIV.setImageResource(R.mipmap.icon_ht_none_sticker);
        } else {
            Glide.with(holder.itemView.getContext())
                .load(gestureList.get(position).getIcon())
                .placeholder(R.drawable.icon_placeholder)
                .into(holder.thumbIV);
//            holder.thumbIV.setImageDrawable(FBMaskResEnum.values()[holder.getAdapterPosition()].getIcon(holder.itemView.getContext()));
        }

        //判断是否已经下载
        if (fbGesture.isDownloaded() == FBDownloadState.COMPLETE_DOWNLOAD) {
            holder.downloadIV.setVisibility(View.GONE);
            holder.loadingIV.setVisibility(View.GONE);
            holder.loadingBG.setVisibility(View.GONE);
            holder.stopLoadingAnimation();
        } else {
            //判断是否正在下载，如果正在下载，则显示加载动画
            if (downloadingGestures.containsKey(fbGesture.getName())) {
                holder.downloadIV.setVisibility(View.GONE);
                holder.loadingIV.setVisibility(View.VISIBLE);
                holder.loadingBG.setVisibility(View.VISIBLE);
                holder.startLoadingAnimation();
            } else {
                holder.downloadIV.setVisibility(View.VISIBLE);
                holder.loadingIV.setVisibility(View.GONE);
                holder.loadingBG.setVisibility(View.GONE);
                holder.stopLoadingAnimation();
            }
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //如果没有下载，则开始下载到本地
                if (fbGesture.isDownloaded() == FBDownloadState.NOT_DOWNLOAD) {
                    int currentPosition = holder.getAdapterPosition();

                    //如果已经在下载了，则不操作
                    if (downloadingGestures.containsKey(fbGesture.getName())) {
                        return;
                    }

                    new DownloadTask.Builder(fbGesture.getUrl(), new File(FBEffect.shareInstance().getGestureEffectPath()))
                            .setMinIntervalMillisCallbackProcess(30)
                            .setConnectionCount(1)
                            .build()
                            .enqueue(new DownloadListener2() {
                                @Override
                                public void taskStart(@NonNull DownloadTask task) {
                                    downloadingGestures.put(fbGesture.getName(), fbGesture.getUrl());
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            notifyDataSetChanged();
                                        }
                                    });
                                }

                                @Override
                                public void taskEnd(@NonNull final DownloadTask task, @NonNull EndCause cause, @Nullable final Exception realCause) {
                                    downloadingGestures.remove(fbGesture.getName());

                                    if (cause == EndCause.COMPLETED) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                File targetDir =
                                                    new File(FBEffect.shareInstance().getGestureEffectPath());
                                                File file = task.getFile();
                                                try {
                                                    //解压到手势目录
                                                    FBUnZip.unzip(file, targetDir);
                                                    if (file != null) {
                                                        file.delete();
                                                    }

                                                    //修改内存与文件
                                                    fbGesture.setDownloaded(FBDownloadState.COMPLETE_DOWNLOAD);
                                                    fbGesture.downloaded();

                                                    FBEffect.shareInstance().setGestureEffect(fbGesture.getName());
                                                    lastPosition = selectedPosition;
                                                    selectedPosition = currentPosition;
                                                    FBSelectedPosition.POSITION_GESTURE = selectedPosition;

                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run(){
                                                            notifyDataSetChanged();
                                                        }
                                                    });

                                                } catch (Exception e) {
                                                    if (file != null) {
                                                        file.delete();
                                                    }
                                                }
                                            }
                                        }).start();

                                    } else {
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                notifyDataSetChanged();
                                                if (realCause != null) {
                                                    Toast.makeText(holder.itemView.getContext(), realCause.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                    notifyItemChanged(selectedPosition);
                                    notifyItemChanged(lastPosition);
                                }
                            });

                } else {
                    if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) {
                        return;
                    }
                    if (holder.getAdapterPosition() == selectedPosition){
                        //如果点击已选中的效果，则取消效果
                        FBEffect.shareInstance().setGestureEffect("");
                        FBSelectedPosition.POSITION_GESTURE = -1;
                        notifyItemChanged(selectedPosition);
                        // notifyItemChanged(-1);
                    }else{
                        //如果已经下载了，则让手势生效
                        FBEffect.shareInstance().setGestureEffect(fbGesture.getName());

                        //切换选中背景
                        int lastPosition = selectedPosition;
                        selectedPosition = holder.getAdapterPosition();
                        FBSelectedPosition.POSITION_GESTURE = selectedPosition;
                        notifyItemChanged(selectedPosition);
                        notifyItemChanged(lastPosition);
//                        RxBus.get().post(FBEventAction.ACTION_RENDER_PICTURE, "");
                    }



                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return gestureList == null ? 0 : gestureList.size();
    }
}
