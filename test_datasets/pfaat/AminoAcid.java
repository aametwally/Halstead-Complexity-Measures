package com.neogenesis.pfaat;


import java.util.*;


/**
 * A single amino acid (including B, Z, X, and gaps).
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:03 $ */
public class AminoAcid {
    private byte idx;
    private String code, code3;
    
    private AminoAcid(String code3, String code, int idx) {
        this.idx = (byte) idx;
        this.code = code;
        this.code3 = code3;
    }

    public int hashCode() {
        return idx;
    }

    public boolean equals(Object other) {
        if (other instanceof AminoAcid) 
            return idx == ((AminoAcid) other).idx;
        return false;
    }

    public String getCode() {
        return code;
    }

    public String getCode3() {
        return code3;
    }

    public int getIndex() {
        return idx;
    }

    public boolean isGap() {
        return this.idx == GAP.idx;
    }

    public boolean isTrueAA() {
        return idx < NUM_TRUE_AA;
    }

    public static final AminoAcid ALA = new AminoAcid("ALA", "A", 0);
    public static final AminoAcid ARG = new AminoAcid("ARG", "R", 1);
    public static final AminoAcid ASN = new AminoAcid("ASN", "N", 2);
    public static final AminoAcid ASP = new AminoAcid("ASP", "D", 3);
    public static final AminoAcid CYS = new AminoAcid("CYS", "C", 4);
    public static final AminoAcid GLN = new AminoAcid("GLN", "Q", 5);
    public static final AminoAcid GLU = new AminoAcid("GLU", "E", 6);
    public static final AminoAcid GLY = new AminoAcid("GLY", "G", 7);
    public static final AminoAcid HIS = new AminoAcid("HIS", "H", 8);
    public static final AminoAcid ILE = new AminoAcid("ILE", "I", 9);
    public static final AminoAcid LEU = new AminoAcid("LEU", "L", 10);
    public static final AminoAcid LYS = new AminoAcid("LYS", "K", 11);
    public static final AminoAcid MET = new AminoAcid("MET", "M", 12);
    public static final AminoAcid PHE = new AminoAcid("PHE", "F", 13);
    public static final AminoAcid PRO = new AminoAcid("PRO", "P", 14);
    public static final AminoAcid SER = new AminoAcid("SER", "S", 15);
    public static final AminoAcid THR = new AminoAcid("THR", "T", 16);
    public static final AminoAcid TRP = new AminoAcid("TRP", "W", 17);
    public static final AminoAcid TYR = new AminoAcid("TYR", "Y", 18);
    public static final AminoAcid VAL = new AminoAcid("VAL", "V", 19);
    public static final AminoAcid B = new AminoAcid("B", "B", 20);
    public static final AminoAcid Z = new AminoAcid("Z", "Z", 21);
    public static final AminoAcid X = new AminoAcid("X", "X", 22);

    public static final AminoAcid GAP = new AminoAcid("GAP", "-", 23);

    private static final AminoAcid[] ALL = {
            ALA, ARG, ASN, ASP, CYS, GLN, GLU, GLY, HIS, ILE, 
            LEU, LYS, MET, PHE, PRO, SER, THR, TRP, TYR, VAL, 
            B, Z, X, GAP
        };

    public static AminoAcid lookupByIndex(int idx) {
        return ALL[idx];
    }
    
    // number of true amino acids
    public static final int NUM_TRUE_AA = 20;
    // number total
    public static final int NUM_AA = 24;

    private static final HashMap lookup_by_code = new HashMap();
    static {
        for (int i = 0; i < NUM_AA; i++) {
            if (lookup_by_code.put(ALL[i].code, ALL[i]) != null)
                throw new RuntimeException("duplicate code definition");
        }
        if (lookup_by_code.put("*", GAP) != null)
            throw new RuntimeException("duplicate code definition");
        if (lookup_by_code.put(" ", GAP) != null)
            throw new RuntimeException("duplicate code definition");
        if (lookup_by_code.put(".", GAP) != null)
            throw new RuntimeException("duplicate code definition");
        if (lookup_by_code.put("~", GAP) != null)
            throw new RuntimeException("duplicate code definition");
    }

    public static AminoAcid lookupByCode(String code) {
        return (AminoAcid) lookup_by_code.get(code.toUpperCase());
    }

    public static AminoAcid lookupByCode(char code) {
        StringBuffer sb = new StringBuffer();

        sb.append(code);
        return (AminoAcid) lookup_by_code.get(sb.toString().toUpperCase());
    }

    private static final HashMap lookup_by_code3 = new HashMap();
    static {
        for (int i = 0; i < NUM_AA; i++) {
            if (lookup_by_code3.put(ALL[i].code3, ALL[i]) != null)
                throw new RuntimeException("duplicate code3 definition");
        }
        if (lookup_by_code3.put("*", GAP) != null)
            throw new RuntimeException("duplicate code definition");
        if (lookup_by_code3.put(" ", GAP) != null)
            throw new RuntimeException("duplicate code definition");
        if (lookup_by_code3.put(".", GAP) != null)
            throw new RuntimeException("duplicate code definition");
        if (lookup_by_code3.put("~", GAP) != null)
            throw new RuntimeException("duplicate code definition");
    }
    public static AminoAcid lookupByCode3(String code3) {
        return (AminoAcid) lookup_by_code3.get(code3.toUpperCase());
    }

    // test if a string is a valid amino acid sequence
    public static boolean isAminoAcidSequence(String s) {
        for (int i = s.length() - 1; i >= 0; i--) {
            if (lookupByCode(String.valueOf(s.charAt(i))) == null)
                return false;
        }
        return true;
    }

    public static AminoAcid[] stringToAA(String s) {
        AminoAcid[] aa = new AminoAcid[s.length()];

        for (int i = aa.length - 1; i >= 0; i--) {
            aa[i] = lookupByCode(s.substring(i, i + 1));
            if (aa[i] == null)
                aa[i] = GAP;
        }
        return aa;
    }

    public static String aaToString(AminoAcid[] aa) {
        StringBuffer sb = new StringBuffer(aa.length);

        for (int i = 0; i < aa.length; i++)
            sb.append(aa[i].getCode());
        return sb.toString();
    }
}
