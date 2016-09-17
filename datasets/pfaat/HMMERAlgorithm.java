package com.neogenesis.pfaat;


import java.io.*;
import java.util.*;

import com.neogenesis.pfaat.util.*;
import com.neogenesis.pfaat.io.*;
import com.neogenesis.pfaat.swingx.*;


abstract public class HMMERAlgorithm {
    protected Sequence[] model;
    protected Sequence[] seqs;
    protected Sequence[] results;
    protected AlignmentFrame owner;

    protected void initialize(AlignmentFrame owner, Sequence[] seqs) {
        this.owner = owner;
        this.seqs = seqs;
    }

    public Sequence[] getResults() {
        return (results);
    }

    abstract public void execute() throws Exception;

    private StringBuffer[] generateSequences(Sequence[] seqs) {
        StringBuffer[] sequences = new StringBuffer[seqs.length];

        for (int i = seqs.length; --i >= 0;) {
            sequences[i] = new StringBuffer(seqs[i].toString());
            sequences[i].deleteCharAt(0);
        }
        return (sequences);
    }

    protected void writeTempFile(File tempFile, Sequence[] seqs) throws Exception {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(tempFile)));
            StringBuffer[] sequences = generateSequences(seqs);

            for (int i = 0; i < seqs.length; i++) {
                out.println("> " + seqs[i].getName());
                int length = sequences[i].length();

                for (int j = 0; j < length; j++) {
                    out.print(sequences[i].charAt(j));
                    if (((j + 1) % 60 == 0) || (j == length - 1)) {
                        out.println("");
                    }
                }
            }
            out.close();
        } catch (IOException e) {
            ErrorDialog.showErrorDialog(owner, "Write to temp file failed.");
            throw e;
        }
    }

    protected void writeTempModelFile(File tempFile, Sequence[] seqs) throws Exception {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(tempFile)));
            StringBuffer[] names = new StringBuffer[seqs.length];
            StringBuffer[] sequences = generateSequences(seqs);

            for (int i = 0; i < seqs.length; i++) {
                names[i] = new StringBuffer(seqs[i].getName());
                for (int j = names[i].length(); --j >= 0;) {
                    if (names[i].charAt(j) == ' ')
                        names[i].setCharAt(j, '_');
                }
                for (int j = names[i].length(); j < 30; j++) {
                    names[i].append(' ');
                }
            }

            for (int i = 0; i < seqs.length; i++) {
                out.println("#=SQ " + names[i].toString() + "  1.0000 - - 0..0::0 -");
            }
            out.println("");

            int length = sequences[0].length();
            int chunks = length / 50;

            for (int i = 0; i < chunks; i++) {
                for (int j = 0; j < seqs.length; j++) {
                    out.println(names[j].toString() +
                        " " + sequences[j].substring(50 * i,
                            50 * (i + 1)));
                }
                out.println("");
            }
            if ((length - (50 * chunks)) > 0) {
                for (int j = 0; j < seqs.length; j++) {
                    out.println(names[j].toString() +
                        " " + sequences[j].substring(50 * chunks,
                            length));
                }
                out.println("");
            }
            out.close();
        } catch (IOException e) {
            ErrorDialog.showErrorDialog(owner, "Write to temp file failed.");
            throw e;
        }

        /*
         try{
         PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(tempFile)));
         Alignment tempAlignment = new Alignment(tempFile.getAbsolutePath(), seqs);
         MSFLoader msfLoader = new MSFLoader();
         msfLoader.saveAlignment(out, tempAlignment);
         out.close();
         }
         catch (IOException e) {
         ErrorDialog.showErrorDialog(owner, "Write to temp file failed.");
         throw e;
         }
         */
    }

    protected void writeTempWithaliFile(File tempFile, Sequence[] seqs) throws Exception {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(tempFile)));
            StringBuffer[] names = new StringBuffer[seqs.length];
            StringBuffer[] sequences = generateSequences(seqs);

            for (int i = 0; i < seqs.length; i++) {
                names[i] = new StringBuffer(seqs[i].getName());
                for (int j = names[i].length(); --j >= 0;) {
                    if (names[i].charAt(j) == ' ')
                        names[i].setCharAt(j, '_');
                }
                for (int j = names[i].length(); j < 30; j++) {
                    names[i].append(' ');
                }
            }

            for (int i = 0; i < seqs.length; i++) {
                out.println("#=SQ " + names[i].toString() + "  1.0000 - - 0..0::0 -");
            }
            out.println("");

            int length = sequences[0].length();
            int chunks = length / 50;

            for (int i = 0; i < chunks; i++) {
                for (int j = 0; j < seqs.length; j++) {
                    out.println(names[j].toString() +
                        " " + sequences[j].substring(50 * i,
                            50 * (i + 1)));
                }
                out.println("");
            }
            if ((length - (50 * chunks)) > 0) {
                for (int j = 0; j < seqs.length; j++) {
                    out.println(names[j].toString() +
                        " " + sequences[j].substring(50 * chunks,
                            length));
                }
                out.println("");
            }
            out.close();
        } catch (IOException e) {
            ErrorDialog.showErrorDialog(owner, "Write to temp file failed.");
            throw e;
        }

        /*
         try{
         PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(tempFile)));
         Alignment tempAlignment = new Alignment(tempFile.getAbsolutePath(), seqs);
         MSFLoader msfLoader = new MSFLoader();
         msfLoader.saveAlignment(out, tempAlignment);
         out.close();
         }
         catch (IOException e) {
         ErrorDialog.showErrorDialog(owner, "Write to temp file failed.");
         throw e;
         }
         */
    }

    protected void loadResults(File tempFile) throws Exception {
        ArrayList idList = new ArrayList();
        ArrayList bufferList = new ArrayList();
        HashMap bufferMap = new HashMap();
        StringBuffer reference = null;

        try {
            BufferedReader in = new BufferedReader(new FileReader(tempFile));

            in.readLine(); // skip output file header

            String line = null;

            while ((line = in.readLine()) != null) {
                if ((line.length() > 0) && (line.startsWith("#=RF") || !line.startsWith("#")) && !line.startsWith(" ")) {
                    StringTokenizer btoken = new StringTokenizer(line);
                    String id = btoken.nextToken();
                    StringBuffer buffer;

                    if (id.equals("#=RF")) {
                        if (reference == null) {
                            reference = new StringBuffer();
                        }
                        buffer = reference;
                    } else {
                        buffer = (StringBuffer) bufferMap.get(id);
                        if (buffer == null) {
                            idList.add(id);
                            buffer = new StringBuffer();
                            bufferMap.put(id, buffer);
                            bufferList.add(buffer);
                        }
                    }
                    buffer.append(btoken.nextToken());
                }
            }
            in.close();
        } catch (IOException e) {
            ErrorDialog.showErrorDialog(owner, "Unable to parse hmmer alignment file.");
            throw e;
        }

        String[] idArray = (String[]) idList.toArray(new String[] {}
            );
        StringBuffer[] bufferArray = (StringBuffer[]) bufferList.toArray(new StringBuffer[] {}
            );

        Sequence[] modelResults = new Sequence[model.length];

        for (int i = model.length; --i >= 0;) {
            modelResults[i] = new Sequence(model[i].getName(), model[i].getAllAA());
            modelResults[i].deleteAA(0);
        }
        trimGaps(modelResults);

        results = new Sequence[bufferArray.length + modelResults.length];
        for (int i = bufferArray.length; --i >= 0;) {
            String resultString = bufferArray[i].toString();

            resultString.replace('x', '-');
            resultString.replace('.', '-');
            resultString.replace('X', '-');
            results[i] = new Sequence(idArray[i], AminoAcid.stringToAA(resultString));
        }
        for (int i = modelResults.length; --i >= 0;) {
            results[i + bufferArray.length] = modelResults[i];
        }
        char[] referenceChars = reference.toString().toCharArray();

        for (int i = 0; i < referenceChars.length; i++) {
            if (referenceChars[i] == '.') {
                for (int j = modelResults.length; --j >= 0;) {
                    results[j + bufferArray.length].insertAA(AminoAcid.GAP, i);
                }
            }
        }
    }

    private static class ProfileAlgorithm extends HMMERAlgorithm {
        public ProfileAlgorithm(AlignmentFrame owner, Sequence[] seqs, Sequence[] model) {
            initialize(owner, seqs);
            this.model = model;
        }

        public void execute() throws Exception {
            try {
                File tempModelFile = null;
                File tempSeqsFile = null;
                File tempHMMFile = null;
                File tempResultsFile = null;

                try {
                    tempModelFile = File.createTempFile("pfaat_model", ".tmp");
                    tempSeqsFile = File.createTempFile("pfaat_seqs", ".tmp");

                    String tempSeqsFileName = tempSeqsFile.getAbsolutePath();
                    int p = tempSeqsFileName.lastIndexOf("_seqs");

                    StringBuffer tempHMMFileName = new StringBuffer(tempSeqsFileName);

                    tempHMMFileName.delete(p, p + 5);
                    tempHMMFileName.insert(p, "_hmmer");
                    tempHMMFile = new File(tempHMMFileName.toString());

                    StringBuffer tempResultsFileName = new StringBuffer(tempSeqsFileName);

                    tempResultsFileName.delete(p, p + 5);
                    tempResultsFileName.insert(p, "_results");
                    tempResultsFile = new File(tempResultsFileName.toString());
                } catch (IOException ioe) {
                    ErrorDialog.showErrorDialog(owner, "Failed to construct temp file");
                    throw ioe;

                }

                writeTempModelFile(tempModelFile, model);
                writeTempFile(tempSeqsFile, seqs);

                try {
                    String s = File.separator;
                    String dir = System.getProperty("pfaat.home") + s + "bin" + s + Utils.getOSDirectoryName() + s;
                    Runtime run = Runtime.getRuntime();
                    Process prc = run.exec(dir + "hmmbuild -n pfaat " +
                            tempHMMFile.getAbsolutePath() + " " +
                            tempModelFile.getAbsolutePath());

                    prc.waitFor();
                } catch (Exception e) {
                    ErrorDialog.showErrorDialog(owner, "Call to hmmbuild failed.");
                    throw e;
                }

                try {
                    String s = File.separator;
                    String dir = System.getProperty("pfaat.home") + s + "bin" + s + Utils.getOSDirectoryName() + s;
                    Runtime run = Runtime.getRuntime();
                    Process prc = run.exec(dir + "hmmalign " +
                            "-o " + tempResultsFile.getAbsolutePath() + " " +
                            "-q " +
                            tempHMMFile.getAbsolutePath() + " " +
                            tempSeqsFile.getAbsolutePath());

                    prc.waitFor();
                } catch (Exception e) {
                    ErrorDialog.showErrorDialog(owner, "Call to hmmalign failed.");
                    throw e;
                }

                loadResults(tempResultsFile);

                tempModelFile.delete();
                tempSeqsFile.delete();
                tempHMMFile.delete();
                tempResultsFile.delete();
            } catch (Exception e) {
                throw e;
            }
        }
    }


    // need to flesh this algorithm out to utilize withali information
    private static class WithaliAlgorithm extends HMMERAlgorithm {
        private Sequence[] withali;

        public WithaliAlgorithm(AlignmentFrame owner, Sequence[] seqs, Sequence[] model, Sequence[] withali) {
            initialize(owner, seqs);
            this.model = model;

            this.withali = withali;
        }

        public void execute() throws Exception {
            try {
                File tempModelFile = null;
                File tempSeqsFile = null;
                File tempWithaliFile = null;
                File tempHMMFile = null;
                File tempResultsFile = null;

                try {
                    tempModelFile = File.createTempFile("pfaat_model", ".tmp");
                    tempSeqsFile = File.createTempFile("pfaat_seqs", ".tmp");
                    tempWithaliFile = File.createTempFile("pfaat_withali", ".tmp");

                    String tempSeqsFileName = tempSeqsFile.getAbsolutePath();
                    int p = tempSeqsFileName.lastIndexOf("_seqs");

                    StringBuffer tempHMMFileName = new StringBuffer(tempSeqsFileName);

                    tempHMMFileName.delete(p, p + 5);
                    tempHMMFileName.insert(p, "_hmmer");
                    tempHMMFile = new File(tempHMMFileName.toString());

                    StringBuffer tempResultsFileName = new StringBuffer(tempSeqsFileName);

                    tempResultsFileName.delete(p, p + 5);
                    tempResultsFileName.insert(p, "_results");
                    tempResultsFile = new File(tempResultsFileName.toString());
                } catch (IOException ioe) {
                    ErrorDialog.showErrorDialog(owner, "Failed to construct temp file");
                    throw ioe;

                }

                writeTempModelFile(tempModelFile, model);
                writeTempWithaliFile(tempWithaliFile, withali);
                writeTempFile(tempSeqsFile, seqs);

                try {
                    String s = File.separator;
                    String dir = System.getProperty("pfaat.home") + s + "bin" + s + Utils.getOSDirectoryName() + s;
                    Runtime run = Runtime.getRuntime();
                    Process prc = run.exec(dir + "hmmbuild -n pfaat " +
                            tempHMMFile.getAbsolutePath() + " " +
                            tempModelFile.getAbsolutePath());

                    prc.waitFor();
                } catch (Exception e) {
                    ErrorDialog.showErrorDialog(owner, "Call to hmmbuild failed.");
                    throw e;
                }

                try {
                    String s = File.separator;
                    String dir = System.getProperty("pfaat.home") + s + "bin" + s + Utils.getOSDirectoryName() + s;
                    Runtime run = Runtime.getRuntime();
                    Process prc = run.exec(dir + "hmmalign " +
                            "-o " + tempResultsFile.getAbsolutePath() + " " +
                            "-q " +
                            "--withali " + tempWithaliFile.getAbsolutePath() + " " +
                            tempHMMFile.getAbsolutePath() + " " +
                            tempSeqsFile.getAbsolutePath());

                    prc.waitFor();
                } catch (Exception e) {
                    ErrorDialog.showErrorDialog(owner, "Call to hmmalign failed.");
                    throw e;
                }

                loadResults(tempResultsFile);

                /*
                 tempModelFile.delete();
                 tempSeqsFile.delete();
                 tempWithaliFile.delete();
                 tempHMMFile.delete();
                 tempResultsFile.delete();
                 */
            } catch (Exception e) {
                throw e;
            }
        }
    }

    protected void trimGaps(Sequence[] seqs) {
        int length = Integer.MAX_VALUE;

        for (int i = seqs.length; --i >= 0;) {
            if (seqs[i].length() < length) {
                length = seqs[i].length();
            }
        }

        boolean trimming = true;;
        while (trimming) {
            boolean trim = true;

            for (int j = seqs.length; --j >= 0;) {
                if (!seqs[j].getAA(0).equals(AminoAcid.GAP)) {
                    trim = false;
                }
            }
            if (trim) {
                for (int j = seqs.length; --j >= 0;) {
                    try {
                        seqs[j].deleteAA(0);
                    } catch (Exception e) {// An exception here would be fatal to the consistency
                        // of the hmmer algorithm
                        // As such, there is no clean way of handling it
                    }
                }
                length--;
                if (length <= 0) {
                    trimming = false;
                }
            } else {
                trimming = false;
            }
        }
    }

    public static HMMERAlgorithm createProfileAlgorithm(AlignmentFrame owner,
        Sequence[] model,
        Sequence[] alignment) {
        return (new ProfileAlgorithm(owner, alignment, model));
    }

    public static HMMERAlgorithm createWithaliAlgorithm(AlignmentFrame owner,
        Sequence[] model,
        Sequence[] alignment,
        Sequence[] withali) {
        return (new WithaliAlgorithm(owner, alignment, model, withali));
    }
}

