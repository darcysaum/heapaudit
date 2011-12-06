package com.foursquare.heapaudit;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.Opcodes;

class HeapNEWINSTANCE extends HeapAudit {

    // Allocations by java/lang/Class/newInstance()Ljava/lang/Object; are
    // triggered via calls to visitMethodInsn(INVOKEVIRTUAL) where the top of
    // the stack contains the name of the class and returns a reference to the
    // newly allocated class object.

    public static void before(boolean debug,
			      boolean trace,
			      MethodAdapter mv) {

	instrumentation(debug,
			"\tNEWINSTANCE.before");

	execution(trace,
		  mv,
		  "\tNEWINSTANCE.before");

        // STACK: [...|class]
        mv.visitLdcInsn(-1);
        // STACK: [...|class|count]
        mv.visitInsn(Opcodes.SWAP);
        // STACK: [...|count|class]
        mv.visitInsn(Opcodes.DUP);
        // STACK: [...|count|class|class]

    }

    public static void after(boolean debug,
			     boolean trace,
			     MethodAdapter mv) {

        instrumentation(debug,
			"\tNEWINSTANCE.after");

	execution(trace,
		  mv,
		  "\tNEWINSTANCE.after");

	Label cleanup = new Label();

	Label finish = new Label();

	if (HeapSettings.dynamic) {

	    // STACK: [...|count|class|obj]
	    visitCheck(mv,
		       cleanup);
	    // STACK: [...|count|class|obj]

	}

        // STACK: [...|count|class|obj]
        mv.visitInsn(Opcodes.DUP_X2);
        // STACK: [...|obj|count|class|obj]
        mv.visitInsn(Opcodes.DUP_X2);
        // STACK: [...|obj|obj|count|class|obj]
        mv.visitInsn(Opcodes.POP);
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

	    visitCleanup(mv,
			 cleanup,
			 finish);
	    // STACK: [...|count|class|obj]
	    mv.visitInsn(Opcodes.SWAP);
	    // STACK: [...|count|obj|class]
	    mv.visitInsn(Opcodes.POP);
	    // STACK: [...|count|obj]
	    mv.visitInsn(Opcodes.SWAP);
	    // STACK: [...|obj|count]
	    mv.visitInsn(Opcodes.POP);
	    // STACK: [...|obj]
	    visitFinish(mv,
			finish);
	    // STACK: [...|obj]

	}

    }

    public static void beforeX(boolean debug,
			       boolean trace,
			       MethodAdapter mv) {

	instrumentation(debug,
			"\tNEWINSTANCE.beforeX");

	execution(trace,
		  mv,
		  "\tNEWINSTANCE.beforeX");

        // STACK: [...|class|count]
        mv.visitInsn(Opcodes.SWAP);
        // STACK: [...|count|class]
        mv.visitInsn(Opcodes.DUP2);
        // STACK: [...|count|class|count|class]
        mv.visitInsn(Opcodes.SWAP);
        // STACK: [...|count|class|class|count]

    }

    public static void afterY(boolean debug,
			      boolean trace,
			      MethodAdapter mv) {

	instrumentation(debug,
			"\tNEWINSTANCE.afterY");

	execution(trace,
		  mv,
		  "\tNEWINSTANCE.afterY");

        Label cleanup = new Label();

        Label finish = new Label();

	if (HeapSettings.dynamic) {

	    // STACK: [...|count|class|obj]
	    visitCheck(mv,
		       cleanup);
	    // STACK: [...|count|class|obj]

	}

        // STACK: [...|count|class|obj]
        mv.visitInsn(Opcodes.DUP_X2);
        // STACK: [...|obj|count|class|obj]
        mv.visitInsn(Opcodes.DUP_X2);
        // STACK: [...|obj|obj|count|class|obj]
        mv.visitInsn(Opcodes.POP);
        // STACK: [...|obj|obj|count|class]
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                           "java/lang/Class",
                           "getName",
                           "()Ljava/lang/String;");
        // STACK: [...|obj|obj|count|type]
        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                           "com/foursquare/heapaudit/HeapAudit",
                           "record",
                           "(Ljava/lang/Object;[ILjava/lang/String;)V");
	// STACK: [...|obj]

	if (HeapSettings.dynamic) {

	    visitCleanup(mv,
			 cleanup,
			 finish);
	    // STACK: [...|count|class|obj]
	    mv.visitInsn(Opcodes.SWAP);
	    // STACK: [...|count|obj|class]
	    mv.visitInsn(Opcodes.POP);
	    // STACK: [...|count|obj]
	    mv.visitInsn(Opcodes.SWAP);
	    // STACK: [...|obj|count]
	    mv.visitInsn(Opcodes.POP);
	    // STACK: [...|obj]
	    visitFinish(mv,
			finish);
	    // STACK: [...|obj]

	}

    }

}
