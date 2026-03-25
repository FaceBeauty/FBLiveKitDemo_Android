package com.nimo.fb_effect.adapter;

import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
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
import com.nimo.fb_effect.R;
import com.nimo.fb_effect.fragment.PorpraitGreenScreenFragment;
import com.nimo.fb_effect.model.FBDownloadState;
import com.nimo.fb_effect.model.FBEventAction;
import com.nimo.fb_effect.model.GreenScreenConfig;
import com.nimo.fb_effect.utils.FBSelectedPosition;
import com.nimo.fb_effect.utils.FBUnZip;
import com.nimo.fb_effect.utils.FileUtils;


import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 绿幕的Item适配器
 */
public class GreenScreenAdapter extends RecyclerView.Adapter<FBStickerViewHolder> {
    public static final String EDIT_GREEN_SCREEN = "EDIT_GREEN_SCREEN";
    private final int ITEM_TYPE_ONE = 1;
    private final int ITEM_TYPE_TWO = 2;
    Bitmap bitmap;

    private int selectedPosition = FBSelectedPosition.POSITION_GREEN_SCREEN;
    private int lastPosition;
    private int deletePosition = -1;

    private final List<GreenScreenConfig.GreenScreen> greenScreenList;
    private final PorpraitGreenScreenFragment.greenScreenClick greenScreenClick;


    private final Handler handler = new Handler();

    private final Map<String, String> downloadingGreenScreens = new ConcurrentHashMap<>();

    public GreenScreenAdapter(List<GreenScreenConfig.GreenScreen> greenScreenList, PorpraitGreenScreenFragment.greenScreenClick click) {
        this.greenScreenList = greenScreenList;
        greenScreenClick = click;
        DownloadDispatcher.setMaxParallelRunningCount(5);
    }

    @Override
    public int getItemViewType(int position) {
        // if (position == 0 || position == 1) {
        if(position == 0){
            return ITEM_TYPE_ONE;
        } else {
            return ITEM_TYPE_TWO;
        }
    }

