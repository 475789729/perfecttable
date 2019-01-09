package com.liuyao.perfecttable;

import android.view.View;

import static android.view.View.MeasureSpec.UNSPECIFIED;

public class MeasureHelper {
    /**
     * 算内容包裹的宽度
     * @param v
     * @return
     */
      public static int measureWrapWidth(View v){
           //第一个参数随便填
           int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(2000, UNSPECIFIED);
           v.measure(widthMeasureSpec, widthMeasureSpec);
           return v.getMeasuredWidth();
      }

    /**
     * 固定宽度条件下，算内容包裹的高度
     * @param v
     * @param width
     * @return
     */
      public static int measureWrapHeight(View v, int width){
          int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
           int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(2000, UNSPECIFIED);
           v.measure(widthMeasureSpec, heightMeasureSpec);
           return v.getMeasuredHeight();
      }
}
