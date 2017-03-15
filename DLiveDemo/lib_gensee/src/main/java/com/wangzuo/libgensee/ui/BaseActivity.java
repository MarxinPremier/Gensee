package com.wangzuo.libgensee.ui;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import com.wangzuo.libgensee.core.RTLive;
import com.wangzuo.libgensee.db.PlayerChatDataBaseManager;
import com.wangzuo.libgensee.receiver.ConnectionReceiver;
import com.wangzuo.libgensee.receiver.PhoneStateReceiver;
import com.wangzuo.libgensee.service.LogCatService;
import com.wangzuo.libgensee.ui.holder.chat.AbsChatImpl;
import com.wangzuo.libgensee.ui.holder.chat.SimpleChatHolder;
import com.wangzuo.libgensee.ui.holder.chat.impl.MsgQueue;
import com.wangzuo.libgensee.ui.view.CustomDialog;
import com.wangzuo.libgensee.ui.view.CustomProgressDialog;
import com.wangzuo.libgensee.ui.view.CustomDialog.Builder;
import com.wangzuo.libgensee.util.ResManager;
import com.gensee.utils.GenseeLog;
import java.util.HashMap;

public class BaseActivity extends Activity {
    protected View linLoadView;
    protected View linloadPb;
    protected View linLoadNetDisconnected;
    protected View relExit;
    protected View lyLoadText;
    protected TextView loadText;
    protected String TAG = this.getClass().getSimpleName();
    private CustomProgressDialog progressDialog;
    protected CustomDialog customDialog;
    private Builder builder;
    private HashMap<Integer, Integer> errMap;
    private Intent serviceIntent;
    private SimpleChatHolder mChatHolder;
    protected AbsChatImpl chatImpl;
    protected int netStatus = 4;
    private ConnectionReceiver connectionReceiver;
    private PhoneStateReceiver phoneStateReceiver;

    public BaseActivity() {
    }

    public AbsChatImpl getChatImpl() {
        return this.chatImpl;
    }

    public void setSimpleChatHolder(SimpleChatHolder mChatHolder) {
        this.mChatHolder = mChatHolder;
    }

    public SimpleChatHolder getSimpleChatHolder() {
        return this.mChatHolder;
    }

    public Builder getCustomDialogBuilder() {
        return this.builder;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GenseeLog.i("fast-sdk activity onCreate, now version is 6.0");
        this.getWindow().setFlags(128, 128);
        this.startLogService();

        try {
            this.deleteDatabase("FastSdkChat.db");
        } catch (Throwable var3) {
            GenseeLog.i("try catch removeCache deleteDataBase " + (var3 != null?var3.getMessage():""));
        }

        PlayerChatDataBaseManager playerDataBaseManager = new PlayerChatDataBaseManager(this.getApplicationContext());
        MsgQueue.getIns().initMsgDbHelper(playerDataBaseManager);
    }

    protected void onDestroy() {
        super.onDestroy();
        this.unRegisterReceiver();
        if(this.chatImpl != null) {
            this.chatImpl.release();
        }

    }

    public void showProgressDialog(int resId) {
        if(resId > 0) {
            this.showProgressDialog(this.getString(resId));
        }

    }

    public void showProgressDialog(String msg) {
        if(this.progressDialog != null) {
            this.progressDialog.dismiss();
        } else {
            this.progressDialog = new CustomProgressDialog(this, ResManager.getStyleId("Custom_Progress"));
        }

        this.progressDialog.show(msg, false, (OnCancelListener)null);
    }

    public void dismissProgressDialog() {
        if(this.progressDialog != null && this.progressDialog.isShowing()) {
            this.progressDialog.dismiss();
        }

    }

