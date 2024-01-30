package AegisLang;

import java.util.ArrayList;

public class InterpreterFunction {

    protected Interpreter interpreter;

    public InterpreterFunction(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public InternalValue execute(ArrayList<InternalValue> values){
        return new InternalValue(InternalValue.ValueType.NONE);
    }

    protected ArrayList<InternalValue> replaceVariblesWithValues(ArrayList<InternalValue> values){
        ArrayList<InternalValue> valuesOut = new ArrayList<>();

        for (InternalValue value: values){
            if(value.getType() == InternalValue.ValueType.ID){
                valuesOut.add(interpreter.getVariableValue(value));
                continue;
            }
            valuesOut.add(value);
        }
        
        return valuesOut;
    }
}