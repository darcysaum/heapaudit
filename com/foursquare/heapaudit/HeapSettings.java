package com.foursquare.heapaudit;

import java.util.ArrayList;

class HeapSettings {

    // The dynamic setting determines whether to optimize for tradeoffs by
    // adding extra bytecode instructions to check and potentially skip the code
    // paths for executing the recording logic. If HeapAudit is expected to
    // always have at least one recorder present, then setting dynamic to false
    // can avoid the checks.

    public static boolean dynamic = false;

    public final static ArrayList<String> classesToAvoid = new ArrayList<String>();

    public final static ArrayList<String> classesToDebug = new ArrayList<String>();

    public final static ArrayList<String> classesToTrace = new ArrayList<String>();

    public final static ArrayList<String> methodsToAvoid = new ArrayList<String>();

    public final static ArrayList<String> methodsToDebug = new ArrayList<String>();

    public final static ArrayList<String> methodsToTrace = new ArrayList<String>();

    private static boolean contains(ArrayList<String> list,
				    String item) {

	for (String regex: list) {

	    if (item.matches(regex)) {

		return true;

	    }

	}

	return false;

    }

    public static boolean avoidClass(String c) {

	return contains(classesToAvoid,
			c);

    }

    public static boolean debugClass(String c) {

	return contains(classesToDebug,
			c);

    }

    public static boolean traceClass(String c) {

	return contains(classesToTrace,
			c);

    }

    public static boolean avoidMethod(String m) {

	return contains(methodsToAvoid,
			m);

    }

    public static boolean debugMethod(String m) {

	return contains(methodsToDebug,
			m);

    }

    public static boolean traceMethod(String m) {

	return contains(methodsToTrace,
			m);

    }

}
