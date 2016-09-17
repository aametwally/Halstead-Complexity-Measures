package com.neogenesis.pfaat.swingx;


import java.awt.*;
import javax.swing.*;


/**
 * JTextField which always uses a fixed width font, good for having sequences
 * line up, etc.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:31:09 $ */
public class FixedWidthTextField extends JTextField {
    private static Font fixed_width_font = new Font("Courier", Font.PLAIN, 12);

    private int width;

    public FixedWidthTextField(String text, int length) {
        // make a little bigger because JTextField doesn't factor in borders
        super(text, length + 1); 
        setFont(fixed_width_font);
        width = getFontMetrics(fixed_width_font).stringWidth("Q");
    }

    public int getCharWidth() {
        return width;
    }
}
