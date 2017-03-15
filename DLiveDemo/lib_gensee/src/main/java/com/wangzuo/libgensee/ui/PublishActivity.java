package com.wangzuo.libgensee.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.wangzuo.libgensee.core.RTLive;
import com.wangzuo.libgensee.ui.holder.chat.AbsChatImpl;
import com.wangzuo.libgensee.ui.holder.chat.impl.RTChatImpl;
import com.wangzuo.libgensee.util.GenseeUtils;
import com.wangzuo.libgensee.util.PreferUtil;
import com.wangzuo.libgensee.util.ResManager;
import com.gensee.utils.GenseeLog;

public class PublishActivity extends BaseLiveActivity
        implements View.OnClickListener {
    private PublishFragment publishFragment;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Object obj = msg.obj;
            switch (msg.what) {
                case 3000:
                    GenseeLog.i("UIMsg.AUDIO_ON_AUDIO_MIC_OPEN");
                    if (PublishActivity.this.publishFragment != null) {
                        PublishActivity.this.publishFragment.onAudioMicOpen();
                    }
                    break;
                case 3001:
                    GenseeLog.i("UIMsg.AUDIO_ON_AUDIO_MIC_CLOSE");
                    if (PublishActivity.this.publishFragment != null) {
                        PublishActivity.this.publishFragment.onAudioMicClose();
                    }
                    break;
                case 9000:
                    GenseeLog.i(PublishActivity.this.TAG, "UIMsg.CHAT_FORBID");
                    PublishActivity.this.showDialog("", PublishActivity.this.getString(ResManager.getStringId("gs_chat_forbid")), PublishActivity.this.getString(ResManager.getStringId("gs_i_known")), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    break;
                case 1000:
                    GenseeLog.i(PublishActivity.this.TAG, "UIMsg.publish UIMsg.ROOM_ON_ROOM_JOIN");
                    PublishActivity.this.showLoadingView(false);
                    PublishActivity.this.onRoomJoin(((Integer) obj).intValue());

                    GenseeUtils.sendLog(PublishActivity.this, true);
                    break;
                case 1008:
                    GenseeLog.i(PublishActivity.this.TAG, "UIMsg.ROOM_ON_ROOM_LEAVE");
                    PublishActivity.this.onRoomLeave(((Integer) obj).intValue());
                    break;
                case 1009:
                    GenseeLog.i(PublishActivity.this.TAG, "UIMsg.ROOM_ON_ROOM_RECONNENT");
                    PublishActivity.this.showReconnectText(PublishActivity.this.netStatus);
                    if (PublishActivity.this.publishFragment != null) {
                        PublishActivity.this.publishFragment.onRoomReconnect();
                    }
                    break;
                case 1007:
                    GenseeLog.i(PublishActivity.this.TAG, "UIMsg.ROOM_ON_ROOM_PUBLISH state=" + obj);
                    if (PublishActivity.this.publishFragment != null) {
                        PublishActivity.this.publishFragment.onRoomPublish(obj);
                    }

                    break;
                case 1006:
                    if ((obj != null) && (PublishActivity.this.publishFragment != null)) {
                        PublishActivity.this.publishFragment.updateTitle((String) obj);
                    }
                    break;
                case 8000:
                    if (PublishActivity.this.publishFragment != null) {
                        PublishActivity.this.publishFragment.showAsDialog();
                    }
                    break;
                case 8001:
                    if (PublishActivity.this.publishFragment != null) {
                        PublishActivity.this.cancelCustomDialog();
                        PublishActivity.this.publishFragment.endAs();
                    }
                    break;
                case 6005:
                    if (PublishActivity.this.publishFragment != null) {
                        PublishActivity.this.onRoleHostDowngrade();
                    }
                    break;
            }
        }
    };

    protected int getLayoutResID() {
        return ResManager.getLayoutId("gs_activity_publish");
    }

    public AbsChatImpl getChatImpl() {
        return this.chatImpl;
    }

    public Handler getHandler() {
        return this.mHandler;
    }

    protected void onCreate(Bundle savedInstanceState) {
        this.simpleImpl = RTLive.getIns();
        this.simpleImpl.initResource(this, this.mHandler);
        super.onCreate(savedInstanceState);
        PreferUtil.getIns().putInt("MIC_STATUS", -1);
        replaceFragment();

    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        this.linLoadView = findViewById(ResManager.getId("gs_linLoadView"));
        this.linloadPb = findViewById(ResManager.getId("gs_linLoadPro"));
        this.linLoadNetDisconnected = findViewById(ResManager.getId("gs_linLoadNetDisconnected"));
        this.relExit = findViewById(ResManager.getId("gs_exit_rel"));
        this.lyLoadText = findViewById(ResManager.getId("gs_lyLoadText"));
        this.loadText = ((TextView) findViewById(ResManager.getId("gs_loadText")));

    }

    protected void onDismissClick(String dismissMsg) {
    }

    protected void joinInitUI(RTLive simpleImpl) {
        this.linLoadView.setVisibility(View.VISIBLE);
        this.linloadPb.setVisibility(View.VISIBLE);
        this.linLoadNetDisconnected.setVisibility(View.GONE);
        ((RTChatImpl) this.chatImpl).setHandler(this.mHandler);
    }

    public void onRoleHostDowngrade() {
        this.relExit.setVisibility(View.VISIBLE);
        super.onRoleHostDowngrade();
    }

    private void replaceFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        if (this.publishFragment == null) {
            this.publishFragment = new PublishFragment();
        }
        transaction.replace(ResManager.getId("gs_main_content_ly"), this.publishFragment);
        try {
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onRoomJoin(int result) {
        int resId = 0;
        switch (result) {
            case -1:
                resId = ResManager.getStringId("gs_join_webcast_err_param");
                break;
            case 5:
                resId = ResManager.getStringId("gs_join_webcast_err_codec");
                break;
            case 3:
                resId = ResManager.getStringId("gs_join_webcast_err_host");
                break;
            case 4:
                resId = ResManager.getStringId("gs_join_webcast_err_license");
                break;
            case 2:
                resId = ResManager.getStringId("gs_join_webcast_err_locked");
                break;
            case 7:
                resId = ResManager.getStringId("gs_join_webcast_err_ip");
                break;
            case 8:
                resId = ResManager.getStringId("gs_join_webcast_err_too_early");
                break;
            case 6:
                break;
            case 1011:
                resId = ResManager.getStringId("gs_join_panelist");
                break;
            case 0:
                this.publishFragment.onRoomJoinSuccess();

                registerAppReceiver();
                break;
        }

        if (resId != 0)
            showErrDialog(getString(resId), getString(ResManager.getStringId("gs_i_known")));
    }

    private void onRoomLeave(int result) {
        unRegisterReceiver();
        int resId = 0;
        if (result == 0) {
            this.relExit.setVisibility(View.GONE);
            exit();
            return;
        }
        String sTip = getString(ResManager.getStringId("gs_i_known"));
        if (result == 1)
            resId = ResManager.getStringId("gs_leave_err_eject_tip");
        else if (result == 2)
            resId = ResManager.getStringId("gs_leave_webcast_err_timeup");
        else if (result == 3) {
            resId = ResManager.getStringId("gs_leave_err_close_tip");
        } else if (result == 4)
            resId = ResManager.getStringId("gs_leave_err_ip_deny");
        else {
            return;
        }
        showErrDialog(getString(resId), sTip);
    }

    public void onBackPressed() {
        String title = getString(ResManager.getStringId("gs_end_webcast"));
        showDialog("", title,
                getString(ResManager.getStringId("gs_end")), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        PublishActivity.this.release();
                        PublishActivity.this.relExit.setVisibility(View.VISIBLE);
                    }
                }
                , getString(ResManager.getStringId("gs_continues")), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
                , null, true);
    }

    protected void showReconnectText(int netStatus) {
        showReconnectView(true);
        if (netStatus == 5)
            this.loadText.setText(getString(ResManager.getStringId("gs_net_have_disconnect")));
        else if (RTLive.getIns().isReconnecting())
            this.loadText.setText(getString(ResManager.getStringId("gs_net_connecting")));
        else
            showReconnectView(false);
    }

    protected void showReconnectView(boolean isShow) {
        this.lyLoadText.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    protected void showLoadingView(boolean isShow) {
        this.linLoadView.setVisibility(isShow ? View.VISIBLE : View.GONE);
        showReconnectView(isShow);
    }

    public void onClick(View v) {
    }

    protected void exit() {
        removeCache();
        super.exit();
    }

    private void removeCache() {
        if (this.chatImpl != null) {
            this.chatImpl.release();
        }
        deleteDatabase("FastSdkChat.db");
        if (this.publishFragment != null)
            this.publishFragment.showTime(false);
    }
}
