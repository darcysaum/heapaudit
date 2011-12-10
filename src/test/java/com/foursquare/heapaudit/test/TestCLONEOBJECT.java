package com.foursquare.heapaudit.test;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestCLONEOBJECT extends TestUtil {

    // Test allocations of an object via cloning.

    @Test public void CLONEOBJECT() {

	TestChild objectL0 = new TestChild();

	clear();

	Object objectL1 = objectL0.clone();

	assertTrue(expect("com.foursquare.heapaudit.test.TestChild",
			  -1,
			  56));

    }

}
