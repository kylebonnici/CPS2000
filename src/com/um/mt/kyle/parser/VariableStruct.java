package com.um.mt.kyle.parser;

/**
 * Created by kylebonnici on 30/04/15.
 */
public class VariableStruct {
    private String identifier;
    private String type;
    private Object value;

    public VariableStruct(String identifier, String type){
        this.identifier = identifier;
        this.type = type;
        this.value = null;
    }

    public VariableStruct(String identifier, String type, Object value){
        this.identifier = identifier;
        this.type = type;
        this.value = value;
    }

    public String getIdentifier(){
        return identifier;
    }

    public String getType(){
        return type;
    }
}
