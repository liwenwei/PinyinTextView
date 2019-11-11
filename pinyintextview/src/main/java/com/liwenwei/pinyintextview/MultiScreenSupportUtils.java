package com.liwenwei.pinyintextview;


import android.content.Context;
import android.util.TypedValue;

public class MultiScreenSupportUtils {

    public static int dp2Px(int dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int sp2Px(int sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public static float px2Dp(int px, Context context){
        return (int) ((float)px / context.getResources().getDisplayMetrics().density);
    }
}
