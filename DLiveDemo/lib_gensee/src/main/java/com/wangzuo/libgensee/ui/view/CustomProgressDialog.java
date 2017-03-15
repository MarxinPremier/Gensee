package com.wangzuo.libgensee.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import com.wangzuo.libgensee.util.ResManager;

public class CustomProgressDialog extends Dialog
{
    public CustomProgressDialog(Context context)
    {
        super(context);
    }

    public CustomProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    public void show(CharSequence message, boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
        setTitle("");
        setContentView(ResManager.getLayoutId("gs_progress_dialog_layout"));
        if ((message == null) || (message.length() == 0)) {
            findViewById(ResManager.getId("tv_message")).setVisibility(8);
        } else {
            TextView tv_message = (TextView)findViewById(ResManager.getId("tv_message"));
            tv_message.setText(message);
        }

        setCancelable(cancelable);

        setOnCancelListener(cancelListener);

        getWindow().getAttributes().gravity = 17;
        WindowManager.LayoutParams lp = getWindow().getAttributes();

        lp.dimAmount = 0.1F;
        getWindow().setAttributes(lp);
        show();
    }
}
