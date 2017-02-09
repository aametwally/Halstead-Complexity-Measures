package com.neogenesis.pfaat.pdb;


import java.io.*;
import java.util.*;

import org.apache.oro.text.perl.*;
import com.neogenesis.pfaat.util.*;


/**
 * Class to assign bonds to standard residues.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:29 $ */
public class PdbResidueData {
    private String type;
    private List bond_list = new ArrayList();

    private PdbResidueData(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    private void addPair(String first, String second) {
        bond_list.add(new AtomPair(first, second));
    }

    public void assignBonds(PdbResidue residue) throws Exception {
        for (int i = bond_list.size() - 1; i >= 0; i--) {
            AtomPair pair = (AtomPair) bond_list.get(i);
            PdbAtom a1 = residue.getByLocation(pair.getFirst());

            if (a1 == null) continue;
            PdbAtom a2 = residue.getByLocation(pair.getSecond());

            if (a2 == null) continue;
            if (a1.isBondedTo(a2)) continue;
            PdbBond bond = new PdbBond(PdbBond.COVALENT_BOND, a1, a2);

            a1.addBond(bond);
            a2.addBond(bond);
        }
    }

    private static class AtomPair {
        private String first, second;
        public AtomPair(String first, String second) {
            this.first = first;
            this.second = second;
        }

        public String getFirst() {
            return first;
        }

        public String getSecond() {
            return second;
        }
    }

    public static void assignResidueBonds(PdbChain chain) throws Exception {
        if (resdata_map == null) loadResidueData();
        int res_cnt = chain.getResidueCount();

        if (res_cnt < 1) return;

        // make the n-terminus to c-terminus connections
        PdbResidue last_r = chain.getResidue(res_cnt - 1);
        PdbResidue r;

        for (int i = res_cnt - 2; i >= 0; i--, last_r = r) {
            r = chain.getResidue(i);
            PdbAtom n_atom = last_r.getByLocation("N");

            if (n_atom == null) continue;
            PdbAtom c_atom = r.getByLocation("C");

            if (c_atom == null) continue;
            if (n_atom.isBondedTo(c_atom)) continue;
            PdbBond bond = new PdbBond(PdbBond.COVALENT_BOND, c_atom, n_atom);

            n_atom.addBond(bond);
            c_atom.addBond(bond);
        }
	
        // make internal bonds in each amino acid
        for (int i = res_cnt - 1; i >= 0; i--) {
            r = chain.getResidue(i);
            PdbResidueData res_data = 
                (PdbResidueData) resdata_map.get(r.getRawType());

            if (res_data != null)
                res_data.assignBonds(r);
        }
    }

    private static Map resdata_map;

    private static void loadResidueData() throws Exception {
        BufferedReader r = new 
            BufferedReader(new FileReader(PathManager.getResidueDataFile()));

        Perl5Util perl = new Perl5Util();
        String line;

        try {
            resdata_map = new HashMap();
            PdbResidueData res = null;

            while ((line = r.readLine()) != null) {
                if (line.startsWith("END"))
                    res = null;
                else if (perl.match("/^RES\\s+(\\S+)/", line)) {
                    String type = perl.group(1);

                    res = new PdbResidueData(type);
                    if (resdata_map.put(type, res) != null)
                        throw new Exception("duplicate residue definition");
                } else if (perl.match("/^BOND\\s+(\\S+)\\s+(\\S+)/", line)) {
                    res.addPair(perl.group(1), perl.group(2));
                }
            }

        } catch (Exception e) {
            resdata_map = null;
            throw e;
        }

        r.close();
    }
}
