package com.neogenesis.pfaat;


import java.applet.Applet;
import java.net.*;
import java.io.*;
import com.neogenesis.pfaat.io.PfamLoader;


public class AlignmentApplet extends Applet {
    String input;
    String base;
    URL url;

    public void init() {
        base = getDocumentBase().toString();
        if (!base.endsWith("/"))  // work around for the inconsistent behavious between jdk1.3.1 and
        // older versions when calling getDocumentBase
        {
            int idx = base.lastIndexOf("/");

            base = base.substring(0, idx + 1);
        }
        System.out.println("Documentbase = " + base);

    }

    public void start() { // read properties and put it into system property
        System.out.println("starting frame");
        // System.setProperty("pfaat.mode", "applet");
        // System.setProperty("pfaat.home", "Dummy");
        // System.out.println("property set");

        input = getParameter("input");
        System.out.println("input =" + input);

        Alignment align = null;

        // get the input pfam file and start  the frame
        if (!input.startsWith("http"))
            input = base + input;

        System.out.println("url =" + input);
        try {
            url = new URL(input);
            URLConnection conn = url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            PfamLoader loader = new PfamLoader();

            align = loader.loadAlignment(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (align != null) {
            System.out.println("Alignment loaded");
            try {
                AlignmentFrame frame = new AlignmentFrame(null, align, "Blosum62", AlignmentFrame.APPLET_MODE);

                frame.show();
            } catch (Exception x) {
                System.out.println("AlignmentFrame unable to create frame: ");
                x.printStackTrace();
            }
        } else
            System.out.println("Unable to load alignment at " + input);

    }
}
