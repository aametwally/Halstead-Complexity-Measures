package com.neogenesis.pfaat.pdb;


import java.io.*;
import java.util.*;
import javax.vecmath.Point3d;

import org.apache.oro.text.perl.*;
import com.neogenesis.pfaat.*;


/**
 * PDB file loader.
 *
 * @author $Author: xih $
 * @version $Revision: 1.4 $, $Date: 2002/10/11 18:29:29 $ */
public class PdbLoader {
    private Perl5Util perl = new Perl5Util();

    // temporary class for storing structure annotations
    private static class SecondaryStructure {
        public String chain, type, start, end;
        public SecondaryStructure(String chain, String type,
            String start, String end) {
            this.chain = chain;
            this.type = type;
            this.start = start;
            this.end = end;
        }

        public String toString() {
            return chain + "," + type + "," + start + "," + end;
        }
    }

    public PdbStructure loadStructure(Reader ir) throws Exception {
        BufferedReader r = new BufferedReader(ir);
        String line;

        PdbStructure structure = null;
        Map atom_map = new HashMap();
        List ss_list = new ArrayList();

        while ((line = r.readLine()) != null) {
            // create the structure
            if (line.startsWith("HEADER")) {
                String id_code = line.substring(62, 66);

                if (structure != null)
                    throw new Exception("PdbLoader: duplicate header lines");
                structure = new PdbStructure(id_code);
            } // secondary structure
            else if (line.startsWith("HELIX")) {
                String chain_start = line.substring(19, 20);
                String start = line.substring(21, 25).trim();
                String chain_end = line.substring(31, 32);
                String end = line.substring(33, 37).trim();

                if (!chain_start.equals(chain_end))
                    throw new
                        Exception("chain mismatch in secondary structure");
                ss_list.add(new SecondaryStructure(chain_start,
                        PdbChain.HELIX,
                        start,
                        end));
            } else if (line.startsWith("SHEET")) {
                String chain_start = line.substring(21, 22);
                String start = line.substring(22, 26).trim();
                String chain_end = line.substring(32, 33);
                String end = line.substring(33, 37).trim();

                if (!chain_start.equals(chain_end))
                    throw new
                        Exception("chain mismatch in secondary structure");
                ss_list.add(new SecondaryStructure(chain_start,
                        PdbChain.SHEET,
                        start,
                        end));
            } else if (line.startsWith("TURN")) {
                String chain_start = line.substring(19, 20);
                String start = line.substring(20, 24).trim();
                String chain_end = line.substring(30, 31);
                String end = line.substring(31, 35).trim();

                if (!chain_start.equals(chain_end))
                    throw new
                        Exception("chain mismatch in secondary structure");
                ss_list.add(new SecondaryStructure(chain_start,
                        PdbChain.SHEET,
                        start,
                        end));
            } // add atoms and residues
            else if (line.startsWith("ATOM") || line.startsWith("HETATM")) {
                if (structure == null)
                    structure = new PdbStructure("Generic PDB File");

                boolean het_atom = line.startsWith("HETATM");

                String chain_id = line.substring(21, 22);

                // parse chain (prefix the chain name of het atoms with 'HET-'
                if (het_atom)
                    chain_id = "HET-" + chain_id;

                PdbChain chain = structure.getChain(chain_id);

                if (chain == null) {
                    chain = new PdbChain(chain_id);
                    structure.addChain(chain);
                }

                // parse residue
                String residue_name = line.substring(22, 26).trim();
                String residue_type = line.substring(17, 20);
                PdbResidue residue = chain.getResidue(residue_name);

                if (residue == null) {
                    residue =
                            new PdbResidue(residue_name,
                                residue_type,
                                AminoAcid.lookupByCode3(residue_type));
                    chain.addResidue(residue);
                }

                // parse atom
                String location = line.substring(12, 16).trim();
                String serial_number = line.substring(6, 11).trim();
                Point3d pos =
                    new Point3d(Double.parseDouble(line.substring(30, 38).trim()),
                        Double.parseDouble(line.substring(38, 46).trim()),
                        Double.parseDouble(line.substring(46, 54).trim()));
                String type;

                // see if there is an explicit element type
                if (line.length() >= 78)
                    type = line.substring(76, 78).trim();
                else { // no explicit atom type, guess
                    type = location.substring(0, 1);
                    if (perl.match("/[0-9]/", type))
                        type = location.substring(1, 2);

                    if (!het_atom) {
                        if (perl.match("/^(?:AS|N)/", residue_type)) {
                            if (location.equals("AD1"))
                                type = "O";
                            else if (location.equals("AD2"))
                                type = "N";
                        } else if (perl.match("/^H/", residue_type)) {
                            if (location.equals("AD1")
                                || location.equals("AE2"))
                                type = "N";
                            else if (location.equals("AE1")
                                || location.equals("AD2"))
                                type = "C";
                        } else if (perl.match("/^(?:GL|Q)/", residue_type)) {
                            if (location.equals("AE1"))
                                type = "O";
                            else if (location.equals("AE2"))
                                type = "N";
                        }
                    }
                }
                PdbAtom atom = new PdbAtom(serial_number, pos, type);

                try {
                    residue.addAtom(atom, location);
                } catch (Exception e) {
                    throw new Exception("could not adding atom with line: "
                            + line + ", error: "
                            + e.getMessage());
                }

                atom_map.put(atom.getName(), atom);
            } // read in a connection (bond) record
            else if (line.startsWith("CONECT")) {
                String atom_name = line.substring(6, 11).trim();
                PdbAtom a =
                    (PdbAtom) atom_map.get(line.substring(6, 11).trim());

                if (a == null)
                    throw new Exception("PdbLoader: conect record found "
                            + "for unknown atom "
                            + atom_name + ": " + line);
                for (int end = 16; end <= 61 && end <= line.length();
                    end += 5) {
                    String b_str = line.substring(end - 5, end).trim();

                    if (b_str.length() < 1)
                        continue;
                    PdbAtom b = (PdbAtom) atom_map.get(b_str);

                    if (b == null)
                        throw new Exception("PdbLoader: conect record found "
                                + "for unknown bonded atom "
                                + b_str + ": "
                                + line);
                    if (a.getName().compareTo(b.getName()) > 0)
                        continue;

                    int bond_type = PdbBond.COVALENT_BOND;

                    switch (end) {
                    case 36:
                    case 41:
                    case 51:
                    case 56:
                        bond_type = PdbBond.HYDROGEN_BOND;
                        break;

                    case 46:
                    case 61:
                        bond_type = PdbBond.SALT_BRIDGE;
                        break;
                    }
                    PdbBond bond = new PdbBond(bond_type, a, b);

                    a.addBond(bond);
                    b.addBond(bond);
                }
            }
        }

        // assign bonds
        for (int i = structure.getChainCount() - 1; i >= 0; i--)
            PdbResidueData.assignResidueBonds(structure.getChain(i));

        // assign secondary structure
        for (int i = 0; i < ss_list.size(); i++) {
            SecondaryStructure ss = (SecondaryStructure) ss_list.get(i);
            PdbChain chain = structure.getChain(ss.chain);

            if (chain == null)
                throw new Exception("unknown chain referenced in secondary "
                        + "structure: " + ss.toString());
            PdbResidue start_r, end_r;
            int start, end;

            if ((start_r = chain.getResidue(ss.start)) == null
                || (start = chain.getOffset(start_r)) < 0)
                throw new Exception("start of secondary structure not found");
            if ((end_r = chain.getResidue(ss.end)) == null
                || (end = chain.getOffset(end_r)) < 0)
                throw new Exception("start of secondary structure not found");
            for (int k = start; k <= end; k++)
                chain.setSecondaryStructure(chain.getResidue(k), ss.type);
        }

        //
        structure.initialize();
        // shift all the coordinates so that the center is at the origin
        resetOrigin(structure);

        return structure;
    }

