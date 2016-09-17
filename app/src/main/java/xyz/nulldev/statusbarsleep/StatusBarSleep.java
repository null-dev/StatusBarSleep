package xyz.nulldev.statusbarsleep;

import android.os.Build;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class StatusBarSleep implements IXposedHookLoadPackage {

    private StatusBarSleepImpl implementation = findImplementation();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        try {
            if (implementation != null) {
                implementation.handleLoadPackage(loadPackageParam);
            } else {
                XposedBridge.log("StatusBarSleep is not supported on this version of Android!");
            }
        } catch (Exception e) {
            XposedBridge.log("StatusBarSleep failed to initialize!");
            XposedBridge.log(e);
        }
    }

    private StatusBarSleepImpl findImplementation() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //Lolipop and Marshmallow
                return new BasicStatusBarSleepImpl("com.android.systemui.statusbar.phone.PhoneStatusBarView",
                        "com.android.systemui.statusbar.phone.NotificationPanelView");
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                //ICS, Jelly Bean, Kitkat
                return new BasicStatusBarSleepImpl("com.android.systemui.statusbar.phone.PhoneStatusBarView");
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                //Gingerbread
                return new BasicStatusBarSleepImpl("com.android.systemui.statusbar.StatusBarView");
            }
        } catch (Exception e) {
            XposedBridge.log("Error setting up StatusBarSleep implementation!");
            XposedBridge.log(e);
        }
        return null;
    }
}
