package com.nimo.fb_effect.model;

import com.google.gson.Gson;
import com.nimo.facebeauty.FBEffect;
import com.nimo.fb_effect.utils.FBConfigTools;


import java.util.List;

/**
 * 人像抠图配置参数
 */

@SuppressWarnings("unused")
public class AISegmentationConfig {
  /**
   * portraits
   */
  private List<AISegmentation> aiseg_effect;

  public List<AISegmentation> getSegmentations() { return aiseg_effect;}

  public void setPortraits(List<AISegmentation> segmentations) { this.aiseg_effect = segmentations;}

  public static class AISegmentation {

    public static final AISegmentation NO_Portrait = new AISegmentation("", "", "", FBDownloadState.COMPLETE_DOWNLOAD);

    public AISegmentation(String name, String category, String icon, int downloaded) {
      this.name = name;
      this.category = category;
      this.icon = icon;
      this.download = downloaded;
    }

    /**
     * name
     */
    private String name;
    /**
     * category
     */
    private String category;
    /**
     * thumb
     */
    private String icon;
    /**
     * downloaded
     */
    private int download;

    public String getName() { return name;}

    public void setName(String name) { this.name = name;}

    public String getCategory() { return category;}

    public void setCategory(String category) { this.category = category;}

    public String getIcon() {
      return FBEffect.shareInstance().getAISegEffectUrl() + icon;
    }

    public void setIcon(String icon) { this.icon = icon;}

    public int isDownloaded() { return download;}

    public String getUrl() {
      return FBEffect.shareInstance().getAISegEffectUrl() + this.name + ".zip";

    }

    public void downloaded() {
      AISegmentationConfig segmentationList = FBConfigTools.getInstance().getAISegmentationList();

      for (AISegmentation portrait : segmentationList.getSegmentations()) {
        if (portrait.name.equals(this.name) && portrait.icon.equals(this.icon)) {
          portrait.setDownloaded(FBDownloadState.COMPLETE_DOWNLOAD);
        }
      }

      FBConfigTools.getInstance().segmentationDownload(new Gson().toJson(segmentationList));

    }

    public void setDownloaded(int downloaded) {
      this.download = downloaded;
    }
  }
}
