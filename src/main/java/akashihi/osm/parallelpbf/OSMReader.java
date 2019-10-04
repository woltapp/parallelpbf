package akashihi.osm.parallelpbf;

import java.util.concurrent.Semaphore;

public abstract class OSMReader implements Runnable {
    private final byte[] blob;
    private final Semaphore tasksLimiter;

    public OSMReader(byte[] blob, Semaphore tasksLimiter) {
        this.blob = blob;
        this.tasksLimiter = tasksLimiter;
    }

    @Override
    public void run() {
        try {
            this.read();
        } finally {
            tasksLimiter.release();
        }
    }

    protected abstract void read();
}
