package xyz.nulldev.statusbarsleep;

import android.view.MotionEvent;

import de.robv.android.xposed.XC_MethodHook;

public class StatusBarMotionHook extends XC_MethodHook {

    private BasicStatusBarSleepImpl parent;

    public StatusBarMotionHook(BasicStatusBarSleepImpl parent) {
        this.parent = parent;
    }

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        if (parent.getGestureDetector() != null) {
            parent.getGestureDetector().onTouchEvent((MotionEvent) param.args[0]);
        }
    }
}
