package mt.edu.um.cs.cps2000.runtime;

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
        this.value = new String("");
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
        if (type.equals("god")){
            if (value instanceof Integer) return "int";
            else if (value instanceof Double) return "real";
            else if (value instanceof Character) return "char";
            else if (value instanceof String) return "string";
            else if (value instanceof Boolean) return "bool";
            else return "ERROR";
        }

        return type;
    }

    public Object getValue(){
        return value;
    }

    public boolean setValue(Object value){
        try{
            if (type.equals("int")){
                this.value = Integer.parseInt(value.toString());
            }else if (type.equals("real")){
                this.value = Double.parseDouble(value.toString());
            }else if (type.equals("bool")){
                this.value = Boolean.parseBoolean(value.toString());
            }else if (type.equals("char")){
                if (value.toString().length() == 1) {
                    this.value = new Character(value.toString().charAt(0));
                }else{
                    throw new Exception("");
                }
            }else if (type.equals("god")){
                setGodValue(value);
            }else {
                this.value = value;
            }

            return true;
        }catch (Exception e){
            System.out.println("Expecting " + getType());
        }

        return false;
    }

    private void setGodValue(Object value){
        this.value = value;
    }


    public VariableStruct clone(){
        VariableStruct out = new VariableStruct(this.identifier,this.type);
        return out;
    }
}
