package com.neogenesis.pfaat.test;


import junit.framework.*;
import junit.textui.*;


public class JnetAnalysisTester extends TestCase {
    public JnetAnalysisTester(String name) {
        super(name);
    }

    protected void setUp() {}

    protected void tearDown() {}

    public static void main(String[] args) {
        TestRunner.run(new TestSuite(JnetAnalysisTester.class));
    }
}
