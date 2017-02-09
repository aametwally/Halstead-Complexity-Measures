package com.neogenesis.pfaat.util;


import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;

import com.neogenesis.pfaat.swingx.*;


/**
 * Do a task in a seperate thread with a progress bar.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:32:06 $ */
public abstract class ProgressWorker 
    implements IterationListener, ActionListener {
    int lastiter;
    boolean cancel, done;
    Exception exp;
    
    ProgressMonitor fprogress;
    Timer timer;
    DecimalFormat df;

    SwingWorker worker;
 
    public ProgressWorker() {
        timer = new Timer(200, this);
        fprogress = new ProgressMonitor(null,
                    "",
                    null,
                    0, 100);
        fprogress.setProgress(0);
        // fprogress.setMillisToDecideToPopup(0);
        // fprogress.setMillisToPopup(0);
        df = new DecimalFormat("00");
    }

    public void go() {
        timer.start();
        lastiter = 0;
        cancel = false;
        done = false;
        exp = null;

        fprogress.setNote(getNote());

        worker = new SwingWorker() {
                    public Object construct() {
                        Object obj = null;

                        try {
                            obj = ProgressWorker.this.construct();
                        } catch (Exception e) {
                            exp = e;
                        }
                        return obj;
                    }

                    public void finished() {
                        done = true;
                        if (exp != null) 
                            ErrorDialog.showErrorDialog(null,
                                getErrorMessage(),
                                exp);
                        ProgressWorker.this.finished(getValue());
                    }
                };
    }

    public long getElapsedTime() {
        return worker.getElapsedTime();
    }

    public void finishedThisIteration(int i) throws Exception {
        if (cancel)
            throw new Exception("process is cancelled");
        lastiter = i;
    }

    public void finishedThisIteration() throws Exception {
        if (cancel)
            throw new Exception("process is cancelled");
        lastiter++;
    }

    public void setTotalIterations(int i) {
        fprogress.setMaximum(i);
    }

    public int getTotalIterations() {
        return fprogress.getMaximum();
    }

    public void actionPerformed(ActionEvent evt) {
        if (done || fprogress.isCanceled()) {
            if (fprogress.isCanceled())
                cancel = true;
            fprogress.close();
            Toolkit.getDefaultToolkit().beep();
            timer.stop();
            timer = null;
        } else {
            fprogress.setProgress(lastiter);
            if (lastiter > 0) {
                double elapsed = getElapsedTime() / 1000.0;
                int elapsed_min = (int) Math.floor(elapsed / 60.0);
                int elapsed_sec = (int) Math.round(elapsed - elapsed_min * 60.0);
                double left = elapsed 
                    * (getTotalIterations() - lastiter) / lastiter;
                int left_min = (int) Math.floor(left / 60.0);
                int left_sec = (int) Math.round(left - left_min * 60.0);

                fprogress.setNote(getNote() + " Elapsed: " 
                    + df.format(elapsed_min) + ":" 
                    + df.format(elapsed_sec)
                    + " Remaining: "
                    + df.format(left_min) + ":" 
                    + df.format(left_sec));
            }
        }
    }

    // to be implemented by subclasses
    public abstract Object construct() throws Exception;

    public abstract void finished(Object constructed);

    public abstract String getNote();

    public abstract String getErrorMessage();
}

