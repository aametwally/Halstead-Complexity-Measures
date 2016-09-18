import static org.junit.Assert.*;

import org.junit.Test;

/**
 * 
 */

/**
 * @author Ahmed
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

}
