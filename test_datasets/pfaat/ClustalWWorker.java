package com.neogenesis.pfaat;


import java.io.*;
import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.io.AlignmentLoader;


public class ClustalWWorker extends Thread {
    // String m_cmd = "";
    // String m_error = "";
    AlignmentFrame m_owner;
    ClustalWAlgorithm m_cwAlg;
    DisplayProperties m_props;

    public ClustalWWorker(AlignmentFrame owner, DisplayProperties props, ClustalWAlgorithm cwAlg) {
        m_owner = owner;
        m_cwAlg = cwAlg;
        m_props = props;
    }

    public void run() {
        try {
            m_cwAlg.execute();
        } catch (Exception e) {
            ErrorDialog.showErrorDialog(m_owner, "ClustalW alignment failed");
            return;
        }
        try {
            File tempAlignmentFile = File.createTempFile("pfaat_tmp_alignment", ".pfam");

            AlignmentLoader.saveAlignmentFile(tempAlignmentFile,
                m_props.getColorScheme(),
                m_owner.getAlignment());
            Alignment newAlignment = AlignmentLoader.loadAlignmentFile(tempAlignmentFile);
            String csHint = AlignmentLoader.getColorSchemeHint(tempAlignmentFile);

            tempAlignmentFile.delete();

            Sequence[] results = m_cwAlg.getResults();

            for (int i = results.length; --i >= 0;) {
                if (results[i].length() < 1) {
                    ErrorDialog.showErrorDialog(m_owner,
                        "Sequence must have data.");
                    return;
                }
                Sequence seq = newAlignment.getSequence(results[i].getName());

                seq.removeGaps();
                results[i].insertAA(AminoAcid.GAP, 0);
                for (int j = 1; j < results[i].length(); j++) {
                    if (results[i].getAA(j).equals(AminoAcid.GAP)) {
                        seq.insertAA(AminoAcid.GAP, j);
                    }
                }
            }

            AlignmentFrame newAlignmentFrame = new AlignmentFrame(null, newAlignment, csHint, AlignmentFrame.getCurrentMode());

            newAlignmentFrame.show();
        } catch (IOException ioe) {
            ErrorDialog.showErrorDialog(m_owner,
                "Could not create temporary alignment file");
            return;
        } catch (Exception e) {
            ErrorDialog.showErrorDialog(m_owner,
                "Unable to create new alignment");

            return;
        }
    }
}
