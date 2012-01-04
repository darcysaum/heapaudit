package com.foursquare.heapaudit;

import java.util.ArrayList;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

public class HeapVariables extends HeapUtil {

    public HeapVariables(int access,
                         String desc,
                         boolean debug,
                         boolean trace,
                         HeapMethod mv) {

        this.debug = debug;

        this.trace = trace;

        this.mv = mv.mv;

        this.lvs = new LocalVariablesSorter(access,
                                            desc,
                                            mv);

    }

    public int define(Type type,
                      Label start,
                      Label end) {

        int index = lvs.newLocal(type);

        instrumentation(debug,
                        "\tDEFINE #" + index);

        execution(trace,
                  mv,
                  "\tDEFINE #" + index);

        variables.add(new Variable(index,
                                   type,
                                   start,
                                   end));

        return index;

    }

    public void declare() {

        for (Variable variable: variables) {

            instrumentation(debug,
                            "\tDECLARE #" + variable.index);

            execution(trace,
                      mv,
                      "\tDECLARE #" + variable.index);

            mv.visitLocalVariable("$" + variable.index,
                                  variable.type.getDescriptor(),
                                  null,
                                  variable.start,
                                  variable.end,
                                  variable.index);

        }

        variables = null;

    }

    private final boolean debug;

    private final boolean trace;

    private final MethodAdapter mv;

    public final LocalVariablesSorter lvs;

    class Variable {

        public Variable(int index,
                        Type type,
                        Label start,
                        Label end) {

            this.index = index;

            this.type = type;

            this.start = start;

            this.end = end;

        }

        public final int index;

        public final Type type;

        public final Label start;

        public final Label end;

    }

    private ArrayList<Variable> variables = new ArrayList<Variable>();

}
