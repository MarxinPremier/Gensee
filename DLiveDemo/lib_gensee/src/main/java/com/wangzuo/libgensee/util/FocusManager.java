package com.wangzuo.libgensee.util;


import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import com.wangzuo.libgensee.core.RTLive;
import com.gensee.utils.GenseeLog;
import java.util.ArrayList;
import java.util.List;

public class FocusManager
{
    private Matrix mMatrix;
    private static final int RESET_TOUCH_FOCUS = 0;
    private static final int RESET_TOUCH_FOCUS_DELAY = 3000;
    private int mState = 0;
    private static final int STATE_IDLE = 0;
    private static final int STATE_FOCUSING = 1;
    private static final int STATE_FOCUSING_SNAP_ON_FINISH = 2;
    private static final int STATE_SUCCESS = 3;
    private static final int STATE_FAIL = 4;
    private static final String TAG = "FocusManager";
    private List<Camera.Area> mFocusArea;
    private List<Camera.Area> mMeteringArea;
    private Camera mCamera;
    private View mPreviewFrame;
    private final AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback();
    private MainHandler mHandler;
    private Camera.Parameters mParameters;
    private Camera.Parameters mInitialParams;
    private boolean mFocusAreaSupported;
    private boolean mMeteringAreaSupported;
    private int mDisplayRotation;
    private int mDisplayOrientation;
    private Camera.CameraInfo mCameraInfo;
    private boolean isInitialFirstTime = true;
    private boolean isInitialParamFirstTime = true;
    private boolean isLandscape;

    public FocusManager()
    {
        this.mMatrix = new Matrix();
        this.mHandler = new MainHandler();
    }

    public void setCameraInfo(Camera.CameraInfo cameraInfo) {
        this.mCameraInfo = cameraInfo;
    }

    public void setCamera(Camera mCamera) {
        this.mCamera = mCamera;
    }

    public void initialize(View previewFrame, Activity activity) {
        this.isLandscape = GenseeUtils.isLandscape(activity);
        this.mPreviewFrame = previewFrame;
        if (this.mCameraInfo == null) {
            return;
        }
        boolean mirror = this.mCameraInfo.facing == 1;

        this.mDisplayRotation = getDisplayRotation(activity);

        int displayOrientation = getDisplayOrientation(this.mDisplayRotation, this.mCameraInfo);
        Matrix matrix = new Matrix();
        prepareMatrix(matrix, mirror, displayOrientation, previewFrame.getWidth(), previewFrame.getHeight());
        matrix.invert(this.mMatrix);
    }

