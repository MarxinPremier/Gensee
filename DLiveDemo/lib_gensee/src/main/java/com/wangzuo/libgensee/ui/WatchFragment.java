package com.wangzuo.libgensee.ui;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;
import com.wangzuo.libgensee.ui.BaseActivity;
import com.wangzuo.libgensee.ui.BaseFragment;
import com.wangzuo.libgensee.ui.WatchActivity;
import com.wangzuo.libgensee.ui.holder.IdcHolder;
import com.wangzuo.libgensee.ui.holder.chat.SimpleChatHolder;
import com.wangzuo.libgensee.ui.view.CustomInputDialog;
import com.wangzuo.libgensee.ui.view.CustomInputDialog.Builder;
import com.wangzuo.libgensee.util.GenseeUtils;
import com.wangzuo.libgensee.util.ResManager;
import com.gensee.utils.GenseeLog;
import com.gensee.view.GSVideoView;
import com.gensee.view.GSVideoView.RenderMode;

public class WatchFragment extends BaseFragment implements OnClickListener {
    private GSVideoView mVideoView;
    private View defView;
    private RelativeLayout videoLayout;
    private ImageView ivChat;
    private ImageView ivExit;
    private ImageView ivNet;
    private ImageView ivBugReport;
    private RelativeLayout rlControl;
    private RelativeLayout rlChat;
    private TextView tvTitle;
    private TextView tvTopState;
    private IdcHolder idcHolder;
    private ImageView iv_audioView;
    private TextView tvAudio;
    private RelativeLayout rlAudio;
    private RelativeLayout rlAudioContainer;
    private boolean isPlaying;
    private Handler handler;

    public WatchFragment() {
    }

    protected void updateTitle(String title) {
        if(!TextUtils.isEmpty(title) && this.tvTitle != null) {
            title = GenseeUtils.formatText(title, 10);
            this.tvTitle.setText(title);
        }

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(ResManager.getLayoutId("gs_fragment_watch"), container, false);
        this.defView = view.findViewById(ResManager.getId("gs_relDef"));
        this.mVideoView = (GSVideoView)view.findViewById(ResManager.getId("gs_videoView"));
        this.mVideoView.setRenderMode(RenderMode.RM_FILL_CENTER_CROP);
        ((WatchInterface)this.getActivity()).setPlay(this.mVideoView);
        this.videoLayout = (RelativeLayout)view.findViewById(ResManager.getId("gs_rlVideoLayout"));
        this.rlChat = (RelativeLayout)view.findViewById(ResManager.getId("gs_rl_chat"));
        this.ivChat = (ImageView)view.findViewById(ResManager.getId("gs_iv_chat"));
        this.ivBugReport = (ImageView)view.findViewById(ResManager.getId("gs_iv_bugReport"));
        this.ivNet = (ImageView)view.findViewById(ResManager.getId("gs_iv_net"));
        this.ivExit = (ImageView)view.findViewById(ResManager.getId("gs_gs_iv_exit"));
        this.rlControl = (RelativeLayout)view.findViewById(ResManager.getId("gs_rl_control"));
        this.tvTitle = (TextView)view.findViewById(ResManager.getId("gs_tv_title"));
        this.tvTopState = (TextView)view.findViewById(ResManager.getId("gs_tv_topState"));
        this.iv_audioView = (ImageView)view.findViewById(ResManager.getId("gs_iv_audioView"));
        this.tvAudio = (TextView)view.findViewById(ResManager.getId("gs_tv_audio"));
        this.rlAudio = (RelativeLayout)view.findViewById(ResManager.getId("gs_rl_audio"));
        this.rlAudioContainer = (RelativeLayout)view.findViewById(ResManager.getId("gs_rl_audio_container"));
        this.setDefView();
        this.idcHolder = new IdcHolder(view.findViewById(ResManager.getId("gs_ly_idc")), (Object)null);
        this.ivExit.setOnClickListener(this);
        this.ivChat.setOnClickListener(this);
        this.ivBugReport.setOnClickListener(this);
        this.ivNet.setOnClickListener(this);
        this.rlAudio.setOnClickListener(this);
        this.rootView = view;
        this.mChatHolder = new SimpleChatHolder(this.rootView, ((BaseActivity)this.getActivity()).getChatImpl());
        ((BaseActivity)this.getActivity()).setSimpleChatHolder(this.mChatHolder);
        return view;
    }

    protected void onJoinSuccess() {
        GenseeLog.e("*****", "WatchFragment-join-success");
        this.showControlView(true);
    }

    public void onVideoStart() {
        this.mVideoView.renderDefault();
        this.showDefView(false);
        this.showAudioView(false);
        this.showVideoView(true);
        this.iv_audioView.setVisibility(View.INVISIBLE);
        this.isPlaying = true;
    }

