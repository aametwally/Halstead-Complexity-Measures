import static org.junit.Assert.*;

import org.junit.Test;

/**
 * 
 */

/**
 * @author Ahmed Metwally
 *
 */
public class HalsteadTest {

	@Test
	public void testgetVocabulary() {		
		HalsteadMetrics testHal = new HalsteadMetrics();
		testHal.setParameters(10, 7, 16, 15);
		assertEquals(17, testHal.getVocabulary());
	}
	
	@Test
	public void testgetProglen() {		
		HalsteadMetrics testHal = new HalsteadMetrics();
		testHal.setParameters(10, 7, 16, 15);
		assertEquals(31, testHal.getProglen());
	}
	
	@Test
	public void testgetVolume() {		
		HalsteadMetrics testHal = new HalsteadMetrics();
		testHal.setParameters(10, 7, 16, 15);		
		System.out.println("getVolume= "+ (double)Math.round(testHal.getVolume()));
		assertEquals(127, Math.round(testHal.getVolume()));
	}
	
	
	@Test
	public void testgetDifficulty() {		
		HalsteadMetrics testHal = new HalsteadMetrics();
		testHal.setParameters(10, 7, 16, 15);		
		System.out.println("getDifficulty= "+ (double)Math.round(testHal.getDifficulty()));
		assertEquals(11, Math.round(testHal.getDifficulty()));
	}
	
	
	@Test
	public void testgetEffort() {		
		HalsteadMetrics testHal = new HalsteadMetrics();
		testHal.setParameters(10, 7, 16, 15);		
		System.out.println("getEffort= "+ (double)Math.round(testHal.getEffort()));
		assertEquals(1358, Math.round(testHal.getEffort()));
	}
	
	
	
	@Test
	public void testgetTimeReqProg() {		
		HalsteadMetrics testHal = new HalsteadMetrics();
		testHal.setParameters(10, 7, 16, 15);		
		System.out.println("getTimeReqProg= "+ (double)Math.round(testHal.getTimeReqProg()));
		assertEquals(75, Math.round(testHal.getTimeReqProg()));
	}
	
	
	@Test
	public void testgetTimeDelBugs() {		
		HalsteadMetrics testHal = new HalsteadMetrics();
		testHal.setParameters(10, 7, 16, 15);		
		System.out.println("getTimeDelBugs= "+ (double)Math.round(testHal.getTimeDelBugs()));
		assertEquals(0, Math.round(testHal.getTimeDelBugs()));
	}
}
