package util;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Utility Class: AutoLockTimer
 *
 * Automatically "locks" the app after 5 minutes of inactivity.
 * The timer resets every time the user does something (adds, edits, etc.).
 * When locked, the GUI logs the user out for security.
 */
public class AutoLockTimer {

    private Timer timer;
    private boolean locked = false;
    private static final long TIMEOUT_MS = 5 * 60 * 1000;

    public void reset() {
        locked = false;
        if (timer != null) timer.cancel();

        timer = new Timer(true); // daemon=true so it doesn't block app shutdown
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                locked = true; // App will lock on next check
            }
        }, TIMEOUT_MS);
    }

    /** Returns true if the app should be locked */
    public boolean isLocked() { return locked; }

    /** Stops the timer (call when user logs out or app closes) */
    public void stop() {
        if (timer != null) timer.cancel();
    }
}
