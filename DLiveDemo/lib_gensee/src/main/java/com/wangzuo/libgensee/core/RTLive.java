package com.wangzuo.libgensee.core;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.text.TextUtils;
import com.gensee.callback.IChatCallBack;
import com.gensee.common.RTSharedPref;
import com.gensee.common.ServiceType;
import com.gensee.entity.InitParam;
import com.wangzuo.libgensee.receiver.PhoneStateReceiver.OnPhoneStateListener;
import com.wangzuo.libgensee.ui.BaseLiveActivity;
import com.wangzuo.libgensee.ui.PublishActivity;
import com.wangzuo.libgensee.ui.WatchActivity;
import com.wangzuo.libgensee.util.GenseeUtils;
import com.wangzuo.libgensee.util.PreferUtil;
import com.wangzuo.libgensee.util.ResManager;
import com.gensee.net.RtComp;
import com.gensee.net.RtComp.Callback;
import com.gensee.player.Player;
import com.gensee.room.RtSdk;
import com.gensee.room.RtSimpleImpl;
import com.gensee.routine.IDCInfo;
import com.gensee.routine.LiveodItem;
import com.gensee.routine.State;
import com.gensee.routine.UserInfo;
import com.gensee.taskret.OnTaskRet;
import com.gensee.user.UserManager;
import com.gensee.utils.GenseeLog;
import com.gensee.view.ILocalVideoView;
import com.wangzuo.libgensee.receiver.PhoneStateReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RTLive extends RtSimpleImpl
        implements RtComp.Callback, PhoneStateReceiver.OnPhoneStateListener
{
    private static final int AUIDO_JOIN = 1;
    private static final int AUIDO_MICAVAILABLE = 2;
    private static final int AUIDO_MIC_OPENED = 4;
    private static final int AUIDO_MIC_REOPEN = 8;
    private static final int VIDEO_START = 32;
    private static final int VIDEO_CAMERA_OPEN = 64;
    private static final int VIDEO_CAMERA_REOPEN = 128;
    private static final int USER_EVENT_PUBLISH = 1;
    public static final int S_IDLE = 1;
    public static final int S_JOIN_BEGIN = 2;
    public static final int S_JOINED = 3;
    public static final int S_RECONNECTING = 4;
    public static final int S_EXIT_BEGIN = 5;
    public static final int STATUS_UNSTART = 0;
    public static final int STATUS_ING = 1;
    public static final int STATUS_PAUSE = 3;
    public static final int STATUS_STOP = 2;
    private static final String TAG = "RTLive";
    private UserInfo self;
    private boolean bInitHostJoin;
    private Map<String, Long> roomData;
    private OnSysMsgListener onSysMsgListener;
    private int avFlag = 0;

    private static RTLive ins = null;
    private RtComp rtComp;
    private Context context;
    private InitParam initParam;
    private Handler roomHandler;
    private AtomicInteger nInitalRole = new AtomicInteger(0);
    private AtomicInteger status = new AtomicInteger(1);

    private AtomicBoolean isAppBackGround = new AtomicBoolean(false);
    private int eventFlag = 0;
    private int liveStatus = 0;
    private int lastWidth;
    private int lastHeight;
    private byte recordStaus = 0;
    private Context applicationContext;
    private List<OnUserCountChangeListener> userCountChangeList;
    private long speakTime = 0L;
    private OnHostStatusChangeListener onHostStatusChangeListener;
    private OnSelfVideoReadyListener onSelfVideoReadyListener;
    private boolean isAsBegin;
    private boolean isLodStart;
    private String liveodId;
    private boolean isPublishMode;
    private ServiceType serviceType;

    public ServiceType getServiceType()
    {
        return this.serviceType;
    }

    public boolean isPublishMode() {
        return this.isPublishMode;
    }

    public void setOnSelfVideoReadyListener(OnSelfVideoReadyListener onSelfVideoReadyListener) {
        this.onSelfVideoReadyListener = onSelfVideoReadyListener;
    }

    public void setOnHostStatusChangeListener(OnHostStatusChangeListener onHostStatusChangeListener) {
        this.onHostStatusChangeListener = onHostStatusChangeListener;
    }

    public void setAppBackGround(boolean isAppBackGround)
    {
        this.isAppBackGround.set(isAppBackGround);
    }

    public void setOnSysMsgListener(OnSysMsgListener onSysMsgListener) {
        this.onSysMsgListener = onSysMsgListener;
    }

    public InitParam getInitParam() {
        return this.initParam;
    }

    public UserInfo getSelf() {
        return this.self;
    }
    private void setStatus(int status) {
        this.status.set(status);
    }

    public int getStatus() {
        return this.status.get();
    }

    private RTLive() {
        this.roomData = new HashMap();
        initRoomData();
    }

    private void initRoomData() {
        this.roomData.put("user.rostrum", Long.valueOf(-1L));
        this.roomData.put("user.asker", Long.valueOf(-1L));
        this.roomData.put("user.asker.1", Long.valueOf(-1L));
        this.roomData.put("user.asker.2", Long.valueOf(-1L));
        this.roomData.put("user.asker.3", Long.valueOf(-1L));
    }

    private void releaseRoomData()
    {
        if (this.roomData != null)
        {
            initRoomData();
        }
    }

    public static RTLive getIns() {
        if (ins == null) {
            synchronized (RTLive.class) {
                if (ins == null) {
                    ins = new RTLive();
                }
            }
        }
        return ins;
    }

    public void startLive(Context context, GSFastConfig config, InitParam initParam)
    {
        this.initParam = initParam;
        this.applicationContext = context.getApplicationContext();
        Intent intent = new Intent();
        this.isPublishMode = config.isPublish();
        if (this.isPublishMode) {
            publishModeInit();
            intent.setClass(context, PublishActivity.class);
        } else {
            watchModeInit();
            intent.setClass(context, WatchActivity.class);
        }
        context.startActivity(intent);
    }

    private void publishModeInit() {
        RtSdk.loadLibrarys();
        RTSharedPref.initPref(this.applicationContext);
        PreferUtil.initPref(this.applicationContext);

        RTSharedPref.getIns().clear();
        String version = GenseeUtils.getVersionName(this.applicationContext);
        RTSharedPref.getIns().putString("app.version", version);
        RTSharedPref.getIns().putInt("save.video.width", 640);
        RTSharedPref.getIns().putInt("save.video.height", 480);
    }

    private void watchModeInit() {
        PreferUtil.initPref(this.applicationContext);
        String version = GenseeUtils.getVersionName(this.applicationContext);
    }

    public void initJoin()
    {
        if (this.rtComp == null) {
            this.rtComp = new RtComp(this.context, this);
        }

        if (this.initParam == null) {
            GenseeLog.e("initParam is null, can not start live");
            return;
        }
        this.rtComp.setbAttendeeOnly(false);
        this.rtComp.setHostJoinOnly(true);
        this.rtComp.initWithGensee(this.initParam);
    }

    public void onInited(String rtParams)
    {
        joinWithParam("", rtParams);
    }

    public void onErr(int errorCode)
    {
        GenseeLog.e("join onError errorCode:" + errorCode);
        ((BaseLiveActivity)this.context).onError(errorCode);
    }

    public void onInit(boolean result)
    {
        if ((result) && (isPanelistEx())) {
            onRoomJoin(1011, this.self, false);
            return;
        }
        if (isHostJoin())
        {
            sendMsg(this.roomHandler, 1012, Boolean.valueOf(result));
        }
        else sendMsg(this.roomHandler, 1013, Boolean.valueOf(result));

        super.onInit(result);
    }

    public void onNetworkReport(byte level)
    {
        super.onNetworkReport(level);
        sendMsg(this.roomHandler, 1014, Byte.valueOf(level));
    }

    public void onRoomData(String key, long value)
    {
        if (!isJoin())
        {
            GenseeLog.i("RTLive", "onRoomData key = " + key + "value = " + value + " isJoin = " + isJoin());
            return;
        }
        if (isInitHostJoin())
        {
            if ("user.rostrum".equals(key))
            {
                if ((this.self != null) && (((Long)this.roomData.get("user.rostrum")).longValue() == this.self.getId()) && (this.self.getId() != value))
                {
                    this.roomData.put(key, Long.valueOf(value));
                    sendMsg(this.roomHandler, 6005, Integer.valueOf(6008));
                }
            }
        }
        else onReceiverRoomData(key, value);

        this.roomData.put(key, Long.valueOf(value));
    }

    public boolean isJoin() {
        return getStatus() == 3;
    }

    public void onRoomJoin(int result, UserInfo self, boolean svrFailover)
    {
        super.onRoomJoin(result, self, svrFailover);
        setStatus(result == 0 ? 3 : 1);
        this.self = self;
        if (result != 0)
        {
            getRtSdk().release(null);
        }
        else {
            this.bInitHostJoin = ((self != null) && (self.IsHost()));
            if (svrFailover)
            {
                if ((!isSelfOnAsker()) && (((Long)this.roomData.get("user.rostrum")).longValue() != self.getId()))
                {
                    PreferUtil.getIns().putInt("CAMERA_STATUS", -1);
                    PreferUtil.getIns().putInt("MIC_STATUS", -1);
                    PreferUtil.getIns().putInt("VIDEO_SELF_ACTIVED", -1);
                }

                if (!this.bInitHostJoin)
                {
                    if (((Long)this.roomData.get("user.rostrum")).longValue() == self.getId())
                    {
                        getRtSdk().roomSetData("user.rostrum", self.getId(),
                                null);
                    }

                    if (isSelfOnAsker())
                    {
                        String askerId = getAskerIdByUserId(self.getId());
                        if (!"".equals(askerId))
                        {
                            getRtSdk().roomSetData(askerId, self.getId(),
                                    null);
                        }
                    }
                }
            }
        }
        if (this.bInitHostJoin)
        {
            getRtSdk().roomSetData("user.rostrum", getIns().getSelf().getId(),
                    null);
        }
        sendMsg(this.roomHandler, 1000, Integer.valueOf(result));
        if (svrFailover) {
            GenseeLog.i("RTLive onRoonJoin svrFailover!");
            sendMsg(this.roomHandler, 1016, Integer.valueOf(0));
        }
    }

    public void leave(boolean isClose)
    {
        GenseeLog.i("RTLive test video leave");
        if (this.userCountChangeList != null) {
            this.userCountChangeList.clear();
        }
        this.eventFlag = 0;
        this.avFlag = 0;
        setStatus(5);
        this.bInitHostJoin = false;
        if (this.self != null)
        {
            if (isSelfOnAsker())
            {
                String selfAskerId = getAskerIdByUserId(this.self.getId());
                if (!"".equals(selfAskerId))
                {
                    getRtSdk().roomSetData(selfAskerId, 0L, null);
                }
            }
            if (((Long)this.roomData.get("user.rostrum")).longValue() == this.self.getId())
            {
                getRtSdk().roomSetData("user.rostrum", 0L, null);
            }
        }
        releaseRoomData();
        super.leave(isClose);
    }

    protected void onRelease(int reason)
    {
        GenseeLog.i("RTLive test video onRelease");
        this.eventFlag = 0;
        this.avFlag = 0;
        this.bInitHostJoin = false;
        setStatus(1);
        this.liveStatus = 0;
        this.recordStaus = 0;
        this.lastWidth = 0;
        this.lastHeight = 0;
        sendMsg(this.roomHandler, 1008, Integer.valueOf(reason));
    }

    public void onRoomReconnecting()
    {
        PreferUtil.getIns().putInt("MIC_STATUS", isMicOpen() ? 1 : 0);
        setStatus(4);
        this.avFlag &= -33;
        if ((this.avFlag & 0x40) == 64) {
            onVideoCameraClosed();
        }
        sendMsg(this.roomHandler, 1009, null);

        super.onRoomReconnecting();
    }

    public void onRoomPublish(State mState)
    {
        byte mValue = mState.getValue();
        this.liveStatus = mValue;

        if (((this.eventFlag & 0x1) != 1) &&
                (this.liveStatus == 1)) {
            UserInfo self = getSelf();
            if ((self != null) && (self.IsHost())) {
                postRecord();
            }
        }

        sendMsg(this.roomHandler, 1007, Byte.valueOf(mState.getValue()));
    }

    private void onReceiverRoomData(String key, long value)
    {
        if ("user.rostrum".equals(key)) {
            if (value == 0L) {
                if ((((Long)this.roomData.get("user.rostrum")).longValue() == this.self.getId()) && (!isSelfOnAsker()))
                {
                    getRtSdk().audioCloseMic(null);
                }
            }
            else {
                UserInfo self = getSelf();
                if ((self != null) && (value == self.getId())) {
                    if ((((Long)this.roomData.get("user.asker")).longValue() == value) ||
                            (((Long)this.roomData.get("user.asker.1")).longValue() == value) ||
                            (((Long)this.roomData.get("user.asker.2")).longValue() == value) ||
                            (((Long)this.roomData.get("user.asker.3")).longValue() == value))
                    {
                        String askeIdx = getAskerIdByUserId(value);
                        if (!"".equals(askeIdx))
                        {
                            getRtSdk().roomSetData(askeIdx, 0L, null);
                        }
                    }

                    getRtSdk().roomHanddown(false, null);
                    if ((this.avFlag & 0x4) != 4)
                    {
                        if ((((Long)this.roomData.get("user.rostrum")).longValue() == self.getId()) && (PreferUtil.getIns().getInt("MIC_STATUS") != 1))
                        {
                            return;
                        }
                        audioOpenMic();
                    }

                }
                else if ((self != null) && (((Long)this.roomData.get("user.rostrum")).longValue() == self.getId()) && (self.getId() != value))
                {
                    getRtSdk().audioCloseMic(null);
                }

            }

        }
        else if (("user.asker".equals(key)) ||
                ("user.asker.1".equals(key)) ||
                ("user.asker.2".equals(key)) ||
                ("user.asker.3".equals(key)))
        {
            if (value == 0L) {
                if ((((Long)this.roomData.get(key)).longValue() == this.self.getId()) && (((Long)this.roomData.get("user.rostrum")).longValue() != this.self.getId()))
                {
                    getRtSdk().audioCloseMic(null);
                }
            }
            else if (value == this.self.getId())
            {
                if (((Long)this.roomData.get("user.rostrum")).longValue() == value)
                {
                    getRtSdk().roomSetData("user.rostrum", 0L, null);
                }
                if ((this.avFlag & 0x4) != 4)
                {
                    if ((isSelfOnAsker()) && (PreferUtil.getIns().getInt("MIC_STATUS") != 1))
                    {
                        return;
                    }
                    audioOpenMic();
                }

                getRtSdk().roomHanddown(false, null);
            }
            else if ((value != this.self.getId()) && (((Long)this.roomData.get(key)).longValue() == this.self.getId()))
            {
                getRtSdk().audioCloseMic(null);
            }
        }
    }

    private boolean isSelfOnAsker()
    {
        if (this.self == null)
        {
            return false;
        }
        return (((Long)this.roomData.get("user.asker")).longValue() == this.self.getId()) ||
                (((Long)this.roomData.get("user.asker.1")).longValue() == this.self.getId()) ||
                (((Long)this.roomData.get("user.asker.2")).longValue() == this.self.getId()) ||
                (((Long)this.roomData.get("user.asker.3")).longValue() == this.self.getId());
    }

    public boolean isSelfOnRostrum()
    {
        if (this.self == null)
        {
            return false;
        }

        return ((Long)this.roomData.get("user.rostrum")).longValue() == this.self.getId();
    }

    private String getAskerIdByUserId(long value)
    {
        String askeIdx = ((Long)this.roomData.get("user.asker")).longValue() == value ? "user.asker" : "";
        if ("".equals(askeIdx))
        {
            askeIdx = ((Long)this.roomData.get("user.asker.1")).longValue() == value ? "user.asker.1" : "";
        }
        if ("".equals(askeIdx))
        {
            askeIdx = ((Long)this.roomData.get("user.asker.2")).longValue() == value ? "user.asker.2" : "";
        }
        if ("".equals(askeIdx))
        {
            askeIdx = ((Long)this.roomData.get("user.asker.3")).longValue() == value ? "user.asker.3" : "";
        }

        return askeIdx;
    }

    private void postRecord() {
        if (this.roomHandler != null)
            this.roomHandler.postDelayed(new Runnable()
                                         {
                                             public void run()
                                             {
                                                 if (RTLive.this.recordStaus != State.S_RUNNING.getValue())
                                                     RTLive.this.roomRecord(State.S_RUNNING.getValue());
                                             }
                                         }
                    , 1000L);
    }

    public boolean isVideoCameraOpen()
    {
        return (this.avFlag & 0x40) == 64;
    }

    public boolean isMicOpen() {
        return ((this.avFlag & 0x8) == 8) ||
                ((this.avFlag & 0x4) == 4);
    }

    protected void onVideoStart()
    {
        this.avFlag |= 32;
        sendMsg(this.roomHandler, 2000, null);
    }

    protected void onVideoEnd()
    {
        this.avFlag &= -33;
        sendMsg(this.roomHandler, 2001, null);
    }

    public void onJoin(boolean b)
    {
    }

    public Context onGetContext()
    {
        return this.context;
    }

    public void onSettingSet(String key, int val)
    {
        if ("training.user.my.role".equals(key)) {
            this.nInitalRole.set(val);
        } else if ("chat.disable.attendee.private".equals(key)) {
            int nSiteChatMode = val == 0 ? 101 :
                    100;
            if (this.onSysMsgListener != null) {
                this.onSysMsgListener.onSitePrivateChatMode(nSiteChatMode);
            }

        }

        super.onSettingSet(key, val);
    }

    public void onSettingSet(String key, String val)
    {
        if ("training.class.name".equals(key))
            sendMsg(this.roomHandler, 1006, val);
    }

    private boolean isHostJoin()
    {
        int nRole = this.nInitalRole.get();
        return (nRole & 0x1) == 1;
    }

    private boolean isPanelistEx() {
        int nRole = this.nInitalRole.get();
        if (((nRole & 0x1) == 1) || ((nRole & 0x2) == 2)) {
            return false;
        }
        return (nRole & 0x4) == 4;
    }

    private void sendMsg(Handler handler, int what, Object obj) {
        if (handler != null) {
            if (obj == null) {
                handler.sendEmptyMessage(what);
                return;
            }
            handler.sendMessage(handler.obtainMessage(what, obj));
        }
    }

    public void chatWithPublic(String text, String rich) {
        getRtSdk().chatWithPublic(text, rich, null);
    }

    public boolean isReconnecting()
    {
        return getStatus() == 4;
    }

    public UserInfo getUserById(long userId) {
        return getRtSdk().getUserById(userId);
    }

    public void initResource(Context context, Handler roomHandler) {
        if (context != null) {
            this.context = context;
        }
        this.roomHandler = roomHandler;
    }

    public void onAudioJoinConfirm(boolean ok)
    {
        if (ok)
            this.avFlag |= 1;
    }

    public void onAudioSpeakerOpened()
    {
        super.onAudioSpeakerOpened();
        if (this.isAppBackGround.get())
        {
            audioCloseSpeaker();
        }
    }

    public void audioCloseSpeaker()
    {
        if ((this.avFlag & 0x1) == 1)
        {
            getRtSdk().audioCloseSpeaker(null);
        }
    }

    public void audioOpenSpeaker()
    {
        if ((this.avFlag & 0x1) == 1)
        {
            getRtSdk().audioOpenSpeaker(null);
        }
    }

    public void audioCloseMic() {
        getRtSdk().audioCloseMic(null);
    }

    public void audioOpenMic()
    {
        if ((this.avFlag & 0x4) != 4)
        {
            getRtSdk().audioOpenMic(null);
        }
    }

    public void videoOpenCamera() {
        if ((this.avFlag & 0x40) != 64)
        {
            getRtSdk().videoOpenCamera(null);
        }
    }

    public void videoCloseCamera()
    {
        getRtSdk().videoCloseCamera(null);
    }

    public void onAsData(byte[] data, int width, int height)
    {
        if ((this.lastWidth != width) || (this.lastHeight != height)) {
            sendMsg(this.roomHandler, 2006, Float.valueOf(width / height));
            this.lastWidth = width;
            this.lastHeight = height;
        }
        super.onAsData(data, width, height);
    }

    public void onVideoCameraOpened()
    {
        super.onVideoCameraOpened();
        GenseeLog.i("RTLive test video onVideoCameraOpened");
        if (this.bInitHostJoin)
        {
            stopLod();
            getRtSdk().videoActive(this.self.getId(), true, null);
            onVideoActived(this.self, true);
            if (this.onSelfVideoReadyListener != null)
            {
                this.onSelfVideoReadyListener.onSelfVideoReady();
            }
            this.avFlag |= 64;
            sendMsg(this.roomHandler, 2003, null);
        }
        else
        {
            this.avFlag |= 64;
            if (isSelfVideoActiveId())
            {
                onVideoActived(this.self, true);
            }
        }
    }

    public boolean isSelfVideoActiveId() {
        return (this.self != null) && (this.self.getId() == getVideoActiveId());
    }

    public void onVideoCameraClosed()
    {
        super.onVideoCameraClosed();

        this.avFlag &= -65;
        if (isSelfVideoActiveId())
        {
            onVideoActived(this.self, false);
        }

        sendMsg(this.roomHandler, 2002, null);
    }

    public void onAudioMicClosed()
    {
        if ((this.avFlag & 0x4) == 4) {
            sendMsg(this.roomHandler, 3001, null);
        }

        this.avFlag &= -5;
        this.speakTime = 0L;
    }

    public void onAudioMicOpened()
    {
        this.avFlag |= 4;
        this.speakTime = System.currentTimeMillis();
        sendMsg(this.roomHandler, 3000, null);
    }

    private void reOpenMic() {
        if ((this.avFlag & 0x8) == 8) {
            this.avFlag &= -9;
            audioOpenMic();
        }
    }

    public void callRinging()
    {
        if ((this.avFlag & 0x4) == 4) {
            this.avFlag |= 8;
            this.avFlag &= -5;
            getRtSdk().audioCloseMic(null);
        }
    }

    public void onAsBegin(long owner)
    {
        super.onAsBegin(owner);
        this.isAsBegin = true;
        if ((this.bInitHostJoin) && (this.self != null) && (owner != this.self.getId()))
            sendMsg(this.roomHandler, 8000, null);
    }

    public void onAsEnd()
    {
        super.onAsEnd();
        this.isAsBegin = false;
        if (this.bInitHostJoin)
            sendMsg(this.roomHandler, 8001, null);
    }

    public void onLodStart(LiveodItem liveodItem)
    {
        super.onLodStart(liveodItem);
        this.isLodStart = true;
        this.liveodId = liveodItem.getId();
    }

    public void onLodStop(LiveodItem liveodItem)
    {
        super.onLodStop(liveodItem);
        this.isLodStart = false;
        this.liveodId = null;
    }

    public void callOffHook()
    {
        reOpenMic();
    }

    public boolean isLiveStart()
    {
        return this.liveStatus != 0;
    }

    public boolean isLivePause()
    {
        return this.liveStatus == 3;
    }

    public boolean isInitHostJoin()
    {
        return this.bInitHostJoin;
    }

    public void roomRecord(byte state)
    {
        getRtSdk().roomRecord(state, null);
    }

    public void roomPublish(byte state)
    {
        getRtSdk().roomPublish(state, new OnTaskRet()
        {
            public void onTaskRet(boolean ret, int arg1, String arg2)
            {
                if (ret)
                    RTLive.this.eventFlag |= 1;
            }
        });
        if (isLiveStart()) {
            if (isLivePause()) {
                getIns().getRtSdk().roomNotifyBroadcastMsg(this.context.getString(
                        ResManager.getStringId("gs_live_resume")), true, null);
            }

        }
        else
        {
            getIns().getRtSdk().roomNotifyBroadcastMsg(
                    this.context.getString(ResManager.getStringId("gs_chat_activity_start")), true, null);
        }

        this.liveStatus = state;
        sendMsg(this.roomHandler, 1007, Byte.valueOf(state));
    }


    public void onRoomUserLeave(UserInfo userInfo) {
        super.onRoomUserLeave(userInfo);
        if(this.userCountChangeList != null && this.userCountChangeList.size() > 0) {
            Iterator var3 = this.userCountChangeList.iterator();

            while(var3.hasNext()) {
                com.gensee.fastsdk.core.RTLive.OnUserCountChangeListener temp = (com.gensee.fastsdk.core.RTLive.OnUserCountChangeListener)var3.next();
                temp.onUserCountChange(String.valueOf(UserManager.getIns().getUserCount()));
            }
        }

    }

    public void activeAndPublish()
    {
        GenseeLog.i("RTLive", "activeSelfVideoLive!");
        getRtSdk().videoActive(this.self.getId(), true, null);
        roomPublish(State.S_RUNNING.getValue());
        roomRecord(State.S_RUNNING.getValue());
        stopLod();
        onVideoActived(this.self, true);
    }

    public void stopLod()
    {
        if ((this.isLodStart) && (!TextUtils.isEmpty(this.liveodId)))
            getRtSdk().lodStop(this.liveodId, null);
    }

    public void onRoomUserUpdate(UserInfo userInfo)
    {
        super.onRoomUserUpdate(userInfo);
        if ((userInfo != null) && (this.self != null) &&
                (this.self.getId() == userInfo.getId())) {
            if ((this.bInitHostJoin) &&
                    (this.self.IsHost()) && (!userInfo.IsHost())) {
                this.bInitHostJoin = false;
                if ((this.self.IsHost()) && (!userInfo.IsHost()) && (userInfo.IsPresentor())) {
                    sendMsg(this.roomHandler, 6005, Integer.valueOf(6007));
                    GenseeLog.i(" onRoomUserUpdate roleHostToPresentor");
                }
                else if ((this.self.IsHost()) && (!userInfo.IsHost()) && (!userInfo.IsPresentor()) && (userInfo.IsPanelist())) {
                    sendMsg(this.roomHandler, 6005, Integer.valueOf(6001));
                    GenseeLog.i(" onRoomUserUpdate roleHostToPanelist");
                }
                else if ((!this.self.IsHost()) && (this.self.IsPresentor()) && (userInfo.IsHost())) {
                    GenseeLog.i(" onRoomUserUpdate rolePresentorToHost");
                    sendMsg(this.roomHandler, 6005, Integer.valueOf(6000));
                }
                else if ((!this.self.IsHost()) && (!this.self.IsPresentor()) && (this.self.IsPanelist()) && (userInfo.IsHost())) {
                    sendMsg(this.roomHandler, 6005, Integer.valueOf(6002));
                    GenseeLog.i(" onRoomUserUpdate rolePanelistToHost");
                }
                else if ((!this.self.IsHost()) && (!this.self.IsPresentor()) && (this.self.IsPanelist()) && (!userInfo.IsHost()) && (userInfo.IsPresentor())) {
                    sendMsg(this.roomHandler, 6005, Integer.valueOf(6003));
                    GenseeLog.i(" onRoomUserUpdate rolePanelistToPresentor");
                }
                else if ((!this.self.IsHost()) && (this.self.IsPresentor()) && (!userInfo.IsHost()) && (!userInfo.IsPresentor()) && (userInfo.IsPanelist())) {
                    sendMsg(this.roomHandler, 6005, Integer.valueOf(6004));
                    GenseeLog.i(" onRoomUserUpdate rolePresentorToPanelist");
                }

            }

            this.self = userInfo;
        }
    }

    public void onRoomBroadcastMsg(String msg)
    {
        if (this.onSysMsgListener != null) {
            this.onSysMsgListener.onBraodcastMsg(msg);
        }
        super.onRoomBroadcastMsg(msg);
    }

    public void onChatMode(int nChatMode)
    {
        super.onChatMode(nChatMode);
        if (this.onSysMsgListener != null)
        {
            if (nChatMode == 0)
                this.onSysMsgListener.onChatMode(nChatMode, this.context.getResources().getString(
                        ResManager.getStringId("gs_chat_publicchat_close")));
            else if (nChatMode == 1)
                this.onSysMsgListener.onChatMode(nChatMode, this.context.getResources().getString(
                        ResManager.getStringId("gs_chat_publicchat_open")));
        }
    }

    public void onLodPause(LiveodItem liveodItem)
    {
        super.onLodPause(liveodItem);
    }

    public IDCInfo[] roomIDCGetList()
    {
        return getRtSdk().getIDCs();
    }

    public String roomIDCGetCurrent(Context context) {
        if (this.isPublishMode) {
            return getRtSdk().getCurIDC();
        }
        return ((WatchActivity)context).getmPlayer().getCurIdc();
    }

    public void roomIDCSetCurrent(String idcID, Context context)
    {
        if (this.isPublishMode)
            getRtSdk().setCurIDC(idcID, null);
        else
            ((WatchActivity)context).getmPlayer().setIdcId(idcID, null);
    }

    public void setLocalTextureVideoView(ILocalVideoView localTextureVideoView)
    {
        getRtSdk().setLocalVideoView(localTextureVideoView);
    }

    public void onVideoDataRender(long userId, int width, int height, int frameFormat, float displayRatio, byte[] data)
    {
        if ((!this.isAsBegin) && (
                (this.lastWidth != width) || (this.lastHeight != height))) {
            sendMsg(this.roomHandler, 2006, Float.valueOf(width / height));
            this.lastWidth = width;
            this.lastHeight = height;
        }

        if (userId != this.self.getId())
            super.onVideoDataRender(userId, width, height, frameFormat,
                    displayRatio, data);
    }

    public void setChatCallBack(IChatCallBack iChatCallBack)
    {
        getRtSdk().setChatCallback(iChatCallBack);
    }

    public boolean isHost()
    {
        return (this.self != null) && (this.self.IsHost());
    }

    public void addOnUserCountChangeListener(OnUserCountChangeListener onUserCountChangeListener)
    {
        if (this.userCountChangeList == null)
        {
            this.userCountChangeList = new ArrayList();
        }
        if (!this.userCountChangeList.contains(onUserCountChangeListener))
        {
            this.userCountChangeList.add(onUserCountChangeListener);
        }
    }

    public static abstract interface OnHostStatusChangeListener
    {
        public abstract void onHostJoin(String paramString);

        public abstract void onHostLeave(String paramString);
    }

    public static abstract interface OnSelfVideoReadyListener
    {
        public abstract void onSelfVideoReady();
    }

    public static abstract interface OnSysMsgListener
    {
        public abstract void onSysMsg(String paramString);

        public abstract void onBraodcastMsg(String paramString);

        public abstract void onChatMode(int paramInt, String paramString);

        public abstract void onSitePrivateChatMode(int paramInt);
    }

    public static abstract interface OnUserCountChangeListener
    {
        public abstract void onUserCountChange(String paramString);
    }
}
