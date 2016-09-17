package com.neogenesis.pfaat.swingx;


import java.awt.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;


/**
 * A slider representing integers.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:31:09 $ */
public class IntSlider extends JSlider {
    public IntSlider(int min, int max, int value) {
        super(min, max, value);

        setMajorTickSpacing(10);
        setMinorTickSpacing(1);

        setLabelTable(createStandardLabels(10));
        setPaintTicks(true);
        setPaintLabels(true);
    }

    public int getInt() { 
        return getValue();
    }

    public void setInt(int x) {
        setValue(x);
    }

    public class LabelChangeListener implements ChangeListener {
        JLabel label;
        public LabelChangeListener(JLabel label) { 
            this.label = label; 
            label.setText(Integer.toString(getInt()));
        }

        public void stateChanged(ChangeEvent e) {
            if (e.getSource() == IntSlider.this) 
                label.setText(Integer.toString(getInt()));
        }
    }

    public void addValueLabel(JLabel label) {
        addChangeListener(new LabelChangeListener(label));
        label.setText(Integer.toString(getInt()));
    }

}

