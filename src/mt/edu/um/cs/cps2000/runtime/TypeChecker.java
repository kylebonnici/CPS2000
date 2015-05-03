package mt.edu.um.cs.cps2000.runtime;

import java_cup.runtime.Symbol;
import mt.edu.um.cs.cps2000.lexer.JParserSym;
import mt.edu.um.cs.cps2000.parser.VariableStruct;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by kylebonnici on 01/05/15.
 */
public class TypeChecker {
    private ArrayList<String> errorLog;
    private boolean useLineNumbers = true;
    private Stack<BlockStackFrame> stackFrames;

    private void errorLogger(int lineNumber, String expected){
        errorLogger(lineNumber, expected, true);
    }

    private void errorLogger(int lineNumber, String expected,boolean exp){
        if (useLineNumbers) {
            errorLog.add("Error: " + (exp ? "Expected: " : "") + expected + "  at line " + lineNumber);
        }else {
            errorLog.add("Error: " + (exp ? "Expected: " : "") + expected);
        }
    }

    private boolean isValidCast(String from, String to ){
        if (from.equals("int")){
            if (to.equals("int") || to.equals("real") || to.equals("char")) return true;
        }else if (from.equals("real")){
            if (to.equals("int") || to.equals("real")) return true;
        }else if (from.equals("char")){
            if (to.equals("int") || to.equals("real" ) || to.equals("string") || to.equals("char")) return true;
        }else if (from.equals("Unknown")){
            return true;
        }

        return false;
    }


    private String getExpressionLiteralType(Node node){
        NodeList children = node.getChildNodes();

        String type =  getSimpleExpressionLiteralType(children.item(0));

        for (int loops = 2 ; loops < children.getLength(); loops += 2){
            String nextType = getSimpleExpressionLiteralType(children.item(loops));
            if (type.equals("int")){
                if (nextType.equals("real") || nextType.equals("int")) type = "bool";
                else type = "ERROR";
            }else if (type.equals("real")){
                if (nextType.equals("real") || nextType.equals("int")) type = "bool";
                else type = "ERROR";
            }else if (type.equals("unit")){
                type = "ERROR";
            }else if (type.equals("bool")){
                if (!nextType.equals("bool")) type = "ERROR";
            }else if (type.equals("char")){
                if (nextType.equals("char")) type = "bool";
                else type = "ERROR";
            }else if (type.equals("string")){
                if (nextType.equals("int")) type = "bool";
                else type = "ERROR";
            }
        }

        return type ;
    }

    private String getSimpleExpressionLiteralType(Node node){
        NodeList children = node.getChildNodes();

        String type =  getTermLiteralType(children.item(0));

        for (int loops = 2 ; loops < children.getLength(); loops += 2){
            String nextType = getTermLiteralType(children.item(loops));
            if (type.equals("int")){
                if (nextType.equals("real")) type = "real";
                else if (nextType.equals("int")) type = "int";
                else type = "ERROR";
            }else if (type.equals("real")){
                if (nextType.equals("real") || nextType.equals("int")) type = "real";
                else type = "ERROR";
            }else if (type.equals("unit")){
                if (!nextType.equals("unit")) type = "ERROR";
            }else if (type.equals("bool")){
                if (!nextType.equals("bool")) type = "ERROR";
            }else if (type.equals("char")){
                if (nextType.equals("char") || nextType.equals("string")) type = "string";
                else type = "ERROR";
            }else if (type.equals("string")){
                if (nextType.equals("char") || nextType.equals("string")) type = "string";
                else type = "ERROR";
            }
        }

        return type ;
    }

    private boolean validSimpleExpressionOperation(String type1 , String type2, String op){
        if (type1.equals("Unknown") ||type2.equals("Unknown") ) return true;

        boolean hasInt = type1.equals("int") || type2.equals("int");
        boolean hasReal = type1.equals("real") || type2.equals("real");
        boolean hasChar = type1.equals("char") || type2.equals("char");
        //boolean hasUnit = type1.equals("unit") || type2.equals("unit");
        boolean hasString = type1.equals("string") || type2.equals("string");
        //boolean hasBool = type1.equals("bool") || type2.equals("bool");

        boolean bothInt = type1.equals("int") && type2.equals("int");
        boolean bothReal = type1.equals("real") && type2.equals("real");
        boolean bothChar = type1.equals("char") && type2.equals("char");
        //boolean bothUnit = type1.equals("unit") && type2.equals("unit");
        boolean bothString = type1.equals("string") && type2.equals("string");
        boolean bothBool = type1.equals("bool") && type2.equals("bool");

        boolean boolOp = op.equals("or");
        boolean numericOp = op.equals("-") || op.equals("+");
        boolean stringOp = op.equals("+");

        if (bothBool && boolOp){
            return true;
        }else if (numericOp && ((hasInt && hasReal) || bothInt || bothReal) ){
            return true;
        }else if (stringOp && ((hasChar && hasString) || bothChar || bothString) ){
            return true;
        }

        return false;
    }

