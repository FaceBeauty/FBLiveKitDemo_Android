package com.nimo.fb_effect.fragment;

import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.nimo.fb_effect.R;
import com.nimo.fb_effect.adapter.AISegmentationAdapter;
import com.nimo.fb_effect.base.FBBaseLazyFragment;
import com.nimo.fb_effect.model.AISegmentationConfig;
import com.nimo.fb_effect.model.FBEventAction;
import com.nimo.fb_effect.utils.FBConfigCallBack;
import com.nimo.fb_effect.utils.FBConfigTools;
import com.nimo.fb_effect.utils.FBSelectedPosition;


import java.util.ArrayList;
import java.util.List;

/**
 * 人像抠图——AI抠图
 */
public class PortraitAIFragment extends FBBaseLazyFragment {

    private final List<AISegmentationConfig.AISegmentation> items = new ArrayList<>();
    AISegmentationAdapter adapter;

    @Override protected int getLayoutId() {
        return R.layout.fragment_fb_sticker;
    }

    @Override protected void initView(View view, Bundle savedInstanceState) {
        if (getContext() == null) return;

        items.clear();
        items.add(AISegmentationConfig.AISegmentation.NO_Portrait);


        AISegmentationConfig portraitConfig = FBConfigTools.getInstance().getAISegmentationList();
        if (portraitConfig != null) {
            items.addAll(portraitConfig.getSegmentations());
            initRecyclerView();
        } else {
            FBConfigTools.getInstance().getAISegmentationConfig(new FBConfigCallBack<List<AISegmentationConfig.AISegmentation>>() {
                @Override public void success(List<AISegmentationConfig.AISegmentation> list) {
                    items.addAll(list);
                    initRecyclerView();
                }

                @Override public void fail(Exception error) {
                    error.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            });
        }

    }

    private void initRecyclerView() {
        adapter = new AISegmentationAdapter(items);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.fbRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 5));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
               tags = { @Tag(FBEventAction.ACTION_SYNC_PORTRAITAI_ITEM_CHANGED) })
    public void changedPoint(Object o) {
        int lastposition = FBSelectedPosition.POSITION_AISEGMENTATION;
        FBSelectedPosition.POSITION_AISEGMENTATION = -1;
        adapter.notifyItemChanged(lastposition);

    }

    @Override protected void onFragmentStartLazy() {
        super.onFragmentStartLazy();
    }
}
