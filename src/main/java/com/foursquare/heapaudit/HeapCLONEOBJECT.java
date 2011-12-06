package com.foursquare.heapaudit;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.Opcodes;

class HeapCLONEOBJECT extends HeapAudit {

    // Allocations by java/lang/reflect/Constructor/newInstance
    // ([Ljava/lang/Object;)Ljava/lang/Object; or by java/lang/Object/clone
    // are triggered via calls to visitMethodInsn(INVOKEVIRTUAL) or to
    // visitMethodInsn(INVOKESPECIAL) where they return the reference to the
    // newly allocated object.

    public static void after(boolean debug,
			     boolean trace,
			     MethodAdapter mv) {

	instrumentation(debug,
			"\tCOPYINSTANCE.after... VERIFY!");

	execution(trace,
		  mv,
		  "\tCOPYINSTANCE.after");

	Label finish = new Label();

	if (HeapSettings.dynamic) {

	    // STACK: [...|obj]
	    visitCheck(mv,
		       finish);
	    // STACK: [...|obj]

	}

	// STACK: [...|obj]
	mv.visitInsn(Opcodes.DUP);
	// STACK: [...|obj|obj]
	mv.visitInsn(Opcodes.DUP);
	// STACK: [...|obj|obj|obj]
	mv.visitLdcInsn(-1);
	// STACK: [...|obj|obj|obj|count]
	mv.visitInsn(Opcodes.SWAP);
	// STACK: [...|obj|obj|count|obj]
	mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
			   "java/lang/Object",
			   "getClass",
			   "()Ljava/lang/Class;");
	// STACK: [...|obj|obj|count|class]
	mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
			   "java/lang/Class",
			   "getName",
			   "()Ljava/lang/String;");
	// STACK: [...|obj|obj|count|type]
	mv.visitLdcInsn((long)-1);
	// STACK: [...|obj|obj|count|type|size]
	mv.visitMethodInsn(Opcodes.INVOKESTATIC,
			   "com/foursquare/heapaudit/HeapAudit",
			   "record",
			   "(Ljava/lang/Object;ILjava/lang/String;J)V");
	// STACK: [...|obj]

	if (HeapSettings.dynamic) {

	    // STACK: [...|obj]
	    visitFinish(mv,
			finish);
	    // STACK: [...|obj]

	}

    }

}
