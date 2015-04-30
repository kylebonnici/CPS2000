package com.um.mt.kyle.parser;

import com.sun.org.apache.xpath.internal.operations.Variable;
import com.um.mt.kyle.lexer.Lexer;
import com.um.mt.kyle.lexer.Token;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.Stack;

import com.um.mt.kyle.lexer.TokenClass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Parser {
    private Lexer lexer = null;
    int tokenIndex = 0;
    private ArrayList<Token> tokens;
    private ArrayList<String> errorLog;
    private Stack<BlockStackFrame> stackFrames;
    private Document doc;
    private int identifierGen = 1;
    private boolean useLineNumbers = true;

    public Parser (String fileToParse){
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(fileToParse));
            lexer = new Lexer(br);
            lexer.lexIt();
            //lexer.displayTokens(lexer.getTokens());
            if (!lexer.hasErrors()){
                parse();
            }
            //lexer.displayTokens(lexer.getTokens());
        } catch (IOException e) {
            System.out.println("Error reading file");
        }
    }

    private void parse (){
        tokens = lexer.getTokens();
        errorLog = new ArrayList<String>();
        stackFrames = new Stack<BlockStackFrame>();
        slx();

        dumpErrors();
    }

    private boolean isDone(){
        return tokenIndex == tokens.size();
    }

    private void slx(){
        DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder icBuilder;

        try {
            icBuilder = icFactory.newDocumentBuilder();
            doc = icBuilder.newDocument();
            Element mainRootElement = doc.createElement("Slx");
            doc.appendChild(mainRootElement);

            //Global Stack Frame for type checking tests
            BlockStackFrame root = new BlockStackFrame(null);
            stackFrames.push(root);

            // append child elements to root element

            Node child = isStatement();

            while (child != null){
                mainRootElement.appendChild(child);
                if (!isDone()) {
                    child = isStatement();
                }else{
                    child = null;
                }
            }

            //--------------------------Pop root from stack-----------------------------------
            stackFrames.pop();
            //---------------------------------------------------------------------------------

            if (!isDone()){
                System.out.println("Error: Not all tokens were used");
            }

            if (stackFrames.size()!=0){
                System.out.println("Error: Frame stack is not empty");
            }


            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("/Users/kylebonnici/IdeaProjects/CPS2000/src/out.xml"));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);

            System.out.println("File saved!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Node isStatement(){
        int currentIndex = tokenIndex;

        Element parent = doc.createElement("Statement");
        Node child = null;
        int lineNumber;

        //FunctionDecl
        if (child == null) {
            child = isFunctionDecl();
        }

        //Assignment
        if (child == null){
            tokenIndex = currentIndex;
            child = isAssignment();
            if (child != null){
                currentIndex = tokenIndex;
                lineNumber = currentTokenLineNumber();
                if (!isSymbol(";",true)){
                    errorLogger(lineNumber, TokenClass.KEY_SYMBOL, ";");
                    tokenIndex = currentIndex;
                }
            }
        }

        //Expression
        if (child == null){
            tokenIndex = currentIndex;
            child = isExpression();
            if (child != null){
                currentIndex = tokenIndex;
                lineNumber = currentTokenLineNumber();
                if (!isSymbol(";",true)){
                    errorLogger(lineNumber, TokenClass.KEY_SYMBOL, ";");
                    tokenIndex = currentIndex;
                }
            }
        }

        //VariableDecl
        if (child == null){
            tokenIndex = currentIndex;
            child = isVariableDecl();
        }

        //ReadStatement
        if (child == null){
            tokenIndex = currentIndex;
            child = isReadStatement();
            if (child != null){
                currentIndex = tokenIndex;
                lineNumber = currentTokenLineNumber();
                if (!isSymbol(";",true)){
                    errorLogger(lineNumber, TokenClass.KEY_SYMBOL, ";");
                    tokenIndex = currentIndex;
                }
            }
        }

        //WriteStatement
        if (child == null){
            tokenIndex = currentIndex;
            child = isWriteStatement();
            if (child != null){
                currentIndex = tokenIndex;
                lineNumber = currentTokenLineNumber();
                if (!isSymbol(";",true)){
                    errorLogger(lineNumber, TokenClass.KEY_SYMBOL, ";");
                    tokenIndex = currentIndex;
                }
            }
        }

        //IfStatement
        if (child == null){
            tokenIndex = currentIndex;
            child = isIfStatement();
        }

        //WhileStatement
        if (child == null){
            tokenIndex = currentIndex;
            child = isWhileStatement();
        }

        //HaltStatement
        if (child == null){
            tokenIndex = currentIndex;
            child = isHaltStatement();
            if (child != null){
                currentIndex = tokenIndex;
                lineNumber = currentTokenLineNumber();
                if (!isSymbol(";",true)){
                    errorLogger(lineNumber, TokenClass.KEY_SYMBOL, ";");
                    tokenIndex = currentIndex;
                }
            }
        }

        //Block
        if (child == null){
            tokenIndex = currentIndex;
            child = isBlock();
        }

        if (child!= null){
            parent.appendChild(child);
            return parent;
        }

        return null;
    }


    private Node isUnary(){
        Element parent = doc.createElement("Unary");

        Token token = tokens.get(tokenIndex++);

        if (token.isTokenClazz(TokenClass.KEY_SYMBOL) || token.isTokenClazz(TokenClass.KEYWORD)) {
            if (token.getLexeme().toString().matches("[+|-|not]")) {

                int lastTokenIndex = tokenIndex;
                int lineNumber = currentTokenLineNumber();

                Node expression = isExpression();

                if (expression == null){
                    expression = doc.createElement("Expression");
                    expression.setTextContent("ERROR");
                    errorLogger(lineNumber, TokenClass.EXPRESSION, "Expression");
                    tokenIndex = lastTokenIndex;
                }

                parent.appendChild(expression);

                return parent;
            }
        }

        return null;
    }

    private Node isSubExpression(){
        Element parent = doc.createElement("SubExpression");

        if (isSymbol("(",false)){
            Node expression = isExpression();
            if (expression != null){
                parent.appendChild(expression);

                int lastTokenIndex = tokenIndex;
                int lineNumber = currentTokenLineNumber();

                if (!isSymbol(")",true)){
                    tokenIndex = lastTokenIndex;
                    errorLogger(lineNumber,TokenClass.KEY_SYMBOL,")");
                }

                return parent;
            }
        }

        return null;
    }

    private Node isTypeCast(){
        Element parent = doc.createElement("TypeCast");

        if (isSymbol("(",false)){
            Node type = isType();
            if (type != null){

                int lastTokenIndex = tokenIndex;
                int lineNumber = currentTokenLineNumber();

                if (!isSymbol(")",true)){
                    tokenIndex = lastTokenIndex;
                    errorLogger(lineNumber,TokenClass.KEY_SYMBOL,")");
                }

                lastTokenIndex = tokenIndex;
                lineNumber = currentTokenLineNumber();

                Node expression = isExpression();

                if (expression == null){
                    expression = doc.createElement("Expression");
                    expression.setTextContent("ERROR");
                    errorLogger(lineNumber, TokenClass.EXPRESSION, "Expression");
                    tokenIndex = lastTokenIndex;
                }

                parent.appendChild(expression);

                return parent;
            }
        }

        return null;
    }

    private Node isActualParams(){
        Element parent = doc.createElement("ActualParams");

        Node expression = isExpression();

        if (expression !=null){
            parent.appendChild(expression);

            int lastTokenIndex = tokenIndex;
            int lineNumber = currentTokenLineNumber();

            while (isSymbol(",",true)){
                lastTokenIndex = tokenIndex;
                lineNumber = currentTokenLineNumber();

                expression = isExpression();

                if (expression == null){
                    expression = doc.createElement("Expression");
                    expression.setTextContent("ERROR");
                    errorLogger(lineNumber, TokenClass.EXPRESSION, "Expression");
                    tokenIndex = lastTokenIndex;
                }

                lastTokenIndex = tokenIndex;

                parent.appendChild(expression);
            }

            tokenIndex = lastTokenIndex;

            return parent;

        }

        return null;
    }


    private Node isFunctionCall(){
        Element parent = doc.createElement("FunctionCall");

        Node identifier = isIdentifier();

        if (identifier != null){
            if (isSymbol("(",false)) {
                parent.appendChild(identifier);
                //wasFunctionDeclared(identifier.getTextContent(),"",currentTokenLineNumber());

                int lastTokenIndex = tokenIndex;

                Node actualParams = isActualParams();

                if (actualParams != null){
                    lastTokenIndex = tokenIndex;
                    parent.appendChild(actualParams);
                }

                tokenIndex = lastTokenIndex;

                int lineNumber = currentTokenLineNumber();

                if (!isSymbol(")",true)){
                    tokenIndex = lastTokenIndex;
                    errorLogger(lineNumber,TokenClass.KEY_SYMBOL,")");
                }

                return  parent;
            }
        }

        return null;
    }



    private Node isLiteral() {
        Element parent = doc.createElement("Literal");

        if (isA(TokenClass.LITERAL)) {
            tokenIndex--;
            Token token = tokens.get(tokenIndex).getDeepestToken();

            switch (token.getClazz()) {
                case BOOLEAN_LITERAL:
                    parent.appendChild(isBooleanLiteral());
                    break;
                case STRING_LITERAL:
                    parent.appendChild(isStringLiteral());
                    break;
                case REAL_LITERAL:
                    parent.appendChild(isRealLiteral());
                    break;
                case INTEGER_LITERAL:
                    parent.appendChild(isIntegerLiteral());
                    break;
                case UNIT_LITERAL:
                    parent.appendChild(isUnitLiteral());
                    break;
                case CHAR_LITERAL:
                    parent.appendChild(isCharLiteral());
                    break;
            }

            return parent;
        }

        return null;
    }


    private Node isFactor(){
        Element parent = doc.createElement("Factor");

        int lastTokenIndex = tokenIndex;


        Node child = isLiteral();

        if (child == null){
            tokenIndex = lastTokenIndex;
            child = isFunctionCall();
        }

        if (child == null){
            tokenIndex = lastTokenIndex;
            child = isIdentifier();
            int lineNumber = currentTokenLineNumber();
            if (child != null){
                wasVariableDeclared(child.getTextContent(), lineNumber);
            }
        }

        if (child == null){
            tokenIndex = lastTokenIndex;
            child = isTypeCast();
        }

        if (child == null){
            tokenIndex = lastTokenIndex;
            child = isSubExpression();
        }

        if (child == null){
            tokenIndex = lastTokenIndex;
            child = isUnary();
        }


        if (child == null){
            return null;
        }else {
            parent.appendChild(child);
        }


        return parent;
    }

    private Node isTerm(){
        Element parent = doc.createElement("Term");

        Node startFactor = isFactor();

        if (startFactor != null){
            parent.appendChild(startFactor);
            while (isA(TokenClass.MULTIPLICATIVE_OP)) {
                tokenIndex--;
                parent.appendChild(isMultiplicativeOp());

                int lastTokenIndex = tokenIndex;
                int lineNumber = currentTokenLineNumber();

                Node factor = isFactor();

                if (factor == null){
                    factor = doc.createElement("Factor");
                    factor.setTextContent("ERROR");
                    errorLogger(lineNumber, TokenClass.FACTOR, "Factor");
                    tokenIndex = lastTokenIndex;
                }

                parent.appendChild(factor);
            }

            tokenIndex--;

            return parent;
        }

        return null;
    }

    private Node isSimpleExpression(){
        Element parent = doc.createElement("SimpleExpression");

        Node startTerm = isTerm();

        if (startTerm != null){
            parent.appendChild(startTerm);

            while (isA(TokenClass.ADDITIVE_OP)) {
                tokenIndex--;
                parent.appendChild(isAdditiveOp());

                int lastTokenIndex = tokenIndex;
                int lineNumber = currentTokenLineNumber();

                Node term = isTerm();

                if (term == null){
                    term = doc.createElement("Term");
                    term.setTextContent("ERROR");
                    errorLogger(lineNumber, TokenClass.TERM, "Term");
                    tokenIndex = lastTokenIndex;
                }

                parent.appendChild(term);
            }

            tokenIndex--;

            return parent;
        }

        return null;
    }

    private Node isFormalParam(){
        Element parent = doc.createElement("FormalParam");

        Node identifier = isIdentifier();
        if (identifier !=null){
            parent.appendChild(identifier);
            int lastTokenIndex = tokenIndex;
            int lineNumber = currentTokenLineNumber();

            if (!isSymbol(":",true)){
                tokenIndex = lastTokenIndex;
                errorLogger(lineNumber,TokenClass.KEY_SYMBOL,":");
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            Node type = isType();

            if (type == null){
                type = doc.createElement("Type");
                type.setTextContent("ERROR");
                errorLogger(lineNumber, TokenClass.TYPE, "Type");
                tokenIndex = lastTokenIndex;
            }

            VariableStruct var =  new VariableStruct(identifier.getTextContent(),type.getTextContent());
            BlockStackFrame block = stackFrames.peek();
            if (block instanceof FunctionStackFrame){
                FunctionStackFrame func = (FunctionStackFrame)block;
                if (!func.addPram(var)){
                    errorLogger(lineNumber, TokenClass.TYPE, "Duplicate identifier name '" + var.getIdentifier() +
                            "' in function '" + func.getIdentifier(),false);
                }
            }else{
                errorLogger(lineNumber, TokenClass.TYPE, "Prams should be in a function");
            }

            parent.appendChild(type);

            return parent;


        }

        return null;
    }

    private Node isFormalParams(){
        Element parent = doc.createElement("FormalParams");

        Node formalParam = isFormalParam();

        if (formalParam !=null){
            parent.appendChild(formalParam);

            int lastTokenIndex = tokenIndex;
            int lineNumber = currentTokenLineNumber();

            while (isSymbol(",",true)){
                lastTokenIndex = tokenIndex;
                lineNumber = currentTokenLineNumber();

                formalParam = isFormalParam();

                if (formalParam == null){
                    formalParam = doc.createElement("FormalParam");
                    formalParam.setTextContent("ERROR");
                    errorLogger(lineNumber, TokenClass.FORMAL_PARAM, "Formal Param");
                    tokenIndex = lastTokenIndex;
                }

                lastTokenIndex = tokenIndex;

                parent.appendChild(formalParam);
            }

            tokenIndex = lastTokenIndex;

            return parent;

        }

        return null;
    }

    private Node isFunctionDecl() {
        Element parent = doc.createElement("FunctionDecl");


        if (isKeyword("function",false)) {
            int lastTokenIndex = tokenIndex;
            int lineNumber = currentTokenLineNumber();

            Node identifier = isIdentifier();

            boolean okIdentifier = true;

            if (identifier == null) {
                identifier = doc.createElement("Identifier");
                identifier.setTextContent("ERROR");
                errorLogger(lineNumber, TokenClass.IDENTIFIER, "Identifier");
                tokenIndex = lastTokenIndex;
                okIdentifier = false;
            }

            //-----------------------Create stack Frame------------------------------
            String funcIdentifier = identifier.getTextContent() + (okIdentifier? "" : identifierGen++);
            BlockStackFrame parentBlock = stackFrames.peek();
            FunctionStackFrame otherFuncSameIdentifier = parentBlock.getFunction(funcIdentifier);
            FunctionStackFrame funcStackFrame = new FunctionStackFrame(stackFrames.peek(),funcIdentifier, lineNumber);
            stackFrames.push(funcStackFrame);
            //-----------------------------------------------------------------------

            parent.appendChild(identifier);

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            if (!isSymbol("(",true)){
                tokenIndex = lastTokenIndex;
                errorLogger(lineNumber,TokenClass.KEY_SYMBOL,"(");
            }

            lastTokenIndex = tokenIndex;

            Node formalParams = isFormalParams();

            //--------------------------check for conflict name and prams-----------------------
            if (otherFuncSameIdentifier != null){
                if (otherFuncSameIdentifier.getFunctionSignature().equals(funcStackFrame.getFunctionSignature())){
                    errorLogger(otherFuncSameIdentifier.getLineNumber(),TokenClass.TYPE,"Function " + (useLineNumbers?"at line " + lineNumber: " ") + "was " + otherFuncSameIdentifier.getIdentifier() +
                            otherFuncSameIdentifier.getFunctionSignature() + " is already defined",false);
                }else{
                   parentBlock.addLocalFunction(funcStackFrame);
                }
            }else{
                parentBlock.addLocalFunction(funcStackFrame);
            }
            //----------------------------------------------------------------------------------

            if (formalParams != null){
                lastTokenIndex = tokenIndex;
                parent.appendChild(formalParams);
            }

            tokenIndex = lastTokenIndex;

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            if (!isSymbol(")",true)){
                tokenIndex = lastTokenIndex;
                errorLogger(lineNumber,TokenClass.KEY_SYMBOL,")");
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            if (!isSymbol(":",true)){
                tokenIndex = lastTokenIndex;
                errorLogger(lineNumber,TokenClass.KEY_SYMBOL,":");
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            Node type = isType();

            if (type == null){
                type = doc.createElement("Type");
                type.setTextContent("ERROR");
                errorLogger(lineNumber, TokenClass.TYPE, "Type");
                tokenIndex = lastTokenIndex;
            }

            //-----------------------Add type to func stack Frame------------------------------
            funcStackFrame.setType(type.getTextContent());
            //---------------------------------------------------------------------------------

            parent.appendChild(type);

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            Node block = isBlock();

            if (block == null){
                block = doc.createElement("Block");
                block.setTextContent("ERROR");
                errorLogger(lineNumber, TokenClass.BLOCK, "Block");
                tokenIndex = lastTokenIndex;
            }

            parent.appendChild(block);

            //--------------------------Pop function from stack-----------------------------------
            stackFrames.pop();
            //------------------------------------------------------------------------------------

            return parent;

        }

        return null;
    }

    private void wasVariableDeclared(String identifier, int lineNumber){
        //--------------------check if variable was declared------------------------------
        BlockStackFrame blockFrame = stackFrames.peek();
        VariableStruct var = null;
        var = blockFrame.getVariable(identifier);
        if (var == null){
            errorLogger(lineNumber, TokenClass.TYPE, "Variable '" + identifier + "' was not declared",false);
        }
        //--------------------------------------------------------------------------------
    }

    private void wasFunctionDeclared(String identifier, String signature, int lineNumber){
        //--------------------check if variable was declared------------------------------
        BlockStackFrame blockFrame = stackFrames.peek();
        FunctionStackFrame func = null;
        func = blockFrame.getFunction(identifier);
        if (func == null || !func.getFunctionSignature().equals(signature)){
            errorLogger(lineNumber, TokenClass.TYPE, "Function '" + identifier + signature + "' was not defined",false);
        }
        //--------------------------------------------------------------------------------
    }


    private Node isAssignment(){
        Element parent = doc.createElement("Assignment");

        if (isKeyword("set",false)) {
            int lastTokenIndex = tokenIndex;
            int lineNumber = currentTokenLineNumber();

            Node identifier = isIdentifier();

            if (identifier == null){
                identifier = doc.createElement("Identifier");
                identifier.setTextContent("ERROR");
                errorLogger(lineNumber, TokenClass.IDENTIFIER, "Identifier");
                tokenIndex = lastTokenIndex;
            }else {
                wasVariableDeclared(identifier.getTextContent(), lineNumber);
            }

            parent.appendChild(identifier);

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            if (!isSymbol("<-",true)){
                tokenIndex = lastTokenIndex;
                errorLogger(lineNumber,TokenClass.KEY_SYMBOL,"<-");
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            Node expression = isExpression();

            if (expression == null){
                expression = doc.createElement("Expression");
                expression.setTextContent("ERROR");
                errorLogger(lineNumber, TokenClass.EXPRESSION, "Expression");
                tokenIndex = lastTokenIndex;
            }

            parent.appendChild(expression);

            return parent;
        }

        return null;
    }

    private Node isExpression(){
        Element parent = doc.createElement("Expression");

        Node startSimpleExpression = isSimpleExpression();

        if (startSimpleExpression != null){
            parent.appendChild(startSimpleExpression);

            while (isA(TokenClass.RELATIONAL_OP)) {
                tokenIndex--;
                parent.appendChild(isRelationalOp());

                int lastTokenIndex = tokenIndex;
                int lineNumber = currentTokenLineNumber();

                Node simpleExpression = isSimpleExpression();

                if (simpleExpression == null){
                    simpleExpression = doc.createElement("SimpleExpression");
                    simpleExpression.setTextContent("ERROR");
                    errorLogger(lineNumber, TokenClass.SIMPLE_EXPRESSION, "Simple Expression");
                    tokenIndex = lastTokenIndex;
                }

                parent.appendChild(simpleExpression);
            }

            tokenIndex--;

            return parent;
        }

        return null;
    }

    private Node isVariableDecl(){
        Element parent = doc.createElement("VariableDecl");

        if (isKeyword("let",false)) {
            int lastTokenIndex = tokenIndex;
            int lineNumber = currentTokenLineNumber();

            Node identifier = isIdentifier();

            boolean okIdentifier = true;

            if (identifier == null){
                identifier = doc.createElement("Identifier");
                identifier.setTextContent("ERROR");
                errorLogger(lineNumber, TokenClass.IDENTIFIER, "Identifier");
                tokenIndex = lastTokenIndex;
                okIdentifier = false;
            }

            parent.appendChild(identifier);

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            if (!isSymbol(":",true)){
                tokenIndex = lastTokenIndex;
                errorLogger(lineNumber,TokenClass.KEY_SYMBOL,":");
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            Node type = isType();

            if (type == null){
                type = doc.createElement("Type");
                type.setTextContent("ERROR");
                errorLogger(lineNumber, TokenClass.TYPE, "Type");
                tokenIndex = lastTokenIndex;
            }

            //----------------------Add identifier to appropriate frame---------------------
            VariableStruct var = new VariableStruct(identifier.getTextContent() + (okIdentifier? "" : identifierGen++), type.getTextContent());
            BlockStackFrame blockFrame = stackFrames.peek();
            if (!blockFrame.addLocalVariable(var)){
                errorLogger(lineNumber,TokenClass.TYPE,"Variable '" + var.getIdentifier() + "' already declared",false);
            }
            //------------------------------------------------------------------------------


            parent.appendChild(type);

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            if (!isSymbol("=",true)){
                tokenIndex = lastTokenIndex;
                errorLogger(lineNumber,TokenClass.KEY_SYMBOL,"=");
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            Node expression = isExpression();

            if (expression == null){
                expression = doc.createElement("Expression");
                expression.setTextContent("ERROR");
                errorLogger(lineNumber, TokenClass.EXPRESSION, "Expression");
                tokenIndex = lastTokenIndex;
            }

            parent.appendChild(expression);

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            if (!isKeyword("in",true)){
                tokenIndex = lastTokenIndex;
                if (!isSymbol(";",true)){
                    errorLogger(lineNumber,TokenClass.KEY_SYMBOL,";");
                    tokenIndex = lastTokenIndex;
                }
            }else{
                lastTokenIndex = tokenIndex;

                Node block = isBlock();

                if (block == null){
                    block = doc.createElement("Block");
                    block.setTextContent("ERROR");
                    tokenIndex = lastTokenIndex;
                }

                parent.appendChild(block);

            }

            return parent;
        }

        return null;
    }

    private Node isWriteStatement(){
        Element parent = doc.createElement("WriteStatement");

        if (isKeyword("write",false)) {
            int lastTokenIndex = tokenIndex;
            int lineNumber = currentTokenLineNumber();

            Node identifier = isIdentifier();

            if (identifier == null){
                identifier = doc.createElement("Identifier");
                identifier.setTextContent("ERROR");
                errorLogger(lineNumber, TokenClass.IDENTIFIER, "Identifier");
                tokenIndex = lastTokenIndex;
            }else {
                wasVariableDeclared(identifier.getTextContent(), lineNumber);
            }

            parent.appendChild(identifier);

            return  parent;
        }

        return null;
    }

    private Node isReadStatement(){
        Element parent = doc.createElement("ReadStatement");

        if (isKeyword("read",false)) {
            int lastTokenIndex = tokenIndex;
            int lineNumber = currentTokenLineNumber();

            Node identifier = isIdentifier();

            if (identifier == null){
                identifier = doc.createElement("Identifier");
                identifier.setTextContent("ERROR");
                errorLogger(lineNumber, TokenClass.IDENTIFIER, "Identifier");
                tokenIndex = lastTokenIndex;
            }else {
                wasVariableDeclared(identifier.getTextContent(), lineNumber);
            }

            parent.appendChild(identifier);

            return  parent;
        }

        return null;
    }

    private Node isIfStatement(){
        Element parent = doc.createElement("IfStatement");

        Node expression = null;
        Node statement = null;

        if (isKeyword("if",false)){
            int lastTokenIndex = tokenIndex;
            int lineNumber = currentTokenLineNumber();

            if (!isSymbol("(",true)){
                tokenIndex = lastTokenIndex;
                errorLogger(lineNumber,TokenClass.KEY_SYMBOL,"(");
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            expression = isExpression();

            if (expression == null){
                tokenIndex = lastTokenIndex;
                expression = doc.createElement("expression");
                expression.setTextContent("ERROR");
                errorLogger(lineNumber,TokenClass.EXPRESSION,"Expression");
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            if (!isSymbol(")",true)){
                tokenIndex = lastTokenIndex;
                errorLogger(lineNumber,TokenClass.KEY_SYMBOL,")");
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            statement = isStatement();

            if (statement == null){
                tokenIndex = lastTokenIndex;
                expression = doc.createElement("Statement");
                expression.setTextContent("ERROR");
                errorLogger(lineNumber,TokenClass.STATEMENT,"Statement");
            }

            parent.appendChild(expression);
            parent.appendChild(statement);

            lastTokenIndex = tokenIndex;

            if (isKeyword("else",false)){
                lastTokenIndex = tokenIndex;
                lineNumber = currentTokenLineNumber();

                statement = isStatement();

                if (statement == null){
                    tokenIndex = lastTokenIndex;
                    expression = doc.createElement("Statement");
                    expression.setTextContent("ERROR");
                    errorLogger(lineNumber,TokenClass.STATEMENT,"Statement");
                }

                parent.appendChild(statement);
            }else {
                tokenIndex = lastTokenIndex;
            }

            return parent;
        }

        return null;
    }

    private Node isWhileStatement(){
        Element parent = doc.createElement("WhileStatement");

        Node expression = null;
        Node statement = null;

        if (isKeyword("while",false)){
            int lastTokenIndex = tokenIndex;
            int lineNumber = currentTokenLineNumber();

            if (!isSymbol("(",true)){
                tokenIndex = lastTokenIndex;
                errorLogger(lineNumber,TokenClass.KEY_SYMBOL,"(");
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            expression = isExpression();

            if (expression == null){
                tokenIndex = lastTokenIndex;
                expression = doc.createElement("expression");
                expression.setTextContent("ERROR");
                errorLogger(lineNumber,TokenClass.EXPRESSION,"Expression");
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            if (!isSymbol(")",true)){
                tokenIndex = lastTokenIndex;
                errorLogger(lineNumber,TokenClass.KEY_SYMBOL,")");
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            statement = isStatement();

            if (statement == null){
                tokenIndex = lastTokenIndex;
                expression = doc.createElement("Statement");
                expression.setTextContent("ERROR");
                errorLogger(lineNumber,TokenClass.STATEMENT,"Statement");
            }

            parent.appendChild(expression);
            parent.appendChild(statement);

            return parent;
        }

        return null;
    }

    private Node isHaltStatement(){
        Element parent = doc.createElement("HaltStatement");

        if (isKeyword("halt", false)) {
            int lastTokenIndex = tokenIndex;
            int lineNumber;

            Node integerLiteral = isIntegerLiteral();

            if (integerLiteral == null){
                tokenIndex = lastTokenIndex;

                lineNumber = currentTokenLineNumber();
                Node identifier = isIdentifier();

                if (identifier == null){
                    identifier = doc.createElement("Identifier");
                    identifier.setTextContent("ERROR");
                    errorLogger(lineNumber, TokenClass.IDENTIFIER, "Identifier");
                    tokenIndex = lastTokenIndex;
                }else {
                    wasVariableDeclared(identifier.getTextContent(), lineNumber);
                }
                parent.appendChild(identifier);
            }else {
                parent.appendChild(integerLiteral);
            }

            return parent;
        }

        return null;
    }

    private Node isBlock(){
        Element parent = doc.createElement("Block");

        int currentIndex = tokenIndex;

        Node child = null;
        if (isSymbol("{", false)) {
            //----------------Add block frame---------------------
            BlockStackFrame block = new BlockStackFrame(stackFrames.peek());
            stackFrames.push(block);
            //----------------------------------------------------

            child = isStatement();
            while (child != null) {
                currentIndex = tokenIndex;
                parent.appendChild(child);
                child = isStatement();
            }
            tokenIndex = currentIndex;
            int lineNumber = currentTokenLineNumber();
            if (!isSymbol("}",true)){
                errorLogger(lineNumber,TokenClass.KEY_SYMBOL,"}");
                tokenIndex = currentIndex;
            }

            //------------------------Pop Block frame---------------
            stackFrames.pop();
            //------------------------------------------------------

            return  parent;
        }


        return null;
    }

    private boolean isSymbol(String symbol, boolean persist){
        if (!isAWith(TokenClass.KEY_SYMBOL, symbol)){
            if (!isDone() && persist){
                if (!isAWith(TokenClass.KEY_SYMBOL, symbol)) {
                    return false;
                }else {
                    return true;
                }
            }else{
                return false;
            }
        }

        return true;
    }

    private boolean isKeyword(String keyword, boolean persist){
        if (!isAWith(TokenClass.KEYWORD, keyword)){
            if (!isDone() && persist){
                if (!isAWith(TokenClass.KEYWORD, keyword)) {
                    return false;
                }else {
                    return true;
                }
            }else{
                return false;
            }
        }

        return true;
    }

    private Node isIntegerLiteral(){
        Element parent = doc.createElement("IntegerLiteral");

        if (!isA(TokenClass.INTEGER_LITERAL)) {
            return null;
        }else{
            parent.setTextContent(tokens.get(tokenIndex - 1).getDeepestToken().getLexeme().toString());
        }

        return parent;
    }

    private Node isBooleanLiteral(){
        Element parent = doc.createElement("BooleanLiteral");

        if (!isA(TokenClass.BOOLEAN_LITERAL)) {
            return null;
        }else{
            parent.setTextContent(tokens.get(tokenIndex - 1).getDeepestToken().getLexeme().toString());
        }

        return parent;
    }

    private Node isRealLiteral(){
        Element parent = doc.createElement("RealLiteral");

        if (!isA(TokenClass.REAL_LITERAL)) {
            return null;
        }else{
            parent.setTextContent(tokens.get(tokenIndex - 1).getDeepestToken().getLexeme().toString());
        }

        return parent;
    }

    private Node isCharLiteral(){
        Element parent = doc.createElement("CharLiteral");

        if (!isA(TokenClass.CHAR_LITERAL)) {
            return null;
        }else{
            parent.setTextContent(tokens.get(tokenIndex - 1).getDeepestToken().getLexeme().toString());
        }

        return parent;
    }

    private Node isStringLiteral(){
        Element parent = doc.createElement("StringLiteral");

        if (!isA(TokenClass.STRING_LITERAL)) {
            return null;
        }else{
            parent.setTextContent(tokens.get(tokenIndex - 1).getDeepestToken().getLexeme().toString());
        }

        return parent;
    }

    private Node isUnitLiteral(){
        Element parent = doc.createElement("UnitLiteral");

        if (!isA(TokenClass.UNIT_LITERAL)) {
            return null;
        }else{
            parent.setTextContent(tokens.get(tokenIndex - 1).getDeepestToken().getLexeme().toString());
        }

        return parent;
    }

    private Node isRelationalOp(){
        Element parent = doc.createElement("RelationalOp");

        if (!isA(TokenClass.RELATIONAL_OP)) {
            return null;
        }else{
            parent.setTextContent(tokens.get(tokenIndex - 1).getLexeme().toString());
        }

        return parent;
    }

    private Node isMultiplicativeOp(){
        Element parent = doc.createElement("MultiplicativeOp");

        if (!isA(TokenClass.MULTIPLICATIVE_OP)) {
            return null;
        }else{
            parent.setTextContent(tokens.get(tokenIndex - 1).getLexeme().toString());
        }

        return parent;
    }

    private Node isAdditiveOp(){
        Element parent = doc.createElement("AdditiveOp");

        if (!isA(TokenClass.ADDITIVE_OP)) {
            return null;
        } else{
            parent.setTextContent(tokens.get(tokenIndex - 1).getLexeme().toString());
        }

        return parent;
    }


    private Node isIdentifier(){
        Element parent = doc.createElement("Identifier");

        int lineNumber = currentTokenLineNumber();

        if (!isA(TokenClass.IDENTIFIER)) {
            return null;
        }else{
            parent.setTextContent(tokens.get(tokenIndex - 1).getLexeme().toString());
        }

        return parent;
    }

    private Node isType(){
        Element parent = doc.createElement("Type");

        if (!isA(TokenClass.TYPE)) {
            return null;
        }else {
            parent.setTextContent(tokens.get(tokenIndex - 1).getLexeme().toString());
        }

        return parent;
    }

    private boolean isA(TokenClass clazz){
        if (isDone()) return false;
        return  tokens.get(tokenIndex++).isTokenClazz(clazz);
    }

    private boolean isAWith(TokenClass clazz, String str){
        return  tokens.get(tokenIndex).getLexeme().equals(str) & isA(clazz);
    }

    private int currentTokenLineNumber(){
        if (tokenIndex == 0) return 1;

        return tokens.get(tokenIndex-1).getLineNumber();
    }

    private void errorLogger(int lineNumber, TokenClass clazz, String expected){
        errorLogger(lineNumber, clazz, expected, true);
    }

    private void errorLogger(int lineNumber, TokenClass clazz, String expected,boolean exp){
        if (useLineNumbers) {
            errorLog.add("Error: " + (exp ? "Expected: " : "") + expected + "  at line " + lineNumber);
        }else {
            errorLog.add("Error: " + (exp ? "Expected: " : "") + expected);
        }
    }

    private void dumpErrors(){
        if (errorLog.size() > 0){
            System.out.println("Error log:");
        }
        for (int loops = 0 ; loops < errorLog.size(); loops ++){
            System.out.println(errorLog.get(loops));
        }
    }

    private TokenClass getExpressionLiteralType(Node node){
        TokenClass clazz = TokenClass.BOOLEAN_LITERAL;



        return clazz;
    }

    private TokenClass getSimpleExpressionLiteralType(Node node){
        TokenClass clazz = TokenClass.BOOLEAN_LITERAL;



        return clazz;
    }

    private TokenClass getTermLiteralType(Node node){
        TokenClass clazz = TokenClass.BOOLEAN_LITERAL;



        return clazz;
    }

    private TokenClass getFactorLiteralType(Node node){
        TokenClass clazz = TokenClass.BOOLEAN_LITERAL;



        return clazz;
    }

    private TokenClass getLiteralType(Node node){
        TokenClass clazz = TokenClass.BOOLEAN_LITERAL;



        return clazz;
    }

    private TokenClass getIdentifierLiteralType(Node node){
        TokenClass clazz = TokenClass.BOOLEAN_LITERAL;



        return clazz;
    }

    private TokenClass getFunctionCallLiteralType(Node node){
        TokenClass clazz = TokenClass.BOOLEAN_LITERAL;



        return clazz;
    }

    private TokenClass getTypeCastLiteralType(Node node){
        TokenClass clazz = TokenClass.BOOLEAN_LITERAL;



        return clazz;
    }

    private TokenClass getSubExpressionLiteralType(Node node){
        TokenClass clazz = TokenClass.BOOLEAN_LITERAL;



        return clazz;
    }

    private TokenClass getUnaryLiteralType(Node node){
        TokenClass clazz = TokenClass.BOOLEAN_LITERAL;



        return clazz;
    }


    private boolean isValidType(String typeToBe, TokenClass isType){
        switch(isType){
            case BOOLEAN_LITERAL:
                return  (typeToBe.equals("bool"));
            case INTEGER_LITERAL:
                return  (typeToBe.equals("int"));
            case STRING_LITERAL:
                return  (typeToBe.endsWith("string") || typeToBe.equals("char"));
            case CHAR_LITERAL:
                return  (typeToBe.equals("char"));
            case REAL_LITERAL:
                return  (typeToBe.equals("real") || typeToBe.equals("int"));
            case UNIT_LITERAL:
                return  (typeToBe.equals("unit"));
        }

        return false;
    }

    private String typeTokenToString(TokenClass clazz){
        switch(clazz){
            case BOOLEAN_LITERAL:
                return  "bool";
            case INTEGER_LITERAL:
                return  "int";
            case STRING_LITERAL:
                return  "string";
            case CHAR_LITERAL:
                return  "char";
            case REAL_LITERAL:
                return  "real";
            case UNIT_LITERAL:
                return  "unit";
        }

        return "";
    }
}
