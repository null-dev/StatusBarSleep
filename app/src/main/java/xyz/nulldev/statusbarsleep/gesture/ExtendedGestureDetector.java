package xyz.nulldev.statusbarsleep.gesture;

import android.content.Context;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;

import de.robv.android.xposed.XposedBridge;

public class ExtendedGestureDetector extends GestureDetector {

    private boolean lastActionFromStatusBar = false;
    private boolean statusBarDown = false;
    private boolean notificationBarDown = false;

    public ExtendedGestureDetector(OnGestureListener listener, Handler handler) {
        super(listener, handler);
    }

    public ExtendedGestureDetector(OnGestureListener listener) {
        super(listener);
    }

    public ExtendedGestureDetector(Context context, OnGestureListener listener) {
        super(context, listener);
    }

    public ExtendedGestureDetector(Context context, OnGestureListener listener, Handler handler) {
        super(context, listener, handler);
    }

    public ExtendedGestureDetector(Context context, OnGestureListener listener, Handler handler, boolean unused) {
        super(context, listener, handler, unused);
    }

    public boolean onTouchEvent(MotionEvent ev, boolean fromStatusBar) {
        int action = ev.getActionMasked();
        //Ignore events that are from the notification bar but the status bar is still being held on
        if(!fromStatusBar && statusBarDown) {
            return false;
        } else if(!fromStatusBar && !notificationBarDown && action != MotionEvent.ACTION_DOWN) {
            return false;
        }
        boolean handled = super.onTouchEvent(ev);
        if(action == MotionEvent.ACTION_DOWN) {
            if(fromStatusBar) {
                statusBarDown = true;
            } else {
                notificationBarDown = true;
            }
            lastActionFromStatusBar = fromStatusBar;
        } else if(action == MotionEvent.ACTION_UP) {
            if(fromStatusBar) {
                statusBarDown = false;
            } else {
                notificationBarDown = false;
            }
        }

        return handled;
    }

    public boolean isLastActionFromStatusBar() {
        return lastActionFromStatusBar;
    }

    public void resetTapStates() {
        statusBarDown = false;
        notificationBarDown = false;
    }
}
