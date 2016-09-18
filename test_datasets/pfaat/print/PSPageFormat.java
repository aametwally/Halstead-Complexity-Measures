package com.neogenesis.pfaat.print;


import java.text.DecimalFormat;


/**
 * Define page size and margins for <code>PSGraphics</code> output.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:30:23 $ */
public class PSPageFormat {
    private int height, width;
    private int top_margin, bottom_margin, left_margin, right_margin;
    private String name;
    private boolean is_metric, is_landscape;

    public static final PSPageFormat[] allFormats = {
            new PSPageFormat("ANSI A (Letter)",
                612, 792,
                false,
                50, 50, 30, 30, false),
            new PSPageFormat("ARCH E",
                2592, 3456,
                false,
                50, 50, 30, 30, false),
            new PSPageFormat("ANSI B (Tabloid)",
                792, 1224,
                false,
                50, 50, 30, 30, false),
            new PSPageFormat("ANSI C",
                1224, 1584,
                false,
                50, 50, 30, 30, false),
            new PSPageFormat("ANSI D",
                1584, 2448,
                false,
                50, 50, 30, 30, false),
            new PSPageFormat("ANSI E",
                2448, 3168,
                false,
                50, 50, 30, 30, false),
            new PSPageFormat("ARCH A",
                648, 864,
                false,
                50, 50, 30, 30, false),
            new PSPageFormat("ARCH B",
                864, 1296,
                false,
                50, 50, 30, 30, false),
            new PSPageFormat("ARCH C",
                1296, 1728,
                false,
                50, 50, 30, 30, false),
            new PSPageFormat("ARCH D",
                1728, 2592,
                false,
                50, 50, 30, 30, false),
            new PSPageFormat("ISO A4",
                595, 842,
                true,
                50, 50, 30, 30, false),
            new PSPageFormat("ISO A3",
                842, 1191,
                true,
                50, 50, 30, 30, false),
            new PSPageFormat("ISO A2",
                1191, 1684,
                true,
                50, 50, 30, 30, false),
            new PSPageFormat("ISO A1",
                1684, 2384,
                true,
                50, 50, 30, 30, false),
            new PSPageFormat("ISO A0",
                2384, 3370,
                true,
                50, 50, 30, 30, false),
            new PSPageFormat("Oversize A2",
                1361, 1772,
                true,
                50, 50, 30, 30, false),
            new PSPageFormat("Oversize A1",
                1772, 2551,
                true,
                50, 50, 30, 30, false),
            new PSPageFormat("Oversize A0",
                2551, 3529,
                true,
                50, 50, 30, 30, false),
            new PSPageFormat("JIS B4",
                729, 1032,
                true,
                50, 50, 30, 30, false),
            new PSPageFormat("JIS B3",
                1032, 1460,
                true,
                50, 50, 30, 30, false),
            new PSPageFormat("JIS B2",
                1460, 2064,
                true,
                50, 50, 30, 30, false),
            new PSPageFormat("JIS B1",
                2064, 2920,
                true,
                50, 50, 30, 30, false),
            new PSPageFormat("24 x 48",
                1728, 3456,
                false,
                50, 50, 30, 30, false),
            new PSPageFormat("24 x 60",
                1728, 4320,
                false,
                50, 50, 30, 30, false)
        };

    public PSPageFormat(String name, 
        int width, int height, 
        boolean is_metric,
        int top_margin, int bottom_margin,
        int left_margin, int right_margin,
        boolean is_landscape) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.is_metric = is_metric;
        this.top_margin = top_margin;
        this.bottom_margin = bottom_margin;
        this.left_margin = left_margin;
        this.right_margin = right_margin;
        this.is_landscape = is_landscape;
    }

    public String getName() { 
        return name + ": " + getDimString();
    }

    public String getDimString() {
        double w, h;
        DecimalFormat df;
        String unit;

        if (is_metric) {
            w = width / 72.0 * 25.4;
            h = height / 72.0 * 25.4;
            df = new DecimalFormat("0");
            unit = "mm";
        } else {
            w = width / 72.0;
            h = height / 72.0;
            df = new DecimalFormat("0.0");
            unit = "in";
        }
        return df.format(w) + " x " + df.format(h) + " " + unit; 
    }

    public PSPageFormat toLandscape() {
        return new PSPageFormat(name,
                height, 
                width,
                is_metric,
                right_margin,
                left_margin,
                top_margin,
                bottom_margin,
                true);
    }

    public PSPageFormat fitToWidth(int x) {
        return new PSPageFormat(name,
                x + left_margin + right_margin,
                height,
                is_metric,
                top_margin,
                bottom_margin,
                left_margin,
                right_margin,
                is_landscape);
    }

    public PSPageFormat fitToHeight(int x) {
        return new PSPageFormat(name,
                width,
                x + top_margin + bottom_margin,
                is_metric,
                top_margin,
                bottom_margin,
                left_margin,
                right_margin,
                is_landscape);
    }

    public int getWidth() {
        return width - left_margin - right_margin;
    }

    public int getHeight() {
        return height - top_margin - bottom_margin;
    }

    public int getPageWidth() {
        return width;
    }

    public int getPageHeight() {
        return height;
    }

    public int getTopMargin() {
        return top_margin;
    }

    public int getLeftMargin() {
        return left_margin;
    }    

    public boolean isLandscape() {
        return is_landscape;
    }
}
    
