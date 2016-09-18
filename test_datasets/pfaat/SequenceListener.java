package com.neogenesis.pfaat;


/**
 * Listener for detecting changes to a sequence.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:02 $ */
public interface SequenceListener {
    public void sequenceNameChanged(Sequence aaseq, String old_name) 
        throws Exception;
    public void sequenceAAChanged(Sequence aaseq);
    public void sequenceAnnotationChanged(Sequence aaseq);
    public void sequenceGroupChanged(Sequence aaseq);
    public void sequenceLineAnnotationsChanged(Sequence aaseq);
    public void sequenceColumnAnnotationsChanged(Sequence aaseq, int column);
    public void sequenceColorChanged(Sequence aasq);
}
