package com.liuyao.perfecttable;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

public class BackgroundDrawableCreater {
    /**
     *
     * @param borderWidth 单位px
     * @param borderColorString 例如: "#000000"
     * @param backGroundColorString 例如: "#000000"
     * @param borderLTRB "传入LTRB"或者子字符串，例如LR就是只有左边框和右边框
     * @return
     */
    public static Drawable getBorderDrawable(int borderWidth, String borderColorString, @NonNull String borderLTRB, @Nullable String backGroundColorString) {
        GradientDrawable rect = new GradientDrawable();
        rect.setShape(GradientDrawable.RECTANGLE);
        rect.setStroke(borderWidth, Color.parseColor(borderColorString));
        if(!TextUtils.isEmpty(backGroundColorString)){
            rect.setColor(Color.parseColor(backGroundColorString));
        }
        int left = -999;
        int top = -999;
        int right = -999;
        int bottom = -999;
        if(borderLTRB.contains("L")){
            left = 0;
        }
        if(borderLTRB.contains("T")){
            top = 0;
        }
        if(borderLTRB.contains("R")){
            right = 0;
        }
        if(borderLTRB.contains("B")){
            bottom = 0;
        }
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{rect});
        layerDrawable.setLayerInset(0, left, top, right, bottom);
        return layerDrawable;
    }

    /**
     *
     * @param borderWidth 单位px
     * @param borderColorString   例如: "#000000"
     * @param borderLTRB "传入LTRB"或者子字符串，例如LR就是只有左边框和右边框
     * @return
     */
    public static Drawable getBorderDrawable(int borderWidth, String borderColorString, @NonNull String borderLTRB) {
        return getBorderDrawable(borderWidth, borderColorString, borderLTRB, null);
    }


}
