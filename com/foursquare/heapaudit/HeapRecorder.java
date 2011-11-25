package com.foursquare.heapaudit;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;

public abstract class HeapRecorder {

    abstract public void record(String type,
				int count,
				long size);

    protected static String friendly(String type) {

	return type.replaceAll("^\\[*L", "").replaceAll(";$", "");

    }

    // TODO (norberthu): If possible, declare isAuditing as final such that JVM
    // can optimize out the status checks during JIT.

    public static boolean isAuditing = false;

    public static Instrumentation instrumentation = null;

    private static ArrayList<HeapRecorder> globalRecorders = new ArrayList<HeapRecorder>();

    private static ThreadLocal<ArrayList<HeapRecorder>> localRecorders = new ThreadLocal<ArrayList<HeapRecorder>>() {

	@Override protected ArrayList<HeapRecorder> initialValue() {

	    return new ArrayList<HeapRecorder>();

	}

    };

    public static ArrayList<HeapRecorder> getRecorders() {

	ArrayList<HeapRecorder> recorders = new ArrayList<HeapRecorder>(globalRecorders);

	recorders.addAll(localRecorders.get());

	return recorders;

    }

    public static synchronized void register(HeapRecorder recorder) {

	ArrayList<HeapRecorder> recorders = new ArrayList<HeapRecorder>(globalRecorders);

	recorders.add(recorder);

	globalRecorders = recorders;

    }

    public static synchronized void unregister(HeapRecorder recorder) {

	ArrayList<HeapRecorder> recorders = new ArrayList<HeapRecorder>(globalRecorders);

	recorders.remove(recorder);

	globalRecorders = recorders;

    }

    public static void register(HeapRecorder recorder,
				boolean global) {

	if (global) {

	    register(recorder);

	}
	else {

	    localRecorders.get().add(recorder);

	}

    }

    public static void unregister(HeapRecorder recorder,
				  boolean global) {

	if (global) {

	    unregister(recorder);

	}
	else {

	    localRecorders.get().remove(recorder);

	}

    }

}
