package com.foursquare.heapaudit;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class HeapMethod extends HeapUtil implements MethodVisitor {

    public HeapMethod(MethodVisitor mv,
		      String methodId,
		      boolean debugAuditing,
		      boolean traceAuditing,
		      boolean injectRecorder,
		      boolean removeRecorder) {

	this.mv = new MethodAdapter(mv);

	this.id = methodId;

	this.debugAuditing = debugAuditing;

	this.traceAuditing = traceAuditing;

	this.injectRecorder = injectRecorder;

	this.removeRecorder = removeRecorder;

	if (removeRecorder) {

	    HeapUtil.remove(id);

	}
	else if (injectRecorder) {

	    HeapUtil.inject(id);

	}

	instrumentation(debugAuditing,
			"\tMETHOD " + id);

    }

    public final MethodAdapter mv;

    private final String id;

    private final boolean debugAuditing;

    private final boolean traceAuditing;

    private final boolean injectRecorder;

    private final boolean removeRecorder;

    public HeapVariables lvs = null;

    public AnnotationVisitor visitAnnotationDefault() {

	instrumentation(debugAuditing,
			"visitAnnotationDefault()");

	return mv.visitAnnotationDefault();

    }

    public AnnotationVisitor visitAnnotation(String desc,
					     boolean visible) {

	instrumentation(debugAuditing,
			"visitAnnotation()");

	return mv.visitAnnotation(desc,
				  visible);

    }

    public AnnotationVisitor visitParameterAnnotation(int parameter,
						      String desc,
						      boolean visible) {

	instrumentation(debugAuditing,
			"visitParameterAnnotation()");

	return mv.visitParameterAnnotation(parameter,
					   desc,
					   visible);
    }

    public void visitAttribute(Attribute attr) {

	instrumentation(debugAuditing,
			"visitAttribute(" + attr.type + ")");

	mv.visitAttribute(attr);

    }

    public void visitCode() {

	instrumentation(debugAuditing,
			"visitCode()");

	mv.visitCode();

	execution(traceAuditing,
		  mv,
		  "visitCode()");

	visitEnter();

    }

    public void visitFrame(int type,
			   int nLocal,
			   Object[] local,
			   int nStack,
			   Object[] stack) {

	instrumentation(debugAuditing,
			"visitFrame()");

	execution(traceAuditing,
		  mv,
		  "visitFrame()");

	mv.visitFrame(type,
		      nLocal,
		      local,
		      nStack,
		      stack);

    }

    public void visitInsn(int opcode) {

	instrumentation(debugAuditing,
			"visitInsn(" + opcode + ")");

	execution(traceAuditing,
		  mv,
		  "visitInsn(" + opcode + ")");

	switch (opcode) {

	case Opcodes.ARETURN:

	case Opcodes.DRETURN:

	case Opcodes.FRETURN:

	case Opcodes.IRETURN:

	case Opcodes.LRETURN:

	case Opcodes.RETURN:

	    visitReturn();

	    break;

	}

	mv.visitInsn(opcode);

    }

    public void visitLdcInsn(Object cst) {

	instrumentation(debugAuditing,
			"visitLdcInsn(" + cst + ")");

	execution(traceAuditing,
		  mv,
		  "visitLdcInsn(" + cst + ")");

	mv.visitLdcInsn(cst);

    }

    public void visitIincInsn(int var,
			      int increment) {

	instrumentation(debugAuditing,
			"visitIincInsn()");

	execution(traceAuditing,
		  mv,
		  "visitIincInsn()");

	mv.visitIincInsn(var,
			 increment);

    }

    public void visitVarInsn(int opcode,
			     int var) {

	instrumentation(debugAuditing,
			"visitVarInsn(" + opcode + ", " + var + ")");

	execution(traceAuditing,
		  mv,
		  "visitVarInsn(" + opcode + ", " + var + ")");

	switch (opcode) {

	case Opcodes.RET:

	    visitReturn();

	    break;

	}

	mv.visitVarInsn(opcode,
			var);

    }

    public void visitFieldInsn(int opcode,
			       String owner,
			       String name,
			       String desc) {

	instrumentation(debugAuditing,
			"visitFieldInsn(" + opcode + ", " + owner + ", " + name + ", " + desc + ")");

	execution(traceAuditing,
		  mv,
		  "visitFieldInsn(" + opcode + ", " + owner + ", " + name + ", " + desc + ")");

	mv.visitFieldInsn(opcode,
			  owner,
			  name,
			  desc);

    }

    public void visitIntInsn(int opcode,
			     int operand) {

	instrumentation(debugAuditing,
			"visitIntInsn(" + opcode + ", " + operand + ")");

	execution(traceAuditing,
		  mv,
		  "visitIntInsn(" + opcode + ", " + operand + ")");

	switch (opcode) {

	case Opcodes.NEWARRAY:

	    HeapNEWARRAY.before(debugAuditing,
				traceAuditing,
				mv,
				operand);

	    break;

	default:

	}

	mv.visitIntInsn(opcode,
			operand);

	switch (opcode) {

	case Opcodes.NEWARRAY:

	    HeapNEWARRAY.after(debugAuditing,
			       traceAuditing,
			       mv,
			       operand);

	    break;

	default:

	}

    }

    private int allocating = 0;

    public void visitTypeInsn(int opcode,
			      String type) {

	instrumentation(debugAuditing,
			"visitTypeInsn(" + opcode + ", " + type + ")");

	execution(traceAuditing,
		  mv,
		  "visitTypeInsn(" + opcode + ", " + type + ")");

	switch (opcode) {

	case Opcodes.NEW:

	    ++allocating;

	    break;

	case Opcodes.ANEWARRAY:

	    HeapANEWARRAY.before(debugAuditing,
				 traceAuditing,
				 mv,
				 type);

	    break;

	default:

	}

	mv.visitTypeInsn(opcode,
			 type);

	switch (opcode) {

	case Opcodes.ANEWARRAY:

	    HeapANEWARRAY.after(debugAuditing,
				traceAuditing,
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

	instrumentation(debugAuditing,
			"visitMethodInsn(" + opcode + ", " + owner + ", " + name + ", " + signature + ")");

	execution(traceAuditing,
		  mv,
		  "visitMethodInsn(" + opcode + ", " + owner + ", " + name + ", " + signature + ")");

	switch (opcode) {

	case Opcodes.INVOKESPECIAL:

	    if (name.equals("<init>")) {

		if (allocating > 0) {

		    HeapNEW.before(debugAuditing,
				   traceAuditing,
				   mv,
				   lvs,
				   signature);

		}

	    }

	    break;

	case Opcodes.INVOKESTATIC:

	    if (owner.equals("java/lang/reflect/Array") &&
		name.equals("newInstance")) {

		HeapNEWINSTANCE.beforeX(debugAuditing,
					traceAuditing,
					mv);

	    }
	    else if (removeRecorder &&
		     owner.equals("com/foursquare/heapaudit/HeapUtil") &&
		     name.endsWith("register")) {

		// STACK: [...|id]
		mv.visitInsn(Opcodes.POP);
		// STACK: [...]

		return;

	    }

	    break;

	case Opcodes.INVOKEVIRTUAL:

	    if (name.equals("newInstance")) {

		if (owner.equals("java/lang/Class") &&
		    signature.equals("()Ljava/lang/Object;")) {

		    HeapNEWINSTANCE.before(debugAuditing,
					   traceAuditing,
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

		    HeapNEW.after(debugAuditing,
				  traceAuditing,
				  mv,
				  owner);

		}

	    }
	    else if (owner.equals("java/lang/Object") &&
		     name.equals("clone")) {

		HeapCLONEOBJECT.after(debugAuditing,
				      traceAuditing,
				      mv);

	    }

	    break;

	case Opcodes.INVOKESTATIC:

	    if (owner.equals("java/lang/reflect/Array") &&
		name.equals("newInstance")) {

		if (signature.equals("(Ljava/lang/Class;I)Ljava/lang/Object;")) {

		    HeapNEWINSTANCE.after(debugAuditing,
					  traceAuditing,
					  mv);

		}
		else if (signature.equals("(Ljava/lang/Class;[I)Ljava/lang/Object;")) {

		    HeapNEWINSTANCE.afterY(debugAuditing,
					   traceAuditing,
					   mv);

		}

	    }

	    break;

	case Opcodes.INVOKEVIRTUAL:

	    if (name.equals("newInstance")) {

		if (owner.equals("java/lang/Class") &&
		    signature.equals("()Ljava/lang/Object;")) {

		    HeapNEWINSTANCE.after(debugAuditing,
					  traceAuditing,
					  mv);

		}
		else if (owner.equals("java/lang/reflect/Constructor") &&
			 signature.equals("([Ljava/lang/Object;)Ljava/lang/Object;")) {

		    HeapCLONEOBJECT.after(debugAuditing,
					  traceAuditing,
					  mv);

		}

	    }
	    else if (owner.startsWith("[") &&
		     name.equals("clone")) {

		HeapCLONEARRAY.after(debugAuditing,
				     traceAuditing,
				     mv,
				     owner);

	    }

	    break;

	default:

	}

    }

    public void visitMultiANewArrayInsn(String desc,
					int dims) {

	instrumentation(debugAuditing,
			"visitMultiANewArrayInsn(" + desc + ", " + dims + ")");

	execution(traceAuditing,
		  mv,
		  "visitMultiANewArrayInsn(" + desc + ", " + dims + ")");

	mv.visitMultiANewArrayInsn(desc,
				   dims);

	HeapMULTIARRAY.after(debugAuditing,
			     traceAuditing,
			     mv,
			     desc);

    }

    public void visitJumpInsn(int opcode,
			      Label label) {

	instrumentation(debugAuditing,
			"visitJumpInsn(" + opcode + ", " + label + ")");

	execution(traceAuditing,
		  mv,
		  "visitJumpInsn(" + opcode + ", " + label + ")");

        mv.visitJumpInsn(opcode,
			 label);

    }

    public void visitLookupSwitchInsn(Label dlft,
				      int[] keys,
				      Label[] labels) {

	instrumentation(debugAuditing,
			"visitLookupSwitchInsn()");

	execution(traceAuditing,
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

	instrumentation(debugAuditing,
			"visitTableSwitchInsn()");

	execution(traceAuditing,
		  mv,
		  "visitTableSwitchInsn()");

	mv.visitTableSwitchInsn(min,
				max,
				dlft,
				labels);

    }

    public void visitLabel(Label label) {

	instrumentation(debugAuditing,
			"visitLabel(" + label + ")");

	execution(traceAuditing,
		  mv,
		  "visitLabel(" + label + ")");

	mv.visitLabel(label);

    }

    public void visitTryCatchBlock(Label start,
				   Label end,
				   Label handler,
				   String type) {

	instrumentation(debugAuditing,
			"visitTryCatchBlock()");

	execution(traceAuditing,
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

	instrumentation(debugAuditing,
			"visitLocalVariable(" + name + ")");

	execution(traceAuditing,
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

	instrumentation(debugAuditing,
			"visitLineNumber(" + start + "#" + line + ")");

	execution(traceAuditing,
		  mv,
		  "visitLineNumber(" + start + "#" + line + ")");

	mv.visitLineNumber(line,
			      start);

    }

    public void visitMaxs(int maxStack,
			  int maxLocals) {

	instrumentation(debugAuditing,
			"visitMaxs(" + maxStack + ", " + maxLocals + ")");

	execution(traceAuditing,
		  mv,
		  "visitMaxs(" + maxStack + ", " + maxLocals + ")");

	lvs.declare();

	mv.visitMaxs(maxStack,
		     maxLocals);

    }

    public void visitEnd() {

	instrumentation(debugAuditing,
			"visitEnd()");

	execution(traceAuditing,
		  mv,
		  "visitEnd()");

	mv.visitEnd();

    }

    private void visitEnter() {

	if (injectRecorder) {

	    // STACK: [...]
	    mv.visitLdcInsn(id);
	    // STACK: [...|id]
	    mv.visitMethodInsn(Opcodes.INVOKESTATIC,
			       "com/foursquare/heapaudit/HeapUtil",
			       "register",
			       "(Ljava/lang/String;)V");
	    // STACK: [...]

	}

    }

    private void visitReturn() {

	if (injectRecorder) {

	    // STACK: [...]
	    mv.visitLdcInsn(id);
	    // STACK: [...|id]
	    mv.visitMethodInsn(Opcodes.INVOKESTATIC,
			       "com/foursquare/heapaudit/HeapUtil",
			       "unregister",
			       "(Ljava/lang/String;)V");
	    // STACK: [...]

	}

    }

}
