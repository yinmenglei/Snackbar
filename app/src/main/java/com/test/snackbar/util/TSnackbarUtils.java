package com.test.snackbar.util;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;

/**
 * Function:
 * Author Name: yinmenglei
 * Date: 2019/7/16 12:23
 * Copyright © 2006-2018 高顿网校, All Rights Reserved.
 */
public class TSnackbarUtils {

    // 常规信息，更新条数
    public static int hint_blue = Color.parseColor("#007AFF");
    // 奖励信息
    public static int hint_orange = Color.parseColor("#FFA226");
    // 告警信息、错误信息、网络异常、数据异常
    public static int hint_red = Color.parseColor("#E62E04");
    // 执行成功、兑换成功、收藏成功
    public static int hint_green = Color.parseColor("#00af66");


    private TSnackbar suSnackbar;

    private static TSnackbarUtils tSnackbarUtils;

    public static TSnackbarUtils getInstance(){
        if(tSnackbarUtils == null){
            tSnackbarUtils = new TSnackbarUtils();
        }
        return tSnackbarUtils;
    }

    /**
     * @param view ：显示在那个view上面
     * @param hint：提示文案
     * @param color：提示颜色
     */
    public void show(View view, String hint, int color) {
        if (view == null) {
            return;
        }

        try {
            if (!TextUtils.isEmpty(hint)) {
                suSnackbar = TSnackbar.make(view, hint, TSnackbar.LENGTH_SHORT);
                suSnackbar.setBackgroundColor(color);
                suSnackbar.show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void dismiss(){
        if(suSnackbar != null){
            suSnackbar.dismiss();
            suSnackbar = null;
        }
    }

}
