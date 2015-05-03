package mt.edu.um.cs.cps2000.runtime;

import org.w3c.dom.Node;

import java.util.ArrayList;

/**
 * Created by kylebonnici on 30/04/15.
 */
public class FunctionStackFrame extends BlockStackFrame {
    private String identifier;
    private ArrayList<VariableStruct> prams = new ArrayList<VariableStruct>();
    private String type = "Unknown";
    private String lineNumber = "-1";
    private Node functionDef = null;

    public FunctionStackFrame(BlockStackFrame parentFrame, String identifier, String lineNumber,Node functionDef){
        super(parentFrame);
        this.lineNumber = lineNumber;
        this.identifier = identifier;
        this.functionDef = functionDef;
    }

    public boolean hasPramVariable(String identifier){
        return getPramVariable(identifier) != null;
    }

    public VariableStruct getPramVariable(String identifier){
        for (int loops = 0 ; loops < prams.size(); loops ++){
            if (prams.get(loops).getIdentifier().equals(identifier)){
                return prams.get(loops);
            }
        }

        return null;
    }


    public VariableStruct getLocalVariable(String identifier){
        VariableStruct var = getPramVariable(identifier);

        if (var == null){
            var = super.getLocalVariable(identifier);
        }

        return var;
    }

    public void setType(String type){
        this.type = type;
    }

    public String getType(){
        return type;
    }

    public String getFunctionSignature(){
        StringBuilder out = new StringBuilder("(");

        for (int loops=0; loops < getNumberOfPrams(); loops ++){
            if (loops != 0){
                out.append(",");
            }
            out.append(getPramType(loops));
        }

        out.append(")");
        return out.toString();
    }

    public boolean addPram(VariableStruct pramVar){
        if (!hasPramVariable(pramVar.getIdentifier())){
            prams.add(pramVar);
            return true;
        }

        return false;
    }

    public String getPramType(int index){
        if (index < prams.size()){
            return prams.get(index).getType();
        }
        return "unit";
    }

    public int getNumberOfPrams(){
        return prams.size();
    }

    public String getIdentifier(){
        return identifier;
    }

    public String getLineNumber(){
        return lineNumber;
    }
}
