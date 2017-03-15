package com.wangzuo.libgensee.ui;


import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import com.gensee.entity.LoginResEntity;
import com.wangzuo.libgensee.core.RTLive;
import com.wangzuo.libgensee.receiver.ConnectionReceiver;
import com.wangzuo.libgensee.receiver.ConnectionReceiver.OnNetSwitchListener;
import com.wangzuo.libgensee.ui.holder.chat.impl.RTChatImpl;
import com.wangzuo.libgensee.util.GenseeUtils;
import com.wangzuo.libgensee.util.ResManager;
import com.gensee.room.RtSdk;
import com.gensee.routine.LiveodItem;
import com.gensee.routine.State;
import com.gensee.routine.UserInfo;
import com.gensee.utils.GenseeLog;

public abstract class BaseLiveActivity extends BaseActivity
        implements ConnectionReceiver.OnNetSwitchListener
{
    private static final String APP_START_TYPE = "APP_START_TYPE";
    private static final String APP_LANCHCODE = "APP_LANCHCODE";
    protected int netStatus = 4;

    private String appStartType = "";
    private String lanchCode = "";
    private LoginResEntity loginResEntity = null;
    public boolean isNeedExitApp = false;
    protected RTLive simpleImpl;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResID());

        this.chatImpl = new RTChatImpl();
        RTLive.getIns().setChatCallBack((RTChatImpl)this.chatImpl);

        initData();
        initView(savedInstanceState);

        if ((savedInstanceState != null) && (this.simpleImpl.getInitParam() != null))
        {
            GenseeLog.d("BaseLiveActivity client api reLogin");

            reLogin();
        }
        else
        {
            joinCheckNetwork();
        }
    }

    private void reLogin()
    {
        int netClassType = ConnectionReceiver.getNetType(this);
        this.netStatus = netClassType;

        if (netClassType == 4)
            reJoinWeb();
        else if (netClassType == 5)
            showErrDialog(getString(ResManager.getStringId("gs_net_no_network")), getString(ResManager.getStringId("gs_i_known")));
        else
            showDialogByNet(getNetTip(netClassType),
                    new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            BaseLiveActivity.this.reJoinWeb();
                        }
                    });
    }

    private void reJoinWeb()
    {
        RTLive.getIns().initJoin();
    }

    protected abstract int getLayoutResID();

    private void initData() {
        this.simpleImpl = RTLive.getIns();
    }

    protected abstract void initView(Bundle paramBundle);

    protected void joinCheckNetwork() {
        if (this.simpleImpl.getStatus() != 1) {
            return;
        }
        int netClassType = ConnectionReceiver.getNetType(this);
        this.netStatus = netClassType;

        if (netClassType == 4)
            join();
        else if (netClassType == 5)
            showErrDialog(getString(ResManager.getStringId("gs_net_no_network")), getString(ResManager.getStringId("gs_i_known")));
        else
            showDialogByNet(getNetTip(netClassType),
                    new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            BaseLiveActivity.this.join();
                        }
                    });
    }

    private void showDialogByNet(String msg, DialogInterface.OnClickListener l)
    {
        showDialog(null, msg, getString(ResManager.getStringId("gs_exit")),
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        BaseLiveActivity.this.exit();
                    }
                }
                , getString(ResManager.getStringId("gs_continues")), l, null);
    }

    protected abstract void onDismissClick(String paramString);

    public void showDialogByLOD(final LiveodItem liveodItem, final String dismissMsg) {
        showDialog(null, getString(ResManager.getStringId("gs_lod_is_on")),
                getString(ResManager.getStringId("gs_cancel")), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        BaseLiveActivity.this.onDismissClick(dismissMsg);
                    }
                }
                , getString(ResManager.getStringId("gs_end")), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        RTLive.getIns().getRtSdk().lodStop(liveodItem.getId(), null);
                    }
                }
                , null);
    }

    public void showDialogByAS(final String dismissMsg) {
        showDialog(null, getString(ResManager.getStringId("gs_as_is_on_msg")),
                getString(ResManager.getStringId("gs_i_known")), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        BaseLiveActivity.this.onDismissClick(dismissMsg);
                    }
                }
                , null, null, null);
    }

    protected void joinInitUI(RTLive simpleImpl) {
    }

    private void join() {
        joinInitUI(this.simpleImpl);
        if (this.simpleImpl.getStatus() == 1)
            RTLive.getIns().initJoin();
    }

    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString("APP_START_TYPE", this.appStartType);
        outState.putString("APP_LANCHCODE", this.lanchCode);
        outState.putSerializable("APP_LUNACH_ENTITY", this.loginResEntity);
    }

    protected void onStop()
    {
        RTLive.getIns().audioCloseSpeaker();
        RTLive.getIns().setAppBackGround(true);

        super.onStop();
    }

    protected void onResume()
    {
        RTLive.getIns().audioOpenSpeaker();
        RTLive.getIns().setAppBackGround(false);
        RTLive.getIns().setRefreshVideo(true);
        super.onResume();
    }

    public void onSwitchMobile(int netClass)
    {
        GenseeLog.d(this.TAG, "onSwitchMobile netStatus:" + this.netStatus);
        if (netClass != this.netStatus) {
            this.netStatus = netClass;
            if (this.netStatus != 4) {
                if (this.netStatus == 5) {
                    showReconnectText(this.netStatus);
                    return;
                }
                if (RTLive.getIns().isLiveStart())
                    showReconnectText(this.netStatus);
                showDialogByNet(getNetTip(netClass),
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                            }
                        });
            }
            else {
                showReconnectView(false);
                cancelCustomDialog();
            }
        }
    }

    protected void showReconnectView(boolean isShow) {
    }

    protected void showReconnectText(int netStatus) {  }


    protected void exit() { finish(); }

    private String getNetTip(int netClassType)
    {
        if (RTLive.getIns().isHost()) {
            return getString(ResManager.getStringId("gs_net_x_g_host"));
        }
        switch (netClassType) {
            case 1:
                return getString(ResManager.getStringId("gs_net_2g"));
            case 2:
                return getString(ResManager.getStringId("gs_net_3g"));
            case 3:
                return getString(ResManager.getStringId("gs_net_4g"));
            case 5:
                return getString(ResManager.getStringId("gs_net_disconnect"));
            case 4:
        }

        return getString(ResManager.getStringId("gs_net_2g"));
    }

    protected void release()
    {
        UserInfo self = RTLive.getIns().getSelf();
        if ((self != null) && (self.IsHost()))
        {
            RTLive.getIns().getRtSdk().roomNotifyBroadcastMsg(
                    GenseeUtils.formatText(self.getName(), 12) +
                            getString(ResManager.getStringId("gs_chat_host_leave")), true, null);
        }
        stopLogService();
        if (this.simpleImpl != null)
        {
            if ((self != null) && (self.IsHost()) && (RTLive.getIns().isSelfOnRostrum()))
            {
                RTLive.getIns().roomRecord(State.S_STOPPED.getValue());
                this.simpleImpl.leave(true);
            }
            else
            {
                this.simpleImpl.leave(false);
            }
        }
    }

    public void onError(final int errorCode)
    {
        runOnUiThread(new Runnable() {
            public void run() {
                String errStr = "";
                if (errorCode == -108)
                    errStr = BaseLiveActivity.this.getString(ResManager.getStringId("gs_join_webcast_err_third_auth"));
                else {
                    errStr = BaseLiveActivity.this.getErrStr(errorCode);
                }
                BaseLiveActivity.this.showErrDialog(errStr, BaseLiveActivity.this.getString(ResManager.getStringId("gs_i_known")));
            }
        });
    }

    public void onRoleHostDowngrade()
    {
        release();
    }

    public byte getDanmakuPriority(UserInfo userInfo)
    {
        byte level = 0;
        if (userInfo.IsHost())
            level = 3;
        else if (userInfo.IsPresentor())
            level = 2;
        else if (userInfo.IsPanelist()) {
            level = 1;
        }
        return level;
    }
}
