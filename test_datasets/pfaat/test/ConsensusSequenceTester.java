package com.neogenesis.pfaat.test;


import junit.framework.*;
import junit.textui.*;


public class ConsensusSequenceTester extends TestCase {
    public ConsensusSequenceTester(String name) {
        super(name);
    }

    protected void setUp() {}

    protected void tearDown() {}

    public static void main(String[] args) {
        TestRunner.run(new TestSuite(ConsensusSequenceTester.class));
    }
}
