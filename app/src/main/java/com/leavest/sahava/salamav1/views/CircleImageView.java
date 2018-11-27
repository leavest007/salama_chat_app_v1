package com.leavest.sahava.salamav1.views;

import android.content.Context;
import android.util.AttributeSet;

import java.util.jar.Attributes;

public class CircleImageView extends android.support.v7.widget.AppCompatImageView {

    public CircleImageView(Context context) {
        super(context);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, heightMeasureSpec);
    }
}
