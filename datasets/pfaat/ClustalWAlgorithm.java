package com.neogenesis.pfaat;


import java.io.*;
import java.util.*;

import com.neogenesis.pfaat.util.*;
import com.neogenesis.pfaat.swingx.*;


abstract public class ClustalWAlgorithm {
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
                    if (((j + 1) % 80 == 0) || (j == length - 1)) {
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

    protected void loadResults(File tempFile) throws Exception {
        ArrayList idList = new ArrayList();
        ArrayList bufferList = new ArrayList();
        HashMap bufferMap = new HashMap();

        try {
            BufferedReader in = new BufferedReader(new FileReader(tempFile));

            in.readLine(); // skip output file header

            String line = null;

            while ((line = in.readLine()) != null) {
                if ((line.length() > 0) && !line.startsWith(" ")) {
                    StringTokenizer btoken = new StringTokenizer(line);
                    String id = btoken.nextToken();
                    StringBuffer buffer = (StringBuffer) bufferMap.get(id);

                    if (buffer == null) {
                        idList.add(id);
                        buffer = new StringBuffer();
                        bufferMap.put(id, buffer);
                        bufferList.add(buffer);
                    }
                    buffer.append(btoken.nextToken());
                }
            }
            in.close();
        } catch (IOException e) {
            ErrorDialog.showErrorDialog(owner, "Unable to parse clustal alignment file.");
            throw e;
        }

        String[] idArray = (String[]) idList.toArray(new String[] {}
            );
        StringBuffer[] bufferArray = (StringBuffer[]) bufferList.toArray(new StringBuffer[] {}
            );

        results = new Sequence[bufferArray.length];
        for (int i = bufferArray.length; --i >= 0;) {
            results[i] = new Sequence(idArray[i], AminoAcid.stringToAA(bufferArray[i].toString()));
        }
    }

    private static class StandardAlgorithm extends ClustalWAlgorithm {
        public StandardAlgorithm(AlignmentFrame owner, Sequence[] seqs) {
            initialize(owner, seqs);
            model = null;
        }

        public void execute() throws Exception {
            try {
                File tempSeqsFile = null;
                File tempDNDFile = null;
                File tempResultsFile = null;

                try {
                    tempSeqsFile = File.createTempFile("pfs", ".fa");

                    String tempSeqsFileName = tempSeqsFile.getAbsolutePath();
                    int p = tempSeqsFileName.lastIndexOf(".fa");

                    StringBuffer tempDNDFileName = new StringBuffer(tempSeqsFileName);

                    tempDNDFileName.delete(p, tempDNDFileName.length());
                    tempDNDFileName.append(".dnd");
                    tempDNDFile = new File(tempDNDFileName.toString());

                    tempResultsFile = File.createTempFile("pfr", ".fa");
                } catch (IOException ioe) {
                    ErrorDialog.showErrorDialog(owner, "Failed to construct temp file pfs.fa. " + ioe.getMessage());
                    throw ioe;
                }
                writeTempFile(tempSeqsFile, seqs);
                try {
                    String s = File.separator;
                    String o = Utils.getCmdOptionChar();
                    String dir = System.getProperty("pfaat.home") + s + "bin" + s + Utils.getOSDirectoryName() + s;
                    Runtime run = Runtime.getRuntime();
                    String cmd = dir + "clustalw " +
                        o + "type=protein " +
                        o + "infile=" + tempSeqsFile.getAbsolutePath() + " " +
                        o + "outfile=" + tempResultsFile.getAbsolutePath();

                    System.out.println("Debug:: " + cmd);
                    Process prc = run.exec(cmd);

                    prc.waitFor();
                } catch (Exception e) {
                    ErrorDialog.showErrorDialog(owner, "Call to clustal failed. " + e.getMessage());
                    throw e;
                }
                loadResults(tempResultsFile);

                tempSeqsFile.delete();
                tempDNDFile.delete();
                tempResultsFile.delete();
            } catch (Exception e) {
                throw e;
            }
        }
    }


    private static class ProfileAlgorithm extends ClustalWAlgorithm {
        public ProfileAlgorithm(AlignmentFrame owner, Sequence[] seqs, Sequence[] model) {
            initialize(owner, seqs);
            this.model = model;
        }

        public void execute() throws Exception {
            try {
                File tempModelFile = null;
                File tempSeqsFile = null;
                File tempDNDFile = null;
                File tempResultsFile = null;

                try {
                    tempModelFile = File.createTempFile("pfm", ".fa");
                    tempSeqsFile = File.createTempFile("pfs", ".fa");

                    String tempSeqsFileName = tempSeqsFile.getAbsolutePath();
                    int p = tempSeqsFileName.lastIndexOf(".fa");

                    StringBuffer tempDNDFileName = new StringBuffer(tempSeqsFileName);

                    tempDNDFileName.delete(p, tempDNDFileName.length());
                    tempDNDFileName.append(".dnd");
                    tempDNDFile = new File(tempDNDFileName.toString());

                    String tempResultFileName = tempSeqsFileName.substring(0, p) + ".aln";

                    tempResultsFile = new File(tempResultFileName);
                } catch (IOException ioe) {
                    ErrorDialog.showErrorDialog(owner, "Failed to construct temp file." + ioe.getMessage());
                    throw ioe;
                }
                writeTempFile(tempModelFile, model);
                writeTempFile(tempSeqsFile, seqs);
                try {
                    String s = File.separator;
                    String o = Utils.getCmdOptionChar();
                    String dir = System.getProperty("pfaat.home") + s + "bin" + s + Utils.getOSDirectoryName() + s;
                    Runtime run = Runtime.getRuntime();
                    String cmd = dir + "clustalw " + o + "sequences " +
                        o + "type=protein " +
                        o + "profile1=" + tempModelFile.getAbsolutePath() + " " +
                        o + "profile2=" + tempSeqsFile.getAbsolutePath();

                    // o + "outfile=" + tempResultsFile.getAbsolutePath();
                    System.out.println("DEBUG:: " + cmd);
                    Process prc = run.exec(cmd);

                    prc.waitFor();
                } catch (Exception e) {
                    ErrorDialog.showErrorDialog(owner, "Call to clustal failed. " + e.getMessage());
                    throw e;
                }
                loadResults(tempResultsFile);

                tempModelFile.delete();
                tempSeqsFile.delete();
                tempDNDFile.delete();
                tempResultsFile.delete();
            } catch (Exception e) {
                throw e;
            }
        }
    }


    private static class ProfileProfileAlgorithm extends ClustalWAlgorithm {
        public ProfileProfileAlgorithm(AlignmentFrame owner, Sequence[] seqs, Sequence[] model) {
            initialize(owner, seqs);
            this.model = model;
        }

        public void execute() throws Exception {
            try {
                File tempModelFile = null;
                File tempSeqsFile = null;
                File tempDNDFile = null;
                File tempResultsFile = null;

                try {
                    tempModelFile = File.createTempFile("pfm", ".fa");
                    tempSeqsFile = File.createTempFile("pfs", ".fa");

                    String tempSeqsFileName = tempSeqsFile.getAbsolutePath();
                    int p = tempSeqsFileName.lastIndexOf(".fa");

                    StringBuffer tempDNDFileName = new StringBuffer(tempSeqsFileName);

                    tempDNDFileName.delete(p, tempDNDFileName.length());
                    tempDNDFileName.append(".dnd");
                    tempDNDFile = new File(tempDNDFileName.toString());

                    String tempResultFileName = tempSeqsFileName.substring(0, p) + ".aln";

                    tempResultsFile = new File(tempResultFileName);
                } catch (IOException ioe) {
                    ErrorDialog.showErrorDialog(owner, "Failed to construct temp file. " + ioe.getMessage());
                    throw ioe;
                }
                writeTempFile(tempModelFile, model);
                writeTempFile(tempSeqsFile, seqs);
                try {
                    String s = File.separator;
                    String o = Utils.getCmdOptionChar();
                    String dir = System.getProperty("pfaat.home") + s + "bin" + s + Utils.getOSDirectoryName() + s;
                    Runtime run = Runtime.getRuntime();
                    Process prc = run.exec(dir + "clustalw " + o + "profile " +
                            o + "type=protein " +
                            o + "profile1=" + tempModelFile.getAbsolutePath() + " " +
                            o + "profile2=" + tempSeqsFile.getAbsolutePath());

                    // o + "outfile=" + tempResultsFile.getAbsolutePath());
                    prc.waitFor();
                } catch (Exception e) {
                    ErrorDialog.showErrorDialog(owner, "Call to clustal failed. " + e.getMessage());
                    throw e;
                }
                loadResults(tempResultsFile);

                tempModelFile.delete();
                tempSeqsFile.delete();
                tempDNDFile.delete();
                tempResultsFile.delete();
            } catch (Exception e) {
                throw e;
            }
        }
    }

    public static ClustalWAlgorithm createStandardAlgorithm(AlignmentFrame owner,
        Sequence[] alignment) {
        return (new StandardAlgorithm(owner, alignment));
    }

    public static ClustalWAlgorithm createProfileAlgorithm(AlignmentFrame owner,
        Sequence[] model,
        Sequence[] alignment) {
        return (new ProfileAlgorithm(owner, alignment, model));
    }

    public static ClustalWAlgorithm createProfileProfileAlgorithm(AlignmentFrame owner,
        Sequence[] model,
        Sequence[] alignment) {
        return (new ProfileProfileAlgorithm(owner, alignment, model));
    }
}

