package com.um.mt.kyle.parser;

import java.util.ArrayList;

/**
 * Created by kylebonnici on 30/04/15.
 */
public class BlockStackFrame  {

    private ArrayList<VariableStruct> localVariables = new ArrayList<VariableStruct>();
    private ArrayList<FunctionStackFrame> localFunctions = new ArrayList<FunctionStackFrame>();
    private BlockStackFrame parentFrame;

    public BlockStackFrame(BlockStackFrame parentFrame){
        this.parentFrame = parentFrame;
    }


    //------------------------Functions-----------------------------
    public final boolean addLocalFunction(FunctionStackFrame func){
        if (!hasLocalFunction(func.getIdentifier())){
            localFunctions.add(func);
            return false;
        }

        return true;
    }

    public final boolean hasLocalFunction(String identifier){
        return getLocalVariable(identifier) != null;
    }


    public FunctionStackFrame getLocalFunction(String identifier){
        for (int loops = 0 ; loops < localFunctions.size(); loops ++){
            if (localFunctions.get(loops).getIdentifier().equals(identifier)){
                return localFunctions.get(loops);
            }
        }

        return null;
    }

    public final FunctionStackFrame getFunction(String identifier){
        FunctionStackFrame func = getLocalFunction(identifier);

        if (func == null && parentFrame !=null){
            parentFrame.getFunction(identifier);
        }

        return func;
    }

    public final boolean hasFunction(String identifier){
        return getFunction(identifier) != null;
    }



    //----------------------Variables--------------------------
    public final boolean addLocalVariable(VariableStruct var){
        if (!hasLocalVariable(var.getIdentifier())){
            localVariables.add(var);
            return true;
        }

        return false;
    }

    public VariableStruct getVariable(String identifier){
        VariableStruct var = getLocalVariable(identifier);

        if (var == null && parentFrame !=null){
            var = parentFrame.getVariable(identifier);
        }

        return var;
    }

    public final boolean hasVariable(String identifier){
        return getVariable(identifier) != null;
    }

    public VariableStruct getLocalVariable(String identifier){
        for (int loops = 0 ; loops < localVariables.size(); loops ++){
            if (localVariables.get(loops).getIdentifier().equals(identifier)){
                return localVariables.get(loops);
            }
        }

        return null;
    }

    public final boolean hasLocalVariable(String identifier){
        return getLocalVariable(identifier) != null;
    }

}
