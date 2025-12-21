package com.edu.english.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableException;

/**
 * Helper class for checking ARCore availability and support
 */
public class ArCoreHelper {
    
    /**
     * Result of AR availability check
     */
    public enum ArAvailability {
        SUPPORTED,              // AR is fully supported
        SUPPORTED_NOT_INSTALLED, // AR supported but ARCore not installed
        NOT_SUPPORTED           // AR not supported on this device
    }
    
    /**
     * Check if ARCore is available on this device
     * This is a synchronous check - for better UX, use async version
     */
    public static ArAvailability checkArAvailability(Context context) {
        try {
            ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(context);
            
            switch (availability) {
                case SUPPORTED_INSTALLED:
                    return ArAvailability.SUPPORTED;
                    
                case SUPPORTED_APK_TOO_OLD:
                case SUPPORTED_NOT_INSTALLED:
                    return ArAvailability.SUPPORTED_NOT_INSTALLED;
                    
                case UNKNOWN_CHECKING:
                    // Still checking, treat as not supported for now
                    // In production, you might want to wait
                    return ArAvailability.NOT_SUPPORTED;
                    
                case UNKNOWN_ERROR:
                case UNKNOWN_TIMED_OUT:
                case UNSUPPORTED_DEVICE_NOT_CAPABLE:
                default:
                    return ArAvailability.NOT_SUPPORTED;
            }
        } catch (Exception e) {
            // If any exception occurs, fallback to not supported
            return ArAvailability.NOT_SUPPORTED;
        }
    }
    
    /**
     * Check if device has camera feature (required for AR)
     */
    public static boolean hasCameraFeature(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }
    
    /**
     * Request ARCore installation if needed
     * @return true if installation was requested, false if already installed or not supported
     */
    public static boolean requestArCoreInstall(Activity activity, boolean userRequestedInstall) {
        try {
            ArCoreApk.InstallStatus installStatus = ArCoreApk.getInstance()
                    .requestInstall(activity, userRequestedInstall);
            
            switch (installStatus) {
                case INSTALLED:
                    return false; // Already installed
                case INSTALL_REQUESTED:
                    return true; // Installation requested
                default:
                    return false;
            }
        } catch (UnavailableException e) {
            return false;
        }
    }
    
    /**
     * Try to create an AR Session
     * @return Session if successful, null otherwise
     */
    public static Session tryCreateArSession(Activity activity) {
        try {
            Session session = new Session(activity);
            Config config = new Config(session);
            config.setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL);
            config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
            session.configure(config);
            return session;
        } catch (UnavailableException e) {
            return null;
        }
    }
    
    /**
     * Simple check if AR can be used (synchronous, blocking)
     * For quick checks - use with caution
     */
    public static boolean isArSupported(Context context) {
        ArAvailability availability = checkArAvailability(context);
        return availability == ArAvailability.SUPPORTED;
    }
}
