package com.foursquare.heapaudit.test;

public class TestChild extends TestParent implements Cloneable {

    public TestChild() {

	super(9);

    }

    public Object clone() {

	return new TestChild();

    }

}
