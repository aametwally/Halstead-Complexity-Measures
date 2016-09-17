package com.neogenesis.pfaat.swingx;


import java.awt.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;


/**
 * A slider representing a percentage.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:31:09 $ */
public class DoubleSlider extends JSlider {
    double min, diff;
    int nsteps;
    DecimalFormat df;
    
    public DoubleSlider(double min, double max, String format) {
        this(min, max, format, 100, 4, 20);
    }

    public DoubleSlider(double min, double max, String format, 
        int nsteps,
        int major_step,
        int minor_step) {
        super(0, nsteps);
        this.min = min;
        this.nsteps = nsteps;
        diff = max - min;

        setMajorTickSpacing(nsteps / major_step);
        setMinorTickSpacing(nsteps / minor_step);
        Hashtable labelTable = new Hashtable();

        df = new DecimalFormat(format);
        for (int i = 0; i <= major_step; i++)
            labelTable.put(new Integer(i * nsteps / major_step), 
                new JLabel(df.format(min + 
                        diff * 1.0 / major_step * i)));
        setLabelTable(labelTable);
        setPaintTicks(true);
        setPaintLabels(true);
    }

    public double getDouble() { 
        return min + diff * getValue() / nsteps;
    }

    public void setDouble(double x) {
        setValue((int) 
            Math.max(Math.min(Math.round((x - min) / diff * nsteps),
                    nsteps), 0));
    }

    public class LabelChangeListener implements ChangeListener {
        JLabel label;
        public LabelChangeListener(JLabel label) { 
            this.label = label; 
            label.setText(df.format(getDouble()));
        }

        public void stateChanged(ChangeEvent e) {
            if (e.getSource() == DoubleSlider.this) 
                label.setText(df.format(getDouble()));
        }
    }

    public void addValueLabel(JLabel label) {
        addChangeListener(new LabelChangeListener(label));
    }

}

