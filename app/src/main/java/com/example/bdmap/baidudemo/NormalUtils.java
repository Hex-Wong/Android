/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.example.bdmap.baidudemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.bdmap.activity.DemoNaviSettingActivity;


public class NormalUtils {



    public static void gotoSettings(Activity activity) {
        Intent it = new Intent(activity, DemoNaviSettingActivity.class);
        activity.startActivity(it);
    }



    public static String getTTSAppID() {
        return "19342473";
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
