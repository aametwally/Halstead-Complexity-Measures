package com.neogenesis.pfaat.print;


import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;

import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.util.*;


/**
 * Dialog for printing.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:30:23 $ */
public class PrintPSDialog extends JDialog implements ActionListener {
    // controls
    private JButton print_button, preview_button, cancel_button;
    private JButton browse_button;
    private JTextField path_field;
    private JComboBox paper_box;
    private JRadioButton portrait_button, landscape_button;
    private JRadioButton no_scale_button, shrink_scale_button, 
        fit_scale_button;
    private JRadioButton standard_media_button, roll_media_button;
    private JLabel page_size_label;

    // components to be printed
    private PrintGrid printable;

    public PrintPSDialog(Frame owner, PrintGrid printable) {
        super(owner, "Print Postscript");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        this.printable = printable;

        getContentPane().setLayout(new BorderLayout());

        JPanel subpanel;
        ButtonGroup bg;

        JPanel label_panel = new JPanel();

        label_panel.setLayout(new GridLayout(0, 1));
        JPanel field_panel = new JPanel();

        field_panel.setLayout(new GridLayout(0, 1));
	
        label_panel.add(new JLabel("Paper Type: "));
        String[] paper_types = new String[PSPageFormat.allFormats.length];

        for (int i = paper_types.length - 1; i >= 0; i--)
            paper_types[i] = PSPageFormat.allFormats[i].getName();
        paper_box = new JComboBox(paper_types);
        paper_box.setSelectedIndex(0);
        paper_box.addActionListener(this);
        field_panel.add(paper_box);

        label_panel.add(new JLabel("Orientation: "));
        subpanel = new JPanel();
        bg = new ButtonGroup();
        portrait_button = new JRadioButton("Portrait", false);
        portrait_button.addActionListener(this);
        bg.add(portrait_button);
        subpanel.add(portrait_button);
        landscape_button = new JRadioButton("Landscape", true);
        landscape_button.addActionListener(this);
        bg.add(landscape_button);
        subpanel.add(landscape_button);
        field_panel.add(subpanel);

        label_panel.add(new JLabel("Scaling: "));
        subpanel = new JPanel();
        bg = new ButtonGroup();
        no_scale_button = new JRadioButton("None");
        no_scale_button.addActionListener(this);
        bg.add(no_scale_button);
        subpanel.add(no_scale_button);
        shrink_scale_button = new JRadioButton("Shrink If Necessary", true);
        shrink_scale_button.addActionListener(this);
        bg.add(shrink_scale_button);
        subpanel.add(shrink_scale_button);
        fit_scale_button = new JRadioButton("To Fit");
        fit_scale_button.addActionListener(this);
        bg.add(fit_scale_button);
        subpanel.add(fit_scale_button);
        field_panel.add(subpanel);

        label_panel.add(new JLabel("Media Type: "));
        subpanel = new JPanel();
        bg = new ButtonGroup();
        standard_media_button = new JRadioButton("Standard", true);
        standard_media_button.addActionListener(this);
        bg.add(standard_media_button);
        subpanel.add(standard_media_button);
        roll_media_button = new JRadioButton("Roll", false);
        roll_media_button.addActionListener(this);
        bg.add(roll_media_button);
        subpanel.add(roll_media_button);
        field_panel.add(subpanel);

        label_panel.add(new JLabel("Page Size: "));
        subpanel = new JPanel();
        page_size_label = new JLabel();
        subpanel.add(page_size_label);
        field_panel.add(subpanel);

        label_panel.add(new JLabel("Output File: "));
        subpanel = new JPanel();
        path_field = new JTextField(50);
        subpanel.add(path_field);
        browse_button = new JButton("Browse");
        browse_button.addActionListener(this);
        subpanel.add(browse_button);
        field_panel.add(subpanel);

        getContentPane().add(label_panel, BorderLayout.WEST);
        getContentPane().add(field_panel, BorderLayout.EAST);

        subpanel = new JPanel();
        print_button = new JButton("Print");
        print_button.addActionListener(this);
        subpanel.add(print_button);
        preview_button = new JButton("Preview");
        preview_button.addActionListener(this);
        subpanel.add(preview_button);
        cancel_button = new JButton("Cancel");
        cancel_button.addActionListener(this);
        subpanel.add(cancel_button);

        getContentPane().add(subpanel, BorderLayout.SOUTH);

        setLocationRelativeTo(owner);
        updatePageSizeLabel();
        pack();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancel_button) 
            dispose();
        else if (e.getSource() == print_button) {
            if (printToFile())
                dispose();
        } else if (e.getSource() == browse_button) {
            JFileChooser fc = new JFileChooser(PathManager.getAlignmentPath());

            fc.setDialogType(JFileChooser.SAVE_DIALOG);
            if (fc.showDialog(this, "Select Output") 
                == JFileChooser.APPROVE_OPTION) {
                PathManager.setAlignmentPath(fc.getCurrentDirectory());
                path_field.setText(fc.getSelectedFile().getAbsolutePath());
            }
        } else if (e.getSource() == preview_button) {
            PrintPreview pp = new PrintPreview(this);

            pp.show();
        } else if (e.getSource() == paper_box
            || e.getSource() == portrait_button
            || e.getSource() == landscape_button
            || e.getSource() == no_scale_button
            || e.getSource() == shrink_scale_button
            || e.getSource() == standard_media_button
            || e.getSource() == roll_media_button) 
            updatePageSizeLabel();
    }

    private void updatePageSizeLabel() {
        double scale = getPageScale();
        PSPageFormat format = getPSPageFormat(scale);

        page_size_label.setText(format.getDimString());
    }

    private double getPageScale() {
        PSPageFormat format = 
            PSPageFormat.allFormats[paper_box.getSelectedIndex()];

        if (landscape_button.isSelected())
            format = format.toLandscape();

        boolean is_x_scaled = true;

        if (roll_media_button.isSelected()) 
            is_x_scaled = !landscape_button.isSelected(); 
	
        double scale = 1.0;
        double scale_fit_x = ((double) format.getWidth()) 
            / ((double) printable.getWidth());
        double scale_fit_y = ((double) format.getHeight()) 
            / ((double) printable.getHeight());
	
        if (shrink_scale_button.isSelected()) {
            if (roll_media_button.isSelected()) {
                if (is_x_scaled && scale_fit_x < 1.0)
                    scale = scale_fit_x;
                else if (!is_x_scaled && scale_fit_y < 1.0)
                    scale = scale_fit_y;
            } else if (scale_fit_x < 1.0 || scale_fit_y < 1.0)
                scale = Math.min(scale_fit_x, scale_fit_y);
        } else if (fit_scale_button.isSelected()) {
            if (roll_media_button.isSelected()) {
                if (is_x_scaled)
                    scale = scale_fit_x;
                else 
                    scale = scale_fit_y;
            } else
                scale = Math.min(scale_fit_x, scale_fit_y);
        }

        return scale;
    }

    private PSPageFormat getPSPageFormat(double scale) {
        PSPageFormat format = 
            PSPageFormat.allFormats[paper_box.getSelectedIndex()];

        if (landscape_button.isSelected())
            format = format.toLandscape();

        if (roll_media_button.isSelected()) {
            if (landscape_button.isSelected())
                format = 
                        format.fitToWidth((int) Math.ceil(printable.getWidth() 
                                * scale));
            else
                format = 
                        format.fitToHeight((int) Math.ceil(printable.getHeight() 
                                * scale));
        }

        return format;
    }

    // try to print, return false on error
    private boolean printToFile() {
        // open a file for output
        FileWriter out;
        String file = path_field.getText();

        if (file.length() < 1) {
            JOptionPane.showMessageDialog(this,
                "Please enter an output file name.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE); 	    
            return false;
        }
        try {
            out = new FileWriter(file);
        } catch (Exception e) {
            ErrorDialog.showErrorDialog(this, 
                "Error opening " 
                + file 
                + " for output.",
                e);
            return false;
        }

        double scale = getPageScale();
        PSPageFormat format = getPSPageFormat(scale);

        Graphics gr = new PSGraphics(out, format, scale, scale);

        renderPrint(gr);
        gr.dispose();

        try {
            out.close();
        } catch (Exception e) {
            ErrorDialog.showErrorDialog(this, 
                "Error closing " 
                + file 
                + " for output.",
                e);
            return false;
        }

        return true;
    }

    private void renderPrint(Graphics gr) {
        for (int i = printable.getComponentCount() - 1; i >= 0; i--) {
            Object c = printable.getComponent(i);
            Point pos = printable.getPosition(c);

            if (c instanceof Image) 
                embedImage(gr, (Image) c, pos.x, pos.y);
            else
                embedComponent(gr, (Component) c, pos.x, pos.y);
        }
    }

    private static void embedImage(Graphics gr, Image im,
        int x, int y) {
        gr.drawImage(im, x, y, null);
    }

    private static void embedComponent(Graphics gr, Component c, 
        int x, int y) {
        setDoubleBufferingEnabled(c, false);
        Graphics this_gr = gr.create();

        this_gr.translate(x, y);
        c.paint(this_gr);
        this_gr.dispose();
        setDoubleBufferingEnabled(c, true);
    }

    private static Frame getFrameForComponent(Component c) {
        if (c == null) 
            return null;
        if (c instanceof Frame)
            return (Frame) c;
        return getFrameForComponent(c.getParent());
    }
	
    private static void setDoubleBufferingEnabled(Component c, boolean state) {
        RepaintManager currentManager = 
            RepaintManager.currentManager(c);

        currentManager.setDoubleBufferingEnabled(state);
    }

    // for print preview
    private static class PagePreview extends JPanel {
        protected int m_w;
        protected int m_h;
        protected Image m_source;
        protected Image m_img;

        public PagePreview(int w, int h, Image source) {
            m_w = w;
            m_h = h;
            m_source = source;
            if (m_source.getHeight(null) != m_h 
                || m_source.getWidth(null) != m_w) 
                m_img = m_source.getScaledInstance(m_w, m_h, Image.SCALE_FAST);
            else
                m_img = m_source;
            setBackground(Color.white);
            setBorder(new MatteBorder(1, 1, 2, 2, Color.black));
        }

        public void setScaledSize(int w, int h) {
            m_w = w;
            m_h = h;
            if (m_source.getHeight(null) != m_h 
                || m_source.getWidth(null) != m_w) 
                m_img = m_source.getScaledInstance(m_w, m_h, Image.SCALE_SMOOTH);
            else
                m_img = m_source;
            revalidate();
            repaint();
        }

        public Dimension getPreferredSize() {
            Insets ins = getInsets();

            return new Dimension(m_w + ins.left + ins.right, 
                    m_h + ins.top + ins.bottom);
        }

        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        public void paint(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            g.drawImage(m_img, 0, 0, this);
            paintBorder(g);
        }
    }


    private static class PrintPreview extends JFrame implements ActionListener {
        private JButton close_button;
        private JComboBox scale_box;
        private PagePreview pp;
        private int height, width;

        public PrintPreview(PrintPSDialog target) {
            super("Print Preview");
            setSize(600, 400);
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            getContentPane().setLayout(new BorderLayout());

            JPanel subpanel = new JPanel();
            String[] scales = { "10%", "25%", "50%", "100%" };

            scale_box = new JComboBox(scales);
            scale_box.setSelectedIndex(3);
            scale_box.addActionListener(this);
            subpanel.add(scale_box);
            close_button = new JButton("Close");
            close_button.addActionListener(this);
            subpanel.add(close_button);
            getContentPane().add(subpanel, BorderLayout.NORTH);

            double page_scale = target.getPageScale();
            PSPageFormat format = target.getPSPageFormat(page_scale);

            width = format.getPageWidth();
            height = format.getPageHeight();
            BufferedImage img = new BufferedImage(width,
                    height,
                    BufferedImage.TYPE_INT_RGB);
            Graphics g = img.getGraphics();

            // for some reason, if we don't set an initial clip area,
            // jdk1.2.2 dies later in JComponent::paintChildren
            g.setClip(0, 0, width, height);

            g.setColor(Color.white);
            g.fillRect(0, 0, width, height);
            g.translate(format.getLeftMargin(), format.getTopMargin());
            ((Graphics2D) g).scale(page_scale, page_scale);
            target.renderPrint(g);
            g.dispose();
            pp = new PagePreview(width, height, img);

            subpanel = new JPanel();
            subpanel.setLayout(new FlowLayout());
            subpanel.add(pp); // , BorderLayout.CENTER);
	    
            JScrollPane jsp = new JScrollPane(subpanel);

            getContentPane().add(jsp, BorderLayout.CENTER);

        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == close_button)
                dispose();
            else if (e.getSource() == scale_box) {
                String scale_str = scale_box.getSelectedItem().toString();
                double scale = 
                    Double.parseDouble(scale_str.substring(0, 
                            scale_str.length() 
                            - 1)) / 100.0;

                pp.setScaledSize((int) (scale * (double) width),
                    (int) (scale * (double) height));
            }
        }
    }

}

