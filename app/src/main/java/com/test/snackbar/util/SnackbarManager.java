package com.test.snackbar.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.Snackbar;

import java.lang.ref.WeakReference;


public class SnackbarManager {
    private static final int MSG_TIMEOUT = 0;

    private static final int SHORT_DURATION_MS = 1500;
    private static final int LONG_DURATION_MS = 2750;

    private static SnackbarManager sSnackbarManager;

    static SnackbarManager getInstance() {
        if (sSnackbarManager == null) {
            sSnackbarManager = new SnackbarManager();
        }
        return sSnackbarManager;
    }

    public final Object mLock;
    public final Handler mHandler;

    public SnackbarRecord mCurrentSnackbar;
    public SnackbarRecord mNextSnackbar;

    private SnackbarManager() {
        mLock = new Object();
        mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MSG_TIMEOUT:
                        handleTimeout((SnackbarRecord) message.obj);
                        return true;
                }
                return false;
            }
        });
    }

   public interface Callback {
        void show();

        void dismiss(int event);
    }

    public void show(int duration, Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbarLocked(callback)) {
                mCurrentSnackbar.duration = duration;

                // If this is the Snackbar currently being shown, call re-schedule it's
                // timeout
                mHandler.removeCallbacksAndMessages(mCurrentSnackbar);
                scheduleTimeoutLocked(mCurrentSnackbar);
                return;
            } else if (isNextSnackbarLocked(callback)) {
                // We'll just update the duration
                mNextSnackbar.duration = duration;
            } else {
                // Else, we need to create a new record and queue it
                mNextSnackbar = new SnackbarRecord(duration, callback);
            }

            if (mCurrentSnackbar != null && cancelSnackbarLocked(mCurrentSnackbar,
                    Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE)) {
                // If we currently have a Snackbar, try and cancel it and wait in line
                return;
            } else {
                // Clear out the current snackbar
                mCurrentSnackbar = null;
                // Otherwise, just show it now
                showNextSnackbarLocked();
            }
        }
    }

    public void dismiss(Callback callback, int event) {
        synchronized (mLock) {
            if (isCurrentSnackbarLocked(callback)) {
                cancelSnackbarLocked(mCurrentSnackbar, event);
            } else if (isNextSnackbarLocked(callback)) {
                cancelSnackbarLocked(mNextSnackbar, event);
            }
        }
    }


    public void onDismissed(Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbarLocked(callback)) {
                // If the callback is from a Snackbar currently show, remove it and show a new one
                mCurrentSnackbar = null;
                if (mNextSnackbar != null) {
                    showNextSnackbarLocked();
                }
            }
        }
    }

    public void onShown(Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbarLocked(callback)) {
                scheduleTimeoutLocked(mCurrentSnackbar);
            }
        }
    }

    public void cancelTimeout(Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbarLocked(callback)) {
                mHandler.removeCallbacksAndMessages(mCurrentSnackbar);
            }
        }
    }

    public void restoreTimeout(Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbarLocked(callback)) {
                scheduleTimeoutLocked(mCurrentSnackbar);
            }
        }
    }

    public boolean isCurrent(Callback callback) {
        synchronized (mLock) {
            return isCurrentSnackbarLocked(callback);
        }
    }

    public boolean isCurrentOrNext(Callback callback) {
        synchronized (mLock) {
            return isCurrentSnackbarLocked(callback) || isNextSnackbarLocked(callback);
        }
    }

    private static class SnackbarRecord {
        private final WeakReference<Callback> callback;
        private int duration;

        SnackbarRecord(int duration, Callback callback) {
            this.callback = new WeakReference<>(callback);
            this.duration = duration;
        }

        boolean isSnackbar(Callback callback) {
            return callback != null && this.callback.get() == callback;
        }
    }

    private void showNextSnackbarLocked() {
        if (mNextSnackbar != null) {
            mCurrentSnackbar = mNextSnackbar;
            mNextSnackbar = null;

            final Callback callback = mCurrentSnackbar.callback.get();
            if (callback != null) {
                callback.show();
            } else {
                // The callback doesn't exist any more, clear out the Snackbar
                mCurrentSnackbar = null;
            }
        }
    }

    private boolean cancelSnackbarLocked(SnackbarRecord record, int event) {
        final Callback callback = record.callback.get();
        if (callback != null) {
            // Make sure we remove any timeouts for the SnackbarRecord
            mHandler.removeCallbacksAndMessages(record);
            callback.dismiss(event);
            return true;
        }
        return false;
    }

    private boolean isCurrentSnackbarLocked(Callback callback) {
        return mCurrentSnackbar != null && mCurrentSnackbar.isSnackbar(callback);
    }

    private boolean isNextSnackbarLocked(Callback callback) {
        return mNextSnackbar != null && mNextSnackbar.isSnackbar(callback);
    }

    private void scheduleTimeoutLocked(SnackbarRecord r) {
        if (r.duration == Snackbar.LENGTH_INDEFINITE) {
            return;
        }

        int durationMs = LONG_DURATION_MS;
        if (r.duration > 0) {
            durationMs = r.duration;
        } else if (r.duration == Snackbar.LENGTH_SHORT) {
            durationMs = SHORT_DURATION_MS;
        }
        mHandler.removeCallbacksAndMessages(r);
        mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_TIMEOUT, r), durationMs);
    }

    private void handleTimeout(SnackbarRecord record) {
        synchronized (mLock) {
            if (mCurrentSnackbar == record || mNextSnackbar == record) {
                cancelSnackbarLocked(record, Snackbar.Callback.DISMISS_EVENT_TIMEOUT);
            }
        }
    }
}