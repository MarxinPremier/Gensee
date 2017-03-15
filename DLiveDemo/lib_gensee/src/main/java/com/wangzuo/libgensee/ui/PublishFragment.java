package com.wangzuo.libgensee.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wangzuo.libgensee.core.RTLive;
import com.wangzuo.libgensee.core.RTLive.OnSelfVideoReadyListener;
import com.wangzuo.libgensee.core.RTLive.OnUserCountChangeListener;
import com.wangzuo.libgensee.entity.chat.AbsChatMessage;
import com.wangzuo.libgensee.ui.holder.IdcHolder;
import com.wangzuo.libgensee.ui.holder.IdcHolder.FastIdc;
import com.wangzuo.libgensee.ui.holder.UserOperateHolder;
import com.wangzuo.libgensee.ui.holder.chat.SimpleChatHolder;
import com.wangzuo.libgensee.ui.view.CustomInputDialog;
import com.wangzuo.libgensee.ui.view.CustomInputDialog.Builder;
import com.wangzuo.libgensee.ui.view.xlistview.XListView;
import com.wangzuo.libgensee.util.FocusManager;
import com.wangzuo.libgensee.util.GenseeToast;
import com.wangzuo.libgensee.util.GenseeUtils;
import com.wangzuo.libgensee.util.PreferUtil;
import com.wangzuo.libgensee.util.ResManager;
import com.gensee.room.RtSdk;
import com.gensee.routine.IDCInfo;
import com.gensee.routine.State;
import com.gensee.routine.UserInfo;
import com.gensee.taskret.OnTaskRet;
import com.gensee.user.UserManager;
import com.gensee.utils.GenseeLog;
import com.gensee.view.ILocalVideoView;
import com.gensee.view.ILocalVideoView.OnCameraInfoListener;
import com.gensee.view.ILocalVideoView.OnCameraPermissionListener;
import com.gensee.view.beauty.GSLocalVideoView;
import java.util.ArrayList;
import java.util.List;

