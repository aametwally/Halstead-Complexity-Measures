package com.neogenesis.pfaat.swingx;


import java.io.*;
import java.awt.Color;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;


/**
 * A display area for console output.  This component
 * will display the output of a PrintStream, PrintWriter,
 * or of a running process in a swing text component.  
 * The text from the output and error pipes to the child 
 * process can be displayed with whatever character 
 * attributes desired.
 * 
 * @author  Timothy Prinzing
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:31:09 $ 
 */
public class ConsolePane extends JScrollPane {
    private JTextComponent outputArea;

    /**
     * Create a console display.  By default
     * the text region is set to not be editable.
     */
    public ConsolePane() {
        super();
        outputArea = createOutputArea();
        outputArea.setEditable(false);
        JViewport vp = getViewport();

        vp.add(outputArea);
        // vp.setBackingStoreEnabled(true);
    }

    /**
     * Create the component to be used to display the
     * process output.  This is a hook to allow the
     * component used to be customized.  
     */
    protected JTextComponent createOutputArea() {
        JTextPane pane = new JTextPane();

        return pane;
    }

    /**
     * Create a PrintStream that will display in the console
     * using the given attributes.
     */
    public PrintStream createPrintStream(AttributeSet a) {
        Document doc = outputArea.getDocument();
        OutputStream out = new DocumentOutputStream(doc, a);
        PrintStream pOut = new PrintStream(out);

        return pOut;
    }

    /**
     * Create a PrintWriter that will display in the console
     * using the given attributes.
     */
    public PrintWriter createPrintWriter(AttributeSet a) {
        Document doc = outputArea.getDocument();
        Writer out = new DocumentWriter(doc, a);
        PrintWriter pOut = new PrintWriter(out);

        return pOut;
    }

    /**
     * Clear the document.
     */
    public void clear() throws Exception { 
        Document doc = outputArea.getDocument();

        doc.remove(0, doc.getLength());
    }

    /**
     * Fetch the component used for the output.  This
     * allows further parsing of the output if desired,
     * and allows things like mouse listeners to be 
     * attached.  This can be useful for things like 
     * compiler output where clicking on an error 
     * warps another view to the location of the error.
     */
    public JTextComponent getOutputArea() {
        return outputArea;
    }

}
