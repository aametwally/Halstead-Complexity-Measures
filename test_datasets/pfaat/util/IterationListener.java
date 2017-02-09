package com.neogenesis.pfaat.util;


/**
 * An interface class for callbacks during a long computation, in
 * order to update on the state of progress.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:32:06 $ */
public interface IterationListener {
    public void finishedThisIteration() throws Exception;
}
