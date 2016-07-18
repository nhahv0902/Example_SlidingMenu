package com.example.user.slidingmenu;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Scroller;


public class MySlidingMenu extends ViewGroup {

    private ViewGroup mMenu;
    private ViewGroup mContent;
    private int mMenuWidth;
    private int mContentWidth;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mMenuRightPadding;
    private Scroller mScroller;
    private int mLastX;
    private int mLastY;
    private int mLastXIntercept;
    private int mLastYIntercept;
    private float scale;
    private boolean isOpen;

    public MySlidingMenu(Context context) {
        this(context, null, 0);
    }

    public MySlidingMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MySlidingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mMenuRightPadding = convertToDp(context, 100);
        mScroller = new Scroller(context);
        isOpen = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mMenu = (ViewGroup) getChildAt(0);
        mContent = (ViewGroup) getChildAt(1);
        mMenuWidth = mMenu.getLayoutParams().width = mScreenWidth - mMenuRightPadding;
        mContentWidth = mContent.getLayoutParams().width = mScreenWidth;
        measureChild(mMenu, widthMeasureSpec, heightMeasureSpec);
        measureChild(mContent, widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mMenuWidth + mContentWidth, mScreenHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mMenu.layout(-mMenuWidth, 0, 0, mScreenHeight);
        mContent.layout(0, 0, mScreenWidth, mScreenHeight);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = false;
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                intercept = false;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) ev.getX() - mLastXIntercept;
                int deltaY = (int) ev.getY() - mLastYIntercept;
                intercept = Math.abs(deltaX) > Math.abs(deltaY);
                break;
            case MotionEvent.ACTION_UP:
                intercept = false;
                break;
        }
        mLastX = x;
        mLastY = y;
        mLastXIntercept = x;
        mLastYIntercept = y;
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastX = (int) event.getX();
                mLastY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                int currentX = (int) event.getX();
                int currentY = (int) event.getY();
                int dx = currentX - mLastX;
                if (dx < 0) {
                    if (getScrollX() + Math.abs(dx) >= 0) {
                        scrollTo(0, 0);
                        mMenu.setTranslationX(0);
                    } else {
                        scrollBy(-dx, 0);
                        slidingMode2();
                    }

                } else {
                    if (getScrollX() - dx <= -mMenuWidth) {
                        scrollTo(-mMenuWidth, 0);
                        mMenu.setTranslationX(0);
                    } else {
                        scrollBy(-dx, 0);
                        slidingMode2();
                    }

                }
                mLastX = currentX;
                mLastY = currentY;
                scale = Math.abs((float) getScrollX()) / (float) mMenuWidth;
                break;

            case MotionEvent.ACTION_UP:
                if (getScrollX() < -mMenuWidth / 2) {
                    mScroller.startScroll(getScrollX(), 0, -mMenuWidth - getScrollX(), 0, 300);
                    isOpen = true;
                    invalidate();
                } else {
                    mScroller.startScroll(getScrollX(), 0, -getScrollX(), 0, 300);
                    isOpen = false;
                    invalidate();
                }

                break;
        }
        return true;
    }


    private int convertToDp(Context context, int num) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, num, context.getResources().getDisplayMetrics());
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            scale = Math.abs((float) getScrollX()) / (float) mMenuWidth;
            slidingMode2();
            invalidate();
        }
    }

    private void slidingMode2() {
        mMenu.setTranslationX(2 * (mMenuWidth + getScrollX()) / 3);
    }

    private void slidingMode3() {
        mMenu.setTranslationX(mMenuWidth + getScrollX() - (mMenuWidth / 2) * (1.0f - scale));
        mMenu.setScaleX(0.7f + 0.3f * scale);
        mMenu.setScaleY(0.7f + 0.3f * scale);
        mMenu.setAlpha(scale);

        mContent.setScaleX(1 - 0.3f * scale);
        mContent.setPivotX(0);
        mContent.setScaleY(1.0f - 0.3f * scale);
    }

    public void toggleMenu() {
        if (isOpen) {
            closeMenu();
        } else {
            openMenu();
        }

    }

    private void closeMenu() {
        mScroller.startScroll(getScrollX(), 0, -getScrollX(), 0, 500);
        invalidate();
        isOpen = false;
    }

    private void openMenu() {
        mScroller.startScroll(getScrollX(), 0, -mMenuWidth - getScrollX(), 0, 500);
        invalidate();
        isOpen = true;
    }
}
