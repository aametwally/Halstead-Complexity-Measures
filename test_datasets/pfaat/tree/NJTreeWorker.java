// //////////
// BETE BEGIN
// ///////////
package com.neogenesis.pfaat.tree;


import java.io.*;
import java.util.*;

import com.neogenesis.pfaat.*;
import com.neogenesis.pfaat.seqspace.*;


/**
 * Spawns a new thread to do nj tree calculation
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:31:50 $ */
public class NJTreeWorker implements Runnable {

    // call on this when work is complete
    private NJTreeWorkerLauncher launcher;
    private NeighborJoiningTree tree;
    private ProgressDialog progress;

    // call to spawn thread to calculate nj tree
    public static Thread calcNJTree(NJTreeWorkerLauncher launcher,
        NeighborJoiningTree tree, ProgressDialog progress) {
        // create a worker thread and set it going
        NJTreeWorker worker = new NJTreeWorker(launcher, tree, progress);
        Thread t = new Thread(worker);

        t.start();

        return t;
    }

    private NJTreeWorker(NJTreeWorkerLauncher launcher,
        NeighborJoiningTree tree, ProgressDialog progress) {
        // save params
        this.launcher = launcher;
        this.tree = tree;
        this.progress = progress;
    }

    // run in new thread
    public void run() {
        // we cant handle having no launcher
        if (launcher == null)
            return;

        // if bad params do nothing
        if (tree == null || tree.getSeqs() == null || tree.getOptions() == null) {
            launcher.njWorkerFinished(null, null, null, null);
        }

        // do bete calculation and report results when done
        try {
            tree.calcNJTree(progress);
        } catch (Exception e) {
            // report this error
            launcher.njWorkerFinished(null, e, tree.getOptions(), progress);

            // done!
            return;
        }

        // report success (or cancel if tree.toString() returns null)
        launcher.njWorkerFinished(tree.toString(), null, tree.getOptions(), progress);

        // done!
    }
}

// /////////
// BETE END
// /////////
