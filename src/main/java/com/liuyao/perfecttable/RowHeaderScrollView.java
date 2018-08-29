package com.liuyao.perfecttable;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class RowHeaderScrollView extends ScrollView {
    private OnScrollChangeListener mOnScrollChangeListener;
    public RowHeaderScrollView(Context context) {
        super(context);
    }

    public RowHeaderScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if(mOnScrollChangeListener != null){
            mOnScrollChangeListener.onScrollChanged(this, getScrollX(), getScrollY());

        }
    }

    public interface OnScrollChangeListener{
        void onScrollChanged(ScrollView scrollView,int scrollX, int scrollY);
    }



    public void setOnScrollChangeListener(OnScrollChangeListener mOnScrollChangeListener) {
        this.mOnScrollChangeListener = mOnScrollChangeListener;
    }
}
