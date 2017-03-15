package com.wangzuo.libgensee.util;


import android.content.Context;
import android.os.Environment;
import android.os.Process;
import com.gensee.fastsdk.util.ConfigApp;
import com.gensee.utils.FileUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogcatHelper {
    private static LogcatHelper INSTANCE = null;
    private static String PATH_LOGCAT;
    private LogcatHelper.LogDumper mLogDumper = null;
    private int mPId;

    public void init(Context context) {
        if(Environment.getExternalStorageState().equals("mounted")) {
            PATH_LOGCAT = ConfigApp.LOGPATH + "logcat" + File.separator;
        } else {
            PATH_LOGCAT = context.getFilesDir().getAbsolutePath() + File.separator + "logcat" + File.separator;
        }

        File file = new File(PATH_LOGCAT);
        if(!file.exists()) {
            file.mkdirs();
        }

    }

    public static LogcatHelper getInstance(Context context) {
        if(INSTANCE == null) {
            INSTANCE = new LogcatHelper(context);
        }

        return INSTANCE;
    }

    private LogcatHelper(Context context) {
        this.init(context);
        this.mPId = Process.myPid();
    }

    public void start() {
        if(this.mLogDumper == null) {
            this.mLogDumper = new LogcatHelper.LogDumper(String.valueOf(this.mPId), PATH_LOGCAT);
        }

        this.mLogDumper.start();
    }

    public void stop() {
        if(this.mLogDumper != null) {
            this.mLogDumper.stopLogs();
            this.mLogDumper = null;
        }

    }

    private class LogDumper extends Thread {
        private java.lang.Process logcatProc;
        private BufferedReader mReader = null;
        private boolean mRunning = true;
        String cmds = null;
        private String mPID;
        private FileOutputStream out = null;
        private String dir = "";

        public LogDumper(String pid, String dir) {
            this.mPID = pid;

            try {
                this.dir = dir;
                this.out = new FileOutputStream(new File(dir, "logcat-" + LogcatHelper.MyDate.getFileName() + ".txt"));
            } catch (FileNotFoundException var5) {
                var5.printStackTrace();
            }

            this.cmds = "logcat ";
        }

        public void stopLogs() {
            this.mRunning = false;
        }

        public void run() {
            try {
                File e = new File(this.dir);
                if(e.exists()) {
                    FileUtil.deleteFileByTime(e, System.currentTimeMillis(), 3600000L);
                }

                this.logcatProc = Runtime.getRuntime().exec(this.cmds);
                this.mReader = new BufferedReader(new InputStreamReader(this.logcatProc.getInputStream()), 1024);
                String line = null;

                while(this.mRunning && (line = this.mReader.readLine()) != null && this.mRunning) {
                    if(line.length() != 0 && this.out != null) {
                        this.out.write((LogcatHelper.MyDate.getDateEN() + "  " + line + "\n").getBytes());
                    }
                }
            } catch (IOException var15) {
                var15.printStackTrace();
            } finally {
                if(this.logcatProc != null) {
                    this.logcatProc.destroy();
                    this.logcatProc = null;
                }

                if(this.mReader != null) {
                    try {
                        this.mReader.close();
                        this.mReader = null;
                    } catch (IOException var14) {
                        var14.printStackTrace();
                    }
                }

                if(this.out != null) {
                    try {
                        this.out.close();
                    } catch (IOException var13) {
                        var13.printStackTrace();
                    }

                    this.out = null;
                }

            }

        }
    }

    private static class MyDate {
        private MyDate() {
        }

        public static String getFileName() {
            SimpleDateFormat format1 = new SimpleDateFormat("yyyyMMddHHmmss");
            String date = format1.format(new Date(System.currentTimeMillis()));
            return date;
        }

        public static String getDateEN() {
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String date1 = format1.format(new Date(System.currentTimeMillis()));
            return date1;
        }
    }
}