public class PublishFragment extends BaseFragment
        implements View.OnClickListener, RTLive.OnUserCountChangeListener, ILocalVideoView.OnCameraPermissionListener, RTLive.OnSelfVideoReadyListener
{
    private Button btn_start_live;
    private GSLocalVideoView localVideoView;
    private Runnable showTimeRunnable;
    private Handler mHandler;
    private TextView tv_room_title;
    private TextView tv_room_users;
    private TextView live_started_time;
    private View iv_exit;
    private View iv_beauty;
    private View iv_switch;
    private View iv_mic;
    private View iv_chat;
    private View iv_more;
    private View iv_change_line;
    private View iv_report_bug;
    private View iv_less;
    private View ly_btns_first_page;
    private View ly_btns_second_page;
    private IdcHolder idcHolder;
    private boolean isFirstOpen = true;
    private UserOperateHolder userOperateHolder;
    private RelativeLayout localvideoview_rl;
    private FocusManager mFocusManager;
    private TextView tv_counter_down;
    private TextView recordView;
    private Animation anim;
    private int count = 3;
    private boolean isHaveStartOnce;
    private CountDownTimer backgroudCount;
    private boolean isAppBackgroud;
    private View gs_ly_counter_down;
    private View gs_ly_have_an_as_on;
    private View gs_tv_stop_as;
    private boolean isMicBtnOnClicked;
    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg) {
            if (PublishFragment.this.count >= 1) {
                PublishFragment.this.gs_ly_counter_down.setVisibility(View.VISIBLE);
                PublishFragment.this.tv_counter_down.setText(PublishFragment.this.count);
                sendEmptyMessageDelayed(0, 1000L);
                PublishFragment.this.small();
                PublishFragment.this.count -= 1;
            } else {
                PublishFragment.this.count = 3;
                PublishFragment.this.gs_ly_counter_down.setVisibility(View.GONE);
                RTLive.getIns().getRtSdk().asEnd();
            }
        }
    };

    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        this.mChatHolder = new SimpleChatHolder(this.rootView, ((PublishActivity)getActivity()).getChatImpl());
        ((PublishActivity)getActivity()).setSimpleChatHolder(this.mChatHolder);
        this.mChatHolder.getLvChat().setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AbsChatMessage chatMessage = (AbsChatMessage)PublishFragment.this.mChatHolder.getLvChat().getAdapter().getItem(position);
                if (chatMessage == null) {
                    return;
                }
                UserInfo sendUserInfo = UserManager.getIns().getUserByUserId(chatMessage.getSendUserId());
                if ((sendUserInfo == null) ||
                        (sendUserInfo.getId() == RTLive.getIns().getSelf().getId())) {
                    return;
                }
                PublishFragment.this.userOperateHolder.selectUser(sendUserInfo);
            }
        });
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        this.mHandler = ((PublishActivity)getActivity()).getHandler();
        View view = inflater.inflate(ResManager.getLayoutId("gs_fragment_publish"), container, false);
        this.rootView = view;
        this.btn_start_live = ((Button)view.findViewById(ResManager.getId("gs_btn_start_live")));
        this.btn_start_live.setOnClickListener(this);
        RTLive.getIns().setOnSelfVideoReadyListener(this);
        this.anim = AnimationUtils.loadAnimation(getActivity(), ResManager.getAnimId("gs_anim_counter_down"));

        this.localVideoView = ((GSLocalVideoView)view.findViewById(ResManager.getId("gs_localvideoview")));
        this.localVideoView.setHardEncode(false);
        this.localVideoView.setOrientation(11);
        this.mFocusManager = new FocusManager();
        this.localVideoView.setOnCameraInfoListener(new MyCameraInfoListener());
        this.localVideoView.setOnCameraPermissionListener(this);
        this.localvideoview_rl = ((RelativeLayout)view.findViewById(ResManager.getId("gs_localvideoview_rl")));
        this.localvideoview_rl.setOnTouchListener(new View.OnTouchListener()
        {
            public boolean onTouch(View view, MotionEvent event) {
                if (!RTLive.getIns().isVideoCameraOpen()) {
                    return false;
                }
                PublishFragment.this.mFocusManager.setCamera(PublishFragment.this.localVideoView.getCamera());
                PublishFragment.this.mFocusManager.initialize(PublishFragment.this.localVideoView, PublishFragment.this.getActivity());
                return PublishFragment.this.mFocusManager.onTouch(event);
            }
        });
        RTLive.getIns().addOnUserCountChangeListener(this);

        this.tv_room_title = ((TextView)view.findViewById(ResManager.getId("gs_tv_room_title")));
        this.tv_room_users = ((TextView)view.findViewById(ResManager.getId("gs_tv_room_users")));
        this.live_started_time = ((TextView)view.findViewById(ResManager.getId("gs_live_started_time")));
        this.iv_exit = view.findViewById(ResManager.getId("gs_gs_iv_exit"));
        this.iv_exit.setOnClickListener(this);
        showTime(true);

        this.iv_beauty = view.findViewById(ResManager.getId("gs_iv_beauty"));
        this.iv_beauty.setOnClickListener(this);
        this.iv_switch = view.findViewById(ResManager.getId("gs_iv_switch"));
        if (Camera.getNumberOfCameras() > 1) {
            this.iv_switch.setVisibility(View.VISIBLE);
            this.iv_switch.setOnClickListener(this);
        }
        this.iv_mic = view.findViewById(ResManager.getId("gs_iv_mic"));
        this.iv_mic.setOnClickListener(this);
        this.iv_chat = view.findViewById(ResManager.getId("gs_iv_chat"));
        this.iv_chat.setOnClickListener(this);
        this.iv_more = view.findViewById(ResManager.getId("gs_iv_more"));
        this.iv_more.setOnClickListener(this);
        this.ly_btns_first_page = view.findViewById(ResManager.getId("gs_ly_btns_first_page"));
        this.ly_btns_second_page = view.findViewById(ResManager.getId("gs_ly_btns_second_page"));
        this.iv_change_line = view.findViewById(ResManager.getId("gs_iv_change_line"));
        this.iv_change_line.setOnClickListener(this);
        this.iv_report_bug = view.findViewById(ResManager.getId("gs_iv_report_bug"));
        this.iv_report_bug.setOnClickListener(this);
        this.iv_less = view.findViewById(ResManager.getId("gs_iv_less"));
        this.iv_less.setOnClickListener(this);

        this.idcHolder = new IdcHolder(view.findViewById(ResManager.getId("gs_ly_idc")), null);

        this.userOperateHolder = new UserOperateHolder(view.findViewById(ResManager.getId("gs_ly_user_operate")), null);
        this.gs_ly_counter_down = view.findViewById(ResManager.getId("gs_ly_counter_down"));
        this.gs_ly_have_an_as_on = view.findViewById(ResManager.getId("gs_ly_have_an_as_on"));
        this.gs_tv_stop_as = view.findViewById(ResManager.getId("gs_tv_stop_as"));
        this.gs_tv_stop_as.setOnClickListener(this);
        this.tv_counter_down = ((TextView)view.findViewById(ResManager.getId("gs_tv_counter_down")));
        this.recordView = ((TextView) view.findViewById(ResManager.getId("gs_public_record_view")));
        this.recordView.setOnClickListener(this);
        return view;
    }

    public void onClick(View v)
    {
        if (v.getId() == ResManager.getId("gs_btn_start_live")) {
            this.isHaveStartOnce = true;
            startLive();
            this.btn_start_live.setVisibility(View.GONE);
        } else if (v.getId() == ResManager.getId("gs_gs_iv_exit")) {
            getActivity().onBackPressed();
        } else if (v.getId() == ResManager.getId("gs_iv_beauty")) {
            this.iv_beauty.setSelected(!this.iv_beauty.isSelected());
            this.localVideoView.switchBeauty(!this.iv_beauty.isSelected());
        } else if (v.getId() == ResManager.getId("gs_iv_switch")) {
            this.mFocusManager.removeResetFocusMessage();
            this.localVideoView.doCameraSwitch();
        }
        else if (v.getId() == ResManager.getId("gs_iv_mic")) {
            this.isMicBtnOnClicked = true;
            if (v.isSelected())
                RTLive.getIns().audioOpenMic();
            else {
                RTLive.getIns().audioCloseMic();
            }
        }
        else if (v.getId() == ResManager.getId("gs_iv_chat")) {
            CustomInputDialog.Builder builder = new CustomInputDialog.Builder(getActivity());
            CustomInputDialog dialog = builder.create();
            dialog.show();
        } else if (v.getId() == ResManager.getId("gs_iv_more")) {
            switchBtnsMore(true);
        } else if (v.getId() == ResManager.getId("gs_iv_change_line")) {
            IDCInfo[] originIDCs = RTLive.getIns().roomIDCGetList();
            List idcList = new ArrayList();
            if (originIDCs != null) {
                for (int i = 0; i < originIDCs.length; i++) {
                    IdcHolder.FastIdc fastIdc = new IdcHolder.FastIdc();
                    fastIdc.name = originIDCs[i].getName();
                    fastIdc.id = originIDCs[i].getId();
                    idcList.add(fastIdc);
                }
            }
            this.idcHolder.selectIdc(idcList);
        } else if (v.getId() == ResManager.getId("gs_iv_report_bug")) {
            GenseeUtils.sendLog(getActivity(), false);
        } else if (v.getId() == ResManager.getId("gs_iv_less")) {
            switchBtnsMore(false);
        } else if (v.getId() == ResManager.getId("gs_tv_stop_as")) {
            showAsDialog();
        }else if (v.getId() == ResManager.getId("gs_public_record_view")){
            Toast.makeText(PublishFragment.this.getActivity(),"录制中",Toast.LENGTH_LONG).show();
        }
    }

    public void onAudioMicOpen() {
        if (this.isMicBtnOnClicked) {
            this.isMicBtnOnClicked = false;
            GenseeToast.showToast(getActivity(),
                    getString(ResManager.getStringId("gs_audio_opened")), true,
                    ResManager.getDrawableId("gs_warming_bg"), 0);
            this.iv_mic.setSelected(false);
        }
    }

    public void onAudioMicClose() {
        if (this.isMicBtnOnClicked) {
            this.isMicBtnOnClicked = false;
            GenseeToast.showToast(getActivity(),
                    getString(ResManager.getStringId("gs_audio_closed")), true,
                    ResManager.getDrawableId("gs_warming_bg"), 0);

            this.iv_mic.setSelected(true);
        }
    }

    public void switchBtnsMore(boolean flag) {
        this.ly_btns_first_page.setVisibility(flag ? View.INVISIBLE: View.VISIBLE);
        this.ly_btns_second_page.setVisibility(flag ? View.VISIBLE: View.INVISIBLE);
    }

    public void startLive() {
        GenseeLog.i(this.TAG, "startLive!");
        UserInfo self = RTLive.getIns().getSelf();
        if ((self != null) && (self.IsHost())) {
            RTLive.getIns().stopLod();
            RTLive.getIns().getRtSdk().videoActive(self.getId(), true, null);
            RTLive.getIns().roomPublish(State.S_RUNNING.getValue());
            RTLive.getIns().roomRecord(State.S_RUNNING.getValue());
            sendHostJoinBroadcastMsg();
        }
    }

    public void resumeLive() {
        UserInfo self = RTLive.getIns().getSelf();
        if ((self != null) && (self.IsHost())) {
            RTLive.getIns().roomPublish(State.S_RUNNING.getValue());
            RTLive.getIns().roomRecord(State.S_RUNNING.getValue());
        }
    }

    protected void onRoomJoinSuccess()
    {
        GenseeLog.i(this.TAG, "onRoomJoinSuccess isLiveStart?" + RTLive.getIns().isLiveStart());
        if (RTLive.getIns().isLiveStart()) {
            sendHostJoinBroadcastMsg();
        }
        RTLive.getIns().setLocalTextureVideoView(this.localVideoView);
        RTLive.getIns().videoOpenCamera();
        boolean bMicOpen = PreferUtil.getIns().getInt("MIC_STATUS") != 0;
        if (bMicOpen)
            RTLive.getIns().audioOpenMic();
    }

    private void sendHostJoinBroadcastMsg()
    {
        UserInfo self = RTLive.getIns().getSelf();
        if (self != null)
            RTLive.getIns().getRtSdk().roomNotifyBroadcastMsg(
                    GenseeUtils.formatText(self.getName(), 12) +
                            getActivity().getString(ResManager.getStringId("gs_chat_host_join")), true, null);
    }

    public void onRoomPublish(Object obj)
    {
        if (((Byte)obj).byteValue() != 0)
            this.btn_start_live.setVisibility(View.GONE);
    }

    private void startCountDownAnim()
    {
        this.handler.sendEmptyMessageDelayed(0, 300L);
    }

    private void small()
    {
        this.anim.reset();
        this.anim.setFillAfter(true);
        this.tv_counter_down.startAnimation(this.anim);
    }

    protected void updateTitle(String title)
    {
        if ((!TextUtils.isEmpty(title)) && (this.tv_room_title != null)) {
            title = GenseeUtils.formatText(title, 10);
            this.tv_room_title.setText(title);
        }
    }

    public void showTime(boolean isShow)
    {
        if (this.showTimeRunnable != null) {
            this.mHandler.removeCallbacks(this.showTimeRunnable);
        }
        if (!isShow) {
            return;
        }
        this.showTimeRunnable = new Runnable()
        {
            public void run() {
                RTLive.getIns().getRtSdk().roomPublishTime(new OnTaskRet()
                {
                    public void onTaskRet(boolean ret, final int id, String desc)
                    {
                        PublishFragment.this.mHandler.post(new Runnable() {
                            public void run() {
                                PublishFragment.this.live_started_time.setText(GenseeUtils.getTimeHHMMSS(id));
                            }
                        });
                    }
                });
                PublishFragment.this.mHandler.postDelayed(this, 1000L);
            }
        };
        this.mHandler.post(this.showTimeRunnable);
    }

    public void onUserCountChange(final String userCount)
    {
        if (this.tv_room_users != null)
            this.mHandler.post(new Runnable()
            {
                public void run() {
                    PublishFragment.this.tv_room_users.setText(userCount);
                }
            });
    }

    public void onAudioOpen()
    {
        if (!this.isFirstOpen)
            GenseeToast.showToast(getActivity(),
                    getString(ResManager.getStringId("gs_audio_opened")), true,
                    ResManager.getDrawableId("gs_warming_bg"), 0);
        else
            this.isFirstOpen = false;
    }

    public void onAudioClose()
    {
        GenseeToast.showToast(getActivity(),
                getString(ResManager.getStringId("gs_audio_closed")), true,
                ResManager.getDrawableId("gs_warming_bg"), 0);
    }

    public void onSelfVideoReady()
    {
        GenseeLog.i(this.TAG, "onSelfVideoReady isLiveStart()=" + RTLive.getIns().isLiveStart() + ",isHaveStartOnce=" + this.isHaveStartOnce);
        this.mHandler.post(new Runnable()
        {
            public void run() {
                if (RTLive.getIns().isLiveStart()) {
                    PublishFragment.this.btn_start_live.setVisibility(View.GONE);
                    RTLive.getIns().activeAndPublish();
                } else {
                    PublishFragment.this.btn_start_live.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    protected void onRoomReconnect()
    {
        this.btn_start_live.setVisibility(View.GONE);
    }

    public void onCameraPermission()
    {
        getActivity().runOnUiThread(new Runnable()
        {
            public void run() {
                if (!GenseeUtils.checkPackagePermission(PublishFragment.this.getActivity(), "android.permission.RECORD_VIDEO"))
                    ((PublishActivity)PublishFragment.this.getActivity()).showCancelErrMsg(PublishFragment.this.getString(ResManager.getStringId("gs_package_no_camera_perssmion")), PublishFragment.this.getString(ResManager.getStringId("gs_i_known")));
            }
        });
    }

    public void onPause()
    {
        if (this.mFocusManager != null) {
            this.mFocusManager.removeResetFocusMessage();
        }
        super.onPause();
    }

    public void onStop()
    {
        GenseeLog.i(this.TAG, "onStop");
        super.onStop();
        this.localvideoview_rl.removeAllViews();
        startBackgroudCount();
    }

    public void onResume()
    {
        GenseeLog.i(this.TAG, "onResume");
        super.onResume();
        stopBackgroudCount();
        if (this.localvideoview_rl.getChildCount() <= 0)
            this.localvideoview_rl.addView(this.localVideoView);
    }

    private void startBackgroudCount()
    {
        this.isAppBackgroud = true;
        if (this.backgroudCount != null) {
            this.backgroudCount.cancel();
        }
        this.backgroudCount = new CountDownTimer(300000L, 1000L) {
            public void onTick(long l) {
            }

            public void onFinish() {
                if ((PublishFragment.this.isAppBackgroud) && (PublishFragment.this.getActivity() != null))
                    ((PublishActivity)PublishFragment.this.getActivity()).release();
            }
        }
                .start();
    }

    private void stopBackgroudCount() {
        this.isAppBackgroud = false;
        if (this.backgroudCount != null) {
            this.backgroudCount.cancel();
            this.backgroudCount = null;
        }
    }

    public void onDestroy()
    {
        GenseeLog.i(this.TAG, "onDestroy");
        super.onDestroy();
        this.localVideoView.release();
        stopBackgroudCount();
    }

    public void showAsDialog() {
        ((BaseLiveActivity)getActivity()).showDialog("", getString(ResManager.getStringId("gs_as_other_begin")),
                getString(ResManager.getStringId("gs_cancel")), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which) {
                        PublishFragment.this.gs_ly_counter_down.setVisibility(View.GONE);
                        PublishFragment.this.gs_ly_have_an_as_on.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                }
                , getString(ResManager.getStringId("gs_end")), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        PublishFragment.this.gs_ly_counter_down.setVisibility(View.VISIBLE);
                        PublishFragment.this.gs_ly_have_an_as_on.setVisibility(View.GONE);
                        if (PublishFragment.this.anim == null)
                        {
                            PublishFragment.this.anim = AnimationUtils.loadAnimation(PublishFragment.this.getActivity(), ResManager.getAnimId("gs_anim_counter_down"));
                        }
                        PublishFragment.this.startCountDownAnim();
                    }
                }
                , null);
    }

    public void endAs()
    {
        if (this.gs_ly_have_an_as_on != null)
            this.gs_ly_have_an_as_on.setVisibility(View.GONE);
    }

    private class MyCameraInfoListener
            implements ILocalVideoView.OnCameraInfoListener
    {
        private MyCameraInfoListener()
        {
        }

        public void onCameraInfo(Camera arg0, Camera.CameraInfo arg1, int arg2)
        {
            if ((PublishFragment.this.mFocusManager != null) && (arg1 != null))
                PublishFragment.this.mFocusManager.setCameraInfo(arg1);
        }

        public void onPreviewSize(int w, int h)
        {
        }
    }
}
