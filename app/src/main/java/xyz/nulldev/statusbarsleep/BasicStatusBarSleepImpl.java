package xyz.nulldev.statusbarsleep;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.MotionEvent;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import xyz.nulldev.statusbarsleep.gesture.ExtendedGestureDetector;

public class BasicStatusBarSleepImpl implements StatusBarSleepImpl {

    private final String viewClass;
    private final String notificationViewClass;
    private ExtendedGestureDetector gestureDetector = null;
    private PowerManager powerManager = null;
    private Object statusBarManager = null;
    private Method closeStatusBarMethod = null;

    public BasicStatusBarSleepImpl(String viewClass) {
        this(viewClass, null);
    }

    public BasicStatusBarSleepImpl(String viewClass, String notificationViewClass) {
        this.viewClass = viewClass;
        this.notificationViewClass = notificationViewClass;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        //System UI package
        if (loadPackageParam.packageName.equals("com.android.systemui")) {
            Class<?> statusBarClass = XposedHelpers.findClass(viewClass, loadPackageParam.classLoader);
            Class<?> notificationClass = null;
            if(notificationViewClass != null) {
                notificationClass = XposedHelpers.findClass(notificationViewClass, loadPackageParam.classLoader);
            }
            hookContext(statusBarClass);
            hookTouchEvent(statusBarClass, notificationClass);
        }
    }

    private void hookContext(final Class<?> statusBarClass) {
        XposedBridge.hookAllConstructors(statusBarClass, new ContextHook());
    }

    private void setupStatusBarManager(Context context) {
        //noinspection WrongConstant
        statusBarManager = context.getSystemService("statusbar");
        String methodName;
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
            methodName = "collapse";
        } else {
            methodName = "collapsePanels";
        }
        try {
            closeStatusBarMethod = statusBarManager.getClass().getMethod(methodName);
            closeStatusBarMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            XposedBridge.log("StatusBarSleep could not setup automatic status bar collapsing!");
            XposedBridge.log(e);
        }
    }

    private void closeStatusBar() {
        if(statusBarManager != null && closeStatusBarMethod != null) {
            try {
                closeStatusBarMethod.invoke(statusBarManager);
            } catch (Exception e) {
                XposedBridge.log("StatusBarSleep could not collapse the status bar!");
                XposedBridge.log(e);
            }
        }
    }

    private void goToSleep() {
        if (powerManager != null) {
            XposedHelpers.callMethod(powerManager, "goToSleep", SystemClock.uptimeMillis());
            //Make sure status bar is closed
            closeStatusBar();
        }
    }

    private ExtendedGestureDetector createGestureDetector(final Context context) {
        return new ExtendedGestureDetector(context, new DoubleTapListener());
    }

    private void hookTouchEvent(Class<?> statusBarClass, Class<?> notificationClass) {
        XposedHelpers.findAndHookMethod(statusBarClass, "onTouchEvent", MotionEvent.class, new StatusBarMotionHook(this, true));
        if(notificationClass != null) {
            XposedHelpers.findAndHookMethod(notificationClass, "onTouchEvent", MotionEvent.class, new StatusBarMotionHook(this, false));
        }
    }

    public ExtendedGestureDetector getGestureDetector() {
        return gestureDetector;
    }

    private class DoubleTapListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //Make sure first event is from the status bar
            if(gestureDetector.isLastActionFromStatusBar() || BuildConfig.ENABLE_EXTENDED_TAP_TARGETS) {
                goToSleep();
                gestureDetector.resetTapStates();
            }
            return true;
        }
    }

    private class ContextHook extends XC_MethodHook {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            if (gestureDetector == null || powerManager == null) {
                Context context = null;
                for (Object object : param.args) {
                    if (object instanceof Context) {
                        context = (Context) object;
                        break;
                    }
                }
                if (context != null) {
                    gestureDetector = createGestureDetector(context);
                    powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    setupStatusBarManager(context);
                } else {
                    XposedBridge.log("StatusBarSleep is unable to acquire context!");
                }
            }
        }
    }
}
