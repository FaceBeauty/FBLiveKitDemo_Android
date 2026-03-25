package com.nimo.fb_effect.model;

import android.content.Context;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.nimo.fb_effect.R;

/**
 * 美体枚举类
 */
public enum BeautyBody {
    long_legs(R.string.longleg, R.drawable.ic_whiteness_black, R.drawable.ic_long_legs_white, BeautyBodyKey.long_legs),
    slimming_down(R.string.bodythin, R.drawable.ic_blurriness_black, R.drawable.ic_slimming_down, BeautyBodyKey.slimming_down),
    slender_waist(R.string.waistslim, R.drawable.ic_rosiness_black, R.drawable.ic_slender_waist, BeautyBodyKey.slender_waist),
    beautiful_shoulder(R.string.shoulderslim, R.drawable.ic_clearness_black, R.drawable.ic_beautiful_shoulder, BeautyBodyKey.beautiful_shoulder),
    hip_repair(R.string.hiptrim, R.drawable.ic_sharpness_black, R.drawable.ic_hip_repair, BeautyBodyKey.hip_repair),
    thin_thigh(R.string.thighthin, R.drawable.ic_sharpfeatured_black, R.drawable.ic_thin_thigh, BeautyBodyKey.thin_thigh),
    swan_neck(R.string.neckslim, R.drawable.ic_brightness_black, R.drawable.ic_swan_neck, BeautyBodyKey.swan_neck),
    breast_augmentation(R.string.chestenlarge, R.drawable.ic_dark_circle_black, R.drawable.ic_breast_augmentation, BeautyBodyKey.breast_augmentation);

    //名称
    private final int name;
    //黑色图标
    private final int drawableRes_black;
    //白色图标
    private final int drawableRes_white;
    //对应的key
    private final BeautyBodyKey fbBeautyBodyKey;


    public BeautyBodyKey getFBBeautyBodyKey() {
        return fbBeautyBodyKey;
    }

    public String getName(@NonNull Context context) {
        return context.getString(name);
    }

    public int getDrawableRes_black() {
        return drawableRes_black;
    }

    public int getDrawableRes_white() {
        return drawableRes_white;
    }

    BeautyBody(@StringRes int name, @DrawableRes int drawableRes_black, @DrawableRes int drawableResWhite, BeautyBodyKey fbBeautyBodyKey) {
        this.name = name;
        this.drawableRes_white = drawableResWhite;
        this.drawableRes_black = drawableRes_black;
        this.fbBeautyBodyKey = fbBeautyBodyKey;
    }
}
