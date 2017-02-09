package com.neogenesis.pfaat.util;


import java.io.*;
import javax.swing.filechooser.*;


/**
 * An implementation of
 * <code>javax.swing.filechooser.FileFilter</code> which allows directories.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:32:06 $ */
public class DirectoryFileFilter extends javax.swing.filechooser.FileFilter 
    implements java.io.FileFilter {
    javax.swing.filechooser.FileFilter other;

    public DirectoryFileFilter() {
        other = null;
    }

    public DirectoryFileFilter(javax.swing.filechooser.FileFilter other) {
        this.other = other;
    }

    public boolean accept(File f) {
        if (f.isDirectory())
            return true;
        return other != null ? other.accept(f) : false;
    }

    public String getDescription() {
        return other != null ? other.getDescription() : null;
    }

}    
