package com.neogenesis.pfaat.util;


import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;


/**
 * Cache commonly used paths for file dialogs.
 *
 * @author $Author: xih $
 * @version $Revision: 1.7 $, $Date: 2002/10/11 18:32:06 $ */
public class PathManager {
    private static File alignment_path = new File(".");
    private static File local_path = new File(".");
    // begin: phdana
    private static File rasmol_executable = null;
    // end: phdana

    public static File getAlignmentPath() {
        return alignment_path;
    }

    public static void setAlignmentPath(File f) {
        alignment_path = f;
    }

    public static File getLocalPath() {
        return local_path;
    }

    public static String getPfaatHome() {
        return System.getProperty("pfaat.home");
    }

    public static File getColorSchemeDirectory() {
        return new File(System.getProperty("pfaat.home"), "colorschemes");
    }

    public static File getResidueDataFile() {
        return new File(System.getProperty("pfaat.home"), "resdata.txt");
    }

    public static File getConfigPropertiesPath() {
        return new File(System.getProperty("pfaat.home"),
                "pfaat.properties");
    }

    // ///////////
    // BETE BEGIN
    // ///////////
    public static File getBeteTreePriorsDirectory() {
        return new File(System.getProperty("pfaat.home"), "betetreepriors");
    }

    // /////////
    // BETE END
    // /////////

    // begin: phdana
    public static File getRasmolDirectory() {
        String s = File.separator;
        String dir = System.getProperty("pfaat.home") + s + "bin" + s + Utils.getOSDirectoryName();

        return new File(dir);
    }

    public static File getRasmolConfigFile() {
        return new File(System.getProperty("pfaat.home"), "rasmol.cfg");
    }

    public static File getRasmolExecutable() {
        // if we already know it
        if (rasmol_executable != null)
            return rasmol_executable;

        // if it does not exist...allow the user to specify it
        File cfg = getRasmolConfigFile();

        if (!cfg.exists())
            return specifyRasmolExecutable();

        // read rasmol executable path from config file
        BufferedReader br = FileUtil.getBufferedReader(cfg);

        if (br == null)
            return null;

        // read the rasmol executable from this file
        String line;

        try {
            line = br.readLine();
        } catch (IOException e) {
            return null;
        }
        if (line == null)
            return null;

        // the only line in file is rasmol executable path
        // if this does not exist...let user specify it
        File f = new File(line);

        if (!f.exists())
            return specifyRasmolExecutable();
        rasmol_executable = f;
        return f;
    }

    public static void setRasmolExecutable(File f) {
        // if this dont exist...do nothing
        if (!f.exists())
            return;

        rasmol_executable = f;

        // save this to config file
        File cfg = getRasmolConfigFile();
        PrintStream ps = FileUtil.getPrintStream(cfg);

        ps.println(f.getAbsolutePath());
        ps.flush();
        ps.close();
    }

    // choose rasmol location
    public static File specifyRasmolExecutable() {
        File f = null;

        // launch browser to open file...
        // in this case we just fake out
        // JOptionPane.showMessageDialog(null,"faking out rasmol choosing");
        String s = File.separator;
        String dir = System.getProperty("pfaat.home") + s + "bin" + s + Utils.getOSDirectoryName() + s;

        if (Utils.getOSDirectoryName().toLowerCase().indexOf("win") >= 0)
            f = new File(dir + "rw32b2a.exe");
        else
            f = new File(dir + "rasmol");

        // if the file was null we canceled out
        if (f == null)
            return null;

        // save this as the new path to rasmol executable
        setRasmolExecutable(f);
        return f;
    }

    // end: phdana
}