    @NonNull
    @Override
    public FBStickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == ITEM_TYPE_ONE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fb_sticker_one, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fb_sticker, parent, false);
        }
        return new FBStickerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FBStickerViewHolder holder, int position) {

        final GreenScreenConfig.GreenScreen htGreenScreen = greenScreenList.get(holder.getAdapterPosition());
        selectedPosition = FBSelectedPosition.POSITION_GREEN_SCREEN;
        holder.deleteIV.setVisibility(View.GONE);
        if (selectedPosition == position) {
            holder.itemView.setSelected(true);
        } else {
            holder.itemView.setSelected(false);
        }

        //显示封面
        if (htGreenScreen == GreenScreenConfig.GreenScreen.NO_GreenScreen) {
            holder.thumbIV.setImageResource(R.mipmap.icon_ht_none_sticker);
        } else {
            if(position == 1){
                Glide.with(holder.itemView.getContext())
                    .load(R.drawable.resource_shangchuan)
                    .into(holder.thumbIV);
            }else if(htGreenScreen.isFromDisk()){
                //来自硬盘的直接加载本体图片
                Glide.with(holder.itemView.getContext())
                    .load(new File(htGreenScreen.getDir()))
                    .placeholder(R.drawable.icon_placeholder)
                    .into(holder.thumbIV);
            }else{
                Glide.with(holder.itemView.getContext())
                    .load(greenScreenList.get(position).getIcon())
                    .placeholder(R.drawable.icon_placeholder)
                    .into(holder.thumbIV);
            }

        }

        //判断是否已经下载
        if (htGreenScreen.isDownloaded() == FBDownloadState.COMPLETE_DOWNLOAD) {
            holder.downloadIV.setVisibility(View.GONE);
            holder.loadingIV.setVisibility(View.GONE);
            holder.loadingBG.setVisibility(View.GONE);
            holder.stopLoadingAnimation();
        } else {
            //判断是否正在下载，如果正在下载，则显示加载动画
            if (downloadingGreenScreens.containsKey(htGreenScreen.getName())) {
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
        holder.deleteIV.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                if (htGreenScreen.isFromDisk()) {
                    if(holder.getAdapterPosition() == selectedPosition){
                        //如果点击已选中的效果，则取消效果
                        FBEffect.shareInstance().setChromaKeyingScene("");
                        FBSelectedPosition.POSITION_GREEN_SCREEN = -1;
                        notifyItemChanged(selectedPosition);
                    }
                    htGreenScreen.delete(deletePosition - 1);
                    greenScreenClick.deleteGreenScreenFromDisk(deletePosition);
                    holder.itemView.setClickable(false);
                    FileUtils.deleteDirOrFile(htGreenScreen.getDir());
                    String path = FBEffect.shareInstance().getChromaKeyingPath() + "/" + htGreenScreen.getName();
                    FileUtils.deleteDirOrFile(path);

                }

            }
        });

        holder.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (htGreenScreen.isFromDisk()) {
                    if(holder.getAdapterPosition() == selectedPosition){
                        //如果点击已选中的效果，则取消效果
                        FBEffect.shareInstance().setChromaKeyingScene("");
                        FBSelectedPosition.POSITION_GREEN_SCREEN = -1;
                        notifyItemChanged(selectedPosition);
                    }else{
                        FBEffect.shareInstance().setChromaKeyingScene(htGreenScreen.getName());

                        int lastPosition = selectedPosition;
                        selectedPosition = holder.getAdapterPosition();
                        FBSelectedPosition.POSITION_GREEN_SCREEN = selectedPosition;
                        notifyItemChanged(selectedPosition);
                        notifyItemChanged(lastPosition);
                    }
                }else{
                    if(holder.getAdapterPosition() == 1){
                        greenScreenClick.clickGreenScreenFromDisk();
                        return;

                    }else{
                        //如果没有下载，则开始下载到本地
                        if (htGreenScreen.isDownloaded() == FBDownloadState.NOT_DOWNLOAD) {
                            int currentPosition = holder.getAdapterPosition();

                            //如果已经在下载了，则不操作
                            if (downloadingGreenScreens.containsKey(htGreenScreen.getName())) {
                                return;
                            }
                            new DownloadTask.Builder(htGreenScreen.getUrl(), new File(FBEffect.shareInstance().getChromaKeyingPath()))
                                .setMinIntervalMillisCallbackProcess(30)
                                .setConnectionCount(1)
                                .build()
                                .enqueue(new DownloadListener2() {
                                    @Override
                                    public void taskStart(@NonNull DownloadTask task) {
                                        downloadingGreenScreens.put(htGreenScreen.getName(), htGreenScreen.getUrl());
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                notifyDataSetChanged();
                                            }
                                        });
                                    }

                                    @Override
                                    public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause, @Nullable final Exception realCause) {
                                        downloadingGreenScreens.remove(htGreenScreen.getName());
                                        if (cause == EndCause.COMPLETED) {
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    File targetDir =
                                                        new File(FBEffect.shareInstance().getChromaKeyingPath());
                                                    File file = task.getFile();
                                                    try {
                                                        //解压到贴纸目录
                                                        FBUnZip.unzip(file, targetDir);
                                                        if (file != null) {
                                                            file.delete();
                                                        }

                                                        //修改内存与文件
                                                        htGreenScreen.setDownloaded(FBDownloadState.COMPLETE_DOWNLOAD);
                                                        htGreenScreen.downLoaded();

                                                        FBEffect.shareInstance().setChromaKeyingScene(htGreenScreen.getName());
                                                        lastPosition = selectedPosition;
                                                        selectedPosition = currentPosition;
                                                        FBSelectedPosition.POSITION_GREEN_SCREEN = selectedPosition;


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

                                            // //修改内存与文件
                                            // htGreenScreen.setDownloaded(FBDownloadState.COMPLETE_DOWNLOAD);
                                            // htGreenScreen.downLoaded();

                                            // handler.post(new Runnable() {
                                            //     @Override
                                            //     public void run() {
                                            //         notifyDataSetChanged();
                                            //     }
                                            // });

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
                                FBEffect.shareInstance().setChromaKeyingScene("");
                                FBSelectedPosition.POSITION_GREEN_SCREEN = 0;
                                notifyItemChanged(selectedPosition);
                                // notifyItemChanged(-1);
                            }else{
                                //如果已经下载了，则让水印生效
                                if (htGreenScreen == GreenScreenConfig.GreenScreen.NO_GreenScreen) {
                                    FBEffect.shareInstance().setChromaKeyingScene("");
                                } else {
                                    FBEffect.shareInstance().setChromaKeyingScene(htGreenScreen.getName());
                                }

                                // RxBus.get().post(HTEventAction.ACTION_GREEN_SCREEN, htGreenScreen.getName());

                                //切换选中背景
                                int lastPosition = selectedPosition;
                                selectedPosition = holder.getAdapterPosition();
                                FBSelectedPosition.POSITION_GREEN_SCREEN = selectedPosition;
                                notifyItemChanged(selectedPosition);
                                notifyItemChanged(lastPosition);
                                //刷新编辑按钮状态
                                notifyItemChanged(1);
                            }


                        }

                    }
                }
                if(deletePosition != -1){
                    notifyItemChanged(deletePosition);
                    deletePosition = -1;
                }
                RxBus.get().post(FBEventAction.ACTION_SYNC_PROGRESS, "");



            }
        });
        holder.itemView.setOnLongClickListener(new OnLongClickListener() {
            @Override public boolean onLongClick(View v) {
                if (htGreenScreen.isFromDisk()) {
                    holder.deleteIV.setVisibility(View.VISIBLE);
                    deletePosition = holder.getAdapterPosition();
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return greenScreenList == null ? 0 : greenScreenList.size();
    }
    public void selectItem(int selectedPosition) {
        int lastPosition = selectedPosition;
        this.selectedPosition = selectedPosition;
        FBSelectedPosition.POSITION_GREEN_SCREEN = selectedPosition;
        FBEffect.shareInstance().setChromaKeyingScene(greenScreenList.get(selectedPosition).getName());
        notifyItemChanged(selectedPosition);
        notifyItemChanged(lastPosition);
    }
}
