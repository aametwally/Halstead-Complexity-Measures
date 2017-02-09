package com.neogenesis.pfaat.colorscheme;


import java.awt.Color;

import com.neogenesis.pfaat.Sequence;


/**
 * Defines colors for sequence display.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:34 $ */
public interface ColorScheme {
    public String getName();
    public Color getForegroundColor(Sequence s, int res_idx);
    public Color getBackgroundColor(Sequence s, int res_idx);
    public void addListener(ColorSchemeListener l);
    public void removeListener(ColorSchemeListener l);
}
