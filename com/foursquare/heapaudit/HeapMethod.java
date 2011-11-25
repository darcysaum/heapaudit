package com.foursquare.heapaudit;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class HeapMethod extends HeapAudit implements MethodVisitor {

    public HeapMethod(MethodVisitor mv,
		      String className,
		      String methodName,
		      String signature,
		      boolean debug,
		      boolean trace) {

	this.mv = new MethodAdapter(mv);

	this.className = className;

	this.methodName = methodName;

	this.signature = signature;

	this.debug = debug;

	this.trace = trace;

	instrumentation(debug,
			"\tMETHOD " + className + "." + methodName + signature + (trace ? " TRACED" : " NOT TRACED"));

    }

    public final MethodAdapter mv;

    private final String className;

    private final String methodName;

    private final String signature;

    private final boolean debug;

    private final boolean trace;

    public HeapVariables lvs = null;

    public AnnotationVisitor visitAnnotationDefault() {

	instrumentation(debug,
			"visitAnnotationDefault()");

	return mv.visitAnnotationDefault();

    }

    public AnnotationVisitor visitAnnotation(String desc,
					     boolean visible) {

	instrumentation(debug,
			"visitAnnotation()");

	return mv.visitAnnotation(desc,
				  visible);

    }

    public AnnotationVisitor visitParameterAnnotation(int parameter,
						      String desc,
						      boolean visible) {

	instrumentation(debug,
			"visitParameterAnnotation()");

	return mv.visitParameterAnnotation(parameter,
					   desc,
					   visible);
    }

    public void visitAttribute(Attribute attr) {

	instrumentation(debug,
			"visitAttribute(" + attr.type + ")");

	mv.visitAttribute(attr);

    }

    public void visitCode() {

	instrumentation(debug,
			"visitCode()");

	mv.visitCode();

	execution(trace,
		  mv,
		  "visitCode()");

    }

    public void visitFrame(int type,
			   int nLocal,
			   Object[] local,
			   int nStack,
			   Object[] stack) {

	instrumentation(debug,
			"visitFrame()");

	execution(trace,
		  mv,
		  "visitFrame()");

	mv.visitFrame(type,
		      nLocal,
		      local,
		      nStack,
		      stack);

    }

    public void visitInsn(int opcode) {

	instrumentation(debug,
			"visitInsn(" + opcode + ")");

	execution(trace,
		  mv,
		  "visitInsn(" + opcode + ")");

	mv.visitInsn(opcode);

    }

    public void visitLdcInsn(Object cst) {

	instrumentation(debug,
			"visitLdcInsn(" + cst + ")");

	execution(trace,
		  mv,
		  "visitLdcInsn(" + cst + ")");

	mv.visitLdcInsn(cst);

    }

    public void visitIincInsn(int var,
			      int increment) {

	instrumentation(debug,
			"visitIincInsn()");

	execution(trace,
		  mv,
		  "visitIincInsn()");

	mv.visitIincInsn(var,
			 increment);

    }

    public void visitVarInsn(int opcode,
			     int var) {

	instrumentation(debug,
			"visitVarInsn(" + opcode + ", " + var + ")");

	execution(trace,
		  mv,
		  "visitVarInsn(" + opcode + ", " + var + ")");

	mv.visitVarInsn(opcode,
			var);

    }

    public void visitFieldInsn(int opcode,
			       String owner,
			       String name,
			       String desc) {

	instrumentation(debug,
			"visitFieldInsn(" + opcode + ", " + owner + ", " + name + ", " + desc + ")");

	execution(trace,
		  mv,
		  "visitFieldInsn(" + opcode + ", " + owner + ", " + name + ", " + desc + ")");

	mv.visitFieldInsn(opcode,
			  owner,
			  name,
			  desc);

    }

    public void visitIntInsn(int opcode,
			     int operand) {

	instrumentation(debug,
			"visitIntInsn(" + opcode + ", " + operand + ")");

	execution(trace,
		  mv,
		  "visitIntInsn(" + opcode + ", " + operand + ")");

	switch (opcode) {

	case Opcodes.NEWARRAY:

	    HeapNEWARRAY.before(debug,
				trace,
				mv,
				operand);

	    break;

	default:

	}

	mv.visitIntInsn(opcode,
			operand);

	switch (opcode) {

	case Opcodes.NEWARRAY:

	    HeapNEWARRAY.after(debug,
			       trace,
			       mv,
			       operand);

	    break;

	default:

	}

    }

    private int allocating = 0;

    public void visitTypeInsn(int opcode,
			      String type) {

	instrumentation(debug,
			"visitTypeInsn(" + opcode + ", " + type + ")");

	execution(trace,
		  mv,
		  "visitTypeInsn(" + opcode + ", " + type + ")");

	switch (opcode) {

	case Opcodes.NEW:

	    ++allocating;

	    break;

	case Opcodes.ANEWARRAY:

	    HeapANEWARRAY.before(debug,
				 trace,
				 mv,
				 type);

	    break;

	default:

	}

	mv.visitTypeInsn(opcode,
			 type);

	switch (opcode) {

	case Opcodes.ANEWARRAY:

	    HeapANEWARRAY.after(debug,
				trace,
				mv,
				type);

	    break;

	default:

	}

    }

    public void visitMethodInsn(int opcode,
				String owner,
				String name,
				String signature) {

	instrumentation(debug,
			"visitMethodInsn(" + opcode + ", " + owner + ", " + name + ", " + signature + ")");

	execution(trace,
		  mv,
		  "visitMethodInsn(" + opcode + ", " + owner + ", " + name + ", " + signature + ")");

	switch (opcode) {

	case Opcodes.INVOKESPECIAL:

	    if (name.equals("<init>")) {

		if (allocating > 0) {

		    HeapNEW.before(debug,
				   trace,
				   mv,
				   lvs,
				   signature);

		}

	    }

	    break;

	case Opcodes.INVOKEVIRTUAL:

	    if (name.equals("newInstance")) {

		if (owner.equals("java/lang/Class") &&
		    signature.equals("()Ljava/lang/Object;")) {

		    HeapNEWINSTANCE.before(debug,
					   trace,
					   mv);

		}

	    }

	    break;

	default:

	}

	mv.visitMethodInsn(opcode,
			   owner,
			   name,
			   signature);

	switch (opcode) {

	case Opcodes.INVOKESPECIAL:

	    if (name.equals("<init>")) {

		if (allocating > 0) {

		    --allocating;

		    HeapNEW.after(debug,
				  trace,
				  mv,
				  owner);

		}

	    }
	    else if (owner.equals("java/lang/Object") &&
		     name.equals("clone")) {

		HeapCLONEOBJECT.after(debug,
				      trace,
				      mv);

	    }

	    break;

	case Opcodes.INVOKESTATIC:

	    if (owner.equals("java/lang/reflect/Array") &&
		name.equals("newInstance")) {

		if (signature.equals("(Ljava/lang/Class;I)Ljava/lang/Object;")) {

		    System.out.println("\tNOT IMPLEMENTED!");

		}
		else if (signature.equals("(Ljava/lang/Class;[I)Ljava/lang/Object;")) {

		    System.out.println("\tNOT IMPLEMENTED!");

		}

	    }

	    break;

	case Opcodes.INVOKEVIRTUAL:

	    if (name.equals("newInstance")) {

		if (owner.equals("java/lang/Class") &&
		    signature.equals("()Ljava/lang/Object;")) {

		    HeapNEWINSTANCE.after(debug,
					  trace,
					  mv);

		}
		else if (owner.equals("java/lang/reflect/Constructor") &&
			 signature.equals("([Ljava/lang/Object;)Ljava/lang/Object;")) {

		    HeapCLONEOBJECT.after(debug,
					  trace,
					  mv);

		}

	    }
	    else if (owner.startsWith("[") &&
		     name.equals("clone")) {

		HeapCLONEARRAY.after(debug,
				     trace,
				     mv,
				     owner);

	    }

	    break;

	default:

	}

    }

    public void visitMultiANewArrayInsn(String desc,
					int dims) {

	instrumentation(debug,
			"visitMultiANewArrayInsn(" + desc + ", " + dims + ")");

	execution(trace,
		  mv,
		  "visitMultiANewArrayInsn(" + desc + ", " + dims + ")");

	mv.visitMultiANewArrayInsn(desc,
				   dims);

	HeapMULTIARRAY.after(debug,
			     trace,
			     mv,
			     desc);

    }

    public void visitJumpInsn(int opcode,
			      Label label) {

	instrumentation(debug,
			"visitJumpInsn(" + label + ")");

	execution(trace,
		  mv,
		  "visitJumpInsn(" + label + ")");

        mv.visitJumpInsn(opcode,
			 label);

    }

    public void visitLookupSwitchInsn(Label dlft,
				      int[] keys,
				      Label[] labels) {

	instrumentation(debug,
			"visitLookupSwitchInsn()");

	execution(trace,
		  mv,
		  "visitLookupSwitchInsn()");

	mv.visitLookupSwitchInsn(dlft,
				 keys,
				 labels);

    }

    public void visitTableSwitchInsn(int min,
				     int max,
				     Label dlft,
				     Label[] labels) {

	instrumentation(debug,
			"visitTableSwitchInsn()");

	execution(trace,
		  mv,
		  "visitTableSwitchInsn()");

	mv.visitTableSwitchInsn(min,
				max,
				dlft,
				labels);

    }

    public void visitLabel(Label label) {

	instrumentation(debug,
			"visitLabel(" + label + ")");

	execution(trace,
		  mv,
		  "visitLabel(" + label + ")");

	mv.visitLabel(label);

    }

    public void visitTryCatchBlock(Label start,
				   Label end,
				   Label handler,
				   String type) {

	instrumentation(debug,
			"visitTryCatchBlock()");

	execution(trace,
		  mv,
		  "visitTryCatchBlock()");

	mv.visitTryCatchBlock(start,
			      end,
			      handler,
			      type);

    }


    public void visitLocalVariable(String name,
				   String desc,
				   String signature,
				   Label start,
				   Label end,
				   int index) {

	instrumentation(debug,
			"visitLocalVariable(" + name + ")");

	execution(trace,
		  mv,
		  "visitLocalVariable(" + name + ")");

	mv.visitLocalVariable(name,
			      desc,
			      signature,
			      start,
			      end,
			      index);

    }

    public void visitLineNumber(int line,
				Label start) {

	instrumentation(debug,
			"visitLineNumber(" + start + "#" + line + ")");

	execution(trace,
		  mv,
		  "visitLineNumber(" + start + "#" + line + ")");

	mv.visitLineNumber(line,
			      start);

    }

    public void visitMaxs(int maxStack,
			  int maxLocals) {

	instrumentation(debug,
			"visitMaxs(" + maxStack + ", " + maxLocals + ")");

	execution(trace,
		  mv,
		  "visitMaxs(" + maxStack + ", " + maxLocals + ")");

	lvs.declare();

	mv.visitMaxs(maxStack,
			maxLocals);

    }

    public void visitEnd() {

	instrumentation(debug,
			"visitEnd()");

	execution(trace,
		  mv,
		  "visitEnd()");

	mv.visitEnd();

    }

}