    public void onVideoEnd() {
        this.showAudioView(true);
        this.isPlaying = false;
    }

    public void showVideoView(boolean isShow) {
        this.videoLayout.setVisibility(isShow?View.VISIBLE:View.INVISIBLE);
        this.mVideoView.setVisibility(isShow?View.VISIBLE:View.GONE);
    }

    private void setDefView() {
        LayoutParams lp = (LayoutParams)this.defView.getLayoutParams();
        lp.height = this.getHeight(1.3333334F);
        this.defView.setLayoutParams(lp);
    }

    public void showDefView(boolean isShow) {
        if(this.defView != null) {
            this.defView.setVisibility(isShow?View.VISIBLE:View.INVISIBLE);
        }

    }

    public void showAudioView(boolean isShow) {
        if(this.iv_audioView != null) {
            this.iv_audioView.setVisibility(isShow?View.VISIBLE:View.GONE);
        }

        if(isShow) {
            LayoutParams lp2 = (LayoutParams)this.iv_audioView.getLayoutParams();
            lp2.height = this.getHeight(1.3333334F);
            this.iv_audioView.setLayoutParams(lp2);
            this.mVideoView.setVisibility(View.GONE);
        }
    }

    public void showControlView(boolean isShow) {
        if(this.rlControl != null) {
            this.rlControl.setVisibility(isShow?View.VISIBLE:View.GONE);
        }

    }

    public void adjustReceiverUI(float aspectRatio) {
        GenseeLog.i(this.TAG, "adjustReceiverUI aspectRatio:" + aspectRatio);
        LayoutParams lp = (LayoutParams)this.videoLayout.getLayoutParams();
        LayoutParams lp3 = (LayoutParams)this.mVideoView.getLayoutParams();
        if(1.0F < aspectRatio) {
            lp.height = this.getHeight(1.3333334F);
            if(aspectRatio >= 1.3333334F) {
                lp3.height = this.getHeight(aspectRatio);
            } else {
                lp3.height = -1;
            }

            lp.addRule(3, ResManager.getId("gs_tv_title"));
        } else {
            lp.height = -1;
            lp.addRule(3, 0);
            lp3.height = -1;
        }

        this.videoLayout.setLayoutParams(lp);
        this.mVideoView.setLayoutParams(lp3);
    }

    private int getHeight(float rate) {
        DisplayMetrics dm = new DisplayMetrics();
        this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        return (int)((float)screenWidth / rate);
    }

    public void onClick(View v) {
        if(v.getId() == ResManager.getId("gs_iv_chat")) {
            Builder builder = new Builder(this.getActivity());
            CustomInputDialog dialog = builder.create();
            dialog.show();
        } else if(v.getId() == ResManager.getId("gs_gs_iv_exit")) {
            this.getActivity().onBackPressed();
        } else if(v.getId() == ResManager.getId("gs_iv_net")) {
            this.idcHolder.selectIdc(((WatchActivity)this.getActivity()).getIdcList());
        } else if(v.getId() == ResManager.getId("gs_iv_bugReport")) {
            GenseeUtils.sendLog(this.getActivity(), false);
        } else if(v.getId() == ResManager.getId("gs_rl_audio")) {
            this.rlAudio.setVisibility(View.GONE);
            ((WatchInterface)this.getActivity()).closeMic(false);
        }

    }

    public void receiveState(int state) {
        if(this.tvTopState != null) {
            this.tvTopState.setVisibility(View.VISIBLE);
            if(state == 0) {
                this.tvTopState.setText(ResManager.getStringId("gs_cast_status_notstart"));
            } else if(state == 1) {
                this.showDefView(false);
                this.videoLayout.setVisibility(View.VISIBLE);
                if(!this.isPlaying) {
                    this.showAudioView(true);
                }

                this.tvTopState.setText(ResManager.getStringId("gs_cast_status_ing"));
            } else if(state == 2) {
                this.tvTopState.setText(ResManager.getStringId("gs_cast_status_pause"));
                this.videoLayout.setVisibility(View.INVISIBLE);
                this.showDefView(true);
                this.showAudioView(false);
            } else if(state == 3) {
                this.tvTopState.setText(ResManager.getStringId("gs_cast_status_end"));
            }

        }
    }

    public void onPause() {
        super.onPause();
    }

    public void audioOpenUI() {
        this.rlAudio.setEnabled(true);
        this.rlAudio.setVisibility(View.VISIBLE);
        this.tvAudio.setText(ResManager.getStringId("gs_mic_hang_up"));
    }

    public void audioCloseUI() {
        this.rlAudio.setVisibility(View.GONE);
    }

    public interface WatchInterface {
        void setPlay(GSVideoView var1);

        void closeMic(boolean var1);
    }
}