    public void showDialog(String title, String msg) {
        this.showDialog(title, msg, this.getString(ResManager.getStringId("gs_sure")), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                BaseActivity.this.finish();
            }
        }, new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                BaseActivity.this.finish();
            }
        });
    }

    public void showDialog(String title, String msg, String btnText, OnClickListener l) {
        this.showDialog(title, msg, btnText, l, (OnCancelListener)null);
    }

    public void showDialog(String title, String msg, String btnText, OnClickListener l, OnCancelListener cancelListener) {
        this.showDialog(title, msg, (String)null, (OnClickListener)null, btnText, l, cancelListener);
    }

    public void cancelCustomDialog() {
        if(this.customDialog != null && this.customDialog.isShowing()) {
            this.customDialog.dismiss();
        }

    }

    public void showDialog(String title, String msg, String btnText, OnClickListener l, String cancelText, OnClickListener cancelClickListener, OnCancelListener cancelListener, boolean isReverseColor) {
        if(!this.isFinishing()) {
            this.cancelCustomDialog();
            this.builder = new Builder(this);
            if(!"".equals(title) && title != null) {
                this.builder.setTitle(title);
            }

            this.builder.setMessage(msg);
            this.builder.setPositiveButtonText("");
            this.builder.setPositiveButton(btnText, l);
            if(cancelText != null && !"".equals(cancelText)) {
                this.builder.setNegativeButton(cancelText, cancelClickListener);
            }

            this.customDialog = this.builder.create();
            this.builder.setPositiveButtonTextColor(isReverseColor?this.getResources().getColor(ResManager.getColorId("gs_dialog_btn_text_red")):this.getResources().getColor(ResManager.getColorId("gs_dialog_btn_text_black")));
            this.builder.setNegativeButtonTextColor(isReverseColor?this.getResources().getColor(ResManager.getColorId("gs_dialog_btn_text_black")):this.getResources().getColor(ResManager.getColorId("gs_dialog_btn_text_red")));
            this.customDialog.setCancelable(false);
            if(cancelListener != null) {
                this.customDialog.setOnCancelListener(cancelListener);
            }

            this.customDialog.show();
            WindowManager windowManager = this.getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            LayoutParams lp = this.customDialog.getWindow().getAttributes();
            if(display.getWidth() > display.getHeight()) {
                lp.width = display.getHeight();
            } else {
                lp.width = display.getWidth();
            }

            this.customDialog.getWindow().setAttributes(lp);
        }
    }

    public void showDialog(String title, String msg, String btnText, OnClickListener l, String cancelText, OnClickListener cancelClickListener, OnCancelListener cancelListener) {
        this.showDialog(title, msg, btnText, l, cancelText, cancelClickListener, cancelListener, false);
    }

    public void showDialog(String msg) {
        this.showDialog("", msg);
    }

    public void showCancelErrMsg(String msg, String btnText) {
        this.showDialog("", msg, btnText, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    @SuppressLint({"UseSparseArrays"})
    private void initErrMap() {
        if(this.errMap == null) {
            this.errMap = new HashMap();
            this.errMap.put(Integer.valueOf(-100), Integer.valueOf(ResManager.getStringId("gs_domain_error")));
            this.errMap.put(Integer.valueOf(-101), Integer.valueOf(ResManager.getStringId("gs_domain_error")));
            this.errMap.put(Integer.valueOf(-102), Integer.valueOf(ResManager.getStringId("gs_domain_error")));
            this.errMap.put(Integer.valueOf(-103), Integer.valueOf(ResManager.getStringId("gs_domain_error")));
            this.errMap.put(Integer.valueOf(-109), Integer.valueOf(ResManager.getStringId("gs_domain_error")));
            this.errMap.put(Integer.valueOf(-1), Integer.valueOf(ResManager.getStringId("gs_domain_error")));
            this.errMap.put(Integer.valueOf(-104), Integer.valueOf(ResManager.getStringId("gs_net_disconnect")));
            this.errMap.put(Integer.valueOf(-105), Integer.valueOf(ResManager.getStringId("gs_error_data_timeout")));
            this.errMap.put(Integer.valueOf(-106), Integer.valueOf(ResManager.getStringId("gs_error_service")));
            this.errMap.put(Integer.valueOf(-107), Integer.valueOf(ResManager.getStringId("gs_error_param")));
            this.errMap.put(Integer.valueOf(0), Integer.valueOf(ResManager.getStringId("gs_error_number_unexist")));
            this.errMap.put(Integer.valueOf(4), Integer.valueOf(ResManager.getStringId("gs_error_token")));
            this.errMap.put(Integer.valueOf(5), Integer.valueOf(ResManager.getStringId("gs_error_login")));
            this.errMap.put(Integer.valueOf(2), Integer.valueOf(ResManager.getStringId("gs_error_role")));
            this.errMap.put(Integer.valueOf(3), Integer.valueOf(ResManager.getStringId("gs_error_fail_webcast")));
            this.errMap.put(Integer.valueOf(6), Integer.valueOf(ResManager.getStringId("gs_error_webcast_unstart")));
            this.errMap.put(Integer.valueOf(7), Integer.valueOf(ResManager.getStringId("gs_error_isonly_web")));
            this.errMap.put(Integer.valueOf(8), Integer.valueOf(ResManager.getStringId("gs_error_room_unenable")));
            this.errMap.put(Integer.valueOf(9), Integer.valueOf(ResManager.getStringId("gs_error_owner_error")));
            this.errMap.put(Integer.valueOf(10), Integer.valueOf(ResManager.getStringId("gs_error_invalid_address")));
            this.errMap.put(Integer.valueOf(11), Integer.valueOf(ResManager.getStringId("gs_error_room_overdue")));
            this.errMap.put(Integer.valueOf(12), Integer.valueOf(ResManager.getStringId("gs_error_authourization_not_enough")));
            this.errMap.put(Integer.valueOf(13), Integer.valueOf(ResManager.getStringId("gs_error_untimely")));
            this.errMap.put(Integer.valueOf(18), Integer.valueOf(ResManager.getStringId("gs_error_unsupport_mobile")));
        }

    }

    public String getErrStr(int resId) {
        this.initErrMap();
        if(this.errMap.get(Integer.valueOf(resId)) != null) {
            int resId1 = ((Integer)this.errMap.get(Integer.valueOf(resId))).intValue();
            if(resId1 <= 0) {
                return "";
            } else {
                String sReturn = this.getString(resId1);
                if(sReturn == null) {
                    sReturn = "";
                }

                return sReturn;
            }
        } else {
            return "";
        }
    }

    protected void showErrDialog(String errorMsg, String sTip) {
        this.showDialog("", errorMsg, sTip, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                BaseActivity.this.finish();
            }
        });
    }

    protected void startLogService() {
        this.serviceIntent = new Intent(this, LogCatService.class);
        this.startService(this.serviceIntent);
    }

    protected void stopLogService() {
        if(this.serviceIntent != null) {
            this.stopService(this.serviceIntent);
        }

    }

    protected void registerAppReceiver() {
        IntentFilter filter;
        if(this.connectionReceiver == null) {
            this.connectionReceiver = new ConnectionReceiver();
            filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            this.registerReceiver(this.connectionReceiver, filter);
        }

        if(this.phoneStateReceiver == null) {
            this.phoneStateReceiver = new PhoneStateReceiver();
            filter = new IntentFilter();
            filter.addAction("android.intent.action.PHONE_STATE");
            this.registerReceiver(this.phoneStateReceiver, filter);
            this.phoneStateReceiver.setOnPhoneStateListener(RTLive.getIns());
        }

    }

    protected void unRegisterReceiver() {
        if(this.connectionReceiver != null) {
            this.unregisterReceiver(this.connectionReceiver);
            this.connectionReceiver = null;
        }

        if(this.phoneStateReceiver != null) {
            this.unregisterReceiver(this.phoneStateReceiver);
            this.phoneStateReceiver = null;
        }

    }
}
