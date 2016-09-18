package com.neogenesis.pfaat.srs;


import java.io.*;
import java.net.*;
import java.util.Properties;

import org.apache.oro.text.perl.*;
import com.neogenesis.pfaat.util.*;


/**
 * Class for SRS queries.
 *
 * @author $Author: xih $
 * @version $Revision: 1.5 $, $Date: 2002/10/11 18:30:55 $ */
public class SRSServer {
    // the server and database to use
    private static String srs_server;

    private Perl5Util perl = new Perl5Util();

    private URL getQueryURL(String id) throws MalformedURLException {
        String query_string = perl.substitute("s/QUERY_NAME/" + id + "/", srs_server);

        return new URL("http://" + query_string);
    }

    public void printQueryResults(String id, PrintWriter out)
        throws IOException, MalformedURLException {
        URL query = getQueryURL(id);

        try {

            BufferedReader in =
                new BufferedReader(new InputStreamReader(query.openStream()));
            String line;

            while ((line = in.readLine()) != null) {
                // remove links, other html tags
                line = perl.substitute("s/<A[^>]*>(.*?)<\\/A>/$1/g", line);
                line = perl.substitute("s/(<\\/?pre>|<BR>)//g", line);
                out.println(line);
                if (line.indexOf("SRS error") >= 0) {
                    out.println("Unable to find the SRS entry with ID " + id);
                    out.println("Change the query id and try again");
                    break;
                }
            }
            in.close();
        } catch (IOException ex) {
            throw new IOException("Invalid URL or SRS server not in service. You may test the url from your internet browser.\n" +
                    "SRS query URL: " + query.toString() + "\n" + ex.getMessage());
        }
    }

    public Reader getQueryReader(String id)
        throws IOException, MalformedURLException {
        StringWriter sw = new StringWriter(10000);
        PrintWriter out = new PrintWriter(sw);

        printQueryResults(id, out);
        out.close();
        return new StringReader(sw.toString());
    }

    // initialize properties
    static {
        Properties props = new Properties();

        try {
            InputStream in =
                new FileInputStream(PathManager.getConfigPropertiesPath());

            props.load(in);
            in.close();
        } catch (IOException e) {
            throw new RuntimeException("unable to load srs configuration "
                    + "properties: " + e.getMessage());
        }

        srs_server = props.getProperty("pfaat.srsServer");
        if (srs_server == null)
            srs_server = "srs.ebi.ac.uk/srs5bin/cgi-bin/wgetz?-e+[swall-id:QUERY_NAME]+-vn+2";
        // srs_database = props.getProperty("pfaat.srsDatabase");
        // if (srs_database == null)
        // srs_database = "swall";
        String http_proxy_host = props.getProperty("http.proxyHost");

        if (http_proxy_host != null)
            System.setProperty("http.proxyHost", http_proxy_host);
        String http_proxy_port = props.getProperty("http.proxyPort");

        if (http_proxy_port != null)
            System.setProperty("http.proxyPort", http_proxy_port);
    }
}
