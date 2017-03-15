package com.wangzuo.libgensee.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.wangzuo.libgensee.util.ResManager;

public class CustomDialog extends Dialog
{
    public CustomDialog(Context context, int theme)
    {
        super(context, theme);
    }

    public CustomDialog(Context context) {
        super(context);
    }
    public static class Builder { protected Context context;
        private String title;
        private String message;
        private String positiveButtonText;
        private String negativeButtonText;
        private View contentView;
        private DialogInterface.OnClickListener positiveButtonClickListener;
        private DialogInterface.OnClickListener negativeButtonClickListener;
        private View layout;

        public Builder(Context context) { this.context = context; }

        public Builder setMessage(String message)
        {
            this.message = message;
            return this;
        }

        public Builder setMessage(int message) {
            this.message = ((String)this.context.getText(message));
            return this;
        }

        public Builder setTitle(int title) {
            this.title = ((String)this.context.getText(title));
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }

        public Builder setPositiveButton(int positiveButtonText, DialogInterface.OnClickListener listener)
        {
            this.positiveButtonText =
                    ((String)this.context
                            .getText(positiveButtonText));
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setPositiveButton(String positiveButtonText, DialogInterface.OnClickListener listener)
        {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }

        public void setPositiveButtonText(String text) {
            if (this.layout == null) {
                return;
            }
            ((Button)this.layout.findViewById(ResManager.getId("gs_positiveButton"))).setText(text);
        }

        public void setPositiveButtonTextColor(int color) {
            if (this.layout == null) {
                return;
            }
            ((Button)this.layout.findViewById(ResManager.getId("gs_positiveButton"))).setTextColor(color);
        }

        public void setPositiveButtonEnable(boolean enable) {
            if (this.layout == null) {
                return;
            }
            ((Button)this.layout.findViewById(ResManager.getId("gs_positiveButton"))).setClickable(enable);
        }

        public void setNegativeButtonTextColor(int color) {
            if (this.layout == null) {
                return;
            }
            ((Button)this.layout.findViewById(ResManager.getId("gs_negativeButton"))).setTextColor(color);
        }

        public Builder setNegativeButton(String negativeButtonText, DialogInterface.OnClickListener listener)
        {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }

        public CustomDialog create()
        {
            LayoutInflater inflater = (LayoutInflater)this.context
                    .getSystemService("layout_inflater");
            this.layout = inflater.inflate(ResManager.getLayoutId("gs_dialog"), null);
            CustomDialog dialog = new CustomDialog(this.context,
                    ResManager.getStyleId("gs_dialog"));
            dialog.addContentView(this.layout, new ViewGroup.LayoutParams(
                    -1, -2));

            return create(this.layout, dialog);
        }

        public CustomDialog create(View layout, CustomDialog mDialog)
        {
            final CustomDialog dialog = mDialog;
            if (this.title != null) {
                ((TextView)layout.findViewById(ResManager.getId("gs_title"))).setText(this.title);
            } else {
                layout.findViewById(ResManager.getId("gs_title")).setVisibility(8);
                layout.findViewById(ResManager.getId("gs_iv_top")).setVisibility(8);
            }

            if (this.positiveButtonText != null) {
                ((Button)layout.findViewById(ResManager.getId("gs_positiveButton")))
                        .setText(this.positiveButtonText);
                ((Button)layout.findViewById(ResManager.getId("gs_positiveButton")))
                        .setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                if (CustomDialog.Builder.this.positiveButtonClickListener != null) {
                                    CustomDialog.Builder.this.positiveButtonClickListener.onClick(dialog,
                                            -1);
                                }
                                dialog.dismiss();
                            }
                        });
            }
            else {
                layout.findViewById(ResManager.getId("gs_positiveButton")).setVisibility(
                        8);
            }

            if (this.negativeButtonText != null) {
                ((Button)layout.findViewById(ResManager.getId("gs_negativeButton")))
                        .setText(this.negativeButtonText);
                ((Button)layout.findViewById(ResManager.getId("gs_negativeButton")))
                        .setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                if (CustomDialog.Builder.this.negativeButtonClickListener != null) {
                                    CustomDialog.Builder.this.negativeButtonClickListener.onClick(dialog,
                                            -2);
                                }
                                dialog.dismiss();
                            }
                        });
            }
            else {
                layout.findViewById(ResManager.getId("gs_negativeButton")).setVisibility(
                        8);
            }

            if ((this.positiveButtonText == null) || (this.negativeButtonText == null))
            {
                layout.findViewById(ResManager.getId("gs_iv_bottom_center")).setVisibility(8);
            }

            if (this.message != null)
                ((TextView)layout.findViewById(ResManager.getId("gs_message"))).setText(this.message);
            else {
                layout.findViewById(ResManager.getId("gs_message")).setVisibility(8);
            }

            if (this.contentView != null)
            {
                ((LinearLayout)layout.findViewById(ResManager.getId("gs_content")))
                        .removeAllViews();
                ((LinearLayout)layout.findViewById(ResManager.getId("gs_content"))).addView(
                        this.contentView, new ViewGroup.LayoutParams(
                                -2,
                                -2));
            }
            dialog.setContentView(layout);
            return dialog;
        }
    }
}