    public static int getDisplayOrientation(int degrees, Camera.CameraInfo info)
    {
        int result;
        if (info.facing == 1) {
            int newResult = (info.orientation + degrees) % 360;
            result = (360 - newResult) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    public static int getDisplayRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        switch (rotation) {
            case 0:
                return 0;
            case 1:
                return 90;
            case 2:
                return 180;
            case 3:
                return 270;
        }
        return 0;
    }

    public void cancelAutoFocus()
    {
        if (this.mCamera == null) {
            return;
        }
        resetTouchFocus();
        if (RTLive.getIns().isVideoCameraOpen())
            try {
                this.mCamera.cancelAutoFocus();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public void resetTouchFocus()
    {
        this.mFocusArea = null;
        this.mMeteringArea = null;
    }

    public boolean onTouch(MotionEvent e) {
        if (this.mCamera == null) {
            return false;
        }

        if ((this.mFocusArea != null) && ((this.mState == 1) ||
                (this.mState == 3) || (this.mState == 4)))
            this.mCamera.cancelAutoFocus();
        try
        {
            initializeParameters();
        } catch (Exception e1) {
            GenseeLog.w("FocusManager", "initializeParameters Exception:" + e1);
            e1.printStackTrace();
            return false;
        }

        int x = Math.round(e.getX());
        int y = Math.round(e.getY());
        int focusWidth = 100;
        int focusHeight = 100;
        int previewWidth = this.mPreviewFrame.getWidth();
        int previewHeight = this.mPreviewFrame.getHeight();
        if (this.mFocusArea == null) {
            this.mFocusArea = new ArrayList();
            this.mFocusArea.add(new Camera.Area(new Rect(), 1));
            this.mMeteringArea = new ArrayList();
            this.mMeteringArea.add(new Camera.Area(new Rect(), 1));
        }

        int temp = 0;
        if (!this.isLandscape) {
            temp = previewWidth;
            previewWidth = previewHeight;
            previewHeight = temp;
        }
        calculateTapArea(focusWidth, focusHeight, 1.0F, x, y, previewWidth, previewHeight,
                ((Camera.Area)this.mFocusArea.get(0)).rect);
        calculateTapArea(focusWidth, focusHeight, 1.5F, x, y, previewWidth, previewHeight,
                ((Camera.Area)this.mMeteringArea.get(0)).rect);
        try {
            setFocusParameters();
        } catch (Exception e1) {
            GenseeLog.w("FocusManager", "setFocusParameters Exception:" + e1);
            e1.printStackTrace();
            return false;
        }

        if (e.getAction() == 1) {
            autoFocus();
        }
        else {
            removeResetFocusMessage();
            this.mHandler.sendEmptyMessageDelayed(0, 3000L);
        }

        return true;
    }

    private void setFocusParameters() throws Exception {
        this.mParameters = this.mCamera.getParameters();
        this.mParameters.setFlashMode("off");
        GenseeLog.i("FocusManager", "camera mFocusAreaSupported:" + this.mFocusAreaSupported + ",mMeteringAreaSupported:" + this.mMeteringAreaSupported);
        if (this.mFocusAreaSupported) {
            this.mParameters.setFocusAreas(this.mFocusArea);
        }
        if (this.mMeteringAreaSupported)
        {
            this.mParameters.setMeteringAreas(this.mMeteringArea);
        }
        this.mCamera.setParameters(this.mParameters);
    }

    private void initializeParameters() throws Exception {
        this.isInitialParamFirstTime = false;
        this.mInitialParams = this.mCamera.getParameters();
        if (this.mInitialParams.getMaxNumFocusAreas() > 0);
        this.mFocusAreaSupported =
                (isSupported("auto",
                        this.mInitialParams.getSupportedFocusModes()));
        this.mMeteringAreaSupported = (this.mInitialParams.getMaxNumMeteringAreas() > 0);
    }

    private static boolean isSupported(String value, List<String> supported) {
        return supported != null;
    }

    private void autoFocus() {
        this.mCamera.autoFocus(this.mAutoFocusCallback);
        this.mState = 1;

        removeResetFocusMessage();
    }

    public void removeResetFocusMessage() {
        this.mHandler.removeMessages(0);
    }

    public void calculateTapArea(int focusWidth, int focusHeight, float areaMultiple, int x, int y, int previewWidth, int previewHeight, Rect rect)
    {
        int areaWidth = (int)(focusWidth * areaMultiple);
        int areaHeight = (int)(focusHeight * areaMultiple);

        if (!this.isLandscape) {
            int temp = previewHeight;
            previewHeight = previewWidth;
            previewWidth = temp;
        }

        int left = clamp(x - areaWidth / 2, 0, previewWidth - areaWidth);
        int top = clamp(y - areaHeight / 2, 0, previewHeight - areaHeight);

        RectF rectF = null;
        int right = left + areaWidth;
        int bottom = top + areaHeight;
        if (right == left) {
            left++;
        }
        if (bottom == top) {
            top++;
        }
        rectF = new RectF(left, top, right, bottom);

        this.mMatrix.mapRect(rectF);
        rectFToRect(rectF, rect);
    }

    public int clamp(int x, int min, int max) {
        if (x > max) return max;
        if (x < min) return min;
        return x;
    }

    public void rectFToRect(RectF rectF, Rect rect) {
        rect.left = Math.round(rectF.left);
        rect.top = Math.round(rectF.top);
        rect.right = Math.round(rectF.right);
        rect.bottom = Math.round(rectF.bottom);
    }

    public void prepareMatrix(Matrix matrix, boolean mirror, int displayOrientation, int viewWidth, int viewHeight)
    {
        matrix.setScale(mirror ? -1 : 1, 1.0F);

        matrix.postRotate(displayOrientation);

        matrix.postScale(viewWidth / 2000.0F, viewHeight / 2000.0F);
        matrix.postTranslate(viewWidth / 2.0F, viewHeight / 2.0F);
    }

    private final class AutoFocusCallback
            implements Camera.AutoFocusCallback
    {
        private AutoFocusCallback()
        {
        }

        public void onAutoFocus(boolean focused, Camera camera)
        {
            if (FocusManager.this.mState == 1)
            {
                if (focused)
                    FocusManager.this.mState = 3;
                else {
                    FocusManager.this.mState = 4;
                }

                if (FocusManager.this.mFocusArea != null)
                    FocusManager.this.mHandler.sendEmptyMessageDelayed(0, 3000L);
            }
        }
    }

    private class MainHandler extends Handler
    {
        private MainHandler()
        {
        }

        public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case 0:
                    FocusManager.this.cancelAutoFocus();
            }
        }
    }
}