package com.foursquare.heapaudit;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

public class HeapClass extends HeapUtil implements ClassVisitor {

    public HeapClass(ClassVisitor cv,
		     String classId) {

	this.cv = new ClassAdapter(cv);

	this.id = classId;

	this.debug = HeapSettings.debugClass(classId);

	this.trace = HeapSettings.traceClass(classId);

	instrumentation(trace,
			"\tCLASS " + id);

    }

    private final ClassAdapter cv;

    private final String id;

    private final boolean debug;

    private final boolean trace;

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

	String method = id + "." + name + desc;

	if (HeapSettings.avoidMethod(method)) {

	    return cv.visitMethod(access,
				  name,
				  desc,
				  signature,
				  exceptions);

        }

	boolean debug = this.debug || HeapSettings.debugMethod(method);

	boolean trace = this.trace || HeapSettings.traceMethod(method);

	boolean injectRecorder = HeapSettings.injectRecorder(method);

	boolean removeRecorder = HeapSettings.removeRecorder(method);

	HeapMethod mv = new HeapMethod(cv.visitMethod(access,
						      name,
						      desc,
						      signature,
						      exceptions),
				       method,
				       debug,
				       trace,
				       injectRecorder,
				       removeRecorder);

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