    private String getTermLiteralType(Node node){
        NodeList children = node.getChildNodes();

        String type =  getFactorLiteralType(children.item(0));

        for (int loops = 2 ; loops < children.getLength(); loops += 2){
            String nextType = getFactorLiteralType(children.item(loops));
            if (type.equals("int")){
                if (nextType.equals("real")) type = "real";
                else if (nextType.equals("int")) type = "int";
                else type = "ERROR";
            }else if (type.equals("real")){
                if (nextType.equals("real") || nextType.equals("int")) type = "real";
                else type = "ERROR";
            }else if (type.equals("unit")){
                if (!nextType.equals("unit")) type = "ERROR";
            }else if (type.equals("bool")){
                if (!nextType.equals("bool")) type = "ERROR";
            }else if (type.equals("char")){
                type = "ERROR";
            }else if (type.equals("string")){
                type = "ERROR";
            }
        }

        return type ;
    }

    private boolean validTermOperation(String type1 , String type2, String op){

        if (type1.equals("Unknown") ||type2.equals("Unknown") ) return true;

        boolean hasInt = type1.equals("int") || type2.equals("int");
        boolean hasReal = type1.equals("real") || type2.equals("real");
        //boolean hasChar = type1.equals("char") || type2.equals("char");
        //boolean hasUnit = type1.equals("unit") || type2.equals("unit");
        //boolean hasString = type1.equals("string") || type2.equals("string");
        //boolean hasBool = type1.equals("bool") || type2.equals("bool");

        boolean bothInt = type1.equals("int") && type2.equals("int");
        boolean bothReal = type1.equals("real") && type2.equals("real");
        //boolean bothChar = type1.equals("char") && type2.equals("char");
        //boolean bothUnit = type1.equals("unit") && type2.equals("unit");
        //boolean bothString = type1.equals("string") && type2.equals("string");
        boolean bothBool = type1.equals("bool") && type2.equals("bool");

        boolean boolOp = op.equals("and");
        boolean numericOp = op.equals("*") || op.equals("/");

        if (bothBool && boolOp){
            return true;
        }else if (numericOp && ((hasInt && hasReal) || bothInt || bothReal) ){
            return true;
        }

        return false;
    }

    private String getFactorLiteralType(Node node){
        Node child = node.getFirstChild();

        if (child.getNodeName().equals("Literal")){
            return getLiteralType(child);
        }else if (child.getNodeName().equals("Identifier")){
            return getIdentifierLiteralType(child);
        }else if (child.getNodeName().equals("FunctionCall")){
            return getFunctionCallLiteralType(child);
        }else if (child.getNodeName().equals("TypeCast")){
            return getTypeCastLiteralType(child);
        }else if (child.getNodeName().equals("SubExpression")){
            return getSubExpressionLiteralType(child);
        }else if (child.getNodeName().equals("Unary")){
            return getUnaryLiteralType(child);
        }

        return "Unknown";
    }

    private String getLiteralType(Node node){
        Node child = node.getFirstChild();

        if (child.getNodeName().equals("RealLiteral")){
            return "real";
        }else if (child.getNodeName().equals("BooleanLiteral")){
            return "bool";
        }else if (child.getNodeName().equals("CharLiteral")){
            return "char";
        }else if (child.getNodeName().equals("UnitLiteral")){
            return "unit";
        }else if (child.getNodeName().equals("IntegerLiteral")){
            return "int";
        }else if (child.getNodeName().equals("StringLiteral")){
            return "string";
        }

        return "Unknown";
    }

    private String getIdentifierLiteralType(Node node){
        VariableStruct var = stackFrames.peek().getVariable(node.getTextContent());

        if (var == null){
            return "Unknown";
        }
        return var.getType();
    }

    private String getFunctionCallLiteralType(Node node){
        Node identifier = node.getFirstChild();
        Node actualPrams = node.getChildNodes().item(1);

        String signature = getSignatureFromActualParams(actualPrams);

        FunctionStackFrame func = stackFrames.peek().getLocalFunction(identifier.getTextContent(), signature);

        if (func == null){
            return "Unknown";
        }else {
            return func.getType();
        }
    }

    private String getTypeCastLiteralType(Node node){
        Node type = node.getFirstChild();
        return type.getTextContent();
    }

