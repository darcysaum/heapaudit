package com.foursquare.heapaudit;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class HeapInstrumenter extends HeapAudit implements ClassFileTransformer {

    static {
	/*
        Runtime.getRuntime().addShutdownHook(new Thread() {

		@Override public void run() {

		    HeapRecorder.instrumentation = null;

		}

	    });
	*/
    }

    public static void premain(String args,
			       Instrumentation instrumentation) throws UnmodifiableClassException {

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
	//     * dynamic - Enable the dynamic setting if recorders are expected
	//                 to be dynamically registered/unregistered and most of
	//                 the time will run with zero recorders. Disabled by
	//                 default.
	//           
	HeapSettings.classesToAvoid.addAll(Arrays.asList("java/lang/ThreadLocal",
							 "org/objectweb/asm/.+",
							 "com/foursquare/heapaudit/.+",
							 "[$].*",
							 "java/.+",
							 "javax/.+",
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

			if (value.equals("dynamic")) {

			    HeapSettings.dynamic = (prefix == '+');

			}

			break;

		    case 'C':

		    case 'M':

			switch (prefix) {

			case '-':

			    if (value == null) {

				throw new IllegalArgumentException(arg);

			    }
			    else {

				(option == 'C' ? HeapSettings.classesToAvoid : HeapSettings.methodsToAvoid).add(value);

			    }

			    break;

			case '+':

			    if (value == null) {

				throw new IllegalArgumentException(arg);

			    }
			    else {

				(option == 'C' ? HeapSettings.classesToTrace : HeapSettings.methodsToTrace).add(value);

			    }

			    break;

			case '*':

			    if (value == null) {

				throw new IllegalArgumentException(arg);

			    }
			    else {

				(option == 'C' ? HeapSettings.classesToDebug : HeapSettings.methodsToDebug).add(value);

				(option == 'C' ? HeapSettings.classesToTrace : HeapSettings.methodsToTrace).add(value);

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

        HeapRecorder.isAuditing = true;

	HeapRecorder.instrumentation = instrumentation;

	instrumentation.addTransformer(new HeapInstrumenter(),
				       true);

	if (instrumentation.isRetransformClassesSupported() &&
	    (HeapInstrumenter.class.getClassLoader() == null)) {

	    ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

	    for (Class<?> c: instrumentation.getAllLoadedClasses()) {

		if (instrumentation.isModifiableClass(c)) {

		    classes.add(c);

		}

	    }

	    instrumentation.retransformClasses(classes.toArray(new Class<?>[classes.size()]));

	}

    }

    // The following is the main entry point for transforming the bytecode.

    public byte[] transform(ClassLoader loader,
			    String className,
			    Class<?> classBeingRedefined,
			    ProtectionDomain protectionDomain,
			    byte[] classfileBuffer) {

	if (HeapSettings.avoidClass(className)) {

	    return null;

	}

	ClassReader cr = new ClassReader(classfileBuffer);

	ClassWriter cw = new ClassWriter(cr,
					 ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

	cr.accept(new HeapClass(cw,
				className),
	          ClassReader.SKIP_FRAMES);

	return cw.toByteArray();

    }

}
