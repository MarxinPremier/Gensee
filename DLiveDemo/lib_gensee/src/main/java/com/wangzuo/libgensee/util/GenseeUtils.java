package com.wangzuo.libgensee.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import com.wangzuo.libgensee.core.RTLive;
import com.wangzuo.libgensee.ui.BaseActivity;
import com.wangzuo.libgensee.util.GenseeToast;
import com.wangzuo.libgensee.util.ResManager;
import com.gensee.utils.GenseeLog;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenseeUtils {
    protected static final String TAG = "GenseeUtils";

    public GenseeUtils() {
    }

    public static String getVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo = null;

        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packInfo.versionName;
        } catch (NameNotFoundException var4) {
            var4.printStackTrace();
            return "";
        }
    }

    public static String filterNickName(String nickName) {
        String value = nickName;
        if(nickName != null && nickName.length() > 12) {
            value = nickName.substring(0, 12) + "...";
        }

        return value;
    }

    public static void hideSoftInputmethod(Context context) {
        Activity activity = (Activity)context;
        if(activity.getCurrentFocus() != null) {
            InputMethodManager im = (InputMethodManager)activity.getSystemService("input_method");
            im.hideSoftInputFromWindow(activity.getCurrentFocus().getApplicationWindowToken(), 2);
        }

    }

    public static String getTimeHHMMSS(int time) {
        return String.format(Locale.CHINESE, "%02d:%02d:%02d", new Object[]{Integer.valueOf(time / 3600), Integer.valueOf(time / 60 % 60), Integer.valueOf(time % 60)});
    }

    public static void sendLog(final Context context, final boolean isAuto) {
        (new Thread(new Runnable() {
            public void run() {
                GenseeUtils.preSendLog(context, isAuto);
                boolean isZipCurLog = !isAuto;
                GenseeLog.zipFile(isZipCurLog);
                String desc = null;
                if(isAuto) {
                    if(GenseeLog.containsZipStackFile()) {
                        desc = "Android fastsdk 4.0 stack log auto upload";
                    }
                } else {
                    desc = "Android fastsdk 4.0 upload by user";
                }

                String reportRet = null;
                if(desc != null) {
                    if(RTLive.getIns().isPublishMode()) {
                        reportRet = RTLive.getIns().getRtSdk().reportDiagonse(desc, false, isZipCurLog);
                    } else {
                        reportRet = GenseeLog.reportDiagonse(context, desc, RTLive.getIns().getServiceType());
                    }
                }

                GenseeLog.i("GenseeUtils", "sendlog/report diagnose reportRet:" + reportRet);
                GenseeUtils.endSendLog(context, isAuto, reportRet);
            }
        })).start();
    }

    private static void preSendLog(final Context context, boolean isAuto) {
        if(!isAuto) {
            ((Activity)context).runOnUiThread(new Runnable() {
                public void run() {
                    ((BaseActivity)context).showProgressDialog(ResManager.getStringId("gs_diagnosis_ing"));
                }
            });
        }

    }

    private static void endSendLog(final Context context, boolean isAuto, final String reportRet) {
        if(!isAuto) {
            ((Activity)context).runOnUiThread(new Runnable() {
                public void run() {
                    ((BaseActivity)context).dismissProgressDialog();
                    boolean isOk = reportRet != null && reportRet.contains("<result>ok</result>");
                    GenseeToast.showToast(context, context.getString(isOk?ResManager.getStringId("gs_diagnosis_end"):ResManager.getStringId("gs_diagnosis_fail")), true, ResManager.getDrawableId("gs_warming_bg"), isOk?ResManager.getDrawableId("gs_diagnose_ok"):ResManager.getDrawableId("gs_diagnose_failure"));
                }
            });
        }

    }

    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == 2;
    }

    public static boolean checkPackagePermission(Context context, String sType) {
        PackageManager pm = context.getPackageManager();
        boolean permission = pm.checkPermission(sType, "packageName") == 0;
        return permission;
    }

    public static String formatText(String name, int maxCNCount) {
        if(!TextUtils.isEmpty(name) && maxCNCount >= 1) {
            Pattern p = Pattern.compile("[一-龥]");
            Matcher m = null;
            int count = 0;

            for(int i = 0; i < name.length(); ++i) {
                String temp = name.substring(i, i + 1);
                m = p.matcher(temp);
                if(m.matches()) {
                    count += 2;
                } else {
                    ++count;
                }

                if(count > maxCNCount * 2) {
                    name = name.substring(0, i - 1) + "...";
                    break;
                }
            }

            return name;
        } else {
            return null;
        }
    }
}
