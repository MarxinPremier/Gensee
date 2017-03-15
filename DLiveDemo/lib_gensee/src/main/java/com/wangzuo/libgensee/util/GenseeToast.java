package com.wangzuo.libgensee.util;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.wangzuo.libgensee.util.ResManager;

public class GenseeToast {
    private static Toast mToast;

    public GenseeToast() {
    }

    public static void showToast(Context mContext, int resId) {
        showToast(mContext, mContext.getString(resId));
    }

    public static void showToastOnUIThread(Activity activity, int resId) {
    }

    public static void showToastOnUIThread(Activity activity, String text) {
    }

    public static void showSysToast(final Context mContext, final String text) {
        if(mContext != null) {
            ((Activity)mContext).runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(mContext, text, 0).show();
                }
            });
        }

    }

    public static void showToast(Context mContext, String text) {
        showToast(mContext, text, 0);
    }

    public static void showToast(Context mContext, int resId, int duration) {
        showToast(mContext, mContext.getString(resId), duration);
    }

    public static void showToast(Context mContext, String text, int duration) {
        showToast(mContext, text, duration, true);
    }

    public static void showToast(Context mContext, int resId, boolean center) {
        showToast(mContext, mContext.getString(resId), center);
    }

    public static void showToast(Context mContext, String text, boolean center) {
        showToast(mContext, text, 0, center);
    }

    public static void showToast(Context mContext, int resId, int duration, boolean center) {
        showToast(mContext, mContext.getString(resId), duration, center);
    }

    public static void showToast(Context mContext, String text, int duration, boolean center) {
        showToast(mContext, text, duration, center, 0, 0);
    }

    public static void showToast(Context mContext, String text, boolean center, int backGround, int imgSrc) {
        showToast(mContext, text, 0, center, backGround, imgSrc);
    }

    public static void showToast(Context mContext, String text, int duration, boolean center, int backGround, int imgSrc) {
        View view = null;
        if(mToast != null) {
            view = mToast.getView();
        } else {
            mToast = new Toast(mContext);
            view = LayoutInflater.from(mContext).inflate(ResManager.getLayoutId("gs_custom_toast_layout"), (ViewGroup)null);
            mToast.setView(view);
        }

        view.setBackgroundResource(backGround != 0?backGround:ResManager.getDrawableId("gs_dialog_bg"));
        TextView tv = (TextView)view.findViewById(ResManager.getId("gs_toast_tv"));
        tv.setText(text);
        ImageView imgToast = (ImageView)view.findViewById(ResManager.getId("gs_imgToast"));
        if(imgSrc != 0) {
            imgToast.setVisibility(0);
            imgToast.setImageResource(imgSrc);
        } else {
            imgToast.setVisibility(8);
        }

        if(center) {
            mToast.setGravity(17, 0, 0);
        } else {
            mToast.setGravity(80, 0, 0);
        }

        mToast.show();
    }

    public static void toastUiThread(final Activity activity, final String msg) {
        if(activity != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    GenseeToast.showToast(activity, msg);
                }
            });
        }

    }
}
