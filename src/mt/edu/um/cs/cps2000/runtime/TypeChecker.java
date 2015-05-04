package mt.edu.um.cs.cps2000.runtime;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Stack;

public class TypeChecker {
    protected Stack<BlockStackFrame> stackFrames;
    protected boolean showLineNumbers = false;
    protected String lineNumber = "";
    protected InputStream inStream = System.in;
    protected String lastExpressionType = null;

    public TypeChecker(){
        stackFrames = new Stack<BlockStackFrame>();
        VariableStruct ans = new VariableStruct("ans","god");
        BlockStackFrame root = new BlockStackFrame(null);
        root.addLocalVariable(ans);
        stackFrames.push(root);
    }

    protected boolean validType(String exp,String found){
        return found.equals("Unknown") || found.equals(exp) || exp.equals("any");
    }

    public boolean typeCheck(Element node){
        BlockStackFrame typeCheck = new BlockStackFrame(stackFrames.peek());
        ArrayList<VariableStruct> vars = stackFrames.peek().getLocalVariables();
        stackFrames.push(typeCheck);
        for (int loops = 0 ; loops < vars.size(); loops ++){
            typeCheck.addLocalVariable(vars.get(loops));
        }
        boolean ok = checkStatements(node.getChildNodes());
        stackFrames.pop();
        return ok;
    }

    private boolean checkStatement(Node node){
        boolean ok = false;
        Node statement = node.getFirstChild();
        if (statement.getNodeName().equals("FunctionDecl")){
            ok = checkFunctionDecl(statement);
            lastExpressionType = null;
        }else if (statement.getNodeName().equals("Assignment")){
            ok = checkAssignment(statement);
            lastExpressionType = null;
        }else if (statement.getNodeName().equals("VariableDecl")){
            ok = checkVariableDecl(statement);
            lastExpressionType = null;
        }else if (statement.getNodeName().equals("ReadStatement")){
            ok = checkReadStatement(statement);
            lastExpressionType = null;
        }else if (statement.getNodeName().equals("WriteStatement")){
            ok = checkWriteStatement(statement);
            lastExpressionType = null;
        }else if (statement.getNodeName().equals("IfStatement")){
            ok = checkIfStatement(statement);
            lastExpressionType = null;
        }else if (statement.getNodeName().equals("WhileStatement")){
            ok = checkWhileStatement(statement);
            lastExpressionType = null;
        }else if (statement.getNodeName().equals("HaltStatement")){
            ok = checkHaltStatement(statement);
            lastExpressionType = null;
        }else if (statement.getNodeName().equals("Block")){
            ok = checkBlock(statement);
        }else if (statement.getNodeName().equals("Expression")){
            ok = checkExpression(statement);
        }

        return ok;
    }

    private boolean checkFunctionDecl(Node node){
        Element identifier = (Element)node.getChildNodes().item(0);
        Node formalParams = node.getChildNodes().item(1);
        Node type = node.getChildNodes().item(2);
        Node block = node.getChildNodes().item(3);

        lineNumber = identifier.getAttribute("lineNumber");

        FunctionStackFrame func = new FunctionStackFrame(stackFrames.peek(),identifier.getTextContent(), lineNumber,node);
        boolean ok = pushFormalPramsToFunc(formalParams,func);

        FunctionStackFrame funcFound = stackFrames.peek().getFunction(func);

        stackFrames.push(func);
        func.setType(type.getTextContent());
        ok = ok && checkBlock(block);
        stackFrames.pop();

        if (!func.getType().equals("unit")){
            if (lastExpressionType != null){
                String expType = lastExpressionType;
                if (!validType(func.getType(),expType)){
                    ok = false;
                    errorLogger(lineNumber, "Return type for function '" + func.getIdentifier() + func.getFunctionSignature() + "' should be '" + func.getType() + "' not '" + expType + "'",false);
                }
            }else {
                errorLogger(lineNumber, "No return expression was found is function '" + func.getIdentifier() + func.getFunctionSignature() + "'",false);
                ok = false;
            }
        }

        if (funcFound !=null){
            errorLogger(lineNumber, "Function '" + func.getIdentifier() + func.getFunctionSignature() + "' was already defined",false);
            ok = false;
        }else {
            if (ok) {
                stackFrames.peek().addLocalFunction(func);
            }
        }

        return ok;
    }

    protected boolean pushFormalPramsToFunc(Node node,FunctionStackFrame func){
        boolean ok = true;
        NodeList list = node.getChildNodes();

        for(int loops = 0 ; loops < list.getLength(); loops ++){
            if (!pushFormalPramToFunc(list.item(loops),func)){
                ok = false;
            }
        }

        return ok;
    }


