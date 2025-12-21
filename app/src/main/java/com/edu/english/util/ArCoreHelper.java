package com.edu.english.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableException;

/**
 * Helper class for checking ARCore support
 */
public class ArCoreHelper {
    
    private static final String TAG = "ArCoreHelper";
    
    /**
     * Check if ARCore is supported and installed on this device
     * @param activity The activity context
     * @return true if AR is available and ready
     */
    public static boolean isArSupported(Activity activity) {
        try {
            ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(activity);
            
            switch (availability) {
                case SUPPORTED_INSTALLED:
                    return true;
                    
                case SUPPORTED_APK_TOO_OLD:
                case SUPPORTED_NOT_INSTALLED:
                    // ARCore is supported but needs to be installed/updated
                    // Try to request install
                    try {
                        ArCoreApk.InstallStatus installStatus = ArCoreApk.getInstance()
                                .requestInstall(activity, true);
                        return installStatus == ArCoreApk.InstallStatus.INSTALLED;
                    } catch (UnavailableException e) {
                        Log.e(TAG, "ARCore installation failed", e);
                        return false;
                    }
                    
                case UNKNOWN_CHECKING:
                    // Still checking, assume not available for now
                    return false;
                    
                case UNKNOWN_ERROR:
                case UNKNOWN_TIMED_OUT:
                case UNSUPPORTED_DEVICE_NOT_CAPABLE:
                default:
                    return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking AR availability", e);
            return false;
        }
    }
    
    /**
     * Check if device has a camera
     * @param context The context
     * @return true if camera feature is available
     */
    public static boolean hasCameraFeature(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }
    
    /**
     * Create an AR session
     * @param activity The activity context
     * @return Session or null if creation failed
     */
    public static Session createArSession(Activity activity) {
        try {
            Session session = new Session(activity);
            Config config = new Config(session);
            config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
            config.setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL);
            session.configure(config);
            return session;
        } catch (UnavailableException e) {
            Log.e(TAG, "Failed to create AR session", e);
            return null;
        }
    }
}
