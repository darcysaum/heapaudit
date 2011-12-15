package com.foursquare.heapaudit;

import java.util.ArrayList;
import java.util.Arrays;

class HeapSettings {

    public static void parse(String args,
			     boolean dynamic) {

	// When the HeapAudit agent is loaded, all classes except the ones below
	// are candidates for instrumentation. To avoid additional classes from
	// begin instrumented, specify an arbitrary number of -C<class_regex>
	// where <class_regex> is used to match the fully qualified class name.
	// To trace a class, specify +C<class_regex>. The -C<class_regex> option
	// takes precedence and tracing information for the matching class will
	// not appear. Specify an arbitrary number of -M<method_regex> or
	// +M<method_regex> options where <method_regex> is used to match the
	// fully qualified method name and signature at the per-method level.
	// The -C options take precedence over the +M options when applied to
	// the same class. Use the * prefix instead of the + prefix to switch
	// from tracing mode to tracing plus debug mode.
	//   i.e.
	//     * To instrument all classes except the class com/foursquare/foo
	//           -Ccom/foursquare/foo
	//     * To instrument all classes and with extra tracing information
	//       for only the class com/foursquare/foo
	//           +Ccom/foursquare/foo
	//     * To instrument all classes except the class com/foursquare/foo
	//       and with extra tracing information for all classes under the
	//       com/foursquare/ namespace
	//           -Ccom/foursquare/foo +Ccom/foursquare/.+
	//     * To instrument all classes except the following toString method
	//       com/foursquare/foo.toString()Ljava/lang/String;
	//           -Mcom/foursquare/foo\.toString\(\)Ljava/lang/String;
	//     * To instrument all classes and with extra tracing information
	//       for all toString methods
	//           +M.+\.toString\(.*\)Ljava/lang/String;
	//     * To instrument all classes and with extra tracing plus debug
	//       information for all classes and methods
	//           *C.+
	// Use +O<setting> or -O<setting> to enable or disable settings where
	// <setting> can be one of the following
	//     * conditional - To enable/disable the conditional setting. This
	//                     may be enabled if the majority of the time zero
	//                     recorders are registered. Enabled by default.
	// When launched in dynamic mode, specify an arbitrary number of
	// +R<method_regex> or -R<method_regex> where <method_regex> is used to
	// match the fully qualified method name and signature to enable or
	// disable injected recorders.
	//     * To dynamically inject recorders for all toString methods in
	//       com/foursquare/foo
	//           +Rcom/foursquare/foo/toString.+
	//     * To dynamically remove recorders for all toString methods in
	//       com/foursquare/foo and dump heap allocations to stdout
	//           -Rcom/foursquare/foo/toString.+

	HeapSettings.dynamic = dynamic;

	classesToAvoid.clear();

	classesToDebug.clear();

	classesToTrace.clear();

	methodsToAvoid.clear();

	methodsToDebug.clear();

	methodsToTrace.clear();

	methodsToInjectRecorder.clear();

	methodsToRemoveRecorder.clear();

	classesToAvoid.addAll(Arrays.asList("java/lang/ThreadLocal",
					    "org/objectweb/asm/.+",
					    "com/foursquare/heapaudit/(?!test/).+",
					    "[$].*",
					    "java/.+",
					    "javax/.+",
					    "org/jcp/.+",
					    "org/xml/.+",
					    "com/apple/.+",
					    "com/sun/.+",
					    "sun/.+"));

	if (args != null) {

	    for (String arg: args.split(" ")) {

		if (arg.length() >= 2) {

		    char prefix = arg.charAt(0);

		    char option = arg.charAt(1);

		    String value = (arg.length() > 2) ? arg.substring(2) : null;

		    switch (option) {

		    case 'O':

			if (value.equals("conditional")) {

			    conditional = (prefix == '+');

			}

			break;

		    case 'C':

		    case 'M':

		    case 'R':

			switch (prefix) {

			case '-':

			    if (value == null) {

				throw new IllegalArgumentException(arg);

			    }
			    else {

				switch (option) {

				case 'C':

				    classesToAvoid.add(value);

				    break;

				case 'M':

				    methodsToAvoid.add(value);

				    break;

				case 'R':

				    methodsToRemoveRecorder.add(value);

				    break;

				}

			    }

			    break;

			case '+':

			    if (value == null) {

				throw new IllegalArgumentException(arg);

			    }
			    else {

				switch (option) {

				case 'C':

				    classesToTrace.add(value);

				    break;

				case 'M':

				    methodsToTrace.add(value);

				    break;

				case 'R':

				    methodsToInjectRecorder.add(value);

				    break;

				}

			    }

			    break;

			case '*':

			    if (value == null) {

				throw new IllegalArgumentException(arg);

			    }
			    else {

				switch (option) {

				case 'C':

				    classesToDebug.add(value);

				    classesToTrace.add(value);

				    break;

				case 'M':

				    methodsToDebug.add(value);

				    methodsToTrace.add(value);

				    break;

				}

			    }

			    break;

			default:

			    throw new IllegalArgumentException(arg);

			}

			break;

		    default:

			throw new IllegalArgumentException(arg);

		    }

		}

	    }

	}

    }

    // The dynamic flag indicates whether the HeapAudit java agent was loaded
    // dynamically or statically.

    public static boolean dynamic = false;

    // The conditional setting determines whether to optimize for tradeoffs by
    // adding extra bytecode instructions to check and potentially skip the code
    // paths for executing the recording logic. If HeapAudit is expected to
    // always have at least one recorder present, then setting conditional to
    // false can avoid the checks.

    public static boolean conditional = true;

    private final static ArrayList<String> classesToAvoid = new ArrayList<String>();

    private final static ArrayList<String> classesToDebug = new ArrayList<String>();

    private final static ArrayList<String> classesToTrace = new ArrayList<String>();

    private final static ArrayList<String> methodsToAvoid = new ArrayList<String>();

    private final static ArrayList<String> methodsToDebug = new ArrayList<String>();

    private final static ArrayList<String> methodsToTrace = new ArrayList<String>();

    private final static ArrayList<String> methodsToInjectRecorder = new ArrayList<String>();

    private final static ArrayList<String> methodsToRemoveRecorder = new ArrayList<String>();

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

    public static boolean injectRecorder(String m) {

	return dynamic &&
	    contains(methodsToInjectRecorder,
		     m);

    }

    public static boolean removeRecorder(String m) {

	return dynamic &&
	    contains(methodsToRemoveRecorder,
		     m);

    }

}