    // set the origin to the center of the structure.
    private void resetOrigin(PdbStructure structure) {
        Point3d center = structure.getCenter();

        for (int i = 0; i < structure.getChainCount(); i++) {
            PdbChain chain = (PdbChain) structure.getChain(i);

            for (int j = 0; j < chain.getResidueCount(); j++) {
                PdbResidue residue = chain.getResidue(j);

                for (int k = 0; k < residue.getAtomCount(); k++) {
                    PdbAtom atom = residue.getAtom(k);
                    Point3d oldPoint = atom.getPosition();
                    Point3d newPoint = new Point3d();

                    newPoint.sub(oldPoint, center);
                    atom.setPosition(newPoint);
                }
            }
        }
    }

    // FileFilter interface
    public javax.swing.filechooser.FileFilter getFileFilter() {
        return new javax.swing.filechooser.FileFilter() {
                public boolean accept(File f) {
                    return f.getName().endsWith(".pdb");
                }

                public String getDescription() {
                    return "PDB (*.pdb)";
                }
            };
    }

    // main interface for testing
    public static void main(String[] argv) throws Exception {
        PdbLoader loader = new PdbLoader();

        for (int i = 0; i < argv.length; i++) {
            try {
                FileReader r = new FileReader(argv[i]);
                PdbStructure s = loader.loadStructure(r);

                r.close();
                System.out.println(argv[i] + ": " + s.getName() + " loaded, "
                    + s.getChainCount() + " chain(s)");
            } catch (Exception e) {
                System.err.println(argv[i] + ": failed to load");
                e.printStackTrace(System.err);
            }
        }
    }

}
