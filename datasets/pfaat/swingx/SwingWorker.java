package com.neogenesis.pfaat.swingx;


// import com.sun.java.swing.SwingUtilities;  //old package name
import javax.swing.SwingUtilities;  // new package name


/**
 * An abstract class that you subclass to perform
 * GUI-related work in a dedicated thread.
 * For instructions on using this class, see 
 * http://java.sun.com/products/jfc/swingdoc-current/threads2.html
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:31:09 $
 */
public abstract class SwingWorker {
    private Object value;  // see getValue(), setValue()
    long startTime, elapsedTime;
    private Thread thread;

    /**
     * Class to maintain reference to current worker thread
     * under separate synchronization control.
     */
    private static class ThreadVar {
        private Thread thread;
        ThreadVar(Thread t) {
            thread = t;
        }

        synchronized Thread get() {
            return thread;
        }

        synchronized void clear() {
            thread = null;
        }
    }

    private ThreadVar threadVar;

    /**
     * Get the value produced by the worker thread, or null if it 
     * hasn't been constructed yet.
     */
    protected synchronized Object getValue() { 
        return value; 
    }

    /**
     * Set the value produced by worker thread 
     */
    private synchronized void setValue(Object x) { 
        value = x; 
    }

    public synchronized long getElapsedTime() { 
        if (elapsedTime >= 0)
            return elapsedTime; 
        return System.currentTimeMillis() - startTime;
    }

    private synchronized void setElapsedTime(long t) {
        elapsedTime = t;
    }

    /**
     * Compute the value to be returned by the <code>get</code> method. 
     */
    public abstract Object construct();

    /**
     * Called on the event dispatching thread (not on the worker thread)
     * after the <code>construct</code> method has returned.
     */
    public void finished() {}

    /**
     * A new method that interrupts the worker thread.  Call this method
     * to force the worker to abort what it's doing.
     */
    public void interrupt() {
        Thread t = threadVar.get();

        if (t != null) {
            t.interrupt();
        }
        threadVar.clear();
    }

    /**
     * Return the value created by the <code>construct</code> method.  
     * Returns null if either the constructing thread or
     * the current thread was interrupted before a value was produced.
     * 
     * @return the value created by the <code>construct</code> method
     */
    public Object get() {
        while (true) {  
            Thread t = threadVar.get();

            if (t == null) {
                return getValue();
            }
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // propagate
                return null;
            }
        }
    }

    /**
     * Start a thread that will call the <code>construct</code> method
     * and then exit.
     */
    public SwingWorker() {
        final Runnable doFinished = new Runnable() {
                public void run() {
                    finished();
                }
            };

        startTime = System.currentTimeMillis();
        elapsedTime = -1;

        Runnable doConstruct = new Runnable() { 
                public void run() {
                    try {
                        setValue(construct());
                    } finally {
                        SwingWorker.this.setElapsedTime(System.currentTimeMillis()
                            - startTime);
                        threadVar.clear();
                    }

                    SwingUtilities.invokeLater(doFinished);
                }
            };

        Thread t = new Thread(doConstruct);

        t.setPriority(Thread.MIN_PRIORITY);
        threadVar = new ThreadVar(t);
        t.start();
    }
}
