package com.neogenesis.pfaat;


import java.io.*;
import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.io.AlignmentLoader;


public class HMMERWorker extends Thread {
    // String m_cmd = "";
    // String m_error = "";
    AlignmentFrame owner;
    HMMERAlgorithm hmmAlg;
    DisplayProperties props;

    public HMMERWorker(AlignmentFrame owner, DisplayProperties props, HMMERAlgorithm hmmAlg) {
        this.owner = owner;
        this.hmmAlg = hmmAlg;
        this.props = props;
    }

    public void run() {
        try {
            hmmAlg.execute();
        } catch (Exception e) {
            ErrorDialog.showErrorDialog(owner, "HMMER alignment failed");
            owner.dispose();
            return;
        }

        try {
            File tempAlignmentFile = File.createTempFile("pfaat_tmp_alignment", ".pfam");

            AlignmentLoader.saveAlignmentFile(tempAlignmentFile,
                props.getColorScheme(),
                owner.getAlignment());
            Alignment newAlignment = AlignmentLoader.loadAlignmentFile(tempAlignmentFile);
            String csHint = AlignmentLoader.getColorSchemeHint(tempAlignmentFile);

            tempAlignmentFile.delete();

            Sequence[] results = hmmAlg.getResults();

            for (int i = results.length; --i >= 0;) {
                if (results[i].length() < 1) {
                    ErrorDialog.showErrorDialog(owner,
                        "Sequence must have data.");
                    return;
                }
                Sequence seq = newAlignment.getSequence(results[i].getName());

                seq.removeGaps();
                results[i].insertAA(AminoAcid.GAP, 0);
                for (int j = 1; j < results[i].length(); j++) {
                    if (results[i].getAA(j).equals(AminoAcid.GAP) || results[i].getAA(j).equals(AminoAcid.X)) {
                        // try {
                        seq.insertAA(AminoAcid.GAP, j);

                        /* }catch (Exception e)
                         {
                         ErrorDialog.showErrorDialog(owner,
                         "Error: pos" + j +"\nresult:"+ results[i].toString()+"\nseq"+seq.toString());
                         throw e;
                         }*/

                    }
                }
            }

            AlignmentFrame newAlignmentFrame = new AlignmentFrame(null, newAlignment, csHint, AlignmentFrame.getCurrentMode());

            newAlignmentFrame.show();
        } catch (IOException ioe) {
            ErrorDialog.showErrorDialog(owner,
                "Could not create temporary alignment file");
            owner.dispose();
            return;
        } catch (Exception e) {
            // StringBuffer st = new StringBuffer();
            StringWriter sw = new StringWriter();

            e.printStackTrace(new PrintWriter(sw));
            ErrorDialog.showErrorDialog(owner,
                "Unable to create new alignment." + e.getMessage() + sw.toString());

            return;
        }
    }
}
