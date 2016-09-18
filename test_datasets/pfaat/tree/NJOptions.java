// ///////////
// BETE BEGIN
// ///////////
package com.neogenesis.pfaat.tree;


import com.neogenesis.pfaat.CmdLineOption;
import java.io.*;
import java.util.*;


/**
 * Contains options for neighbor joining tree calculation
 *
 * @author $Author: xih $
 * @version $Revision: 1.7 $, $Date: 2002/10/11 18:31:50 $ */
public class NJOptions {
    // types of Neighbor Joining methods calculations supported
    public static final int PercentageIdentity = 0;
    public static final int Blosum62 = 1;
    public static final int Blosum50 = 2;
    public static final int PhysicoChemical = 3;

    public static final float DefaultGapCutoffPercent = 60.0f;
    public static final int DefaultNumBootStrapIterations = 100;

    // get string for a single method type
    public static String getMethodType(int method) {
        if (method == PercentageIdentity)
            return "Percentage Identity";
        if (method == Blosum62)
            return "Blosum62";
        if (method == Blosum50)
            return "Blosum50";
        if (method == PhysicoChemical)
            return "Physico-Chemical";
        return "unknown method";
    }

    // get array of strings describing the distance types
    public static String[] getMethodTypes() {
        String[] s = new String[4];

        s[PercentageIdentity] = getMethodType(PercentageIdentity);
        s[Blosum62] = getMethodType(Blosum62);
        s[Blosum50] = getMethodType(Blosum50);
        // s[PhysicoChemical]      = getMethodType(PhysicoChemical);

        return s;
    }

    // constructors...
    public NJOptions() {
        includedColumns = new boolean[7500];
        for (int i = 0; i < 7500; i++)
            includedColumns[i] = true; // default to include all columns
    }

    public NJOptions(boolean blosum62) {
        this();
        if (blosum62)
            method = NJOptions.Blosum62;
    }

    // method of NJ to use
    public int method = PercentageIdentity;

    // are we using weighted columns for distance calculation?
    public boolean useWeightedColumns = false;

    // are we skipping columns with gaps > cutoff?
    public boolean skipGappyColumns = false;

    // what is the percentage cutoff for skipping gappy columns?
    public float gapCutoffPercent = DefaultGapCutoffPercent;

    // Column included return an array of true or 1. 0 mean position idx is excluded
    public boolean[] includedColumns;// the maximun length of alignment

    // do we want to do boot strapping
    public boolean bootStrapping = false;

    // if we are doing bootstrapping...how many iterations
    public int numBootStrapIterations = DefaultNumBootStrapIterations;

    // get a title for tree window based on these options
    public String getTitle(String alignmentName) {
        String m = getMethodType(method);
        String weighted = "";

        if (useWeightedColumns)
            weighted = "Col Wgtd - ";
        String skip = "";

        if (skipGappyColumns)
            skip = "Skip Col. w/ > " + gapCutoffPercent + " gaps -";

        String s = "Nbr Join Tree - " + m + " - " + weighted + skip +
            " (" + alignmentName + ")";

        return s;
    }

    // get included columns
    public static void getIncludedColumns(NJOptions options, String s)
        throws Exception {
        StringTokenizer st = new StringTokenizer(s.trim(), ",");

        while (st.hasMoreTokens()) {
            String tmp = st.nextToken().trim();
            int start = 0;
            int end = 0;
            int idx = tmp.indexOf('-');

            if (idx < 0)
                start = end = Integer.parseInt(tmp);
            else {
                start = Integer.parseInt(tmp.substring(0, idx));
                end = Integer.parseInt(tmp.substring(idx + 1));
            }
            for (int i = start; i <= end; i++) {
                options.includedColumns[i] = false;
            }
        }
    }

    // get excluded columns
    public static void getExcludedColumns(NJOptions options, String s)
        throws Exception {
        boolean[] included = new boolean[7500];
        StringTokenizer st = new StringTokenizer(s.trim(), ",");

        while (st.hasMoreTokens()) {
            String tmp = st.nextToken().trim();
            int start = 0;
            int end = 0;
            int idx = tmp.indexOf('-');

            if (idx < 0)
                start = end = Integer.parseInt(tmp);
            else {
                start = Integer.parseInt(tmp.substring(0, idx));
                end = Integer.parseInt(tmp.substring(idx + 1));
            }

            for (int i = start; i <= end; i++) {
                included[i] = true;
            }
        }

        // set the complement value
        for (int i = 0; i < 7500; i++)
            options.includedColumns[i] = included[i];
    }

    // get options for nj analysis on command line
    public static NJOptions commandLineNJOptions(String[] argv) {
        NJOptions options = new NJOptions();

        // skip the filename and the -analysis nj
        CmdLineOption cl = new CmdLineOption(argv, 3);

        // method types
        String[] types = getMethodTypes();

        // loop through arguments setting options
        for (;;) {
            String option = cl.nextOption();

            if (option == null)
                break;

            if (option.compareToIgnoreCase("-help") == 0) {
                System.out.println("Pfaat command line mode:");
                System.out.println("Supported -analysis nj options are:");
                System.out.println("  -help");
                System.out.println("  -useWeightedColumns");
                for (int i = 0; i < types.length; i++)
                    System.out.println("  -method " + types[i]);
                System.out.println("  -skipGappyColumns <percentCutoff>");
                System.out.println("  -bootStrapping <numIterations>");
                System.out.println("  -includeColumns <columns>");
                System.out.println("  -excludeColumns <columns>");
                continue;
            }

            // process options that require no value
            if (option.compareToIgnoreCase("-useWeightedColumns") == 0) {
                options.useWeightedColumns = true;
                continue;
            }

            // process options that require a value
            String value = cl.nextValue();

            if (value == null) {
                System.out.println("Error parsing NJ options..." +
                    "no value for " + option + " option.");
                return null;
            }

            if (option.compareToIgnoreCase("-method") == 0) {
                options.method = -1;
                for (int i = 0; i < types.length; i++) {
                    if (value.compareToIgnoreCase(types[i]) == 0) {
                        options.method = i;
                        break;
                    }
                }
                if (options.method < 0) {
                    System.out.println("Error parsing NJ options.");
                    System.out.println("-method must be one of: ");
                    for (int i = 0; i < types.length; i++)
                        System.out.println("  " + types[i]);
                    return null;
                }
            } else if (option.compareToIgnoreCase("-skipGappyColumns") == 0) {
                // a value of 100% means dont skip any columns
                float percent = Float.parseFloat(value);

                if (percent > 0 && percent < 100) {
                    options.skipGappyColumns = true;
                    options.gapCutoffPercent = percent;
                }
            } else if (option.compareToIgnoreCase("-bootStrapping") == 0) {
                int iterations = Integer.parseInt(value);

                if (iterations > 0 && iterations < 10000) {
                    options.bootStrapping = true;
                    options.numBootStrapIterations = iterations;
                }
            } else if (option.compareToIgnoreCase("-includeColumns") == 0) {
                try {
                    getIncludedColumns(options, value);
                } catch (Exception e) {
                    System.out.println("Error parsing NJ options.");
                    System.out.println("columns should be specified as start1-end1,start2-end2, etc. and end < 7500");
                    return null;
                }
            } else if (option.compareToIgnoreCase("-excludeColumns") == 0) {
                try {
                    getExcludedColumns(options, value);
                } catch (Exception e) {
                    System.out.println("Error parsing NJ options.");
                    System.out.println("columns should be specified as start1-end1,start2-end2, etc. and end < 7500");
                    return null;
                }
            }
        }

        return options;
    }

}

// /////////
// BETE END
// /////////
