package com.liuyao.perfecttable;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;

public class CustomLinearLayout extends LinearLayout {
    private ColumnHeaderScrollView columnHeaderScrollView;
    private RowHeaderScrollView rowHeaderScrollView;
    private Scroller mScroller;
    private VelocityTracker velocityTracker;
     //判定为拖动的最小移动像素数
    private int mTouchSlop;
    //产生惯性的速度
    private int mMinimumVelocity;
     // 手机按下时的屏幕坐标
    private float mXDown;
    //手机当时所处的屏幕坐标
    private float mXMove;
    //上次触发ACTION_MOVE事件时的屏幕坐标
    private float mXLastMove;
    // 手机按下时的屏幕坐标
    private float mYDown;
    //手机当时所处的屏幕坐标
    private float mYMove;
    //上次触发ACTION_MOVE事件时的屏幕坐标
    private float mYLastMove;
     public static final int HORIZONTAL = 0;
     public static final int VERTICAL = 1;
     public static final int NONE = -1;
    private int direction = 0;

    private FlingComputeResultCache flingComputeResultCache;


    public CustomLinearLayout(Context context) {
        super(context);
        init();
    }

    public CustomLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mScroller = new Scroller(getContext());
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        // 获取TouchSlop值
        mTouchSlop = configuration.getScaledPagingTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
    }



    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                stopScroll();
                columnHeaderScrollView.stopScroller();
                rowHeaderScrollView.stopScroller();
                velocityTracker = VelocityTracker.obtain();
                mXDown = ev.getRawX();
                mYDown = ev.getRawY();
                direction = NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                mXMove = ev.getRawX();
                mYMove = ev.getRawY();
                float diff = Math.max(Math.abs(mXMove - mXDown), Math.abs(mYDown - mYMove));
                // 当手指拖动值大于TouchSlop值时，认为应该进行滚动，拦截子控件的事件
                if (diff > mTouchSlop) {
                    direction = Math.abs(mXMove - mXDown) > Math.abs(mYDown - mYMove) ? HORIZONTAL : VERTICAL;
                    mYLastMove = mYMove;
                    mXLastMove = mXMove;
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                if(direction == HORIZONTAL){
                    handleMoveHorizontal(event);
                }else if(direction == VERTICAL){
                    handleMoveVertical(event);
                }else{
                    mXMove = event.getRawX();
                    mYMove = event.getRawY();
                    float diff = Math.max(Math.abs(mXMove - mXDown), Math.abs(mYDown - mYMove));
                    // 当手指拖动值大于TouchSlop值时，认为应该进行滚动，拦截子控件的事件
                    if (diff > mTouchSlop) {
                        direction = Math.abs(mXMove - mXDown) > Math.abs(mYDown - mYMove) ? HORIZONTAL : VERTICAL;
                        mYLastMove = mYMove;
                        mXLastMove = mXMove;
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if(direction == HORIZONTAL){
                    handleUpHorizontal(event);
                }else if(direction == VERTICAL){
                    handleUpVertical(event);
                }
                //回收
                velocityTracker.clear();
                velocityTracker.recycle();
                break;
            case MotionEvent.ACTION_CANCEL:
                //回收
                velocityTracker.clear();
                velocityTracker.recycle();
                break;
        }
        return super.onTouchEvent(event);
    }

    private void handleMoveHorizontal(MotionEvent event){
        mXMove = event.getRawX();
        int scrolledX = (int) (mXLastMove - mXMove);
        columnHeaderScrollView.scrollBy(ruleScrollHorizontalValue(scrolledX), 0);
        mXLastMove = mXMove;
        velocityTracker.addMovement(event);
    }

    private int ruleScrollHorizontalValue(int scrolledX){
        int maxScroll = getColumnHeaderScrollMaxAmount();
        if(columnHeaderScrollView.getScrollX() + scrolledX > maxScroll){
            return maxScroll - columnHeaderScrollView.getScrollX();
        }else{
            return scrolledX;
        }
    }

    private void handleMoveVertical(MotionEvent event){
          mYMove = event.getRawY();
          int scrolledY = (int) (mYLastMove - mYMove);
          rowHeaderScrollView.scrollBy(0, ruleScrollVerticalValue(scrolledY));
          mYLastMove = mYMove;
          velocityTracker.addMovement(event);
    }

    private int ruleScrollVerticalValue(int scrolledY){
        int maxScroll = getRowHeaderScrollMaxAmount();
        if(rowHeaderScrollView.getScrollY() + scrolledY > maxScroll){
            return maxScroll - rowHeaderScrollView.getScrollY();
        }else{
            return scrolledY;
        }
    }

    private int getRowHeaderScrollMaxAmount(){
        return rowHeaderScrollView.getChildAt(0).getHeight() - rowHeaderScrollView.getHeight();
    }

    private int getColumnHeaderScrollMaxAmount(){
        return columnHeaderScrollView.getChildAt(0).getWidth() - columnHeaderScrollView.getWidth();
    }
    private void handleUpHorizontal(MotionEvent event){
        velocityTracker.addMovement(event);
        velocityTracker.computeCurrentVelocity(1000);
        int xVelocity = (int) velocityTracker.getXVelocity();
        if(Math.abs(xVelocity) > mMinimumVelocity){
            flingComputeResultCache = new FlingComputeResultCache(HORIZONTAL, getColumnHeaderScrollMaxAmount(), getRowHeaderScrollMaxAmount());
            mScroller.fling(getScrollX(), getScrollY(), -xVelocity, 0, 0, getColumnHeaderScrollMaxAmount(), getScrollY(), getScrollY());
            postInvalidateOnAnimation();
        }

    }

    private void handleUpVertical(MotionEvent event){
        velocityTracker.addMovement(event);
        velocityTracker.computeCurrentVelocity(1000);
        int yVelocity = (int) velocityTracker.getYVelocity();
        if(Math.abs(yVelocity) > mMinimumVelocity){
            flingComputeResultCache = new FlingComputeResultCache(VERTICAL, getColumnHeaderScrollMaxAmount(), getRowHeaderScrollMaxAmount());
            mScroller.fling(getScrollX(), getScrollY(), 0, -yVelocity, getScrollX(), getScrollX(), 0, getRowHeaderScrollMaxAmount());
            postInvalidateOnAnimation();
        }
    }

    @Override
    public void computeScroll() {

        if (mScroller.computeScrollOffset()) {
            if(flingComputeResultCache.direction == VERTICAL){
                int scrollY = mScroller.getCurrY();
                if(scrollY < 0 || scrollY > flingComputeResultCache.rowHeaderScrollMaxAmount){
                    mScroller.abortAnimation();
                }else{
                    rowHeaderScrollView.scrollTo(0, scrollY);
                    postInvalidateOnAnimation();
                }
            }else if(flingComputeResultCache.direction == HORIZONTAL){
                int scrollX = mScroller.getCurrX();
                if(scrollX < 0 || scrollX > flingComputeResultCache.columnHeaderScrollMaxAmount){
                    mScroller.abortAnimation();
                }else{
                    columnHeaderScrollView.scrollTo(scrollX, 0);
                    postInvalidateOnAnimation();
                }
            }
        }
    }

    private void stopScroll(){
        mScroller.abortAnimation();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize,
                    MeasureSpec.UNSPECIFIED);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize,  MeasureSpec.UNSPECIFIED);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public ColumnHeaderScrollView getColumnHeaderScrollView() {
        return columnHeaderScrollView;
    }

    public void setColumnHeaderScrollView(ColumnHeaderScrollView columnHeaderScrollView) {
        this.columnHeaderScrollView = columnHeaderScrollView;
        this.columnHeaderScrollView.setOnScrollChangeListener(new ColumnHeaderScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChanged(HorizontalScrollView scrollView, final int scrollX, int scrollY) {
                if(mScroller.computeScrollOffset()){
                    CustomLinearLayout.this.columnHeaderScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollTo(scrollX, getScrollY());
                        }
                    });
                }else{
                    scrollTo(scrollX, getScrollY());
                }


            }
        });
        this.columnHeaderScrollView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    stopScroll();
                    CustomLinearLayout.this.rowHeaderScrollView.stopScroller();
                }
                return false;
            }
        });
    }

    public RowHeaderScrollView getRowHeaderScrollView() {
        return rowHeaderScrollView;
    }

    public void setRowHeaderScrollView(RowHeaderScrollView rowHeaderScrollView) {
        this.rowHeaderScrollView = rowHeaderScrollView;
        this.rowHeaderScrollView.setOnScrollChangeListener(new RowHeaderScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChanged(ScrollView scrollView, int scrollX, final int scrollY) {
                if(mScroller.computeScrollOffset()){
                    CustomLinearLayout.this.rowHeaderScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollTo(getScrollX(), scrollY);
                        }
                    });
                }else{
                    scrollTo(getScrollX(), scrollY);
                }


            }
        });
        this.rowHeaderScrollView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    stopScroll();
                    CustomLinearLayout.this.columnHeaderScrollView.stopScroller();
                }
                return false;
            }
        });
    }
    //手动同步一下
    public void syncScroll(){
        scrollTo(columnHeaderScrollView.getScrollX(), rowHeaderScrollView.getScrollY());
    }

    public static class FlingComputeResultCache{
        public int direction;
        public int columnHeaderScrollMaxAmount;
        public int rowHeaderScrollMaxAmount;
        public FlingComputeResultCache(int direction, int columnHeaderScrollMaxAmount, int rowHeaderScrollMaxAmount){
             this.direction = direction;
             this.columnHeaderScrollMaxAmount = columnHeaderScrollMaxAmount;
             this.rowHeaderScrollMaxAmount = rowHeaderScrollMaxAmount;
        }

    }
}
