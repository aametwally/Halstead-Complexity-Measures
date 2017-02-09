package com.neogenesis.pfaat;


// simple class to parse command line options
// of the form -option value
public class CmdLineOption {
    String[] argv;
    int n;

    public CmdLineOption(String[] argv, int n) {
        this.argv = argv;
        this.n = n;
    }

    public String nextOption() {
        // search for the next argument that begins with a dash
        for (; n < argv.length; n++) {
            if (argv[n].startsWith("-"))
                return argv[n];
        }
        return null;
    }

    public String nextValue() {
        // return next argument...if there is none return null
        if (n >= argv.length)
            return null;
        return argv[n++];
    }
}
