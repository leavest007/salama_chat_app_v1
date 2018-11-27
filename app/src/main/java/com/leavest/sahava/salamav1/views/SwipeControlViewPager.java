package com.leavest.sahava.salamav1.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.jar.Attributes;

public class SwipeControlViewPager extends ViewPager {

    private boolean SwipeAble = true;

    public SwipeControlViewPager(Context context) {
        super(context);
    }

    public SwipeControlViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.SwipeAble) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.SwipeAble) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    public void setSwipeAble(boolean swipe) {
        this.SwipeAble = swipe;
    }
}
