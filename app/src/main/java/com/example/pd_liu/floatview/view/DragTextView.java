package com.example.pd_liu.floatview.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

/**
 * @author pd_liu.2017年7月5日
 *         This is used to show float view.
 *         This class should not be implemented.
 */
public final class DragTextView extends AppCompatTextView {

    private static final String TAG = "DragTextView";
    /**
     * Request code of window permission.
     */
    public static final int REQUEST_CODE_WINDOW_PERMISSION = 1;
    /**
     * The view parent activity.
     */
    private Activity mCurrentActivity;
    /**
     * This WindowManager
     */
    private WindowManager mWindowManager;
    /**
     * The view parent context.
     */
    private Context mContext;
    /**
     * WindowManager
     */
    private WindowManager.LayoutParams mWindowLayoutParams;
    /**
     * PermissionDialog
     */
    private AlertDialog mPermissionDialog;
    /**
     * Touch Slop
     */
    private int mTouchSlop;
    /**
     * The view rect
     */
    private Rect mRect = new Rect();
    /**
     * Dragging state
     */
    private boolean isDragging;
    /**
     * whether has added view.
     */
    private boolean mIsAddView;
    /**
     * positions
     */
    private float mTouchX;
    private float mTouchY;
    private float mStartX;
    private float mStartY;

    public DragTextView(Context context) {
        this(context, null);
    }

    public DragTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        getWindowVisibleDisplayFrame(mRect);
        mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        mTouchSlop = mTouchSlop * mTouchSlop;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchX = event.getX() + getLeft();
                mTouchY = event.getY() + getTop();
                mStartX = event.getRawX();
                mStartY = event.getRawY();
                isDragging = false;
                break;
            case MotionEvent.ACTION_MOVE:

                int dx = (int) (event.getRawX() - mStartX);
                int dy = (int) (event.getRawY() - mStartY);
                if ((dx * dx + dy * dy) > mTouchSlop) {
                    isDragging = true;
                    mWindowLayoutParams.x = (int) (event.getRawX() - mTouchX);
                    mWindowLayoutParams.y = (int) (event.getRawY() - mTouchY);
                    mWindowManager.updateViewLayout(this, mWindowLayoutParams);
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                mTouchX = mTouchY = 0.0f;
                if (isDragging) {
                    reposition();
                    isDragging = false;
                    return true;
                }
        }
        return false;
    }

    /**
     * This method reposition this view position
     */
    private void reposition() {
        if (mWindowLayoutParams.x < mRect.left) {
            mWindowLayoutParams.x = mRect.left;
        }
        if (mWindowLayoutParams.x > mRect.width()) {
            mWindowLayoutParams.x = mRect.width();
        }
        if (mWindowLayoutParams.y < mRect.top) {
            mWindowLayoutParams.y = mRect.top;
        }
        if (mWindowLayoutParams.y > mRect.height()) {
            mWindowLayoutParams.y = mRect.height();
        }
        mWindowManager.updateViewLayout(this, mWindowLayoutParams);
    }

    /**
     * This method add a default config view.
     *
     * @param activity view parent activity.
     * @return Whether to add success.
     */
    public boolean addToApplication(Activity activity) {
        if (activity == null) {
            return false;
        }
        boolean addState;
        mCurrentActivity = activity;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (!Settings.canDrawOverlays(mContext)) {

                addState = false;
                showDialog(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        enterPermissionSettings();
                    }
                });

            } else {
                // Already hold the SYSTEM_ALERT_WINDOW permission, do add view or something.
                addView();
                addState = true;
            }

        } else {
            addView();
            addState = true;
        }

        return addState;
    }

    /**
     * add view
     */
    private void addView() {
        mIsAddView = true;
        mWindowLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, 0, 0,
                PixelFormat.TRANSPARENT);
        mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        mWindowLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mWindowLayoutParams.gravity = Gravity.TOP | Gravity.START;
        mWindowLayoutParams.x = 300;
        mWindowLayoutParams.y = 300;
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        }
        mWindowManager.addView(this, mWindowLayoutParams);
    }

    /**
     * This method provide:confirm the permission of window manager.
     * If permission not granted. it will to open setting ui .
     * If permission already granted. it will return a true state.
     *
     * @param requestCode requestCode
     * @return isOkPermission
     */
    public boolean isOkPermission(int requestCode) {

        if (requestCode == REQUEST_CODE_WINDOW_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(mContext)) {
                    // SYSTEM_ALERT_WINDOW permission not granted...
                    showDialog(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            if (mCurrentActivity != null) {
                                enterPermissionSettings();
                            }
                        }
                    });
                    return false;
                } else {
                    // Already hold the SYSTEM_ALERT_WINDOW permission, do add view or something.
                    return true;
                }
            } else {
                return true;
            }
        }

        return false;
    }

    /**
     * This method can remove special view from application
     *
     * @param removeView The special remove view.
     * @return remove state: success or fail.
     * @see #removeThisView()
     */
    public boolean removeFromApplication(@Nullable View removeView) {
        if (removeView != null && mWindowManager != null) {
            if(mIsAddView){
                mWindowManager.removeView(removeView);
                mIsAddView = false;
            }
            if (mPermissionDialog != null) {
                mPermissionDialog.cancel();
            }
            mCurrentActivity = null;
            return true;
        }
        return false;
    }

    /**
     * This method can remove this view from application.
     *
     * @return remove state: success or fail.
     * @see #removeFromApplication(View) .
     */
    public boolean removeThisView() {
        return removeFromApplication(this);
    }

    /**
     * This method is used to enter permission setting.
     * After this,You can open the permission or not .
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void enterPermissionSettings() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + mCurrentActivity.getPackageName()));
        mCurrentActivity.startActivityForResult(intent, REQUEST_CODE_WINDOW_PERMISSION);
    }

    /**
     * Show dialog
     *
     * @param positiveClick permission.
     */
    public void showDialog(DialogInterface.OnClickListener positiveClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mCurrentActivity);
        builder.setMessage("您的手机没有开通相关权限以及悬浮窗权限\n导致某些功能无法正常使用！");

        builder.setPositiveButton("去设置", positiveClick)
                .setNegativeButton("稍后设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        mPermissionDialog = builder.create();
        mPermissionDialog.show();
    }
}
