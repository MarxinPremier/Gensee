package com.wangzuo.libgensee.ui.view;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.gensee.utils.GenseeLog;

public class ResizeLayout extends LinearLayout
{
    private OnResizeListener mListener;
    private int oldH = 0;

    public void setOnResizeListener(OnResizeListener l)
    {
        this.mListener = l;
    }

    public ResizeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        GenseeLog.d("onMeasure", "onMeasure width=" + getMeasuredWidth() +
                " height=" + getMeasuredHeight());
        if (this.mListener != null) {
            int newH = getMeasuredHeight();
            if ((newH != this.oldH) && (Math.abs(newH - this.oldH) > 100)) {
                if (this.oldH > 0) {
                    this.mListener.OnResize(newH, this.oldH);
                }
                this.oldH = newH;
            }
        }
    }

    public static abstract interface OnResizeListener
    {
        public abstract void OnResize(int paramInt1, int paramInt2);
    }
}
