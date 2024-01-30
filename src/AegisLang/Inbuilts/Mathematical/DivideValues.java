package AegisLang.Inbuilts.Mathematical;

import AegisLang.InternalValue;
import AegisLang.Interpreter;
import AegisLang.InterpreterFunction;
import AmbrosiaUI.Utility.Logger;

import java.util.ArrayList;

public class DivideValues extends InterpreterFunction {
    public DivideValues(Interpreter interpreter) {
        super(interpreter);
    }

    @Override
    public InternalValue execute(ArrayList<InternalValue> values) {
        values = replaceVariblesWithValues(values);

        if(values.size() == 0){
            Logger.printWarning("Function divide executed with no arguments.");
            return new InternalValue(InternalValue.ValueType.NONE);
        }
        if(values.size() == 1){
            return values.get(0);
        }

        InternalValue out = values.get(0);

        for(int i = 1;i < values.size();i++){
            out = divideTwoValues(out, values.get(i));
        }

        return out;
    }

    private InternalValue divideTwoValues(InternalValue value1, InternalValue value2){
        if(value1.getType() == InternalValue.ValueType.INT && value2.getType() == InternalValue.ValueType.INT){
            return new InternalValue(InternalValue.ValueType.INT,
                    Integer.parseInt(value1.getValue()) / Integer.parseInt(value2.getValue()) + "");
        }

        return new InternalValue(InternalValue.ValueType.NONE);
    }
}