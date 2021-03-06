package app.subversive.groceryratings;

import android.os.Handler;
import java.util.HashSet;

/**
 * Created by rob on 10/6/14.
 */
public class ManagedTimer {
    public static class RunnableController {
        final long delay;
        final RunWrapper wrapper;

        private RunnableController(RunWrapper wrapper, long delay) {
            this.delay = delay;
            this.wrapper = wrapper;
        }

        public void cancel() {
            cancelRunnable(wrapper);
        }

        public void restart() {
            cancel();
            postDelayed(wrapper, delay);
        }

        public boolean isPending() { return wrapper.isPending; }
    }

    private static class RunWrapper implements Runnable {
        final Runnable innerRunnable;
        private boolean isPending;
        public RunWrapper (Runnable r) {
            innerRunnable = r;
            isPending = false;
        }

        @Override
        public void run() {
            innerRunnable.run();
            cancelRunnable(this);
        }
    }

    private static final HashSet<RunWrapper> wrappers =
        new HashSet<RunWrapper>();
    private static final Handler handler = new Handler();

    public static RunnableController getController(Runnable r, Long delay) {
        RunWrapper wrapper = new RunWrapper(r);
        return new RunnableController(wrapper, delay);
    }


    private static void postDelayed(RunWrapper w, Long delay) {
        wrappers.add(w);
        handler.postDelayed(w, delay);
        w.isPending = true;
    }

    public static synchronized void cancelAll() {
        for (RunWrapper wrapper : wrappers) {
            handler.removeCallbacks(wrapper);
            wrapper.isPending = false;
        }
    }

    private static synchronized void cancelRunnable(RunWrapper w) {
       if (wrappers.remove(w)) {
           handler.removeCallbacks(w);
           w.isPending = false;
       }
    }
}
