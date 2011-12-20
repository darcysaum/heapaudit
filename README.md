# HeapAudit

HeapAudit is a java agent which audits heap allocations for JVM processes.

HeapAudit runs in two modes:

- STATIC: This requires a simple integration hook to be implemented by the java
process of interest. The callback hook defines how the allocations are recorded
and the callback code is only executed when the java agent is loaded.
- DYNAMIC: This injects HeapQuantile recorders to all matching methods and dumps
heap allocations to stdout when removed.

## Building the HeapAudit java agent

Build project with Maven:

	$ mvn clean package

The built jar will be in 'target/'.

## Implementing the HeapAudit hook

Currently, two recorders are provided with HeapAudit:

- [HeapActivity](https://github.com/foursquare/heapaudit/blob/master/src/main/java/com/foursquare/heapaudit/HeapActivity.java) prints each heap allocation to stdout
- [HeapQuantile](https://github.com/foursquare/heapaudit/blob/master/src/main/java/com/foursquare/heapaudit/HeapQuantile.java) accumulates allocations and dumps out summary at the end

Both of the above inherit from the base class HeapRecorder. Additional recording
behavior can be extended by implementing the record method in [HeapRecorder](https://github.com/foursquare/heapaudit/blob/master/src/main/java/com/foursquare/heapaudit/HeapRecorder.java).

	class MyRecorder extends HeapRecorder {

	    @Override public void record(String name,
	                                 int count,
	                                 long size) {

	        System.out.println("Allocated " + name +
	                           "[" + count + "] " + size + " bytes");

	    }

	}

Recording starts when it is registered and stops when it is unregistered. Each
recorder can be registered globally across all threads or local to the current.

	MyRecorder r = new MyRecorder();

	HeapRecorder.register(r, false);

	MyObject o = new MyObject();

	HeapRecorder.unregister(r, false);

## Launching the HeapAudit java agent

Launch HeapAudit statically along with the process of interest (requires MyTest
to implement the integration hook to register heap recorders).

	$ java -javaagent:heapaudit.jar MyTest

Launch HeapAudit dynamically by attaching to the process of interest (does not
require MyTest to have any prior intrumentations).

	$ java -jar heapaudit.jar 999 -Icom/foursquare/test/MyTest@test.+

	$ java -jar heapaudit.jar 999 -Rcom/foursquare/test/MyTest@test.+

The JDK's tools.jar library is required to launch HeapAudit dynamically. If
launching within JRE, specify the -Xbootclasspath command line arg to point to
the tools.jar file.

	$ java -Xbootclasspath/a:/usr/local/lib/tools.jar -jar heapaudit.jar 999 -Icom/foursquare/test/MyTest@test.+

Additional options can be passed to HeapAudit to customize which classes and/or
methods are not to be instrumented for recording allocations. For additional
information on how to specify the options, see [HeapSettings.java](https://github.com/foursquare/heapaudit/blob/master/src/main/java/com/foursquare/heapaudit/HeapSettings.java).

	$ java -javaagent:heapaudit.jar="-Acom/foursquare/test/.+" MyTest

## Dependencies

- [ASM](http://asm.ow2.org/)

## Maintainers

- Norbert Y. Hu norberthu@foursquare.com
