package com.wangzuo.libgensee.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import com.gensee.entity.PingEntity;
import com.gensee.entity.UserInfo;
import com.wangzuo.libgensee.core.RTLive;
import com.wangzuo.libgensee.receiver.ConnectionReceiver;
import com.wangzuo.libgensee.receiver.ConnectionReceiver.OnNetSwitchListener;
import com.wangzuo.libgensee.ui.BaseActivity;
import com.wangzuo.libgensee.ui.WatchFragment.WatchInterface;
import com.wangzuo.libgensee.ui.holder.IdcHolder.FastIdc;
import com.wangzuo.libgensee.ui.holder.chat.impl.PlayerChatImpl;
import com.wangzuo.libgensee.util.ResManager;
import com.gensee.player.OnPlayListener;
import com.gensee.player.Player;
import com.gensee.taskret.OnTaskRet;
import com.gensee.utils.GenseeLog;
import com.gensee.view.GSVideoView;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class WatchActivity extends BaseActivity implements OnPlayListener, OnNetSwitchListener, WatchInterface {
    private Player mPlayer;
    public static final int S_IDLE = 1;
    public static final int S_JOIN_BEGIN = 2;
    public static final int S_JOINED = 3;
    public static final int S_RECONNECTING = 4;
    public static final int S_EXIT_BEGIN = 5;
    private AtomicInteger status = new AtomicInteger(1);
    private boolean bJoinSuccess;
    private WatchFragment watchFragment;
    private Handler mHandler;
    private ArrayList<FastIdc> idcList;
    private static final int WHAT_0 = 0;
    private static final int WHAT_ERROR = 1;
    private static final int WHAT_JOIN = 2;
    private static final int WHAT_RECONNECT = 3;
    private static final int WHAT_INVITE = 4;
    private static final int WHAT_VIDEO_START = 5;
    private static final int WHAT_VIDEO_END = 6;
    private static final int WHAT_SUBJECT = 7;
    private static final int WHAT_PUBLISH = 8;
    private static final int WHAT_LEAVE = 10;
    private static final int WHAT_SIZE_CHANGE = 11;
    private static final int WHAT_CACHING = 12;
    private static final int WHAT_CACHING_COMPLETE = 13;
    private static final int WHAT_MIC_NOTIFY = 14;
    private static final int INVITE_TYPE_AUDIO = 1;
    private static final int INVITE_TYPE_VIDEO = 2;
    private static final int INVITE_TYPE_MUTI = 3;
    private static final int STATUS_ON_VIDEO = 1001;
    private int micStatus;
    private static final int MIC_NOTIFY_OPEN = 1;
    private static final int MIC_NOTIFY_CLOSE = 2;
    private static final int MIC_NOTIFY_FAILED = 3;
    public static final int LIVE_STATUS_NOT_START = 0;
    public static final int LIVE_STATUS_ING = 1;
    public static final int LIVE_STATUS_PAUSE = 2;
    public static final int LIVE_STATUS_END = 3;
    private int live_status;

    public WatchActivity() {
    }

    public ArrayList<FastIdc> getIdcList() {
        return this.idcList;
    }

    public Player getmPlayer() {
        return this.mPlayer;
    }

    private void setStatus(int status) {
        this.status.set(status);
    }

    public int getStatus() {
        return this.status.get();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(ResManager.getLayoutId("gs_activity_watch"));
        this.findViews();
        FragmentManager fm = this.getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        if(this.watchFragment == null) {
            this.watchFragment = new WatchFragment();
        }

        transaction.add(ResManager.getId("gs_main_content_ly"), this.watchFragment);

        try {
            transaction.commit();
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(WatchActivity.this.watchFragment != null) {
                    switch(msg.what) {
                        case 1:
                            WatchActivity.this.onError(((Integer)msg.obj).intValue());
                            break;
                        case 2:
                            WatchActivity.this.showReconnectView(false);
                            WatchActivity.this.onRoomJoin(((Integer)msg.obj).intValue());
                            break;
                        case 3:
                            WatchActivity.this.netStatus = 4;
                            WatchActivity.this.showReconnectText(WatchActivity.this.netStatus);
                            break;
                        case 4:
                            Bundle status1 = (Bundle)msg.obj;
                            boolean isOpen = status1.getBoolean("isOpen");
                            int type = status1.getInt("type");
                            if(!isOpen) {
                                if(type == 1) {
                                    if(WatchActivity.this.customDialog != null && WatchActivity.this.customDialog.isShowing()) {
                                        WatchActivity.this.customDialog.dismiss();
                                    }

                                    WatchActivity.this.micControl(false);
                                }
                            } else {
                                WatchActivity.this.acceptInvite(type);
                            }
                            break;
                        case 5:
                            WatchActivity.this.watchFragment.onVideoStart();
                            break;
                        case 6:
                            WatchActivity.this.watchFragment.onVideoEnd();
                            break;
                        case 7:
                            WatchActivity.this.watchFragment.updateTitle((String)msg.obj);
                            break;
                        case 8:
                            WatchActivity.this.live_status = ((Integer)msg.obj).intValue();
                            WatchActivity.this.watchFragment.receiveState(((Integer)msg.obj).intValue());
                        case 9:
                        case 13:
                        default:
                            break;
                        case 10:
                            WatchActivity.this.onRoomLeave(((Integer)msg.obj).intValue());
                            break;
                        case 11:
                            WatchActivity.this.watchFragment.adjustReceiverUI(((Float)msg.obj).floatValue());
                            break;
                        case 12:
                            if(((Boolean)msg.obj).booleanValue()) {
                                if(WatchActivity.this.live_status != 2) {
                                    WatchActivity.this.showLoadingView(true);
                                }
                            } else {
                                WatchActivity.this.showLoadingView(false);
                            }
                            break;
                        case 14:
                            int status = ((Integer)msg.obj).intValue();
                            if(status == 3) {
                                WatchActivity.this.micControl(WatchActivity.this.micStatus == 1);
                            } else {
                                WatchActivity.this.micStatus = status;
                                if(status == 1) {
                                    WatchActivity.this.watchFragment.audioOpenUI();
                                    WatchActivity.this.mPlayer.inviteAck(1, true, (OnTaskRet)null);
                                } else {
                                    WatchActivity.this.watchFragment.audioCloseUI();
                                    WatchActivity.this.mPlayer.inviteAck(1, false, (OnTaskRet)null);
                                }
                            }
                    }

                }
            }
        };
        this.joinCheckNetwork();
    }

    private void sendMsg(int what, Object obj) {
        if(obj == null) {
            this.mHandler.sendEmptyMessage(what);
        } else {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(what, obj));
        }
    }

    private void findViews() {
        this.linLoadView = this.findViewById(ResManager.getId("gs_linLoadView"));
        this.linloadPb = this.findViewById(ResManager.getId("gs_linLoadPro"));
        this.linLoadNetDisconnected = this.findViewById(ResManager.getId("gs_linLoadNetDisconnected"));
        this.relExit = this.findViewById(ResManager.getId("gs_exit_rel"));
        this.lyLoadText = this.findViewById(ResManager.getId("gs_lyLoadText"));
        this.loadText = (TextView)this.findViewById(ResManager.getId("gs_loadText"));
    }

    private void join() {
        this.mPlayer = new Player();
        this.chatImpl = new PlayerChatImpl(this);
        this.mPlayer.setOnChatListener((PlayerChatImpl)this.chatImpl);
        ((PlayerChatImpl)this.chatImpl).setModuleHandle(this.mPlayer);
        this.linLoadView.setVisibility(View.VISIBLE);
        this.linloadPb.setVisibility(View.VISIBLE);
        this.linLoadNetDisconnected.setVisibility(View.GONE);
        if(this.getStatus() == 1) {
            this.mPlayer.join(this.getApplicationContext(), RTLive.getIns().getInitParam(), this);
        }

    }

    protected void joinCheckNetwork() {
        if(this.getStatus() == 1) {
            int netClassType = ConnectionReceiver.getNetType(this);
            this.netStatus = netClassType;
            if(netClassType == 4) {
                this.join();
            } else if(netClassType == 5) {
                this.showErrDialog(this.getString(ResManager.getStringId("gs_net_no_network")), this.getString(ResManager.getStringId("gs_i_known")));
            } else {
                this.showDialogByNet(this.getNetTip(netClassType), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        WatchActivity.this.join();
                    }
                });
            }

        }
    }

    public void onSwitchMobile(int netClass) {
        GenseeLog.d(this.TAG, "onSwitchMobile netStatus:" + this.netStatus);
        if(netClass != this.netStatus) {
            this.netStatus = netClass;
            if(this.netStatus != 4) {
                if(this.netStatus == 5) {
                    this.showReconnectText(this.netStatus);
                    return;
                }

                if(RTLive.getIns().isLiveStart()) {
                    this.showReconnectText(this.netStatus);
                }

                this.showDialogByNet(this.getNetTip(netClass), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
            } else {
                this.showReconnectView(false);
            }
        }

    }

    protected void showLoadingView(boolean isShow) {
        this.linLoadView.setVisibility(isShow?View.VISIBLE:View.GONE);
    }

    protected void showReconnectView(boolean isShow) {
        this.lyLoadText.setVisibility(isShow?View.VISIBLE:View.GONE);
    }

    protected void showReconnectText(int netStatus) {
        this.showReconnectView(true);
        if(netStatus == 5) {
            this.loadText.setText(this.getString(ResManager.getStringId("gs_net_have_disconnect")));
        } else if(netStatus == 4) {
            this.loadText.setText(this.getString(ResManager.getStringId("gs_net_connecting")));
        } else if(netStatus == 1001) {
            this.loadText.setText(ResManager.getStringId("gs_no_video"));
        } else {
            this.showReconnectView(false);
        }

    }

    private void showDialogByNet(String msg, OnClickListener l) {
        this.showDialog((String)null, msg, this.getString(ResManager.getStringId("gs_exit")), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                WatchActivity.this.finish();
            }
        }, this.getString(ResManager.getStringId("gs_continues")), l, (OnCancelListener)null);
    }

    private String getNetTip(int netClassType) {
        if(RTLive.getIns().isHost()) {
            return this.getString(ResManager.getStringId("gs_net_x_g_host"));
        } else {
            switch(netClassType) {
                case 1:
                    return this.getString(ResManager.getStringId("gs_net_2g"));
                case 2:
                    return this.getString(ResManager.getStringId("gs_net_3g"));
                case 3:
                    return this.getString(ResManager.getStringId("gs_net_4g"));
                case 4:
                default:
                    return this.getString(ResManager.getStringId("gs_net_2g"));
                case 5:
                    return this.getString(ResManager.getStringId("gs_net_disconnect"));
            }
        }
    }

    public void onBackPressed() {
        if(!this.bJoinSuccess) {
            super.onBackPressed();
        } else {
            String title = this.getString(ResManager.getStringId("gs_exit_webcast"));
            this.showDialog("", title, this.getString(ResManager.getStringId("gs_cancel")), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }, this.getString(ResManager.getStringId("gs_exit")), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    WatchActivity.this.exit();
                    WatchActivity.this.relExit.setVisibility(View.VISIBLE);
                }
            }, (OnCancelListener)null);
        }

    }

    protected void onDestroy() {
        this.unRegisterReceiver();
        this.stopLogService();
        this.releasePlayer();
        super.onDestroy();
    }

    private void releasePlayer() {
        if(this.mPlayer != null && this.bJoinSuccess) {
            this.mPlayer.leave();
            this.mPlayer.release(this);
            this.bJoinSuccess = false;
        }

    }

    public void onJoin(int result) {
        this.sendMsg(2, Integer.valueOf(result));
        GenseeLog.e("******", "房间加入" + result);
    }

    public void onUserJoin(UserInfo info) {
    }

    public void onUserLeave(UserInfo info) {
    }

    public void onUserUpdate(UserInfo info) {
    }

    public void onRosterTotal(int total) {
    }

    public void onReconnecting() {
        GenseeLog.e("*****", "短线重连");
        this.sendMsg(3, (Object)null);
    }

    public void onLeave(int reason) {
        GenseeLog.e("*****", "离开房间：" + reason);
        this.setStatus(5);
        this.sendMsg(10, Integer.valueOf(reason));
    }

    public void onCaching(boolean isCaching) {
        GenseeLog.e("******", "缓冲：" + isCaching);
        this.sendMsg(12, Boolean.valueOf(isCaching));
    }

    public void onErr(int errCode) {
        GenseeLog.e("******", "房间加入错误" + errCode);
        this.sendMsg(1, Integer.valueOf(errCode));
    }

    public void onDocSwitch(int docType, String docName) {
    }

    public void onIdcList(List<PingEntity> idcs) {
        if(idcs != null && idcs.size() > 0) {
            this.idcList = new ArrayList();
            Iterator var3 = idcs.iterator();

            while(var3.hasNext()) {
                PingEntity pingEntity = (PingEntity)var3.next();
                FastIdc fastIdc = new FastIdc();
                fastIdc.name = pingEntity.getDescription();
                fastIdc.id = pingEntity.getIdcId();
                this.idcList.add(fastIdc);
            }
        }

    }

    public void onVideoBegin() {
        GenseeLog.e("******", "视频开始");
        this.sendMsg(5, (Object)null);
    }

    public void onVideoEnd() {
        GenseeLog.e("******", "视频结束");
        this.sendMsg(6, (Object)null);
    }

    public void onVideoSize(int width, int height, boolean isAs) {
        float rate = (float)width / (float)height;
        GenseeLog.e("******", "视频SIZE:" + rate);
        this.sendMsg(11, Float.valueOf(rate));
    }

    public void onAudioLevel(int level) {
    }

    public void onPublish(boolean isPlaying) {
        GenseeLog.e("*****", "状态:" + isPlaying);
        this.sendMsg(8, Integer.valueOf(isPlaying?1:2));
    }

    public void onSubject(String subject) {
        GenseeLog.e("******", "标题" + subject);
        this.sendMsg(7, subject);
    }

    public void onPageSize(int pos, int width, int height) {
    }

    public void onPublicMsg(long userId, String msg) {
        if(this.chatImpl != null) {
            ((PlayerChatImpl)this.chatImpl).onSysMsg(msg);
        }

    }

    public void onLiveText(String language, String text) {
    }

    public void onRollcall(int timeOut) {
    }

    public void onLottery(int cmd, String lotteryInfo) {
    }

    public void onFileShare(int cmd, String fileName, String fileUrl) {
    }

    public void onFileShareDl(int ret, String fileUrl, String filePath) {
    }

    public void onInvite(int type, boolean isOpen) {
        GenseeLog.e("******", "语音邀请" + type + isOpen);
        Bundle bundle = new Bundle();
        bundle.putBoolean("isOpen", isOpen);
        bundle.putInt("type", type);
        if(type == 1) {
            this.sendMsg(4, bundle);
        } else {
            this.mPlayer.inviteAck(type, false, new OnTaskRet() {
                public void onTaskRet(boolean b, int i, String s) {
                }
            });
        }

    }

    public void onMicNotify(int notify) {
        this.sendMsg(14, Integer.valueOf(notify));
        GenseeLog.e("******", "MIC状态:" + notify);
    }

    public void onScreenStatus(boolean isAs) {
    }

    public void onModuleFocus(int mode) {
    }

    private void acceptInvite(int type) {
        this.showDialog("", this.getString(ResManager.getStringId("gs_reminder_open_mic")), this.getString(ResManager.getStringId("gs_accept_mic_refuse")), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                WatchActivity.this.micControl(false);
            }
        }, this.getString(ResManager.getStringId("gs_accept_mic_open")), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                WatchActivity.this.micControl(true);
            }
        }, (OnCancelListener)null);
    }

    private void onRoomJoin(int result) {
        int resId = 0;
        this.bJoinSuccess = false;
        switch(result) {
            case 6:
            case 10:
            case 13:
            case 14:
            case 15:
            case 16:
            default:
                break;
            case 7:
                resId = 1;
                break;
            case 8:
                resId = ResManager.getStringId("gs_net_disconnect");
                break;
            case 9:
                resId = ResManager.getStringId("gs_join_webcast_timeout");
                break;
            case 11:
                this.sendMsg(8, Integer.valueOf(0));
                resId = ResManager.getStringId("gs_join_webcast_err_too_early");
                break;
            case 12:
                resId = ResManager.getStringId("gs_join_webcast_err_license");
                break;
            case 17:
                resId = ResManager.getStringId("gs_join_webcast_err_ip");
        }

        if(resId == 0) {
            this.bJoinSuccess = true;
            this.showLoadingView(false);
            this.watchFragment.onJoinSuccess();
            this.registerAppReceiver();
        } else if(resId != 1) {
            this.showErrDialog(this.getString(resId), this.getString(ResManager.getStringId("gs_i_known")));
        }

    }

    public void onError(final int errorCode) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                String errStr = "";
                if(errorCode == -108) {
                    errStr = WatchActivity.this.getString(ResManager.getStringId("gs_join_webcast_err_third_auth"));
                } else {
                    errStr = WatchActivity.this.getErrStr(errorCode);
                }

                WatchActivity.this.showErrDialog(errStr, WatchActivity.this.getString(ResManager.getStringId("gs_i_known")));
            }
        });
    }

    private void onRoomLeave(int result) {
        int resId = 0;
        if(result == 1) {
            this.relExit.setVisibility(View.GONE);
            this.exit();
        } else {
            String sTip = this.getString(ResManager.getStringId("gs_i_known"));
            if(result == 2) {
                resId = ResManager.getStringId("gs_leave_err_eject_tip");
            } else if(result == 4) {
                this.sendMsg(8, Integer.valueOf(3));
                resId = ResManager.getStringId("gs_leave_err_close_tip");
            } else if(result != 14 && result != 5) {
                return;
            }

            this.unRegisterReceiver();
            this.showErrDialog(this.getString(resId), sTip);
        }
    }

    protected void exit() {
        this.finish();
    }

    public void setPlay(GSVideoView gsVideoView) {
        if(this.mPlayer != null) {
            this.mPlayer.setGSVideoView(gsVideoView);
        }

    }

    public void closeMic(boolean isOpen) {
        this.micControl(isOpen);
    }

    public void micControl(boolean isOpen) {
        if(this.mPlayer != null) {
            this.mPlayer.openMic(this, isOpen, new OnTaskRet() {
                public void onTaskRet(boolean b, int i, String s) {
                }
            });
        }

    }

    protected void onPause() {
        if(this.mPlayer != null && this.bJoinSuccess) {
            this.mPlayer.audioSet(true);
        }

        super.onPause();
    }

    protected void onResume() {
        if(this.mPlayer != null && this.bJoinSuccess) {
            this.mPlayer.audioSet(false);
        }

        super.onResume();
    }
}
