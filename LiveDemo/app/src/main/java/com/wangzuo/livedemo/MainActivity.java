package com.wangzuo.livedemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.gensee.common.ServiceType;
import com.gensee.entity.InitParam;
import com.gensee.fastsdk.core.GSFastConfig;
import com.wangzuo.libgensee.GenseeLive;

public class MainActivity extends AppCompatActivity {

    private String joinPwd = "Z1489542595782";
    private boolean isPublishMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    /**
     * 主播参数
     * @param view
     */
    public void teacher(View view) {
        joinPwd = "Z1489542595782";
        isPublishMode = true;
    }

    /**
     * 观众参数
     * @param view
     */
    public void student(View view) {
        joinPwd = "033751";
        isPublishMode = false;
    }

    /**
     * 开启直播
     * @param view
     */
    public void start(View view) {
        String domain = "pxzz.gensee.com";
        String number = "26650783";
        String account = "";
        String pwd = "";
        String nickName = "pxzz";
        String k = "";
        ServiceType serviceType = ServiceType.WEBCAST;

        InitParam initParam = new InitParam();
        //若一个url为http://test.gensee.com/site/webcast   域名是“test.gensee.com”
        initParam.setDomain(domain);
        //设置对应编号，如果是点播则是点播编号，是直播便是直播编号。
        //请注意不要将id理解为编号。
        //作用等价于id，但不是id。有id可以不用编号，有编号可以不用id
        initParam.setNumber(number);
        //设置站点认证账号 即登录站点的账号
        initParam.setLoginAccount(account);
        //设置站点认证密码 即登录站点的密码,如果后台设置直播需要登录或点播需要登录，那么登录密码要正确  且帐号同时也必须填写正确
        initParam.setLoginPwd(pwd);
        //设置昵称  用于直播间显示或统计   一定要填写
        initParam.setNickName(nickName);
        //可选 如果后台设置了保护密码 请填写对应的口令
        initParam.setJoinPwd(joinPwd);
        //第三方认证K值，如果启用第三方集成的时候必须传入有效的K值
        initParam.setK(k);
        //若一个url为http://test.gensee.com/site/webcast ,serviceType是 ServiceType.WEBCAST,
        //url为http://test.gensee.com/site/training,serviceTypeserviceType是 ServiceType.TRAINING
        initParam.setServiceType(serviceType);

        GSFastConfig config = new GSFastConfig();
        //是否是主播端，false和默认观看端
        config.setPublish(isPublishMode);

        GenseeLive.startLive(MainActivity.this, config, initParam);
    }
}
