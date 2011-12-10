package com.foursquare.heapaudit.test;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestNEWARRAY extends TestUtil {

    // Test allocations of primitive type arrays.

    @Test public void NEWARRAY_Z() {

	clear();

	boolean[] arrayZ = new boolean[1];

	assertTrue(expect("boolean",
			  1,
			  24));

    }

    @Test public void NEWARRAY_B() {

	clear();

	byte[] arrayB = new byte[1];

	assertTrue(expect("byte",
			  1,
			  24));

    }

    @Test public void NEWARRAY_C() {

	clear();

	char[] arrayC = new char[1];

	assertTrue(expect("char",
			  1,
			  24));

    }

    @Test public void NEWARRAY_S() {

	clear();

	short[] arrayS = new short[2];

	assertTrue(expect("short",
			  2,
			  24));

    }

    @Test public void NEWARRAY_I() {

	clear();

	int[] arrayI = new int[4];

	assertTrue(expect("int",
			  4,
			  32));

    }

    @Test public void NEWARRAY_J() {

	clear();

	long[] arrayJ = new long[8];

	assertTrue(expect("long",
			  8,
			  80));

    }

    @Test public void NEWARRAY_F() {

	clear();

	float[] arrayF = new float[4];

	assertTrue(expect("float",
			  4,
			  32));

    }

    @Test public void NEWARRAY_D() {

	clear();

	double[] arrayD = new double[8];

	assertTrue(expect("double",
			  8,
			  80));

    }

}
