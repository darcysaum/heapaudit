package com.foursquare.heapaudit.test;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestMULTIARRAY extends TestUtil {

    // Test allocations of multi-dimensional arrays.

    @Test public void MULTIARRAY_I() {

	clear();

	int[][] arrayI = new int[3][5];

	assertTrue(expect("int",
			  15,
			  480));

    }

    @Test public void MULTIARRAY_X() {

        clear();

	TestChild[][] arrayL = new TestChild[1][7];

	assertTrue(expect("com.foursquare.heapaudit.test.TestChild",
			  7,
			  392));

    }

}
