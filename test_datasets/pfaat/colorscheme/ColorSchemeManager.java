package com.neogenesis.pfaat.colorscheme;


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;

import com.neogenesis.pfaat.*;
import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.util.*;


/**
 * Controls color scheme menus, selection, etc.
 *
 * @author $Author: xih $
 * @version $Revision: 1.3 $, $Date: 2002/10/11 18:28:34 $ */
public class ColorSchemeManager implements DisplayPropertiesListener {
    private DisplayProperties display_props;
    private ButtonGroup cs_bg;
    private JMenu color_scheme_menu;
    private JRadioButtonMenuItem[] cs_stored_menu_item;
    private ResidueColorScheme[] cs_stored;
    private JMenuItem cs_blosum_menu_item, cs_pid_menu_item, cs_default_menu_item,
        cs_user_menu_item;
    private AlignmentFrame owner;

    public ColorSchemeManager(AlignmentFrame owner,
        JMenu menu,
        DisplayProperties dp) {
        this.owner = owner;
        this.color_scheme_menu = menu;
        setDisplayProperties(dp);

        JMenuItem cs_refresh_menu_item = new JMenuItem("Refresh List");

        cs_refresh_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    refreshColorSchemes();
                }
            }
        );

        if (!owner.isAppletMode()) { // do not show this if it is in applet mode
            color_scheme_menu.add(cs_refresh_menu_item);
            color_scheme_menu.addSeparator();
        }

        cs_bg = new ButtonGroup();

        cs_blosum_menu_item = new JRadioButtonMenuItem("Blosum62", false);
        cs_blosum_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    display_props.setColorScheme(new
                        Blosum62ColorScheme(ColorSchemeManager.this.owner.getConsensus()));
                }
            }
        );
        cs_bg.add(cs_blosum_menu_item);
        color_scheme_menu.add(cs_blosum_menu_item);
        cs_pid_menu_item =
                new JRadioButtonMenuItem("PID", false);
        cs_pid_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    display_props.setColorScheme(new
                        PIDColorScheme(ColorSchemeManager.this.owner.getConsensus()));
                }
            }
        );
        cs_bg.add(cs_pid_menu_item);
        color_scheme_menu.add(cs_pid_menu_item);

        cs_default_menu_item =
                new JRadioButtonMenuItem("Default", false);
        cs_default_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    display_props.setColorScheme(new
                        DefaultColorScheme(ColorSchemeManager.this.owner.getConsensus()));
                }
            }
        );
        cs_bg.add(cs_default_menu_item);
        color_scheme_menu.add(cs_default_menu_item);

        color_scheme_menu.addSeparator();
        cs_user_menu_item =
                new JRadioButtonMenuItem("User Defined", false);
        cs_user_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    ColorSchemeDialog d =
                        new ColorSchemeDialog(ColorSchemeManager.this.owner,
                            display_props,
                            ColorSchemeManager.this);

                    d.show();
                }
            }
        );
        cs_bg.add(cs_user_menu_item);
        color_scheme_menu.add(cs_user_menu_item);

        cs_stored_menu_item = null;
        refreshColorSchemes();
    }

    public void refreshColorSchemes() {
        // for applet do nothing
        if (owner.isAppletMode())
            return;

        if (cs_stored_menu_item != null) {
            for (int i = 0; i < cs_stored_menu_item.length; i++) {
                JRadioButtonMenuItem b = cs_stored_menu_item[i];

                cs_bg.remove(b);
                color_scheme_menu.remove(b);
            }
            cs_stored_menu_item = null;
        }

        File[] f = PathManager.getColorSchemeDirectory().listFiles();
        javax.swing.filechooser.FileFilter fl =
            ResidueColorScheme.getFileFilter();
        ArrayList csl = new ArrayList(f.length);
        ArrayList names = new ArrayList(f.length);

        for (int i = 0; i < f.length; i++) {
            if (fl.accept(f[i])) {
                ResidueColorScheme cs;

                try {
                    cs = new ResidueColorScheme(null, f[i]);
                } catch (Exception e) {
                    ErrorDialog.showErrorDialog(owner,
                        "Unable to load color scheme "
                        + f[i].getAbsolutePath()
                        + ": ",
                        e);
                    continue;
                }
                csl.add(cs);
                names.add(Utils.getBaseFileName(f[i]));
            }
        }
        ActionListener cs_listener = new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    for (int i = cs_stored_menu_item.length - 1; i >= 0; i--) {
                        if (evt.getSource() == cs_stored_menu_item[i]) {
                            ResidueColorScheme cs = cs_stored[i];

                            display_props.setColorScheme(new
                                ResidueColorScheme(owner.getConsensus(),
                                    cs.getPID(),
                                    cs.getColorTable(),
                                    cs.getName()));
                        }
                    }
                }
            };

        cs_stored = new ResidueColorScheme[csl.size()];
        cs_stored_menu_item = new JRadioButtonMenuItem[csl.size()];
        for (int i = 0; i < cs_stored.length; i++) {
            cs_stored[i] = (ResidueColorScheme) csl.get(i);
            String name = (String) names.get(i);

            cs_stored_menu_item[i] =
                    new JRadioButtonMenuItem(name, false);
            cs_stored_menu_item[i].addActionListener(cs_listener);
            cs_bg.add(cs_stored_menu_item[i]);
            color_scheme_menu.add(cs_stored_menu_item[i]);
        }
    }

    public void setDisplayProperties(DisplayProperties dp) {
        if (display_props != null)
            display_props.removeListener(this);
        display_props = dp;
        if (display_props != null)
            display_props.addListener(this);
        setColorSchemeSelection();
    }

    // set a user defined color scheme
    public void  setUserDefinedColorScheme(ResidueColorScheme cs) {
        display_props.setColorScheme(new
            ResidueColorScheme(owner.getConsensus(),
                cs.getPID(),
                cs.getColorTable(),
                cs.getName()));
    }

    public void setColorSchemeSelection() {
        if (display_props == null) return;
        ColorScheme cs = display_props.getColorScheme();

        if (cs == null) return;

        cs_blosum_menu_item.setSelected(false);
        cs_pid_menu_item.setSelected(false);
        cs_user_menu_item.setSelected(false);
        if (cs_stored_menu_item != null) { // just a sanity check
            for (int i = cs_stored_menu_item.length - 1; i >= 0; i--)
                cs_stored_menu_item[i].setSelected(false);
        }

        if (cs instanceof Blosum62ColorScheme)
            cs_blosum_menu_item.setSelected(true);
        else if (cs instanceof PIDColorScheme)
            cs_pid_menu_item.setSelected(true);
        else if (cs instanceof DefaultColorScheme)
            cs_default_menu_item.setSelected(true);
        else if (cs instanceof ResidueColorScheme) {
            String name = cs.getName();
            boolean found = false;

            if (cs_stored_menu_item != null) {
                for (int i = cs_stored_menu_item.length - 1; i >= 0; i--) {
                    if (cs_stored_menu_item[i].getText().equals(name)) {
                        cs_stored_menu_item[i].setSelected(true);
                        found = true;
                        break;
                    }
                }
            }
            if (!found)
                cs_user_menu_item.setSelected(true);
        }
    }

    // DisplayProperties listener
    public void displayAnnViewChanged(DisplayProperties dp,
        Sequence seq,
        boolean show) {}

    public void displaySeqSelectChanged(DisplayProperties dp,
        Sequence seq,
        boolean select) {}

    public void displayColorSchemeChanged(DisplayProperties dp,
        ColorScheme old) {
        if (dp != display_props)
            throw new
                RuntimeException("bound to incorrect display properties");
        setColorSchemeSelection();
    }

    public void displayFontChanged(DisplayProperties dp) {}

    public void displayRenderGapsChanged(DisplayProperties dp) {}

    public void displayGroupEditingChanged(DisplayProperties dp) {}

    public void displayOverwriteChanged(DisplayProperties dp) {}

    public void displayHighlightsChanged(DisplayProperties dp,
        Sequence seq) {}

    public void displayHighlightsChanged(DisplayProperties dp,
        Sequence[] seqs) {}

}