    private boolean pushFormalPramToFunc(Node node,FunctionStackFrame func){
        Element identifier = (Element)node.getChildNodes().item(0);
        Node type = node.getChildNodes().item(1);

        VariableStruct var = func.getPramVariable(identifier.getTextContent());

        if (var != null){
            errorLogger(identifier.getAttribute("lineNumber"),"Variable '" + var.getIdentifier() + "' was already declared in function '" + func.getIdentifier() + "'",false);
            return false;
        }else{
            var = new VariableStruct(identifier.getTextContent(), type.getTextContent());
            func.addPram(var);
            return true;
        }
    }

    private boolean checkAssignment(Node node){
        Node identifier = node.getChildNodes().item(0);
        Node expression = node.getChildNodes().item(1);

        if (!checkExpression(expression)){
            return false;
        }

        return checkIdentifier(identifier,getExpressionLiteralType(expression), false);
    }

    private boolean checkVariableDecl(Node node){
        Element val = ((Element)node.getChildNodes().item(0));
        Node type = node.getChildNodes().item(1);
        Node expression = node.getChildNodes().item(2);

        if (type.getTextContent().equals("unit")){
            errorLogger(val.getAttribute("lineNumber"),"Cannot declare a variable of type 'unit'",false);
            return false;
        }

        boolean ok = !wasVariableDeclared(val.getTextContent(),val.getAttribute("lineNumber"));
        boolean addVarToStackFrame = ok;

        ok = ok && checkExpression(expression);

        if (ok){
            String typeFound = getExpressionLiteralType(expression);
            if (!validType(type.getTextContent(),typeFound)) {
                errorLogger(val.getAttribute("lineNumber"),"Found '" + typeFound + "' expecting '" + type.getTextContent() + "'",false);
                ok = false;
            }
        }

        VariableStruct var = new VariableStruct(val.getTextContent(),type.getTextContent());

        if (node.getChildNodes().getLength() == 4){
            Node block = node.getChildNodes().item(3);
            ok = ok && checkBlock(block,var);
        }else{
            if (ok) {
                stackFrames.peek().addLocalVariable(var);
                checkIdentifier(val, type.getTextContent(),true);
            }
        }

        return ok;
    }

    private boolean checkReadStatement(Node node){
        return checkIdentifier(node.getFirstChild(),"any",true);
    }

    private boolean checkWriteStatement(Node node){
        return checkIdentifier(node.getFirstChild(),"any",true);
    }

    private boolean checkIfStatement(Node node){
        boolean ok = true;
        Node expression = node.getChildNodes().item(0);
        Node statement = node.getChildNodes().item(1);

        if (!checkExpression(expression)){
            return false;
        }

        String type = getExpressionLiteralType(expression);

        if (!validType("bool", type)){
            errorLogger(lineNumber,"Found '" + type + "' expecting 'int'",false);
            ok = false;
        }

        ok = ok && checkStatement(statement);

        if (node.getChildNodes().getLength() == 3){
            statement = node.getChildNodes().item(2);
            ok = ok && checkStatement(statement);
        }

        return ok;
    }
    private boolean checkWhileStatement(Node node){
        boolean ok = true;
        Node expression = node.getChildNodes().item(0);
        Node statement = node.getChildNodes().item(1);

        if (!checkExpression(expression)){
            return false;
        }

        String type = getExpressionLiteralType(expression);

        if (!validType("bool", type)){
            errorLogger(lineNumber,"Found '" + type + "' expecting 'int'",false);
            ok = false;
        }

        ok = ok && checkStatement(statement);

        return ok;
    }

    private boolean checkHaltStatement(Node node){
        NodeList list = node.getChildNodes();

        if (list.getLength() != 0) {
            Element val = (Element) list.item(0);
            if (val.getNodeName().equals("Identifier")) {
                return checkIdentifier(val, "int",true);
            }
        }

        return true;
    }

    protected boolean checkBlock(Node node){
        BlockStackFrame blockStackFrame = new BlockStackFrame(stackFrames.peek());
        stackFrames.push(blockStackFrame);
        boolean ok = checkStatements(node.getChildNodes());
        stackFrames.pop();
        return ok;
    }

    private boolean checkBlock(Node node,VariableStruct var){
        BlockStackFrame blockStackFrame = new BlockStackFrame(stackFrames.peek());
        stackFrames.push(blockStackFrame);
        blockStackFrame.addLocalVariable(var);
        boolean ok = checkStatements(node.getChildNodes());
        stackFrames.pop();
        return ok;
    }

    private boolean checkStatements(NodeList list){
        boolean ok = true;
        for (int loops = 0 ; loops < list.getLength(); loops ++){
            Node node = list.item(loops);
            if (node.getNodeName().equals("Statement")) {
                if (!checkStatement(node)) {
                    ok = false;
                }
            }else {
                ok = false;
            }
        }
        return ok;
    }

