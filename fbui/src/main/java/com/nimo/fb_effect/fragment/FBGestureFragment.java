package com.nimo.fb_effect.fragment;

import android.gesture.Gesture;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.nimo.fb_effect.R;
import com.nimo.fb_effect.adapter.GestureAdapter;
import com.nimo.fb_effect.base.FBBaseLazyFragment;
import com.nimo.fb_effect.model.FBEventAction;
import com.nimo.fb_effect.model.GestureConfig;
import com.nimo.fb_effect.utils.FBConfigCallBack;
import com.nimo.fb_effect.utils.FBConfigTools;
import com.nimo.fb_effect.utils.FBSelectedPosition;

import java.util.ArrayList;
import java.util.List;

/**
 * 手势特效
 */
public class FBGestureFragment extends FBBaseLazyFragment {

    private final List<GestureConfig.Gesture> items = new ArrayList<>();
    GestureAdapter gestureAdapter;


    @Override protected int getLayoutId() {
        return R.layout.fragment_fb_sticker;
    }

    @Override protected void initView(View view, Bundle savedInstanceState) {
        if (getContext() == null) return;

        items.clear();
        // items.add(FBMask.NO_MASK);

        GestureConfig gestureList = FBConfigTools.getInstance().getGestureList();

        if (gestureList != null && gestureList.getGestures() != null && gestureList.getGestures().size() != 0) {
            items.addAll(gestureList.getGestures());
            initRecyclerView();
        } else {
            FBConfigTools.getInstance().getGesturesConfig(new FBConfigCallBack<List<GestureConfig.Gesture>>() {
                @Override public void success(List<GestureConfig.Gesture> list) {
                    items.addAll(list);
                    initRecyclerView();
                }

                @Override public void fail(Exception error) {
                    Looper.prepare();
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            });
        }

    }

    private void initRecyclerView() {
        RecyclerView fbGestureRV = (RecyclerView) findViewById(R.id.fbRecyclerView);
        gestureAdapter = new GestureAdapter(items);
//        fbMaskRV.setLayoutManager(new GridLayoutManager(getContext(), 5));
        fbGestureRV.setAdapter(gestureAdapter);
    }

    /*@Subscribe(thread = EventThread.MAIN_THREAD,
               tags = { @Tag(FBEventAction.ACTION_SYNC_MASK_ITEM_CHANGED) })
    public void changedPoint(Object o) {
        int lastposition = FBSelectedPosition.POSITION_MASK;
        FBSelectedPosition.POSITION_MASK = -1;
        maskAdapter.notifyItemChanged(lastposition);

    }*/

//    @SuppressLint("NotifyDataSetChanged")
//    @Override protected void onFragmentStartLazy() {
//        super.onFragmentStartLazy();
//        //同步item改变
//        RxBus.get().post(FBEventAction.ACTION_SYNC_MASK_ITEM_CHANGED, "");
//    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = { @Tag(FBEventAction.ACTION_SYNC_GESTURE_ITEM_CHANGED) })
    public void changedPoint(Object o) {
        if (gestureAdapter == null) {
            Log.e("FBGestureFragment", "gestureAdapter is null, cannot notify item changed.");
            return;
        }

        int lastPosition = FBSelectedPosition.POSITION_GESTURE;

        FBSelectedPosition.POSITION_GESTURE= -1;

        if (lastPosition >= 0 && lastPosition < gestureAdapter.getItemCount()) {
            gestureAdapter.notifyItemChanged(lastPosition);
        } else {
            Log.w("FBGesturerFragment", "Invalid position: " + lastPosition);
        }
    }

}
