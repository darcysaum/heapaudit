package com.foursquare.heapaudit;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
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


        HeapSettings.parse(args);
        
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
