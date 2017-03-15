package com.wangzuo.libgensee.ui.holder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.wangzuo.libgensee.core.RTLive;
import com.wangzuo.libgensee.util.GenseeUtils;
import com.wangzuo.libgensee.util.ResManager;
import com.gensee.room.RtSdk;
import com.gensee.routine.UserInfo;
import java.util.List;

public class IdcHolder extends BaseHolder
{
    private static final long DURAION = 300L;
    private LinearLayout lyIdcContent;
    private LinearLayout lyIdcNoData;
    private String selectIdc = "";
    private View tvCancel;
    private View idcBlankArea;
    private LinearLayout idcAllContent;
    private List<FastIdc> idcs;

    public IdcHolder(View rootView, Object value)
    {
        super(rootView, value);
    }

    public void onClick(View v)
    {
        if ((v.getId() == ResManager.getId("gs_tv_cancel_btm_view")) || (v.getId() == ResManager.getId("gs_idc_blank_area")))
            show(false);
    }

    private void sure()
    {
        if ((!"".equals(this.selectIdc)) && (!this.selectIdc.equals(RTLive.getIns().roomIDCGetCurrent(getContext()))))
        {
            UserInfo self = RTLive.getIns().getSelf();
            if ((self != null) && (self.IsHost()))
            {
                RTLive.getIns().getRtSdk().roomNotifyBroadcastMsg(
                        GenseeUtils.formatText(self.getName(), 12) + getString(ResManager.getStringId("gs_chat_host_leave")), true, null);
            }
            RTLive.getIns().roomIDCSetCurrent(this.selectIdc, getContext());
        }
        show(false);
    }

    public void selectIdc(final List<FastIdc> idcs)
    {
        this.idcs = idcs;
        this.lyIdcContent.removeAllViews();
        if ((idcs == null) || (idcs.size() <= 0)) {
            this.lyIdcNoData.setVisibility(View.GONE);
        } else {
            this.lyIdcNoData.setVisibility(View.GONE);
            String sCurIdc = RTLive.getIns().roomIDCGetCurrent(getContext());
            this.selectIdc = (sCurIdc == null ? "" : sCurIdc);
            int nsize = idcs.size();
            for (int i = 0; i < nsize; i++) {
                TextView tv = new TextView(getContext());
                tv.setGravity(17);

                tv.setTextSize(0,
                        getContext().getResources().getDimensionPixelSize(ResManager.getDimenId("gs_idc_item_text_size")));
                tv.setSingleLine();
                tv.setEllipsize(TextUtils.TruncateAt.END);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        -1,
                        getContext().getResources().getDimensionPixelSize(ResManager.getDimenId("gs_idc_item_height")));
                tv.setText(((FastIdc)idcs.get(i)).name);
                if ((!"".equals(this.selectIdc)) && (this.selectIdc.equals(((FastIdc)idcs.get(i)).id)))
                    tv.setTextColor(Color.parseColor("#e43e36"));
                else {
                    tv.setTextColor(-16777216);
                }
                final int nItemIndex = i;
                tv.setOnClickListener(new View.OnClickListener()
                {
                    public void onClick(View v) {
                        IdcHolder.this.selectIdc = ((IdcHolder.FastIdc)idcs.get(nItemIndex)).id;
                        IdcHolder.this.sure();
                    }
                });
                if (i != 0) {
                    View line = new View(getContext());
                    line.setBackgroundResource(ResManager.getColorId("gs_idc_line_color"));
                    LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(
                            -1, 2);
                    this.lyIdcContent.addView(line, lineLp);
                }
                this.lyIdcContent.addView(tv, lp);
            }
            if (nsize > 4) {
                int oneItemHeight = getContext().getResources().getDimensionPixelSize(ResManager.getDimenId("gs_idc_item_height")) + 2;
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(-1, oneItemHeight * 5);
                this.idcAllContent.setLayoutParams(lp);
            }
        }
        show(true);
    }

    protected void initData(Object value)
    {
    }

    protected void initComp(Object value)
    {
        this.tvCancel = findViewById(ResManager.getId("gs_tv_cancel_btm_view"));
        this.tvCancel.setOnClickListener(this);
        this.lyIdcContent = ((LinearLayout)findViewById(ResManager.getId("gs_idc_content_ly")));
        this.lyIdcNoData = ((LinearLayout)findViewById(ResManager.getId("idc_no_data_ly")));
        this.idcBlankArea = findViewById(ResManager.getId("gs_idc_blank_area"));
        this.idcBlankArea.setOnClickListener(this);
        this.idcAllContent = ((LinearLayout)findViewById(ResManager.getId("gs_idc_all_content")));
    }

    public static void fromUpToBottom(final View view) {
        TranslateAnimation animation = new TranslateAnimation(0.0F, 0.0F, 0.0F, (float)view.getHeight());
        animation.setDuration(300L);
        animation.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }
        });
        view.startAnimation(animation);
    }

    public static void fromBottomToUp(final View view) {
        TranslateAnimation animation = new TranslateAnimation(0.0F, 0.0F, (float)view.getHeight(), 0.0F);
        animation.setDuration(300L);
        animation.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(animation);
    }

    public static class FastIdc
    {
        public String name;
        public String id;
    }
}
