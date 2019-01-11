package com.liuyao.perfecttable;

import android.view.View;
import android.view.ViewGroup;

public class MarginUtils {
      public static void setMargins(View v, int l, int t, int r, int b){
          ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
          if(layoutParams != null && layoutParams instanceof ViewGroup.MarginLayoutParams){
              ((ViewGroup.MarginLayoutParams) layoutParams).setMargins(l, t, r, b);
              v.setLayoutParams(layoutParams);
          }
      }
}
