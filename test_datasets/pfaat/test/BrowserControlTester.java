package com.neogenesis.pfaat.test;


import junit.framework.*;
import junit.textui.*;


public class BrowserControlTester extends TestCase {
    public BrowserControlTester(String name) {
        super(name);
    }

    protected void setUp() {}

    protected void tearDown() {}

    public static void main(String[] args) {
        TestRunner.run(new TestSuite(BrowserControlTester.class));
    }
}
