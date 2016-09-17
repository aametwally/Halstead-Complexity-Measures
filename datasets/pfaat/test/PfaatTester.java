package com.neogenesis.pfaat.test;


import junit.framework.*;
import junit.textui.*;


public class PfaatTester extends TestSuite {
    public static TestSuite suite() {
        TestSuite suite = new TestSuite();

        suite.addTestSuite(AlignmentAnnPanelTester.class);
        suite.addTestSuite(AlignmentFrameTester.class);
        suite.addTestSuite(AlignmentGroupPanelTester.class);
        suite.addTestSuite(AlignmentListenerTester.class);
        suite.addTestSuite(AlignmentNamePanelTester.class);
        suite.addTestSuite(AlignmentPanelTester.class);
        suite.addTestSuite(AlignmentSorterTester.class);
        suite.addTestSuite(AlignmentTester.class);
        suite.addTestSuite(AminoAcidTester.class);
        suite.addTestSuite(AnalysisComponentTester.class);
        suite.addTestSuite(AnalysisNameComponentTester.class);
        suite.addTestSuite(AnalysisNamePanelTester.class);
        suite.addTestSuite(AnalysisPanelTester.class);
        suite.addTestSuite(BrowserControlTester.class);
        suite.addTestSuite(CachedAnnotationTester.class);
        suite.addTestSuite(ClustalWAlgorithmTester.class);
        suite.addTestSuite(ClustalWDialogTester.class);
        suite.addTestSuite(CollatedColumnAnnotationTester.class);
        suite.addTestSuite(CollatedLineAnnotationTester.class);
        suite.addTestSuite(ColorSequenceDialogTester.class);
        suite.addTestSuite(ColumnAnnotationComponentTester.class);
        suite.addTestSuite(ComparisonFrameTester.class);
        suite.addTestSuite(ConsensusComponentTester.class);
        suite.addTestSuite(ConsensusSequenceTester.class);
        suite.addTestSuite(DisplayPropertiesListenerTester.class);
        suite.addTestSuite(DisplayPropertiesTester.class);
        suite.addTestSuite(EditAnnotationsDialogTester.class);
        suite.addTestSuite(EditLineAnnDialogTester.class);
        suite.addTestSuite(EditResAnnDialogTester.class);
        suite.addTestSuite(EditSequenceDialogTester.class);
        suite.addTestSuite(ExtractProfileDialogTester.class);
        suite.addTestSuite(FindDialogTester.class);
        suite.addTestSuite(FontManagerTester.class);
        suite.addTestSuite(GroupComparisonDialogTester.class);
        suite.addTestSuite(GroupDialogTester.class);
        suite.addTestSuite(HMMERAlgorithmTester.class);
        suite.addTestSuite(HMMERDialogTester.class);
        suite.addTestSuite(JnetAnalysisTester.class);
        suite.addTestSuite(JnetLoaderTester.class);
        suite.addTestSuite(MutualInformationDialogTester.class);
        suite.addTestSuite(PairwiseAlignmentFrameTester.class);
        suite.addTestSuite(RemoveGroupDialogTester.class);
        suite.addTestSuite(RemoveRedundantSequencesDialogTester.class);
        suite.addTestSuite(RulerAnnotationComponentTester.class);
        suite.addTestSuite(RulerComponentTester.class);
        suite.addTestSuite(SequenceAnnComponentTester.class);
        suite.addTestSuite(SequenceComponentTester.class);
        suite.addTestSuite(SequenceGroupComponentTester.class);
        suite.addTestSuite(SequenceListenerTester.class);
        suite.addTestSuite(SequenceNameComponentTester.class);
        suite.addTestSuite(SequenceTester.class);
        suite.addTestSuite(StartIndexDialogTester.class);
        return (suite);
    }

    public static void main(String[] args) {
        TestRunner.run(PfaatTester.suite());
    }
}
