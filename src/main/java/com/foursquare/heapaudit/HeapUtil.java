package com.foursquare.heapaudit;

import java.util.concurrent.ConcurrentHashMap;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public abstract class HeapUtil {

    public static void log(String text) {

	System.out.println(text);

    }

    public static void instrumentation(boolean debug,
				       String text) {

	if (debug) {

	    log("\t" + text);

	}

    }

    public static void execution(boolean trace,
				 MethodAdapter mv,
				 String text) {

	if (trace) {

	    // STACK [...]
	    mv.visitLdcInsn(text);
	    // STACK [...|text]
	    mv.visitMethodInsn(Opcodes.INVOKESTATIC,
			       "com/foursquare/heapaudit/HeapUtil",
			       "log",
			       "(Ljava/lang/String;)V");
	    // STACK [...]

	}

    }

    protected static void visitCheck(MethodVisitor mv,
				     Label cleanup) {

	// STACK: [...]
	mv.visitMethodInsn(Opcodes.INVOKESTATIC,
			   "com/foursquare/heapaudit/HeapRecorder",
			   "hasRecorders",
			   "()Z");
	// STACK: [...|status]
	mv.visitJumpInsn(Opcodes.IFEQ,
			 cleanup);
	// STACK: [...]

    }

    protected static void visitCleanup(MethodVisitor mv,
				       Label cleanup,
				       Label finish) {

	// STACK: [...]
	mv.visitJumpInsn(Opcodes.GOTO,
			 finish);
	// STACK: [...]
	mv.visitLabel(cleanup);
	// STACK: [...]

    }

    protected static void visitFinish(MethodVisitor mv,
				      Label finish) {

	// STACK: [...]
	mv.visitLabel(finish);
	// STACK: [...]

    }

    // The following holds a cache of type to size mappings.

    private static ConcurrentHashMap<String, Long> sizes = new ConcurrentHashMap<String, Long>();

    private static long sizeOf(Object obj,
                               String type) {

        Long size = sizes.get(type);

        if (size == null) {

	    size = HeapRecorder.instrumentation.getObjectSize(obj);

	    sizes.put(type, size);

	}

        return size;

    }

    // This is a per thread status to ignore allocations performed within the
    // record code path.

    private static ThreadLocal<Integer> recording = new ThreadLocal<Integer>() {

	@Override protected Integer initialValue() {

	    return 0;

	}

    };

    public static void record(Object obj,
                              int count,
                              String type,
                              long size) {

        // The following suppresses recording of allocations due to the
	// HeapAudit library itself to avoid being caught in an infinite loop.

        int index = recording.get();

        recording.set(index + 1);

        if (index == 0) {

	    record(count,
		   type,
		   size < 0 ? sizeOf(obj, type) : size);

	}

        recording.set(index);

    }

    public static void record(Object obj,
                              String type) {

        if (type.charAt(0) != '[') {

	    record(obj,
		   -1,
		   type,
		   -1);

	}
        else {

	    Object[] o = (Object[])obj;

	    int count = o.length;

	    for (int i = 1; i < type.length(); ++i) {

		if (type.charAt(i) == '[') {

		    switch (type.charAt(i + 1)) {

		    case 'Z':

			count *= ((boolean[])o[0]).length;

			break;

		    case 'B':

			count *= ((byte[])o[0]).length;

			break;

		    case 'C':

			count *= ((char[])o[0]).length;

			break;

		    case 'S':

			count *= ((short[])o[0]).length;

			break;

		    case 'I':

			count *= ((int[])o[0]).length;

			break;

		    case 'J':

			count *= ((long[])o[0]).length;

			break;

		    case 'F':

			count *= ((float[])o[0]).length;

			break;

		    case 'D':

			count *= ((double[])o[0]).length;

			break;

		    default:

			o = (Object[])(o[0]);

			count *= o.length;

		    }

                }
                else {

		    String name = type.substring(i);

		    record(obj,
			   count,
			   name,
			   sizeOf(obj, name) * count);

		    break;
		}

            }

        }

    }

    public static void record(Object obj,
			      int[] dimensions,
			      String type) {

	int count = 1;

	for (int i = 0; i < dimensions.length; ++i) {

	    count *= dimensions[i];

	}

	record(obj,
	       count,
	       type,
	       -1);

    }

    public static void record(int count,
                              String type,
                              long size) {

	try {

	    for (HeapRecorder recorder: HeapRecorder.getRecorders()) {

		recorder.record(type,
				count,
				size);

	    }

	} catch (Exception e) {

	    System.err.println(e);

	}

    }

}
