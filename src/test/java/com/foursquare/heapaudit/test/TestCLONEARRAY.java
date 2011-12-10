package com.foursquare.heapaudit.test;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestCLONEARRAY extends TestUtil {

    // Test allocations of an array via cloning.

    @Test public void CLONEARRAY() {

	TestChild[] arrayL0 = new TestChild[10];

	clear();

	TestChild[] arrayL1 = arrayL0.clone();

	assertTrue(expect("com.foursquare.heapaudit.test.TestChild",
			  10,
			  56));

    }

}
