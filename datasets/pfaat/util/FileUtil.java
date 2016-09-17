package com.neogenesis.pfaat.util;


import java.io.*;
import java.util.*;
import javax.swing.*;


public class FileUtil {
    // create a buffered reader for a given file
    public static BufferedReader getBufferedReader(File f) {
        if (f == null)
            return null;

        BufferedReader br;

        try {
            br = new BufferedReader(new FileReader(f.getAbsolutePath()));
        } catch (FileNotFoundException fexception) {
            return null;
        } catch (IOException fexception) {
            return null;
        }

        return br;
    }

    // create a print stream for a given file
    public static PrintStream getPrintStream(File f) {
        if (f == null)
            return null;

        PrintStream ps;

        try {
            FileOutputStream fos = new FileOutputStream(f);

            ps = new PrintStream(new BufferedOutputStream(fos));
        } catch (FileNotFoundException fexception) {
            return null;
        } catch (IOException fexception) {
            return null;
        }

        return ps;
    }

    // close a given buffered reader
    public static boolean close(BufferedReader br) {
        if (br == null)
            return false;

        boolean noProbs = true;

        try {
            br.close();
        } catch (IOException e) {
            noProbs = false;
        }

        return noProbs;
    }

    // recursively delete a given folder
    public static boolean deleteAll(File folder) {
        if (folder == null)
            return true;

        // if this is file...just delete it
        if (folder.isFile())
            return folder.delete();

        // delete all the files and folders in this folder
        boolean noError = true;
        File[]list = folder.listFiles();

        for (int i = 0; i < list.length; i++) {
            File f = list[i];

            // if this is a file delete it
            if (f.isFile()) {
                if (!f.delete())
                    noError = false;
            } else if (f.isDirectory()) {
                // if this is a folder...delete it
                if (!deleteAll(f))
                    noError = false;
            }
        }

        return noError;
    }

    // count lines in a given ascii file
    // on any file error return -1
    // otherwise return the number of lines in the ascii file
    public static int countLines(File f) {
        BufferedReader br = getBufferedReader(f);

        if (br == null)
            return -1;

        // read lines
        int numLines = 0;
        String line;

        for (;;) {
            try {
                line = br.readLine();
            } catch (IOException io_exception) {
                System.out.println("IOException");
                return -1;
            }

            // end of file
            if (line == null)
                break;

            // count this
            numLines++;
        }

        return numLines;
    }

