package com.foursquare.heapaudit;

import java.util.Stack;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

class HeapNEW extends HeapAudit {

    // Allocations by NEW are triggered via calls to both visitTypeInsn and
    // visitMethodInsn(INVOKESPECIAL) where the top of the stack after the first
    // call to visitTypeInsn contains an uninitialized reference to the newly
    // allocated object. The reference to the object can only be touched (with
    // the exception of (DUP/SWAP/POP operations) after it is fully initialized
    // by the call to visitMethodInsn(INVOKESPECIAL). The top of the stack when
    // calling visitMethodInsn(INVOKESPECIAL) contains the reference to the
    // uninitialized newly allocated object and all the parameter values for the
    // constructor arguments.

    public static void before(boolean debug,
			      boolean trace,
			      MethodAdapter mv,
			      HeapVariables lvs,
			      String signature) {

	instrumentation(debug,
			"\tNEW.before");

	execution(trace,
		  mv,
		  "\tNEW.before");

	Type[] args = Type.getArgumentTypes(signature);

	int[] vars = new int[args.length];

	Label start = new Label();

	Label end = new Label();

	mv.visitLabel(start);

	for (int i = args.length - 1; i >= 0; --i) {

	    vars[i] = lvs.define(args[i],
				 start,
				 end);

	    // STACK [...|obj|...|arg]
	    mv.visitVarInsn(args[i].getOpcode(Opcodes.ISTORE),
			    vars[i]);
	    // STACK [...|obj|...]

	}

	// STACK [...|obj]
	mv.visitInsn(Opcodes.DUP);
	// STACK [...|obj|obj]

	for (int i = 0; i < args.length; ++i) {

	    // STACK [...|obj|obj|...]
	    mv.visitVarInsn(args[i].getOpcode(Opcodes.ILOAD),
			    vars[i]);
	    // STACK [...|obj|obj|...|arg]

	}

	mv.visitLabel(end);

    }

    public static void after(boolean debug,
			     boolean trace,
			     MethodAdapter mv,
			     String owner) {

	instrumentation(debug,
			"\tNEW.after");

	execution(trace,
		  mv,
		  "\tNEW.after");

	Label cleanup = new Label();

	Label finish = new Label();

	if (HeapSettings.conditional) {

	    // STACK: [...|obj]
	    visitCheck(mv,
		       cleanup);
	    // STACK: [...|obj]

	}

	// STACK: [...|obj]
	mv.visitLdcInsn(-1);
	// STACK: [...|obj|count]
	mv.visitLdcInsn(owner);
        // STACK: [...|obj|count|type]
        mv.visitLdcInsn((long)-1);
        // STACK: [...|obj|count|type|size]
        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
			   "com/foursquare/heapaudit/HeapAudit",
			   "record",
			   "(Ljava/lang/Object;ILjava/lang/String;J)V");
	// STACK: [...]

	if (HeapSettings.conditional) {

	    visitCleanup(mv,
			 cleanup,
			 finish);
	    // STACK: [...|obj]
	    mv.visitInsn(Opcodes.POP);
	    // STACK: [...]
	    visitFinish(mv,
			finish);
	    // STACK: [...]

	}

    }

}