    private boolean checkIdentifier(Node node, String type, boolean typeExpecting){
        Element val = (Element)node;
        String identifier = val.getTextContent();
        VariableStruct var = stackFrames.peek().getVariable(identifier);
        if (var != null){
            boolean validType;

            if (typeExpecting) validType = validType(type,var.getType());
            else validType = validType(var.getType(), type);

            if (!validType && typeExpecting) errorLogger(val.getAttribute("lineNumber"),"Found '" + var.getType() + "' expecting '" + type + "'",false);
            else if (!validType && !typeExpecting) errorLogger(val.getAttribute("lineNumber"),"Found '" + type  + "' expecting '" + var.getType() + "'",false);
            return validType;
        }else {
            errorLogger(val.getAttribute("lineNumber"),"Variable '" + identifier + "' was not declared",false);
            return false;
        }
    }

    private boolean checkExpression(Node node){
        boolean ok = checkExpression2(node);
        if (ok) lastExpressionType = getExpressionLiteralType(node);
        return ok;
    }

    private boolean checkExpression2(Node node){
        NodeList list  = node.getChildNodes();

        boolean ok = checkSimpleExpression(node.getFirstChild());

        if (list.getLength() == 1){
            return ok;
        }

        String expType = getSimpleExpressionLiteralType(node.getFirstChild());
        String op = list.item(1).getTextContent();

        if (expType.equals("bool")){
            if (!op.equals("==") && !op.equals("!=")){
                errorLogger(lineNumber, "Operator '" + op + "' cannot be used with type  '" + expType + "'", false);
                ok = false;
            }
        }

        ok = ok && checkSimpleExpression(list.item(2));
        String found = "";
        if (ok) found =  getSimpleExpressionLiteralType(list.item(2));

        if (ok && !validType(expType, found )) {
            errorLogger(lineNumber, "Type mismatch " + expType + " and " + found, false);
            ok = false;
        }


        return ok;
    }

    private boolean checkTerm(Node node){
        NodeList list  = node.getChildNodes();

        boolean ok = checkFactor(node.getFirstChild());

        if (list.getLength() == 1){
            return ok;
        }

        String expType = getFactorLiteralType(node.getFirstChild());

        for (int loops = 2 ; loops < list.getLength() && ok; loops +=2 ){
            String op = list.item(loops-1).getTextContent();
            if (expType.equals("char") || expType.equals("string") || expType.equals("unit")) {
                errorLogger(lineNumber, "Operator '" + op + "' cannot be used with type  '" + expType + "'", false);
                ok = false;
            }else if ((op.equals("and") && !expType.equals("bool")) || (!op.equals("and") && expType.equals("bool"))){
                errorLogger(lineNumber, "Operator '" + op +  "' cannot be used with type  '" + expType + "'", false);
                ok = false;
            }

            if (!expType.equals("bool") && ok) {
                ok =  checkFactor(list.item(loops));
                String found = getFactorLiteralType(list.item(loops));

                if (ok && !(validType("int", found) || validType("real", found)) ) {
                    errorLogger(lineNumber, "Type mismatch " + expType + " and " + found, false);
                    ok = false;
                }
            }
        }

        return ok;
    }

    private boolean checkSimpleExpression(Node node){
        NodeList list  = node.getChildNodes();

        boolean ok = checkTerm(node.getFirstChild());

        if (list.getLength() == 1){
            return ok;
        }

        String expType = getTermLiteralType(node.getFirstChild());

        for (int loops = 2 ; loops < list.getLength() && ok; loops +=2 ){
            String op = list.item(loops-1).getTextContent();
            if (expType.equals("unit")) {
                errorLogger(lineNumber, "Operator '" + op + "' cannot be used with type  '" + expType + "'", false);
                ok = false;
            }else if ((op.equals("or") && !expType.equals("bool")) || (!op.equals("or") && expType.equals("bool"))){
                errorLogger(lineNumber, "Operator '" + op +  "' cannot be used with type  '" + expType + "'", false);
                ok = false;
            }else if ((op.equals("-") && expType.equals("char")) || (op.equals("-") && expType.equals("string")) ){
                errorLogger(lineNumber, "Operator '" + op +  "' cannot be used with type  '" + expType + "'", false);
                ok = false;
            }

            if (ok && !expType.equals("bool")) {
                ok = checkTerm(list.item(loops));
                String found = getTermLiteralType(list.item(loops));

                if (expType.equals("int") || expType.equals("real")) {
                    if (ok && !(validType("int", found) || validType("real", found))) {
                        errorLogger(lineNumber, "Type mismatch " + expType + " and " + found, false);
                        ok = false;
                    }
                }else if (expType.equals("char") || expType.equals("string")) {
                    if (ok && !(validType("char", found) || validType("string", found))) {
                        errorLogger(lineNumber, "Type mismatch " + expType + " and " + found, false);
                        ok = false;
                    }
                }
            }
        }

        return ok;
    }

