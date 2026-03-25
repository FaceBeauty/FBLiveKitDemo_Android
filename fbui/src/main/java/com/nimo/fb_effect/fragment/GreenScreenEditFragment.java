package com.nimo.fb_effect.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.nimo.fb_effect.R;
import com.nimo.fb_effect.base.FBBaseLazyFragment;
import com.nimo.fb_effect.model.FBEventAction;
import com.nimo.fb_effect.model.FBState;
import com.nimo.fb_effect.model.FBViewState;
import com.nimo.fb_effect.utils.FBSelectedPosition;
import com.nimo.fb_effect.utils.FBUICacheUtils;
import com.nimo.fb_effect.view.FBResetDialog;

public class GreenScreenEditFragment extends FBBaseLazyFragment {
    private ImageView ivRestore;
    private TextView tvRestore;
    private ImageView ivSimilarity;
    private TextView tvSimilarity;
    private ImageView ivSmoothness;
    private TextView tvSmoothness;
    private ImageView ivAlpha;
    private TextView tvAlpha;
    private ImageView ivDecolor;
    private TextView tvDecolor;

    private final FBResetDialog resetDialog = new FBResetDialog();

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_fb_green_screen_edit;
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        ivRestore = findViewById(R.id.iv_gs_restore);
        tvRestore = findViewById(R.id.tv_gs_restore);
        ivSimilarity = findViewById(R.id.iv_gs_similarity);
        tvSimilarity = findViewById(R.id.tv_gs_similarity);
        ivSmoothness = findViewById(R.id.iv_gs_smoothness);
        tvSmoothness = findViewById(R.id.tv_gs_smoothness);
        ivAlpha = findViewById(R.id.iv_gs_alpha);
        tvAlpha = findViewById(R.id.tv_gs_alpha);
        ivDecolor = findViewById(R.id.iv_gs_decolor);
        tvDecolor = findViewById(R.id.tv_gs_decolor);


        ivSimilarity.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                whichSelected(0);
                FBUICacheUtils.beautyEditPosition(0);

            }
        });
        ivSmoothness.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                whichSelected(1);
                FBUICacheUtils.beautyEditPosition(1);

            }
        });
        ivAlpha.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                whichSelected(3);
                FBUICacheUtils.beautyEditPosition(3);

            }
        });

        ivDecolor.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                whichSelected(2);
                FBUICacheUtils.beautyEditPosition(2);

            }
        });

        ivRestore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                resetDialog.show(getChildFragmentManager(), "greenscreen");
                //RxBus.get().post(FBEventAction.ACTION_SYNC_RESET, "");
            }
        });


    }


    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(FBEventAction.ACTION_CHANGE_EDIT_ITEM)})
    public void whichSelected(int position) {
        if (position == 0) {
            ivSimilarity.setSelected(true);
            tvSimilarity.setSelected(true);
            ivSmoothness.setSelected(false);
            tvSmoothness.setSelected(false);
            ivAlpha.setSelected(false);
            tvAlpha.setSelected(false);
            ivDecolor.setSelected(false);
            tvDecolor.setSelected(false);

            FBState.currentSecondViewState = FBViewState.GREENSCREEN_SIMILARITY;

            RxBus.get().post(FBEventAction.ACTION_SYNC_PROGRESS, "");

        } else if (position == 1) {
            ivSimilarity.setSelected(false);
            tvSimilarity.setSelected(false);
            ivSmoothness.setSelected(true);
            tvSmoothness.setSelected(true);
            ivAlpha.setSelected(false);
            tvAlpha.setSelected(false);
            ivDecolor.setSelected(false);
            tvDecolor.setSelected(false);
            FBState.currentSecondViewState = FBViewState.GREENSCREEN_SMOOTHNESS;
            RxBus.get().post(FBEventAction.ACTION_SYNC_PROGRESS, "");

        } else if (position == 3) {
            ivSimilarity.setSelected(false);
            tvSimilarity.setSelected(false);
            ivSmoothness.setSelected(false);
            tvSmoothness.setSelected(false);
            ivAlpha.setSelected(true);
            tvAlpha.setSelected(true);
            ivDecolor.setSelected(false);
            tvDecolor.setSelected(false);
            FBState.currentSecondViewState = FBViewState.GREENSCREEN_ALPHA;
            RxBus.get().post(FBEventAction.ACTION_SYNC_PROGRESS, "");

        } else if (position == 2) {
            ivSimilarity.setSelected(false);
            tvSimilarity.setSelected(false);
            ivSmoothness.setSelected(false);
            tvSmoothness.setSelected(false);
            ivAlpha.setSelected(false);
            tvAlpha.setSelected(false);
            ivDecolor.setSelected(true);
            tvDecolor.setSelected(true);
            FBState.currentSecondViewState = FBViewState.GREENSCREEN_DECOLOR;
            RxBus.get().post(FBEventAction.ACTION_SYNC_PROGRESS, "");
        } else {
            ivSimilarity.setSelected(false);
            tvSimilarity.setSelected(false);
            ivSmoothness.setSelected(false);
            tvSmoothness.setSelected(false);
            ivAlpha.setSelected(false);
            tvAlpha.setSelected(false);
            ivDecolor.setSelected(false);
            tvDecolor.setSelected(false);
            RxBus.get().post(FBEventAction.ACTION_SYNC_PROGRESS, "");
        }
        FBSelectedPosition.POSITION_GREEN_SCREEN_EDIT = position;
    }

    /**
     * 同步重置按钮状态
     *
     * @param message support版本Rxbus
     *                传入boolean类型会导致接收不到参数
     */
    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(FBEventAction.ACTION_SYNC_RESET)})
    public void syncReset(String message) {
        ivRestore.setEnabled(FBUICacheUtils.greenscreenResetEnable());
        tvRestore.setEnabled(FBUICacheUtils.greenscreenResetEnable());

        if (message.equals("true")) {
            // for (HtSkinItem item : itemViews) {
            //     item.updateData();
            // }
        }

        // btnBlur.update();
        RxBus.get().post(FBEventAction.ACTION_SYNC_ITEM_CHANGED, "");

    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onFragmentStartLazy() {
        super.onFragmentStartLazy();
        //更新ui状态
        FBState.currentViewState = FBViewState.PORTRAIT;

        // int position = FBUICacheUtils.beautyEditPosition();
        // whichSelected(position);


        // RxBus.get().post(FBEventAction.ACTION_SHOW_SCREEN_COLOR, true);
        syncReset("");
        int selectPosition = FBSelectedPosition.POSITION_GREEN_SCREEN_EDIT;
        whichSelected(selectPosition);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ivSimilarity.setSelected(false);
        tvSimilarity.setSelected(false);
        ivSmoothness.setSelected(false);
        tvSmoothness.setSelected(false);
        ivAlpha.setSelected(false);
        tvAlpha.setSelected(false);
        ivDecolor.setSelected(false);
        tvDecolor.setSelected(false);
    }
}