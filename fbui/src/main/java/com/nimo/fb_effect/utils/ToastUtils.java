package com.nimo.fb_effect.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {

    private ToastUtils() {}

    public static void show(Context context, String message) {
        if (context == null || message == null) return;

        Toast.makeText(
                context.getApplicationContext(),
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}
