package com.wangzuo.libgensee.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.gensee.chat.gif.SpanResource;
import com.wangzuo.libgensee.util.ResManager;
import java.util.Map;
import java.util.Set;

public class GridViewAvatarAdapter extends BaseAdapter
{
    private Object[] resIds;
    private Object[] res1;
    private final int MEXPRESSIONNUM = 18;
    private Map<String, Drawable> browMap;
    private SelectAvatarInterface selectAvatarInterface;

    public GridViewAvatarAdapter(Context context, SelectAvatarInterface selectAvatarInterface, int num, int endSum)
    {
        this.browMap = SpanResource.getBrowMap(context);

        this.res1 = this.browMap.keySet().toArray();

        if ((endSum >= 1) && (endSum < 18)) {
            this.resIds = new Object[endSum];
            int i = num; for (int j = 0; i < num + endSum; j++) {
                this.resIds[j] = this.res1[i];

                i++;
            }
        }
        else {
            this.resIds = new Object[18];
            int i = num; for (int j = 0; i < num + 18; j++) {
                this.resIds[j] = this.res1[i];

                i++;
            }
        }

        this.selectAvatarInterface = selectAvatarInterface;
    }

    public int getCount()
    {
        return this.resIds.length;
    }

    public Object getItem(int position)
    {
        return this.resIds[position];
    }

    public long getItemId(int position)
    {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        GridViewHolder viewHolder = null;
        if (convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(ResManager.getLayoutId("gs_single_expression_layout"),
                    null);
            viewHolder = new GridViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (GridViewHolder)convertView.getTag();
        }
        viewHolder.init((String)getItem(position),
                (Drawable)this.browMap.get(getItem(position)), convertView);
        return convertView;
    }

    private class GridViewHolder {
        private ImageView ivAvatar;

        public GridViewHolder(View view) {
            this.ivAvatar = ((ImageView)view.findViewById(ResManager.getId("gs_image")));
        }

        public void init(final String sAvatar, final Drawable resDrawable, View view)
        {
            this.ivAvatar.setBackgroundDrawable(resDrawable);
            view.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    if (GridViewAvatarAdapter.this.selectAvatarInterface != null)
                        GridViewAvatarAdapter.this.selectAvatarInterface.selectAvatar(sAvatar, resDrawable);
                }
            });
        }
    }

    public static abstract interface SelectAvatarInterface
    {
        public abstract void selectAvatar(String paramString, Drawable paramDrawable);
    }
}