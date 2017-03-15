package com.wangzuo.libgensee.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractAdapter extends BaseAdapter
{
    protected List<Object> objectList;

    public AbstractAdapter()
    {
        this.objectList = new ArrayList();
    }

    public int getCount()
    {
        return this.objectList.size();
    }

    public Object getItem(int position)
    {
        return this.objectList.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public <T> void notifyData(List<T> objectList) {
        this.objectList.clear();
        this.objectList.addAll(objectList);
        notifyDataSetChanged();
    }

    public <T> void addData(T object, int index) {
        this.objectList.add(index, object);
        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        AbstractViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = createView(parent.getContext());
            viewHolder = createViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (AbstractViewHolder)convertView.getTag();
        }
        viewHolder.initValue(position);
        return convertView;
    }

    protected abstract View createView(Context paramContext);

    protected abstract AbstractViewHolder createViewHolder(View paramView);
}
