package com.nimo.fb_effect.model;

import com.google.gson.Gson;
import com.nimo.facebeauty.FBEffect;
import com.nimo.facebeauty.model.FBItemEnum;
import com.nimo.fb_effect.utils.FBConfigTools;

import java.util.List;

/**
 * 贴纸列表配置
 */
@SuppressWarnings("unused")
public class GestureConfig {

  /**
   * stickers
   */
  private List<Gesture> gesture_effect;

  @Override public String toString() {
    return "FBMaskConfig{" +
        "fbMasks=" + gesture_effect.size() +
        "个}";
  }

  public List<Gesture> getGestures() {
    return gesture_effect;
  }

  public GestureConfig(List<Gesture> masks) {
    this.gesture_effect = masks;
  }

  public void setMasks(List<Gesture> tiMasks) { this.gesture_effect = tiMasks;}

  public static class Gesture {

    public static final Gesture NO_GESTURE = new Gesture("", "", "", FBDownloadState.COMPLETE_DOWNLOAD);
    public static final Gesture NEW_GESTURE = new Gesture("", "", "", FBDownloadState.COMPLETE_DOWNLOAD);

    /**
     * name
     */
    private String name;


    public Gesture(String name, String category, String icon, int downloaded) {
      this.name = name;
      this.category = category;
      this.icon = icon;
      this.download = downloaded;
    }

    @Override public String toString() {
      return "Gesture{" +
          "name='" + name + '\'' +
          ", category='" + category + '\'' +
          ", icon='" + icon + '\'' +
          ", downloaded=" + download +
          '}';
    }

    /**
     * category
     */
    private String category;
    /**
     * icon
     */
    private String icon;
    /**
     * downloaded
     */
    private int download;

    public String getUrl() {

      return FBEffect.shareInstance().getARItemUrlBy(FBItemEnum.FBItemMask.getValue()) + name + ".zip";

    }

    public String getName() { return name;}

    public void setName(String name) { this.name = name;}

    public String getCategory() { return category;}

    public void setCategory(String category) { this.category = category;}

    public String getIcon() {
      //todo 等待接口
      return FBEffect.shareInstance().getGestureEffectUrl() + this.icon;
//      return FBEffect.shareInstance().getARItemUrlBy(FBItemEnum.FBItemMask.getValue()) + icon;


    }

    public void setThumb(String icon) { this.icon = icon;}

    public int isDownloaded() { return download;}

    public void setDownloaded(int downloaded) {
      this.download = downloaded;
    }

    /**
     * 下载完成更新缓存数据
     */
    public void downloaded() {
      GestureConfig tiMaskConfig = FBConfigTools.getInstance().getGestureList();

      for (Gesture gesture_effect : tiMaskConfig.getGestures()) {
        if (this.name.equals(gesture_effect.name) && gesture_effect.icon.equals(this.icon)) {
          gesture_effect.setDownloaded(FBDownloadState.COMPLETE_DOWNLOAD);
        }
      }
      FBConfigTools.getInstance().maskDownload(new Gson().toJson(tiMaskConfig));
    }

  }

}
