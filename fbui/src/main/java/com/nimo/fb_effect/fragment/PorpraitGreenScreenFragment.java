package com.nimo.fb_effect.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.nimo.fb_effect.R;
import com.nimo.fb_effect.adapter.GreenScreenAdapter;
import com.nimo.fb_effect.base.FBBaseLazyFragment;
import com.nimo.fb_effect.model.FBDownloadState;
import com.nimo.fb_effect.model.FBEventAction;
import com.nimo.fb_effect.model.FBState;
import com.nimo.fb_effect.model.FBViewState;
import com.nimo.fb_effect.model.GreenScreenConfig;
import com.nimo.fb_effect.utils.FBConfigCallBack;
import com.nimo.fb_effect.utils.FBConfigTools;
import com.nimo.fb_effect.utils.FBSelectedPosition;
import com.nimo.fb_effect.utils.FBUploadBitmapUtils;


import java.util.ArrayList;
import java.util.List;

/**
 * 绿幕抠图
 */
public class PorpraitGreenScreenFragment extends FBBaseLazyFragment {

    private final List<GreenScreenConfig.GreenScreen> items = new ArrayList<>();
    GreenScreenAdapter greenScreenAdapter;
    private static final int IMAGE_REQUEST_CODE = 0;//标题图片的选中返回

    @Override protected int getLayoutId() {
        return R.layout.fragment_fb_sticker;
    }

    @Override protected void initView(View view, Bundle savedInstanceState) {
        if (getContext() == null) return;

        items.clear();
        items.add(GreenScreenConfig.GreenScreen.NO_GreenScreen);
        //items.add(new GreenScreenConfig.GreenScreen(HtGreenScreenAdapter.EDIT_GREEN_SCREEN, "", FBDownloadState.COMPLETE_DOWNLOAD));

        GreenScreenConfig greenScreenList = FBConfigTools.getInstance().getGreenScreenList();

        if (greenScreenList != null && greenScreenList.getGreenScreens() != null && greenScreenList.getGreenScreens().size() != 0) {
            items.addAll(greenScreenList.getGreenScreens());
            initRecyclerView();
        } else {
            FBConfigTools.getInstance().getGreenScreenConfig(new FBConfigCallBack<List<GreenScreenConfig.GreenScreen>>() {
                @Override public void success(List<GreenScreenConfig.GreenScreen> list) {
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

    @Override protected void onFragmentStartLazy() {
        // RxBus.get().post(FBEventAction.ACTION_SHOW_SCREEN_COLOR, 0);
        FBState.currentSecondViewState = FBViewState.GREENSCREEN_BACKGROUND;
        RxBus.get().post(FBEventAction.ACTION_SYNC_PROGRESS, "");


        super.onFragmentStartLazy();
    }

    private void initRecyclerView() {
        RecyclerView fbGreenScreenRV = (RecyclerView) findViewById(R.id.fbRecyclerView);
        greenScreenAdapter = new GreenScreenAdapter(items, new greenScreenClick() {
            @Override public void clickGreenScreenFromDisk() {
                //调起相册
                openAlbum();
            }

            @Override public void deleteGreenScreenFromDisk(int position) {
                deleteGreenScreen(position);

            }
        });
        fbGreenScreenRV.setLayoutManager(new GridLayoutManager(getContext(), 5));
        fbGreenScreenRV.setAdapter(greenScreenAdapter);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
               tags = { @Tag(FBEventAction.ACTION_SYNC_PORTRAITTGS_ITEM_CHANGED) })
    public void changedPoint(Object o) {
        int lastposition = FBSelectedPosition.POSITION_GREEN_SCREEN;
        FBSelectedPosition.POSITION_GREEN_SCREEN = -1;
        greenScreenAdapter.notifyItemChanged(lastposition);

    }

    /**
     * 打开相册
     */
    private void openAlbum() {
        //检测权限
        if (ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
        }
        //调取系统相册
        Intent intent = new Intent(Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null || data.getData() == null) return;
        if (requestCode == IMAGE_REQUEST_CODE) {
            String imagePath = FBUploadBitmapUtils.handleImageBeforeKitKat(data, getActivity());

            if(imagePath != null){
                GreenScreenConfig.GreenScreen htGreenScreen = new GreenScreenConfig.GreenScreen("", "", FBDownloadState.COMPLETE_DOWNLOAD,"");
                htGreenScreen.setFromDisk(true, requireContext(), imagePath);
                items.add(htGreenScreen);
                greenScreenAdapter.selectItem(items.size() - 1);
                greenScreenAdapter.notifyDataSetChanged();
            }

        }

    }

    /*
    删除绿幕
     */
    private void deleteGreenScreen(int position) {
        items.remove(items.get(position));
        // FBConfigTools.getInstance().watermarkDownload(new Gson().toJson(items));
        // watermarkAdapter.selectItem(0);
        greenScreenAdapter.notifyDataSetChanged();
    }

    public interface greenScreenClick {
        //点击从相册中选择背景
        void clickGreenScreenFromDisk();
        //删除从相册选择的背景
        void deleteGreenScreenFromDisk(int position);

    }

    @Override protected void onDestroyViewLazy() {
        super.onDestroyViewLazy();
        // RxBus.get().unregister(this);
    }
}