    // skip next line...return false if error
    public static boolean skipLine(BufferedReader br) {
        String line;

        try {
            line = br.readLine();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    // get the index from a line from a CSV file
    public static int getIndex(String line) {
        // if the line dont contain a ":" then there is an error
        if (line.indexOf(":") < 0)
            return -1;

        // try to get the index from the line
        StringTokenizer st = new StringTokenizer(line, ":");
        String s = st.nextToken();
        int index = Integer.parseInt(s);

        return index;
    }

    // get the max index from a CSV file...return -1 on error
    public static int getMaxIndex(File f) {
        BufferedReader br = getBufferedReader(f);

        if (br == null)
            return -1;

        int maxIndex = -1;
        String line;

        for (;;) {
            try {
                line = br.readLine();
            } catch (IOException e) {
                return -1;
            }
            if (line == null)
                break;
            int index = getIndex(line);

            if (index < 0)
                return -1;
            if (index > maxIndex)
                maxIndex = index;
        }
        return maxIndex;
    }

    // write array of floats as one line of Indexed .csv file
    public static boolean writeLineIndexedCSV(PrintStream ps, int index, float[] f) {
        ps.print(index);
        ps.print(": ");
        for (int i = 0; i < f.length; i++) {
            ps.print(f[i]);
            if (i != f.length - 1)
                ps.print(',');
        }
        ps.println();
        if (ps.checkError())
            return false;
        return true;
    }

    // read just the first column from the next line of a given CSV file
    // after caling this you can call a 'readLine' function to
    // get the data for the line
    public static String readFirstColumn(BufferedReader br) {
        char buff[] = new char[128];
        int n = 0;
        int c = 0;

        for (;;) {
            // read chars until ":" or ',' or cr/lf
            // if we hit ',' or crlf then this is an error
            try {
                c = br.read();
            } catch (IOException io_exception) {
                return null;
            }
            if (c == ',')
                break;
            if (c == ' ' || c == '\t')
                continue;
            // end of line
            if (c == '\r' || c == '\n')
                return null;
            // if we hit end of buffer thats an error
            if (n >= 128)
                return null;
            // ok save this char
            if (n < 128)
                buff[n++] = (char) c;
        }
        // zero term this sucker
        buff[n] = 0;

        // get value
        String value = new String(buff, 0, n);

        return value;
    }

    // read just the index from the next line of a given Indexed CSV file
    // after caling this you can call either form of readLineCSV() to
    // get the data for the line
    public static int readIndex(BufferedReader br) {
        String s = readFirstColumn(br);

        if (s == null)
            return -1;

        int index = Integer.parseInt(s);

        return index;
    }

    // read an array of floats from next line of given Indexed CSV file
    // this version is for reading into preallocated array...the array
    // can be very large because this function does not buffer all the input
    // into a String, it scans the file char by char parsing the line.
    // return -1 on error or the index number for the line read
    public static int readLineIndexedCSV(BufferedReader br, float[] f) {
        int index = readIndex(br);

        if (index < 0)
            return index;

        if (!readLineCSV(br, f))
            return -1;

        return index;
    }

    // write array of floats as one line of .csv file
    public static boolean writeLineCSV(PrintStream ps, float[] f) {
        for (int i = 0; i < f.length; i++) {
            ps.print(f[i]);
            if (i != f.length - 1)
                ps.print(',');
        }
        ps.println();
        if (ps.checkError())
            return false;
        return true;
    }

    // write given number of entries from array of floats as one line of .csv file
    public static boolean writeLineCSV(PrintStream ps, float[] f, int num) {
        if (num > f.length)
            return false;

        for (int i = 0; i < num; i++) {
            ps.print(f[i]);
            if (i != f.length - 1)
                ps.print(',');
        }
        ps.println();
        if (ps.checkError())
            return false;
        return true;
    }

    // write a 2D array of floats as a .csv file
    public static boolean writeMatrixCSV(File file, float[][] f) {
        PrintStream ps = getPrintStream(file);

        if (ps == null)
            return false;

        for (int i = 0; i < f.length; i++) {
            if (!writeLineCSV(ps, f[i]))
                return false;
        }
        ps.flush();
        ps.close();

        return true;
    }

    // write a 2D array of floats as an INDEXED .csv file, using given array f
    // and given index array 'index'. Start at given offset into f, and write
    // index.length items
    public static boolean writeIndexedMatrixCSV(File file, float[][] f,
        int offset, int[] index) {
        PrintStream ps = getPrintStream(file);

        if (ps == null)
            return false;
        if (f.length < offset + index.length)
            return false;

        for (int i = 0; i < index.length; i++) {
            // write the index first
            ps.print(index[i]);
            ps.print(", ");

            // now write the rest
            if (!writeLineCSV(ps, f[i + offset]))
                return false;
        }
        ps.flush();
        ps.close();

        return true;
    }

    // read an array of floats from next line of given file
    // this version is for reading into preallocated array
    // which can be very large
    public static boolean readLineCSV(BufferedReader br, float[] f) {
        // one line as a string could be many megabytes
        // so lets NOT just call readLine() but do it the hard way
        int numRead = 0;
        char buff[] = new char[64];

        for (int i = 0; i < f.length; i++) {
            int n = 0;
            int c = 0;

            for (;;) {
                // read chars until comma or cr/lf
                try {
                    c = br.read();
                } catch (IOException io_exception) {
                    return false;
                }
                if (c == ',')
                    break;
                if (c == ' ' || c == '\t')
                    continue;
                // end of line
                if (c == '\r' || c == '\n')
                    break;
                // ok save it
                buff[n++] = (char) c;
            }
            // zero term this sucker
            buff[n] = 0;

            // ok now we have a char array
            String value = new String(buff, 0, n);

            f[i] = Float.parseFloat(value);
            numRead++;

            // if we read a carriage return
            // attemp to read a line feed
            // if we dont then restore the char we read
            if (c == '\r') {
                try {
                    br.mark(2);
                    int c2 = br.read();

                    if (c2 != '\n')
                        br.reset();
                } catch (IOException io_exception) {
                    return false;
                }
            }
        }

        // success
        return true;
    }

    // read an array of floats from next line of given file
    // this version allocates array for you based on num of nums in line of file
    // dont use for ginormous arrays because this reads entire line into string
    public static float[] readLineCSV(BufferedReader br) {
        String s;

        try {
            s = br.readLine();
        } catch (IOException io_exception) {
            return null;
        }
        if (s == null)
            return null;
        StringTokenizer st = new StringTokenizer(s, ",");
        int numFloats = st.countTokens();

        if (numFloats < 1)
            return null;
        float[] f = new float[numFloats];

        if (f == null)
            return null;
        for (int i = 0; i < numFloats; i++)
            f[i] = Float.parseFloat(st.nextToken());
        return f;
    }

    // read an array of doubles next line of given file
    // this version allocates array for you based on num of nums in line of file
    // dont use for ginormous arrays because this reads entire line into string
    public static double[] readLineDoubleCSV(BufferedReader br) {
        String s;

        try {
            s = br.readLine();
        } catch (IOException io_exception) {
            return null;
        }
        if (s == null)
            return null;
        StringTokenizer st = new StringTokenizer(s, ",");
        int numDoubles = st.countTokens();

        if (numDoubles < 1)
            return null;
        double[] d = new double[numDoubles];

        for (int i = 0; i < numDoubles; i++)
            d[i] = Double.parseDouble(st.nextToken());
        return d;
    }

    // read an array of ints from next line of given file
    // this version allocates array for you based on num of nums in line of file
    // dont use for ginormous arrays because this reads entire line into string
    public static int[] readIntLineCSV(BufferedReader br) {
        String s;

        try {
            s = br.readLine();
        } catch (IOException io_exception) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(s, ",");
        int numInts = st.countTokens();

        // if (numInts < 1)
        // return null;
        if (numInts < 0)
            return null;
        int[] a = new int[numInts];

        if (a == null)
            return null;
        for (int i = 0; i < numInts; i++) {
            String d = st.nextToken().trim();

            a[i] = Integer.parseInt(d);
        }
        return a;
    }

    // read 2D array into an already allocated array
    public static boolean readMatrixCSV(File file, float[][] f) {
        BufferedReader br = getBufferedReader(file);

        if (br == null)
            return false;

        for (int i = 0; i < f.length; i++) {
            if (!readLineCSV(br, f[i]))
                return false;
        }

        return true;
    }

    // read and allocate a 2D array from a given file
    public static float[][] readMatrixCSV(File file) {
        int numLines = countLines(file);

        if (numLines < 1)
            return null;

        float[][] f = new float[numLines][];

        if (f == null)
            return null;

        BufferedReader br = getBufferedReader(file);

        if (br == null)
            return null;

        for (int i = 0; i < f.length; i++) {
            f[i] = readLineCSV(br);
            if (f[i] == null)
                return null;
        }

        return f;
    }

    // read and allocate a 2D array from a given file
    // ignore indexed information at start of each line
    public static float[][] readMatrixIgnoreIndexesCSV(File file) {
        int numLines = countLines(file);

        if (numLines < 1)
            return null;

        float[][] f = new float[numLines][];

        if (f == null)
            return null;

        BufferedReader br = getBufferedReader(file);

        if (br == null)
            return null;

        for (int i = 0; i < f.length; i++) {
            // just ignore index at start of line
            int index = readIndex(br);

            f[i] = readLineCSV(br);
            if (f[i] == null)
                return null;
        }

        return f;
    }

    // merge an indexed matrix into an array
    public static boolean mergeIndexedMatrixCSV(File file, float[][] f,
        int numColumns) {
        // count the lines
        int numLines = countLines(file);

        if (numLines < 0)
            return false;
        if (numLines == 0)
            return true;

        // merge each line of this file
        BufferedReader br = getBufferedReader(file);

        if (br == null)
            return false;
        for (int i = 0; i < numLines; i++) {
            float[] row = new float[numColumns];
            int index = readLineIndexedCSV(br, row);

            if (index < 0 || index >= f.length)
                return false;
            f[index] = row;
        }

        try {
            br.close();
        } catch (IOException e) {}

        return true;
    }

    // read an array of ints from a given file
    public static int[] readColumnCSV(File file) {
        // count lines
        int numLines = countLines(file);

        if (numLines < 0)
            return null;
        int[] a = new int[numLines];

        if (numLines == 0)
            return a;

        // get buffered reader
        BufferedReader br = getBufferedReader(file);

        if (br == null)
            return null;

        // read into array
        String line;

        for (int i = 0; i < a.length; i++) {
            try {
                line = br.readLine();
            } catch (IOException io_exception) {
                return null;
            }

            // read this
            a[i] = Integer.parseInt(line);
        }

        // success
        return a;
    }

    // read an array of ints from a given file
    public static boolean readColumnCSV(File file, int[] iarray) {
        // get buffered read
        BufferedReader br = getBufferedReader(file);

        if (br == null)
            return false;

        // read into array
        String line;

        for (int i = 0; i < iarray.length; i++) {
            // read chars until comma or cr/lf
            try {
                line = br.readLine();
            } catch (IOException io_exception) {
                return false;
            }

            // read this
            iarray[i] = Integer.parseInt(line);
        }

        // success
        return true;
    }

    // write array of ints to a given file
    public static boolean writeColumnCSV(File file, int[] iarray) {
        // print stream
        PrintStream ps = getPrintStream(file);

        if (ps == null)
            return false;

        // write array
        for (int i = 0; i < iarray.length; i++) {
            ps.println(iarray[i]);
            if (ps.checkError())
                return false;
        }

        return true;
    }

    // read the first column from a .csv file and return as array of strings
    public static String[] readFirstColumnCSV(File file) {
        // count lines
        int numLines = countLines(file);

        if (numLines < 0)
            return null;
        String[] a = new String[numLines];

        if (numLines == 0)
            return a;

        // get buffered read
        BufferedReader br = getBufferedReader(file);

        if (br == null)
            return null;

        // read into array of strings
        String line;

        for (int i = 0; i < a.length; i++) {
            // read chars until comma or cr/lf
            try {
                line = br.readLine();
            } catch (IOException io_exception) {
                return null;
            }

            StringTokenizer st = new StringTokenizer(line, ",");

            a[i] = st.nextToken();
        }
        return a;
    }

    // read matrix from CSV file and return as doubles (skip first column)
    public static double[][] readMatrixSkipFirstColumnCSV(File file) {
        int numLines = countLines(file);

        if (numLines < 1)
            return null;

        double[][] d = new double[numLines][];

        if (d == null)
            return null;

        BufferedReader br = getBufferedReader(file);

        if (br == null)
            return null;

        for (int i = 0; i < d.length; i++) {
            // just ignore name in the first column
            String name = readFirstColumn(br);

            d[i] = readLineDoubleCSV(br);
            if (d[i] == null)
                return null;
        }

        return d;
    }

    // browse for a file and return file or null if canceled
    // if prevFile non null then use that as default selection
    public static File browseFile(File prevFile) {
        // display the 'open file' dialog
        JFileChooser fc = new JFileChooser();

        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (prevFile != null)
            fc.setSelectedFile(prevFile);
        int returnVal = fc.showOpenDialog(null);

        if (returnVal != JFileChooser.APPROVE_OPTION)
            return null;
        File file = fc.getSelectedFile();

        return file;
    }

}

