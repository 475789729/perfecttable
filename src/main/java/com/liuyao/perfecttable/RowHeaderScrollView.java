package com.liuyao.perfecttable;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class RowHeaderScrollView extends ScrollView {
    private OnScrollChangeListener mOnScrollChangeListener;
    public RowHeaderScrollView(Context context) {
        super(context);
        init();
    }

    public RowHeaderScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        setOverScrollMode(OVER_SCROLL_NEVER);
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

    public void stopScroller(){
        Object scroller = ReflectionUtil.getFieldValue(this, "mScroller");
        ReflectionUtil.invokeMethod(scroller, "abortAnimation", null, null);
    }
}
