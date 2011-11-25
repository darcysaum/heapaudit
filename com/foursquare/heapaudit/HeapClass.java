package com.foursquare.heapaudit;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

public class HeapClass extends HeapAudit implements ClassVisitor {

    public HeapClass(ClassVisitor cv,
		     String className) {

	this.cv = new ClassAdapter(cv);

	this.className = className;

	for (String regex: HeapInstrumenter.classesToDebug) {

	    if (className.matches(regex)) {

		this.debug = true;

	    }

	}

	for (String regex: HeapInstrumenter.classesToTrace) {

	    if (className.matches(regex)) {

		this.trace = true;

	    }

	}

	instrumentation(trace,
			"\tCLASS " + className);

    }

    private final ClassAdapter cv;

    private final String className;

    private boolean debug = false;

    private boolean trace = false;

    public void visit(int version,
		      int access,
		      String name,
		      String signature,
		      String superName,
		      String[] interfaces) {

	instrumentation(debug,
			"visit()");

	cv.visit(version,
		 access,
		 name,
		 signature,
		 superName,
		 interfaces);

    }

    public void visitSource(String source,
			    String debug) {

	instrumentation(this.debug,
			"visitSource(" + source + ", " + debug + ")");

	cv.visitSource(source,
		       debug);

    }

    public void visitOuterClass(String owner,
				String name,
				String desc) {

	instrumentation(debug,
			"visitOuterClass()");

	cv.visitOuterClass(owner,
			   name,
			   desc);

    }

    public AnnotationVisitor visitAnnotation(String desc,
					     boolean visible) {

	instrumentation(debug,
			"visitAnnotation()");

	return cv.visitAnnotation(desc,
				  visible);

    }

    public void visitAttribute(Attribute attr) {

	instrumentation(debug,
			"visitAttribute()");

	cv.visitAttribute(attr);

    }

    public void visitInnerClass(String name,
				String outerName,
				String innerName,
				int access) {

	instrumentation(debug,
			"visitInnerClass()");

	cv.visitInnerClass(name,
			   outerName,
			   innerName,
			   access);

    }

    public FieldVisitor visitField(int access,
				   String name,
				   String desc,
				   String signature,
				   Object value) {

	instrumentation(debug,
			"visitField()");

	return cv.visitField(access,
			     name,
			     desc,
			     signature,
			     value);

    }

    public MethodVisitor visitMethod(int access,
				     String name,
				     String desc,
				     String signature,
				     String[] exceptions) {

	instrumentation(debug,
			"visitMethod()");

	String method = className + "." + name + desc;

        for (String regex: HeapInstrumenter.methodsToAvoid) {

            if (method.matches(regex)) {

		return cv.visitMethod(access,
				      name,
				      desc,
				      signature,
				      exceptions);

            }

        }

	boolean debug = this.debug;

	if (!debug) {

	    for (String regex: HeapInstrumenter.methodsToDebug) {

		if (method.matches(regex)) {

		    debug = true;

		    break;

		}

	    }

	}

	boolean trace = this.trace;

	if (!trace) {

	    for (String regex: HeapInstrumenter.methodsToTrace) {

		if (method.matches(regex)) {

		    trace = true;

		    break;

		}

	    }

	}

	HeapMethod mv = new HeapMethod(cv.visitMethod(access,
						      name,
						      desc,
						      signature,
						      exceptions),
				       className,
				       name,
				       desc,
				       debug,
				       trace);

	// The following sets up the weird cyclic dependency whereby the
	// HeapMethod implementation uses the HeapVariables class for injecting
	// new local variables but the HeapVariables wraps the HeapMethod from
	// the outside.

	mv.lvs = new HeapVariables(access,
				   desc,
				   debug,
				   trace,
				   mv);

	return mv.lvs.lvs;

    }

    public void visitEnd() {

	instrumentation(debug,
			"visitEnd()");

	cv.visitEnd();

    }

}
