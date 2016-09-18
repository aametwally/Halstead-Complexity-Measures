package com.neogenesis.pfaat.swingx;


import java.io.Writer;
import java.io.IOException;
import javax.swing.text.*;


/**
 * An Writer implementation that places it's
 * output in a swing text model (Document).  The 
 * Document can be either a plain text or styled
 * document implementation.  If styled, the attributes
 * assigned to the output stream will be used in
 * the display of the output.
 *
 * @author  Timothy Prinzing
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:31:09 $ 
 */
public class DocumentWriter extends Writer {

    /**
     * Constructs an output stream that will output to the
     * given document with the given set of character attributes.
     *
     * @param doc the document to write to.
     * @param a the character attributes to use for the written
     *  text.
     */
    public DocumentWriter(Document doc, AttributeSet a) {
        this.doc = doc;
        this.a = a;
    }

    /**
     * Constructs an output stream that will output to the
     * given document with whatever the default attributes
     * are.
     *
     * @param doc the document to write to.
     */
    public DocumentWriter(Document doc) {
        this(doc, null);
    }

    /**
     * Write a portion of an array of characters.
     *
     * @param  cbuf  Array of characters
     * @param  off   Offset from which to start writing characters
     * @param  len   Number of characters to write
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void write(char cbuf[], int off, int len) throws IOException {
        write(new String(cbuf, off, len));
    }

    /**
     * Write a portion of a string.
     *
     * @param  str  A String
     * @param  off  Offset from which to start writing characters
     * @param  len  Number of characters to write
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void write(String str, int off, int len) throws IOException {
        write(str.substring(off, len));
    }

    /**
     * Write a string.
     *
     * @param  str  String to be written
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void write(String str) throws IOException {
        if (doc == null) {
            throw new IOException("Writer was closed");
        }
        try {
            doc.insertString(doc.getLength(), str, a);
        } catch (BadLocationException ble) {
            throw new IOException(ble.getMessage());
        }
    }

    /**
     * Flush the stream.  If the stream has saved any characters from the
     * various write() methods in a buffer, write them immediately to their
     * intended destination.  Then, if that destination is another character or
     * byte stream, flush it.  Thus one flush() invocation will flush all the
     * buffers in a chain of Writers and OutputStreams.
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void flush() throws IOException {
        if (doc == null) {
            throw new IOException("Writer was closed");
        }
        // nothing to do, everything is immediately placed into the
        // document model.
    }

    /**
     * Close the stream, flushing it first.  Once a stream has been closed,
     * further write() or flush() invocations will cause an IOException to be
     * thrown.  Closing a previously-closed stream, however, has no effect.
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void close() throws IOException {
        doc = null;
    }

    private Document doc;
    private AttributeSet a;
}
