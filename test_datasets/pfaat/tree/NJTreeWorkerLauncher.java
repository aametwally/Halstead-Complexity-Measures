// ///////////
// BETE BEGIN
// ///////////
package com.neogenesis.pfaat.tree;


import com.neogenesis.pfaat.*;


/**
 * This class has method that is called when NJTreeWorker has finished
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:31:50 $ */
public interface NJTreeWorkerLauncher {
    // this method called by worker when work is complete.
    // if there was a failure or calc was canceleed then tree will be null
    // if there was an exception then e will be non null
    public void njWorkerFinished(String tree, Exception e,
        NJOptions options, ProgressDialog progress);
}

// /////////
// BETE END
// /////////
