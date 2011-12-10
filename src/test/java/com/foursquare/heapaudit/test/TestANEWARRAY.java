package com.foursquare.heapaudit.test;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestANEWARRAY extends TestUtil {

    // Test allocation of an array of references.

    @Test public void ANEWARRAY() {

	clear();

	TestChild[] arrayL = new TestChild[10];

	assertTrue(expect("com.foursquare.heapaudit.test.TestChild",
			  arrayL.length,
			  56));

    }

}