    private String getSubExpressionLiteralType(Node node){
        Node expression = node.getFirstChild();

        return getExpressionLiteralType(expression);
    }

    private String getUnaryLiteralType(Node node){
        Node expression = node.getChildNodes().item(1);
        return getExpressionLiteralType(expression);
    }


    private boolean isValidType(String typeToBe, Symbol sym){
        int isType = sym.sym;

        switch(isType){
            case JParserSym.BOOLEAN_LITERAL:
                return  (typeToBe.equals("bool"));
            case JParserSym.INTEGER_LITERAL:
                return  (typeToBe.equals("int"));
            case JParserSym.STRING_LITERAL:
                return  (typeToBe.endsWith("string") || typeToBe.equals("char"));
            case JParserSym.CHAR_LITERAL:
                return  (typeToBe.equals("char"));
            case JParserSym.REAL_LITERAL:
                return  (typeToBe.equals("real") || typeToBe.equals("int"));
            case JParserSym.UNIT_LITERAL:
                return  (typeToBe.equals("unit"));
        }

        return false;
    }

    private String typeTokenToString(int type){
        switch(type){
            case JParserSym.BOOLEAN_LITERAL:
                return  "bool";
            case JParserSym.INTEGER_LITERAL:
                return  "int";
            case JParserSym.STRING_LITERAL:
                return  "string";
            case JParserSym.CHAR_LITERAL:
                return  "char";
            case JParserSym.REAL_LITERAL:
                return  "real";
            case JParserSym.UNIT_LITERAL:
                return  "unit";
        }

        return "";
    }

    private boolean validExpressionOperation(String type1 , String type2, String op){
        if (type1.equals("Unknown") ||type2.equals("Unknown") ) return true;

        boolean hasInt = type1.equals("int") || type2.equals("int");
        boolean hasReal = type1.equals("real") || type2.equals("real");
        //boolean hasChar = type1.equals("char") || type2.equals("char");
        //boolean hasUnit = type1.equals("unit") || type2.equals("unit");
        //boolean hasString = type1.equals("string") || type2.equals("string");
        //boolean hasBool = type1.equals("bool") || type2.equals("bool");

        boolean bothInt = type1.equals("int") && type2.equals("int");
        boolean bothReal = type1.equals("real") && type2.equals("real");
        boolean bothChar = type1.equals("char") && type2.equals("char");
        //boolean bothUnit = type1.equals("unit") && type2.equals("unit");
        boolean bothString = type1.equals("string") && type2.equals("string");
        boolean bothBool = type1.equals("bool") && type2.equals("bool");

        if (bothBool){
            return true;
        }else if ((hasInt && hasReal) || bothInt || bothReal){
            return true;
        }else if (bothChar || bothString){
            return true;
        }

        return false;
    }

    private boolean wasVariableDeclared(String identifier, int lineNumber){
        //--------------------check if variable was declared------------------------------
        BlockStackFrame blockFrame = stackFrames.peek();
        VariableStruct var = null;
        var = blockFrame.getVariable(identifier);
        if (var == null){
            errorLogger(lineNumber, "Variable '" + identifier + "' was not declared",false);
            return false;
        }
        return true;
        //--------------------------------------------------------------------------------
    }

    private void wasFunctionDeclared(String identifier, String signature, int lineNumber){
        //--------------------check if function was declared------------------------------
        BlockStackFrame blockFrame = stackFrames.peek();
        FunctionStackFrame func = null;
        func = blockFrame.getFunction(identifier, signature);
        if (func == null || !func.getFunctionSignature().equals(signature)){
            errorLogger(lineNumber, "Function '" + identifier + signature + "' was not defined",false);
        }
        //--------------------------------------------------------------------------------
    }

    private void verifyValidTypeForIdentifier(String identifier, Node expression, int lineNumber){
        BlockStackFrame blockFrame = stackFrames.peek();
        VariableStruct var = null;
        var = blockFrame.getVariable(identifier);
        if (var != null){
            String expType = getExpressionLiteralType(expression);
            if (!var.getType().equals(expType) && !expType.equals("ERROR") && !expType.equals("Unknown")) {
                errorLogger(lineNumber, "Wrong type. Found '" + expType + "' expecting '" + var.getType() + "'", false);
            }
        }
    }

    private String getSignatureFromActualParams(Node actualParams){
        StringBuilder signature = new StringBuilder("(");

        NodeList expressions = actualParams.getChildNodes();

        for (int loops=0; loops < expressions.getLength(); loops ++){
            if (loops != 0){
                signature.append(",");
            }
            signature.append(getExpressionLiteralType(expressions.item(loops)));
        }

        signature.append(")");

        return signature.toString();
    }
}
