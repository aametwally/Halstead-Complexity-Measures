package com.neogenesis.pfaat;


/**
 * Listener for editing events from an <code>Alignment</code>.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:03 $ */
public interface AlignmentListener {
    public void alignmentNameChanged(Alignment align);
    public void alignmentSeqInserted(Alignment align, int i);
    public void alignmentSeqDeleted(Alignment align, int i, Sequence aaseq);
    public void alignmentSeqSwapped(Alignment align, int i, int j);
    public void alignmentSeqAAChanged(Alignment align, Sequence aaseq);
}    
