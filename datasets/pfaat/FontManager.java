package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.neogenesis.pfaat.colorscheme.ColorScheme;


/**
 * Controls fint menus, selection, etc.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:02 $ */
public class FontManager implements DisplayPropertiesListener {
    private DisplayProperties display_props;
    private JRadioButtonMenuItem[] font_name_menu_item;
    private JRadioButtonMenuItem[] font_style_menu_item;
    private JRadioButtonMenuItem[] font_size_menu_item;
    private JCheckBoxMenuItem fast_render_menu_item;
    private JCheckBoxMenuItem render_gaps_menu_item;
    private AlignmentFrame owner;
    private JMenu font_family_menu, font_style_menu, font_size_menu;

    public FontManager(AlignmentFrame owner, 
        JMenu menu, 
        DisplayProperties dp) {
        this.owner = owner;
        setDisplayProperties(dp);

        font_family_menu = new JMenu("Font Family");
        loadFontFamilies();
        menu.add(font_family_menu);

        font_style_menu = new JMenu("Font Style");
        loadFontStyles();
        menu.add(font_style_menu);

        font_size_menu = new JMenu("Font Size");
        loadFontSizes();
        menu.add(font_size_menu);

        menu.addSeparator();
        fast_render_menu_item = 
                new JCheckBoxMenuItem("Fast Render", false);
        fast_render_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (fast_render_menu_item.getState()) {
                        Font f = getFont();

                        f = new Font("Courier", f.getStyle(), f.getSize());
                        display_props.setFont(f,
                            FontManager.this.owner.getFontMetrics(f),
                            true);
                    } else {
                        Font f = getFont();

                        display_props.setFont(f,
                            FontManager.this.owner.getFontMetrics(f),
                            false);
                    }
                }
            }
        );
        menu.add(fast_render_menu_item);	

        render_gaps_menu_item = 
                new JCheckBoxMenuItem("Render Gaps", false);
        render_gaps_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    display_props.setRenderGaps(render_gaps_menu_item.getState());
                }
            }
        );
        menu.add(render_gaps_menu_item);

        setFontSelection();
    }

    // load names of known font families
    private void loadFontFamilies() {
        // GraphicsEnvironment gEnv =
        // GraphicsEnvironment.getLocalGraphicsEnvironment();
        // String envfonts[] = gEnv.getAvailableFontFamilyNames();
        // font_name_menu_item = new JRadioButtonMenuItem [envfonts.length];
        // ButtonGroup bg = new ButtonGroup();
        // boolean selected = false;
        // for (int i = 0; i < envfonts.length; i++) {
        // font_name_menu_item[i] = 
        // new JRadioButtonMenuItem(envfonts[i], false);
        // font_name_menu_item[i].addActionListener(this);
        // bg.add(font_name_menu_item[i]);
        // font_family_menu.add(font_name_menu_item[i]);
        // if (!selected && envfonts[i].equals("SansSerif")) {
        // font_name_menu_item[i].setSelected(true);
        // selected = true;
        // }
        // }
        // if (selected == false)
        // font_name_menu_item[0].setSelected(true);

        font_name_menu_item = new JRadioButtonMenuItem[3];
        ButtonGroup bg = new ButtonGroup();
        int cnt = 0;

        ActionListener font_listener = new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    Font font = getFont();

                    display_props.setFont(font, 
                        FontManager.this.owner.getFontMetrics(font),
                        fast_render_menu_item.getState());
                }
            };

        font_name_menu_item[cnt] = new JRadioButtonMenuItem("SansSerif", true);
        font_name_menu_item[cnt].addActionListener(font_listener);
        bg.add(font_name_menu_item[cnt]);
        font_family_menu.add(font_name_menu_item[cnt]);
        cnt++;

        font_name_menu_item[cnt] = new JRadioButtonMenuItem("Courier", false);
        font_name_menu_item[cnt].addActionListener(font_listener);
        bg.add(font_name_menu_item[cnt]);
        font_family_menu.add(font_name_menu_item[cnt]);
        cnt++;

        font_name_menu_item[cnt] = new JRadioButtonMenuItem("Times New Roman", 
                    false);
        font_name_menu_item[cnt].addActionListener(font_listener);
        bg.add(font_name_menu_item[cnt]);
        font_family_menu.add(font_name_menu_item[cnt]);
        cnt++;
	
    }

    // load names of font sizes
    private void loadFontStyles() {
        font_style_menu_item = new JRadioButtonMenuItem[3];
        ButtonGroup bg = new ButtonGroup();
        int cnt = 0;
        ActionListener font_listener = new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    Font font = getFont();

                    display_props.setFont(font, 
                        FontManager.this.owner.getFontMetrics(font),
                        fast_render_menu_item.getState());
                }
            };

        font_style_menu_item[cnt] = new JRadioButtonMenuItem("Plain", true);
        font_style_menu_item[cnt].addActionListener(font_listener);
        bg.add(font_style_menu_item[cnt]);
        font_style_menu.add(font_style_menu_item[cnt++]);
        font_style_menu_item[cnt] = new JRadioButtonMenuItem("Bold", false);
        font_style_menu_item[cnt].addActionListener(font_listener);
        bg.add(font_style_menu_item[cnt]);
        font_style_menu.add(font_style_menu_item[cnt++]);
        font_style_menu_item[cnt] = new JRadioButtonMenuItem("Italic", false);
        font_style_menu_item[cnt].addActionListener(font_listener);
        bg.add(font_style_menu_item[cnt]);
        font_style_menu.add(font_style_menu_item[cnt++]);
    }

    // load names of font sizes
    private void loadFontSizes() {
        int[] sizes = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 18, 20 };

        font_size_menu_item = new JRadioButtonMenuItem[sizes.length];
        ActionListener font_listener = new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    Font font = getFont();

                    display_props.setFont(font, 
                        FontManager.this.owner.getFontMetrics(font),
                        fast_render_menu_item.getState());
                }
            };
        ButtonGroup bg = new ButtonGroup();

        for (int i = 0; i < sizes.length; i++) {
            font_size_menu_item[i] = 
                    new JRadioButtonMenuItem(Integer.toString(sizes[i]), false);
            font_size_menu_item[i].addActionListener(font_listener);
            bg.add(font_size_menu_item[i]);
            font_size_menu.add(font_size_menu_item[i]);
        }
        font_size_menu_item[3].setSelected(true);
    }

    public Font getFont() {
        String font_name = null;

        for (int i = font_name_menu_item.length - 1; i >= 0; i--) {
            if (font_name_menu_item[i].isSelected()) {
                font_name = font_name_menu_item[i].getText();
                break;
            }
        }
        int font_size = -1;

        for (int i = font_size_menu_item.length - 1; i >= 0; i--) {
            if (font_size_menu_item[i].isSelected()) {
                font_size = 
                        Integer.valueOf(font_size_menu_item[i].getText()).intValue();
                break;
            }
        }
        int font_style = Font.PLAIN;

        for (int i = font_style_menu_item.length - 1; i >= 0; i--) {
            if (font_style_menu_item[i].isSelected()) {
                String style = font_style_menu_item[i].getText();

                if (style.equals("Plain"))
                    font_style = Font.PLAIN;
                else if (style.equals("Bold"))
                    font_style = Font.BOLD;
                else if (style.equals("Italic"))
                    font_style = Font.ITALIC;
                else
                    throw new RuntimeException("unknown font style: " + style);
                break;
            }
        }

        return new Font(font_name, font_style, font_size);
    }
    
    public void setDisplayProperties(DisplayProperties dp) {
        if (display_props != null)
            display_props.removeListener(this);
        display_props = dp;
        if (display_props != null)
            display_props.addListener(this);
        setFontSelection();
    }

    public void setFontSelection() {
        if (display_props == null) return;
        Font font = display_props.getFont();
        boolean found;

        String font_name = font.getName();

        for (int i = font_name_menu_item.length - 1; i >= 0; i--) 
            font_name_menu_item[i].setSelected(font_name_menu_item[i].getText().equals(font_name));
        int font_size = font.getSize();

        for (int i = font_size_menu_item.length - 1; i >= 0; i--) 
            font_size_menu_item[i].setSelected(Integer.valueOf(font_size_menu_item[i].getText()).intValue() == font_size);
        int font_style = font.getStyle();

        for (int i = font_style_menu_item.length - 1; i >= 0; i--) {
            if (font_style_menu_item[i].isSelected()) {
                String style = font_style_menu_item[i].getText();

                font_style_menu_item[i].setSelected((style.equals("Plain") 
                        && font_style == Font.PLAIN)
                    || (style.equals("Bold") 
                        && font_style == Font.BOLD)
                    || (style.equals("Italic") 
                        && font_style == Font.ITALIC));
            }
        }

        if (display_props.isFastRender()) {
            fast_render_menu_item.setState(true);
            for (int i = font_name_menu_item.length - 1; i >= 0; i--) 
                font_name_menu_item[i].setEnabled(false);
        } else {
            fast_render_menu_item.setState(false);
            for (int i = font_name_menu_item.length - 1; i >= 0; i--) 
                font_name_menu_item[i].setEnabled(true);
        }

        render_gaps_menu_item.setSelected(display_props.isGapRendered());
    }

    // DisplayProperties listener
    public void displayAnnViewChanged(DisplayProperties dp, 
        Sequence seq,
        boolean show) {}

    public void displaySeqSelectChanged(DisplayProperties dp, 
        Sequence seq,
        boolean select) {}

    public void displayColorSchemeChanged(DisplayProperties dp, 
        ColorScheme old) {}

    public void displayFontChanged(DisplayProperties dp) {
        if (dp != display_props)
            throw new 
                RuntimeException("bound to incorrect display properties");
        setFontSelection();
    }

    public void displayRenderGapsChanged(DisplayProperties dp) {
        if (dp != display_props)
            throw new 
                RuntimeException("bound to incorrect display properties");
        setFontSelection();
    }

    public void displayGroupEditingChanged(DisplayProperties dp) {}

    public void displayOverwriteChanged(DisplayProperties dp) {}

    public void displayHighlightsChanged(DisplayProperties dp,
        Sequence seq) {}

    public void displayHighlightsChanged(DisplayProperties dp,
        Sequence[] seqs) {}

}    
