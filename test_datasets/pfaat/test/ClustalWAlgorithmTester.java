package com.neogenesis.pfaat.test;


import junit.framework.*;
import junit.textui.*;


public class ClustalWAlgorithmTester extends TestCase {
    public ClustalWAlgorithmTester(String name) {
        super(name);
    }

    protected void setUp() {}

    protected void tearDown() {}

    public static void main(String[] args) {
        TestRunner.run(new TestSuite(ClustalWAlgorithmTester.class));
    }
}
