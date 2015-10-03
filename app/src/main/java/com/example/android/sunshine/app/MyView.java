package com.example.android.sunshine.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Mohamed Yasser on 9/18/2015.
 */
public class MyView extends View {


    public MyView(Context context) {
        super(context);
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
