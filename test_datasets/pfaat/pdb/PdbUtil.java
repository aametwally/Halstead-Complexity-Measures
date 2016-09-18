package com.neogenesis.pfaat.pdb;


import java.io.*;
import java.util.*;
import org.apache.oro.text.perl.*;


public class PdbUtil {
    public static Perl5Util perl = new Perl5Util();
    public static boolean needsRenumber(File infile, String chain)
        throws Exception {
        if (!infile.exists())
            throw new IOException(infile.getAbsolutePath() + " does not exist");

        if (chain.length() != 1)
            throw new Exception("Chain name has to be a single letter");

        if (chain.equals("-"))
            chain = " ";

        BufferedReader br = new BufferedReader(new FileReader(infile));

        String line = null;
        int newResNum = 0;
        String resNum = null;

        // example
        // ATOM     30  CA  GLU A   5      47.438   3.458 -26.710  1.00 47.26           C
        while ((line = br.readLine()) != null) {
            if (perl.match("/^ATOM.{9}CA.{2}\\w{3}." + chain + "(.{4})/", line)) {
                resNum = perl.group(1).trim();
                newResNum++;
                if (!resNum.equalsIgnoreCase("" + newResNum))
                    return true;
            }
        }
        br.close();
        return false;
    }

    /**
     * return the new pdb file with renumbered chain at chain A.
     */
    public static File renumberPDB(File infile, String chain)
        throws Exception {
        if (!needsRenumber(infile, chain) && chain.equals("A"))
            return infile;

        if (!infile.exists())
            throw new IOException(infile.getAbsolutePath() + " does not exist");

        if (chain.length() != 1)
            throw new Exception("Chain name has to be a single letter");

        if (chain.equals("-"))
            chain = " ";
        String infilename = infile.getAbsolutePath();
        String s = File.separator;
        int idx1 = infilename.lastIndexOf(s) + 1;
        int idx2 = infilename.lastIndexOf(".");
        String relativeName = infilename.substring(idx1, idx2);

        String outfilename = "";

        if (chain.equals(" "))
            outfilename = infilename.substring(0, idx1) + "." + relativeName + "-chain-" + infilename.substring(idx2);
        else
            outfilename = infilename.substring(0, idx1) + "." + relativeName + "-chain" + chain + infilename.substring(idx2);
        File outfile = new File(outfilename);

        if (outfile.exists())
            return outfile; // this file already exist. nothing to do.

        PrintWriter pw = new PrintWriter(new FileWriter(outfile));
        BufferedReader br = new BufferedReader(new FileReader(infile));

        String line = null;
        String temp = null;
        String resNum1 = "-1";
        String resNum2 = "-1";
        int newResNum = 0;
        String strNewResNum = "";

        // example
        // ATOM     30  CA  GLU A   5      47.438   3.458 -26.710  1.00 47.26           C
        while ((line = br.readLine()) != null) {
            if (line.startsWith("SHEET") ||
                line.startsWith("TURN") ||
                line.startsWith("HELIX"))
                continue; // skip all this secondary annotation. This will be fixed in next release.

            if (perl.match("/^ATOM.{17}" + chain + "(.{4})/", line)) {
                resNum2 = perl.group(1).trim();
                if (!resNum2.equalsIgnoreCase(resNum1)) {// new residue
                    newResNum++;
                    resNum1 = resNum2;
                    if (newResNum < 10)
                        strNewResNum = "   " + newResNum;
                    else if (newResNum < 100)
                        strNewResNum = "  " + newResNum;
                    else if (newResNum < 1000)
                        strNewResNum = " " + newResNum;
                    else
                        strNewResNum = "" + newResNum;
                }
                temp = line.substring(0, 21) + "A" + strNewResNum + line.substring(26);
            } else if (perl.match("/^TER.{18}" + chain + "/", line)) {
                temp = line.substring(0, 21) + "A" + strNewResNum + line.substring(26);
            } else if (!chain.equalsIgnoreCase("A")
                && perl.match("/^ATOM.{17}A/", line)) {
                temp = line.substring(0, 21) + chain + line.substring(22);
            } else if (!chain.equalsIgnoreCase("A")
                && perl.match("/^TER.{18}A/", line)) {
                temp = line.substring(0, 21) + chain + line.substring(22);
            } else
                temp = line;
            pw.println(temp);
        }
        pw.flush();
        pw.close();
        br.close();
        return outfile;
    }

    // return all the protein chain names in the pdb file
    public static String[] getProChainNames(File infile)
        throws IOException {
        ArrayList names = new ArrayList();

        if (!infile.exists())
            throw new IOException(infile.getAbsolutePath() + " does not exist");

        BufferedReader br = new BufferedReader(new FileReader(infile));
        String line = null;

        while ((line = br.readLine()) != null) {
            if (line.startsWith("ATOM")) {
                names.add(line.substring(21, 22));
                // skip to the end of the seq
                while ((line = br.readLine()) != null && !line.startsWith("TER")) {;
                }
            }
        }
        String[]chainNames = new String[names.size()];

        for (int i = 0; i < chainNames.length; i++) {
            chainNames[i] = (String) names.get(i);
            if (chainNames[i].equals(" "))
                chainNames[i] = "-";
        }
        br.close();
        return chainNames;
    }

    public static void main(String argv[]) {// test
        try {
            PdbUtil.renumberPDB(new File(argv[0]), argv[1]);
            String[] names = PdbUtil.getProChainNames(new File(argv[0]));
            boolean r = PdbUtil.needsRenumber(new File(argv[2]), argv[3]);

            System.out.println("");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

