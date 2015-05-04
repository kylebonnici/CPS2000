package mt.edu.um.cs.cps2000.runtime;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.IntSummaryStatistics;
import java.util.Objects;


/**
 * Created by kylebonnici on 03/05/15.
 */
public class Execute extends TypeChecker {
    private Object lastExpression = null;

    public void run (Element node){
        if (typeCheck(node)){
            runStatements(node.getChildNodes());
        }
    }

    private void runStatements(NodeList list){
        for (int loops = 0 ; loops < list.getLength(); loops ++){
            Node node = list.item(loops);
            if (node.getNodeName().equals("Statement")) {
                runStatement(node);
                if (stackFrames.peek().getParentFrame() == null && lastExpression!=null){
                    VariableStruct ans = stackFrames.peek().getVariable("ans");
                    ans.setValue(lastExpression.toString());
                    printVariable(ans);
                }
            }
        }
    }

    private void runStatement(Node node){
        Node statement = node.getFirstChild();
        if (statement.getNodeName().equals("FunctionDecl")){
            runFunctionDecl(statement);
            lastExpression = null;
        }else if (statement.getNodeName().equals("Assignment")){
            runAssignment(statement);
        }else if (statement.getNodeName().equals("VariableDecl")){
            runVariableDecl(statement);
        }else if (statement.getNodeName().equals("ReadStatement")){
            runReadStatement(statement);
            lastExpression = null;
        }else if (statement.getNodeName().equals("WriteStatement")){
            runWriteStatement(statement);
            lastExpression = null;
        }else if (statement.getNodeName().equals("IfStatement")){
            runIfStatement(statement);
        }else if (statement.getNodeName().equals("WhileStatement")){
            runWhileStatement(statement);
        }else if (statement.getNodeName().equals("HaltStatement")){
            runHaltStatement(statement);
        }else if (statement.getNodeName().equals("Block")){
            runBlock(statement);
        }else if (statement.getNodeName().equals("Expression")){
            runExpression(statement);
        }
    }

    private void runFunctionDecl(Node node){
        Node identifier = node.getChildNodes().item(0);
        Node formalParams = node.getChildNodes().item(1);
        Node type = node.getChildNodes().item(2);
        Node block = node.getChildNodes().item(3);

        FunctionStackFrame func = new FunctionStackFrame(stackFrames.peek(),identifier.getTextContent(),"-1",block);
        pushFormalPramsToFunc(formalParams,func);
        func.setType(type.getTextContent());
        stackFrames.peek().addLocalFunction(func);
    }

    private void runAssignment(Node node){
        Node identifier = node.getChildNodes().item(0);
        Node expression = node.getChildNodes().item(1);

        VariableStruct var = stackFrames.peek().getVariable(identifier.getTextContent());

        var.setValue(runExpression(expression).toString());
    }

    private void runIfStatement(Node node){
        Node expression = node.getChildNodes().item(0);
        Node statement = node.getChildNodes().item(1);

        if (runExpression(expression).toString().equals("true")){
            runStatement(statement);
        }else if (node.getChildNodes().getLength() == 3){
            Node elseStatement = node.getChildNodes().item(2);
            runStatement(elseStatement);
        }
    }

    private void runWhileStatement(Node node){
        Node expression = node.getChildNodes().item(0);
        Node statement = node.getChildNodes().item(1);

        while (runExpression(expression).toString().equals("true")){
            runStatement(statement);
        }

    }

    private void runReadStatement(Node node){
        Node identifier = node.getFirstChild();

        VariableStruct var = stackFrames.peek().getVariable(identifier.getTextContent());

        readVariable(var);
    }

