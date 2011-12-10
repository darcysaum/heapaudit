package com.foursquare.heapaudit.test;

public class TestChild extends TestParent implements Cloneable {

    public TestChild() {

	super('C');

    }

    public Object clone() {

	return new TestChild();

    }

}
