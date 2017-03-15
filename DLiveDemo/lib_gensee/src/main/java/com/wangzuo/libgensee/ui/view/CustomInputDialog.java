package com.wangzuo.libgensee.ui.view;


import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import com.wangzuo.libgensee.ui.holder.chat.DialogInputHolder;
import com.wangzuo.libgensee.util.ResManager;

public class CustomInputDialog extends Dialog
{
    public CustomInputDialog(Context context, int theme)
    {
        super(context, theme);
    }
    public static class Builder {
        protected Context context;
        private ResizeLayout layout;
        public DialogInputHolder dialogInputHolder;

        public Builder(Context context) {
            this.context = context;
        }

        public CustomInputDialog create() {
            LayoutInflater inflater = (LayoutInflater)this.context
                    .getSystemService("layout_inflater");
            this.layout = ((ResizeLayout)inflater.inflate(ResManager.getLayoutId("gs_input_dialog"), null));
            CustomInputDialog dialog = new CustomInputDialog(this.context, ResManager.getStyleId("gs_Dialog_Fullscreen"));
            dialog.addContentView(this.layout, new ViewGroup.LayoutParams(
                    -1, -1));
            dialog.getWindow().setGravity(80);

            this.dialogInputHolder = new DialogInputHolder(this.layout, dialog);
            return dialog;
        }
    }
}
