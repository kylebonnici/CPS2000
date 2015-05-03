package mt.edu.um.cs.cps2000.runtime;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Created by kylebonnici on 03/05/15.
 */
public class Execute extends TypeChecker {
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
            }
        }
    }

    private void runStatement(Node node){
        Node statement = node.getFirstChild();
        if (statement.getNodeName().equals("FunctionDecl")){
            runFunctionDecl(statement);
        }else if (statement.getNodeName().equals("Assignment")){
            runAssignment(statement);
        }else if (statement.getNodeName().equals("VariableDecl")){
            runVariableDecl(statement);
        }else if (statement.getNodeName().equals("ReadStatement")){
            runReadStatement(statement);
        }else if (statement.getNodeName().equals("WriteStatement")){
            runWriteStatement(statement);
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

    private void runWriteStatement(Node node){
        Node identifier = node.getFirstChild();

        VariableStruct var = stackFrames.peek().getVariable(identifier.getNodeValue());

        System.out.println("Val " + var.getIdentifier() + " : " + var.getType() + " = " + var.getValue().toString());
    }

    private void runVariableDecl(Node node){
        Node identifier = node.getChildNodes().item(0);
        Node type = node.getChildNodes().item(1);
        Node expression = node.getChildNodes().item(2);

        VariableStruct var = new VariableStruct(identifier.getTextContent(),type.getTextContent());

        var.setValue(runExpression(expression));

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
        return null;
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
