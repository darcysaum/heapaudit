package com.foursquare.heapaudit;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.Opcodes;

class HeapNEWARRAY extends HeapUtil {

    // Allocations by NEWARRAY are triggered via calls to visitIntInsn where the
    // top of the stack contains the number of elements in the array and returns
    // a reference to the newly allocated array object.

    public static void before(boolean debug,
			      boolean trace,
			      MethodAdapter mv,
			      int operand) {

	instrumentation(debug,
			"\tNEWARRAY.before(" + types[operand] + ")");

	execution(trace,
		  mv,
		  "\tNEWARRAY.before(" + types[operand] + ")");

        // STACK: [...|count]
        mv.visitInsn(Opcodes.DUP);
        // STACK: [...|count|count]

    }

    public static void after(boolean debug,
			     boolean trace,
			     MethodAdapter mv,
			     int operand) {

	instrumentation(debug,
			"\tNEWARRAY.after(" + types[operand] + ")");

	execution(trace,
		  mv,
		  "\tNEWARRAY.after(" + types[operand] + ")");

	Label cleanup = new Label();

	Label finish = new Label();

	if (HeapSettings.conditional) {

	    // STACK: [...|count|obj]
	    visitCheck(mv,
		       cleanup);
	    // STACK: [...|count|obj]

	}

	// STACK: [...|count|obj]
        mv.visitInsn(Opcodes.DUP_X1);
        // STACK: [...|obj|count|obj]
        mv.visitInsn(Opcodes.SWAP);
        // STACK: [...|obj|obj|count]
        mv.visitLdcInsn(types[operand]);
        // STACK: [...|obj|obj|count|type]
        mv.visitLdcInsn((long)-1);
        // STACK: [...|obj|obj|count|type|size]
        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                           "com/foursquare/heapaudit/HeapUtil",
                           "record",
                           "(Ljava/lang/Object;ILjava/lang/String;J)V");
	// STACK: [...|obj]

	if (HeapSettings.conditional) {

	    visitCleanup(mv,
			 cleanup,
			 finish);
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

    // Mappings from primitive type opcode to friendly name.

    private static final String[] types = new String[] {
	"INVALID0",
	"INVALID1",
	"INVALID2",
	"INVALID3",
	"Z",
	"C",
	"F",
	"D",
	"B",
	"S",
	"I",
	"J"
    };

}
