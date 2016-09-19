package xyz.nulldev.statusbarsleep;

import android.view.MotionEvent;

import de.robv.android.xposed.XC_MethodHook;

public class StatusBarMotionHook extends XC_MethodHook {

    private final BasicStatusBarSleepImpl parent;
    private final boolean fromStatusBar;

    public StatusBarMotionHook(BasicStatusBarSleepImpl parent, boolean fromStatusBar) {
        this.parent = parent;
        this.fromStatusBar = fromStatusBar;
    }

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        if (parent.getGestureDetector() != null) {
            MotionEvent event = (MotionEvent) param.args[0];
            if(event != null) {
                parent.getGestureDetector().onTouchEvent(event, fromStatusBar);
            }
        }
    }
}
