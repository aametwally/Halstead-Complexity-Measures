package com.neogenesis.pfaat.swingx;


import javax.swing.*;


/**
 * Model for ScrollBar's which do not fire events while adjusting.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:31:09 $ */
public class LazyBoundedRangeModel extends DefaultBoundedRangeModel {
    public int cachedValue;
    public boolean hasCached = false;
    public boolean isLazy = true;

    public LazyBoundedRangeModel(boolean isLazy) {
        super();
        this.isLazy = isLazy;
    }

    public void setLazy(boolean b) {
        isLazy = b;
    }
    
    public void setRangeProperties(int newValue, 
        int newExtent, 
        int newMin, 
        int newMax, 
        boolean adjusting) {
        if (newMin > newMax) newMin = newMax;
        if (newValue > newMax) newMax = newValue;
        if (newValue < newMin) newMin = newValue;
	
        /* Convert the addends to long so that extent can be 
         * Integer.MAX_VALUE without rolling over the sum.
         * A JCK test covers this, see bug 4097718.
         */
        if (((long) newExtent + (long) newValue) > newMax) 
            newExtent = newMax - newValue;
	
        if (newExtent < 0) newExtent = 0;

        boolean isValueChanged = newValue != getValue();
        boolean isAdjustedChanged = adjusting != getValueIsAdjusting();
        boolean isOtherChanged = 
            (newExtent != getExtent()) ||
            (newMin != getMinimum()) ||
            (newMax != getMaximum());

        boolean isChanged;

        if (isOtherChanged) 
            isChanged = true;
        else if (isAdjustedChanged) {
            if (!isValueChanged && hasCached && newValue != cachedValue) {
                hasCached = false;
                newValue = cachedValue;
            }
            isChanged = true;
        } else if (isValueChanged && adjusting) {
            hasCached = true;
            cachedValue = newValue;
            isChanged = false;
        } else 
            isChanged = true;

        if (isChanged || !isLazy) 
            super.setRangeProperties(newValue, newExtent, newMin, newMax, 
                adjusting);
    }
}
