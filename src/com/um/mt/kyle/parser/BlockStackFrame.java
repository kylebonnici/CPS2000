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
        if (!hasLocalFunction(func.getIdentifier(),func.getFunctionSignature())){
            localFunctions.add(func);
            return true;
        }

        return false;
    }

    public final boolean hasLocalFunction(String identifier, String signature){
        return getLocalFunction(identifier,signature) != null;
    }


    public FunctionStackFrame getLocalFunction(String identifier, String signature){
        for (int loops = 0 ; loops < localFunctions.size(); loops ++){
            FunctionStackFrame funcIterator = localFunctions.get(loops);
            if (funcIterator.getIdentifier().equals(identifier) && funcIterator.getFunctionSignature().equals(signature)){
                return localFunctions.get(loops);
            }
        }

        return null;
    }

    public FunctionStackFrame getFunction(FunctionStackFrame func){
        return getFunction(func.getIdentifier(),func.getFunctionSignature());
    }

    public final FunctionStackFrame getFunction(String identifier, String signature){
        FunctionStackFrame localFunc = getLocalFunction(identifier, signature);

        if (localFunc == null && parentFrame !=null){
            parentFrame.getFunction(identifier,signature);
        }

        return localFunc;
    }

    public final boolean hasFunction(String identifier, String signature){
        return getFunction(identifier,signature) != null;
    }

    public final boolean hasFunction(FunctionStackFrame func){
        return hasFunction(func.getIdentifier(),func.getFunctionSignature());
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
