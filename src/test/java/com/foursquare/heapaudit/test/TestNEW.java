package com.foursquare.heapaudit.test;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestNEW extends TestUtil {

    // Test allocation of an object.

    @Test public void NEW() {

	clear();

	TestChild objectL = new TestChild();

	assertTrue(expect("com.foursquare.heapaudit.test.TestChild",
			  -1,
			  56));

    }

}