    private boolean checkFactor(Node node){
        Node child = node.getFirstChild();

        if (child.getNodeName().equals("Literal")){
            return true;
        }else if (child.getNodeName().equals("Identifier")){
            return checkIdentifier(child,"any",true);
        }else if (child.getNodeName().equals("FunctionCall")){
            return checkFunctionCall(child);
        }else if (child.getNodeName().equals("TypeCast")){
            return checkTypeCast(child);
        }else if (child.getNodeName().equals("SubExpression")){
            return checkSubExpression(child);
        }else if (child.getNodeName().equals("Unary")){
            return checkUnary(child);
        }

        return true;
    }

    private boolean checkUnary(Node node){
        String op = node.getTextContent();
        Node expression = node.getFirstChild();

        boolean ok = checkExpression(expression);

        if (ok){
            String expType = getExpressionLiteralType(expression);
            if ((expType.equals("bool") && !op.equals("not")) || (!expType.equals("bool") && op.equals("not"))){
                errorLogger(lineNumber, "Operator '" + op +  "' cannot be used with type  '" + expType + "'", false);
                ok = false;
            }else if (expType.equals("char") || expType.equals("string") || expType.equals("unit")){
                errorLogger(lineNumber, "Operator '" + op +  "' cannot be used with type  '" + expType + "'", false);
                ok = false;
            }
        }

        return ok;
    }

    private boolean checkSubExpression(Node node){
        return checkExpression(node.getFirstChild());
    }

    private boolean checkFunctionCall(Node node){
        Element identifier = (Element)node.getChildNodes().item(0);
        Node actualPrams = node.getChildNodes().item(1);

        lineNumber = identifier.getAttribute("lineNumber");

        if (checkActualPrams(actualPrams)){
            String signature = getSignatureFromActualParams(actualPrams);
            if (!stackFrames.peek().hasFunction(identifier.getTextContent(), signature)){
                errorLogger(lineNumber, "Function  '" + identifier.getTextContent()+signature + "' was not declared", false);
                return false;
            }
            return true;
        }

        return false;
    }

    private boolean checkActualPrams(Node node){
        boolean ok = true;

        NodeList list = node.getChildNodes();

        for (int loops = 0 ; loops < list.getLength(); loops ++){
            if (checkExpression(list.item(loops))){
                String type = getExpressionLiteralType(list.item(loops));
                ok = ok && !type.equals("unit");
                if (!ok){
                    errorLogger(lineNumber, "Cannot have a 'unit' type in a function call", false);
                }
            }else{
                ok = false;
            }
        }

        return ok;
    }

    private boolean checkTypeCast(Node node){
        Node type = node.getChildNodes().item(0);
        Node expression = node.getChildNodes().item(1);

        boolean ok = checkExpression(expression);

        if (ok){
            String expType = getExpressionLiteralType(expression);
            ok = isValidCast(expType,type.getTextContent());
            if(!ok){
                errorLogger(lineNumber, "Cannot cast from '" + expType + "' to '" + type.getTextContent() + "'", false);
            }
        }

        return ok;
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

    protected void errorLogger(String lineNumber, String expected,boolean exp){
        if (showLineNumbers) {
            System.out.println("Error: " + (exp ? "Expected: " : "") + expected + "  at line " + lineNumber);
        } else {
            System.out.println("Error: " + (exp ? "Expected: " : "") + expected);
        }
    }

    protected String getExpressionLiteralType(Node node){
        NodeList children = node.getChildNodes();

        String type =  getSimpleExpressionLiteralType(children.item(0));

         if (children.getLength() == 3){
            String nextType = getSimpleExpressionLiteralType(children.item(2));
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
                if (nextType.equals("string")) type = "bool";
                else type = "ERROR";
            }
        }

        return type ;
    }

    protected String getSimpleExpressionLiteralType(Node node){
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

    protected String getTermLiteralType(Node node){
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
        Element child = (Element)node.getFirstChild();

        lineNumber = child.getAttribute("lineNumber");

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

        lineNumber = ((Element)node).getAttribute("lineNumber");

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

    private boolean wasVariableDeclared(String identifier, String lineNumber){
        //--------------------check if variable was declared------------------------------
        BlockStackFrame blockFrame = stackFrames.peek();
        VariableStruct var = blockFrame.getLocalVariable(identifier);
        if (var != null){
            errorLogger(lineNumber, "Variable '" + identifier + "' is already declared",false);
            return true;
        }
        return false;
        //--------------------------------------------------------------------------------
    }

    protected String getSignatureFromActualParams(Node actualParams){
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
