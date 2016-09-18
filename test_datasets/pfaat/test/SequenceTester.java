package com.neogenesis.pfaat.test;


import junit.framework.*;
import junit.textui.*;


public class SequenceTester extends TestCase {
    public SequenceTester(String name) {
        super(name);
    }

    protected void setUp() {}

    protected void tearDown() {}

    public static void main(String[] args) {
        TestRunner.run(new TestSuite(SequenceTester.class));
    }
}
