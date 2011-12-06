package com.foursquare.heapaudit;

public class HeapActivity extends HeapRecorder {

    public void record(String type,
		       int count,
		       long size) {

	String length = "";

	if (count >= 0) {

	    length = "[" + count + "]";

	}

	System.out.println("new " + type + length + " (" + size + " bytes)");

    }

}