    private void readVariable(VariableStruct var){
        try {
            System.out.print("Val " + var.getIdentifier() + " : " + var.getType() + " = ");
            BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
            String s = br.readLine();
            while (!var.setValue(s)){
                System.out.print("Val " + var.getIdentifier() + " : " + var.getType() + " = ");
                s = br.readLine();
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private void runWriteStatement(Node node){
        Node identifier = node.getFirstChild();

        VariableStruct var = stackFrames.peek().getVariable(identifier.getTextContent());

        //printVariable(var);
        System.out.print(var.getValue().toString());
    }

    private void printVariable(VariableStruct var){
        System.out.println("Val " + var.getIdentifier() + " : " + var.getType() + " = " + var.getValue().toString());
    }

    private void runVariableDecl(Node node){
        Node identifier = node.getChildNodes().item(0);
        Node type = node.getChildNodes().item(1);
        Node expression = node.getChildNodes().item(2);

        VariableStruct var = new VariableStruct(identifier.getTextContent(),type.getTextContent());

        var.setValue(runExpression(expression).toString());

        if (node.getChildNodes().getLength() == 4){
            Node block = node.getChildNodes().item(3);
            runBlock(block, var);
        }else{
            stackFrames.peek().addLocalVariable(var);
        }
    }

    private void runBlock(Node node){
        BlockStackFrame blockStackFrame = new BlockStackFrame(stackFrames.peek());
        stackFrames.push(blockStackFrame);
        runStatements(node.getChildNodes());
        stackFrames.pop();
    }

    private void runBlock(Node node, VariableStruct var){
        BlockStackFrame blockStackFrame = new BlockStackFrame(stackFrames.peek());
        stackFrames.push(blockStackFrame);
        stackFrames.peek().addLocalVariable(var);
        runStatements(node.getChildNodes());
        stackFrames.pop();
    }

    private Object runExpression(Node node){
        lastExpression = expRunner(node);
        return lastExpression;
    }

    private Object expRunner(Node node){
        Object sExp1 = runSimpleExpression(node.getFirstChild());

        if (node.getChildNodes().getLength() != 1){
            String expType = getSimpleExpressionLiteralType(node.getFirstChild());
            String op = node.getChildNodes().item(1).getTextContent();
            Object sExp2 = runSimpleExpression(node.getChildNodes().item(2));
            if (expType.equals("bool")){
                boolean b1 = (new Boolean(sExp1.toString())).booleanValue();
                boolean b2 = (new Boolean(sExp2.toString())).booleanValue();

                if (op.equals("==")){
                    return new Boolean(b1==b2);
                }else {
                    return new Boolean(b1!=b2);
                }
            }else if (expType.equals("real") || expType.equals("int")){
                double d1 = (new Double(sExp1.toString())).doubleValue();
                double d2 = (new Double(sExp2.toString())).doubleValue();

                if (op.equals("==")){
                    return new Boolean(d1==d2);
                }else if (op.equals("!=")){
                    return new Boolean(d1!=d2);
                }else if (op.equals(">")){
                    return new Boolean(d1>d2);
                }else if (op.equals("<")){
                    return new Boolean(d1<d2);
                }else if (op.equals(">=")){
                    return new Boolean(d1>=d2);
                }else if (op.equals("<=")){
                    return new Boolean(d1<=d2);
                }
            }else if (expType.equals("char") || expType.equals("string")){
                String c1 = sExp1.toString();
                String c2 = sExp2.toString();

                if (op.equals("==")){
                    return new Boolean(c1.equals(c2));
                }else if (op.equals("!=")){
                    return new Boolean(!c1.equals(c2));
                }else if (op.equals(">")){
                    return new Boolean(c1.compareTo(c2) > 0);
                }else if (op.equals("<")){
                    return new Boolean(c1.compareTo(c2) < 0);
                }else if (op.equals(">=")){
                    return new Boolean(c1.compareTo(c2) >= 0);
                }else if (op.equals("<=")){
                    return new Boolean(c1.compareTo(c2) <= 0);
                }
            }
        }else{
            return sExp1;
        }
        return null;
    }

    private Object runSimpleExpression(Node node){
        Object sExp1 = runTermExpression(node.getFirstChild());

        if (node.getChildNodes().getLength() == 1){
            return sExp1;
        }else {
            for (int loops = 2 ; loops < node.getChildNodes().getLength();loops +=2){
                String expType = getSimpleExpressionLiteralType(node);
                String op = node.getChildNodes().item(loops - 1).getTextContent();
                Object sExp2 = runTermExpression(node.getChildNodes().item(loops));
                if (expType.equals("bool")){
                    if (sExp1.toString().equals("true") || sExp2.toString().equals("true")){
                        return new Boolean(true);
                    }else {
                        sExp1 = sExp2;
                    }
                }else if (expType.equals("int")){
                    int i1 = (new Integer(sExp1.toString())).intValue();
                    int i2 = (new Integer(sExp2.toString())).intValue();
                    if (op.equals("+")){
                        sExp1 = new Integer(i1 + i2);
                    }else {
                        sExp1 = new Integer(((Integer)sExp1).intValue() - ((Integer)sExp2).intValue());
                    }
                }else if (expType.equals("real")){
                    double d1 = (new Double(sExp1.toString())).doubleValue();
                    double d2 = (new Double(sExp2.toString())).doubleValue();
                    if (op.equals("+")){
                        sExp1 = new Double(d1 + d2);
                    }else {
                        sExp1 = new Double(d1 - d2);
                    }
                }else if (expType.equals("string") || expType.equals("char")){
                    String s1 = sExp1.toString();
                    String s2 = sExp2.toString();
                    sExp1 = s1 + s2;
                }
            }
        }
        return sExp1;
    }

    private Object runTermExpression(Node node){
        Object sExp1 = runFactor(node.getFirstChild());

        if (node.getChildNodes().getLength() == 1){
            return sExp1;
        }else {
            for (int loops = 2 ; loops < node.getChildNodes().getLength();loops +=2){
                String expType = getTermLiteralType(node);
                String op = node.getChildNodes().item(loops - 1).getTextContent();
                Object sExp2 = runFactor(node.getChildNodes().item(loops));
                if (expType.equals("bool")){
                    if (sExp1.toString().equals("false") || sExp1.toString().equals("false")){
                        return new Boolean(false);
                    }else {
                        sExp1 = sExp2;
                    }
                }else if (expType.equals("int")){
                    int i1 = (new Integer(sExp1.toString())).intValue();
                    int i2 = (new Integer(sExp2.toString())).intValue();
                    if (op.equals("*")){
                        sExp1 = new Integer(i1 * i2);
                    }else {
                        sExp1 = new Integer(((Integer)sExp1).intValue() / ((Integer)sExp2).intValue());

                    }
                }else if (expType.equals("real")){
                    double d1 = (new Double(sExp1.toString())).intValue();
                    double d2 = (new Double(sExp2.toString())).intValue();
                    if (op.equals("*")){
                        sExp1 = new Double(d1 * d2);
                    }else {
                        sExp1 = new Double(d1 / d2);
                    }
                }
            }
        }
        return sExp1;
    }

    private Object runFactor(Node node){
        Node child = node.getChildNodes().item(0);

        if (child.getNodeName().equals("Literal")){
            String val = child.getFirstChild().getTextContent();
            if (child.getFirstChild().getNodeName().equals("BooleanLiteral")){
                return new Boolean(val);
            }else if (child.getFirstChild().getNodeName().equals("IntegerLiteral")){
                return new Integer(val);
            }else if (child.getFirstChild().getNodeName().equals("RealLiteral")){
                return new Double(val);
            }else if (child.getFirstChild().getNodeName().equals("CharLiteral")){
                return (val.length() == 0? new Character('\0') : new Character(val.charAt(0)));
            }else if (child.getFirstChild().getNodeName().equals("StringLiteral")){
                return val;
            }
        }else if (child.getNodeName().equals("Identifier")){
            return  stackFrames.peek().getVariable(child.getTextContent()).getValue();
        }else if (child.getNodeName().equals("FunctionCall")){
            return runFunctionCall(child);
        }else if (child.getNodeName().equals("TypeCast")){
            return runTypeCase(child);
        }else if (child.getNodeName().equals("SubExpression")){
            return runSubExpression(child);
        }else if (child.getNodeName().equals("Unary")){
            return runUnary(child);
        }
        return null;
    }

    private Object runFunctionCall(Node node){
        Node identifier = node.getChildNodes().item(0);
        Node actualParams = node.getChildNodes().item(1);

        FunctionStackFrame func = stackFrames.peek().getFunction(identifier.getTextContent(),getSignatureFromActualParams(actualParams)).clone();

        for (int loops = 0 ; loops < actualParams.getChildNodes().getLength(); loops ++){
            Node expression = actualParams.getChildNodes().item(loops);
            func.getPram(loops).setValue(runExpression(expression).toString());
        }

        stackFrames.push(func);
        boolean ok = true;
        lastExpressionType = null;
        if (checkBlock(func.getFuncBlock())) {
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
            if (ok) {
                runBlock(func.getFuncBlock());
                if (func.getType().equals("unit")) lastExpression = null;
            }
            else {
                lastExpression = null;
            }
        }
        stackFrames.pop();

        return lastExpression;
    }

    private Object runTypeCase(Node node){
        Node type = node.getChildNodes().item(0);
        Node expression = node.getChildNodes().item(1);

        Object ex = runExpression(expression);

        if (type.getTextContent().equals("int")){
            if (ex instanceof Character){
                return new Integer(((Character)ex).charValue());
            }
            return new Integer(new Double(ex.toString()).intValue());
        }else if (type.getTextContent().equals("real")){
            if (ex instanceof Character){
                return new Double(((Character)ex).charValue());
            }
            return new Double(ex.toString());
        }else if (type.getTextContent().equals("string")){
            return ex.toString();
        }else if (type.getTextContent().equals("char")){
            if (ex instanceof Character) return ex;
            return new Character((char)(new Integer(ex.toString())).intValue());
        }

        return null;
    }

    private Object runUnary(Node node){
        Node expression = node.getFirstChild();

        Object ex = runExpression(expression);
        String op = ((Element)node).getAttribute("op");

        if (ex instanceof Boolean){
            return new Boolean(!ex.toString().equals("true"));
        }else if (ex instanceof Integer){
            int i = new Integer(ex.toString()).intValue();
            if (op.equals("-")) i = -i;
            return new Integer(i);
        }else if (ex instanceof Double){
            double d = new Double(ex.toString()).doubleValue();
            if (op.equals("-")) d = -d;
            return new Double(d);
        }

        return null;
    }

    private Object runSubExpression(Node node){
        return runExpression(node.getFirstChild());
    }

    private void runHaltStatement(Node node){
        int halt = 0;

        if (node.getChildNodes().getLength() != 0){
            Node child = node.getFirstChild();
            if (child.getNodeName().equals("Identifier")){
                halt = ((Integer)stackFrames.peek().getVariable(child.getTextContent()).getValue()).intValue();
            }else {
                halt = Integer.parseInt(child.getTextContent());
            }
            System.out.println("System Halted with message " + halt);
        }else{
            System.out.println("System Halted");
        }

        System.exit(0);
    }
}
