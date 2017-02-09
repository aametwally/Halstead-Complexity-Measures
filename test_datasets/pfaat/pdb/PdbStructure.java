package com.neogenesis.pfaat.pdb;


import java.util.*;


/**
 * 3d protein structure.
 *
 * @author $Author: xih $
 * @version $Revision: 1.3 $, $Date: 2002/10/11 18:29:28 $ */
public class PdbStructure extends PdbAtomContainer {
    private String name;
    private List chain_list = new ArrayList();
    private Map chain_name_map = new HashMap();

    private Map atom2chain_map, atom2residue_map;

    public PdbStructure(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public PdbChain getChain(int i) {
        return (PdbChain) chain_list.get(i);
    }

    public PdbChain getChain(String chain_name) {
        return (PdbChain) chain_name_map.get(chain_name);
    }

    public int getChainCount() {
        return chain_list.size();
    }

    public PdbResidue getResidue(PdbAtom atom) {
        return (PdbResidue) atom2residue_map.get(atom);
    }

    public PdbChain getChain(PdbAtom atom) {
        return (PdbChain) atom2chain_map.get(atom);
    }

    public void addChain(PdbChain chain) throws Exception {
        if (chain_name_map.put(chain.getName(), chain) != null)
            throw new Exception("duplicate chain definition");
        chain_list.add(chain);
    }

    public void addAtom(PdbAtom atom) throws Exception {
        throw new Exception("direct atom addition not supported");
    }

    public void initialize() throws Exception {
        atom2chain_map = new HashMap();
        atom2residue_map = new HashMap();
        for (int i = 0; i < getChainCount(); i++) {
            PdbChain chain = (PdbChain) chain_list.get(i);

            chain.initialize();
            for (int j = 0; j < chain.getResidueCount(); j++) {
                PdbResidue residue = chain.getResidue(j);

                for (int k = 0; k < residue.getAtomCount(); k++) {
                    PdbAtom atom = residue.getAtom(k);

                    super.addAtom(atom);
                    atom2chain_map.put(atom, chain);
                    atom2residue_map.put(atom, residue);

                }
            }
        }
    }
}

