package com.wangzuo.libgensee.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

public class ChatViewPageAdapter extends PagerAdapter
{
    private List<View> mListViews;

    public ChatViewPageAdapter(List<View> mListViews)
    {
        this.mListViews = mListViews;
    }

    public void destroyItem(ViewGroup container, int position, Object object)
    {
        container.removeView((View)this.mListViews.get(position % this.mListViews.size()));
    }

    public Object instantiateItem(ViewGroup container, int position)
    {
        try
        {
            ((ViewPager)container).addView(
                    (View)this.mListViews.get(position % this.mListViews.size()), 0);
        }
        catch (Exception localException)
        {
        }

        return this.mListViews.get(position % this.mListViews.size());
    }

    public int getCount()
    {
        return 1;
    }

    public boolean isViewFromObject(View arg0, Object arg1)
    {
        return arg0 == arg1;
    }
}
