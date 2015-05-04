package mt.edu.um.cs.cps2000.parseandlexer;

import java_cup.runtime.Symbol;
import java.io.*;
import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Parser {
    int tokenIndex = 0;
    private ArrayList<Symbol> tokens;
    private ArrayList<String> errorLog;
    private Document doc;
    public boolean useLineNumbers = false;
    private boolean done = false;
    private boolean error = false;

    public boolean getError(){
        return error;
    }

    public void setDoc(Document doc){
        this.doc = doc;
    }

    public Parser(java_cup.runtime.Scanner s) {
        BufferedReader br = null;
        tokens = new ArrayList<Symbol>();

        try {
            Symbol token = s.next_token();

            while (token.sym != JParserSym.EOF) {
                tokens.add(token);
                token.toString();
                token = s.next_token();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void parse() {
        errorLog = new ArrayList<String>();
        slx();

        dumpErrors();
    }

    private boolean isDone() {
        done = tokenIndex >= tokens.size();
        return done;
    }

    private void slx() {
        try {
            Element mainRootElement = doc.createElement("Slx");
            doc.appendChild(mainRootElement);

            Node child = isStatement();

            while (child != null) {
                mainRootElement.appendChild(child);
                if (!isDone()) {
                    child = isStatement();
                } else {
                    child = null;
                }
            }

            if (!isDone()) {
                System.out.println("Error: Not all tokens were used");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private Node isStatement() {
        int currentIndex = tokenIndex;

        Element parent = doc.createElement("Statement");
        Node child = null;
        int lineNumber;

        //FunctionDecl
        if (child == null) {
            child = isFunctionDecl();
        }

        //Assignment
        if (child == null) {
            tokenIndex = currentIndex;
            child = isAssignment();
            if (child != null) {
                currentIndex = tokenIndex;
                lineNumber = currentTokenLineNumber();
                if (!isSymbol(";", true)) {
                    errorLogger(lineNumber, ";");
                    tokenIndex = currentIndex;
                }
            }
        }

        //Expression
        if (child == null) {
            tokenIndex = currentIndex;
            child = isExpression();
            if (child != null) {
                currentIndex = tokenIndex;
                lineNumber = currentTokenLineNumber();
                if (!isSymbol(";", true)) {
                    errorLogger(lineNumber, ";");
                    tokenIndex = currentIndex;
                }
            }
        }

        //VariableDecl
        if (child == null) {
            tokenIndex = currentIndex;
            child = isVariableDecl();
        }

        //ReadStatement
        if (child == null) {
            tokenIndex = currentIndex;
            child = isReadStatement();
            if (child != null) {
                currentIndex = tokenIndex;
                lineNumber = currentTokenLineNumber();
                if (!isSymbol(";", true)) {
                    errorLogger(lineNumber, ";");
                    tokenIndex = currentIndex;
                }
            }
        }

        //WriteStatement
        if (child == null) {
            tokenIndex = currentIndex;
            child = isWriteStatement();
            if (child != null) {
                currentIndex = tokenIndex;
                lineNumber = currentTokenLineNumber();
                if (!isSymbol(";", true)) {
                    errorLogger(lineNumber, ";");
                    tokenIndex = currentIndex;
                }
            }
        }

        //IfStatement
        if (child == null) {
            tokenIndex = currentIndex;
            child = isIfStatement();
        }

        //WhileStatement
        if (child == null) {
            tokenIndex = currentIndex;
            child = isWhileStatement();
        }

        //HaltStatement
        if (child == null) {
            tokenIndex = currentIndex;
            child = isHaltStatement();
            if (child != null) {
                currentIndex = tokenIndex;
                lineNumber = currentTokenLineNumber();
                if (!isSymbol(";", true)) {
                    errorLogger(lineNumber, ";");
                    tokenIndex = currentIndex;
                }
            }
        }

        //Block
        if (child == null) {
            tokenIndex = currentIndex;
            child = isBlock();
        }

        if (child != null) {
            parent.appendChild(child);
            return parent;
        }

        return null;
    }


    private Node isUnary() {
        Element parent = doc.createElement("Unary");

        if (tokenIndex+1 >= tokens.size()) return null;

        Symbol token = tokens.get(tokenIndex++);

        String value = (token.sym == JParserSym.PLUS? "+": (token.sym == JParserSym.MINUS? "-" : (token.sym == JParserSym.NOT? "not" : ""))  );

        if (!value.equals("")) {
            parent.setTextContent(token.value.toString());

                int lastTokenIndex = tokenIndex;
                int lineNumber = currentTokenLineNumber();

                Node expression = isExpression();

                if (expression == null) {
                    expression = doc.createElement("Expression");
                    expression.setTextContent("ERROR");
                    errorLogger(lineNumber, "Expression");
                    tokenIndex = lastTokenIndex;
                }

                parent.appendChild(expression);

                return parent;
        }

        return null;
    }

    private Node isSubExpression() {
        Element parent = doc.createElement("SubExpression");

        if (isSymbol("(", false)) {
            Node expression = isExpression();
            if (expression != null) {
                parent.appendChild(expression);

                int lastTokenIndex = tokenIndex;
                int lineNumber = currentTokenLineNumber();

                if (!isSymbol(")", true)) {
                    tokenIndex = lastTokenIndex;
                    errorLogger(lineNumber, ")");
                }

                return parent;
            }
        }

        return null;
    }

    private Node isTypeCast() {
        Element parent = doc.createElement("TypeCast");

        if (isSymbol("(", false)) {
            Node type = isType();
            if (type != null) {
                parent.appendChild(type);
                int lastTokenIndex = tokenIndex;
                int lineNumber = currentTokenLineNumber();

                if (!isSymbol(")", true)) {
                    tokenIndex = lastTokenIndex;
                    errorLogger(lineNumber, ")");
                }

                lastTokenIndex = tokenIndex;
                lineNumber = currentTokenLineNumber();

                Node expression = isExpression();

                if (expression == null) {
                    expression = doc.createElement("Expression");
                    expression.setTextContent("ERROR");
                    errorLogger(lineNumber, "Expression");
                    tokenIndex = lastTokenIndex;
                }

                parent.appendChild(expression);

                return parent;
            }
        }

        return null;
    }

    private Node isActualParams() {
        Element parent = doc.createElement("ActualParams");

        int lastTokenIndex = tokenIndex;

        Node expression = isExpression();

        if (expression != null) {
            parent.appendChild(expression);

            int lineNumber;

            lastTokenIndex = tokenIndex;

            while (isSymbol(",", true)) {
                lastTokenIndex = tokenIndex;
                lineNumber = currentTokenLineNumber();

                expression = isExpression();

                if (expression == null) {
                    expression = doc.createElement("Expression");
                    expression.setTextContent("ERROR");
                    errorLogger(lineNumber, "Expression");
                    tokenIndex = lastTokenIndex;
                }

                lastTokenIndex = tokenIndex;

                parent.appendChild(expression);
            }

            tokenIndex = lastTokenIndex;

            return parent;

        }else {
            tokenIndex = lastTokenIndex;
        }

        return parent;
    }


    private Node isFunctionCall() {
        Element parent = doc.createElement("FunctionCall");

        Node identifier = isIdentifier();

        if (identifier != null) {
            if (isSymbol("(", false)) {
                parent.appendChild(identifier);

                int lastTokenIndex = tokenIndex;

                Node actualParams = isActualParams();

                if (actualParams != null) {
                    lastTokenIndex = tokenIndex;
                    parent.appendChild(actualParams);
                }else {
                    tokenIndex = lastTokenIndex;
                }

                int lineNumber = currentTokenLineNumber();

                if (!isSymbol(")", true)) {
                    tokenIndex = lastTokenIndex;
                    errorLogger(lineNumber, ")");
                }

                return parent;
            }
        }

        return null;
    }

    private Node isLiteral() {
        Element parent = doc.createElement("Literal");

        Node child = null;

        int lastTokenIndex = tokenIndex;

        child = isBooleanLiteral();

        if (child == null){
            tokenIndex = lastTokenIndex;
            child = isStringLiteral();
        }

        if (child == null){
            tokenIndex = lastTokenIndex;
            child = isRealLiteral();
        }

        if (child == null){
            tokenIndex = lastTokenIndex;
            child = isIntegerLiteral();
        }

        if (child == null){
            tokenIndex = lastTokenIndex;
            child = isUnitLiteral();
        }

        if (child == null){
            tokenIndex = lastTokenIndex;
            child = isCharLiteral();
        }

        if (child != null){
            parent.appendChild(child);
            return parent;
        }

        return null;
    }


    private Node isFactor() {
        Element parent = doc.createElement("Factor");

        int lastTokenIndex = tokenIndex;


        Node child = isLiteral();

        if (child == null) {
            tokenIndex = lastTokenIndex;
            child = isTypeCast();
        }

        if (child == null) {
            tokenIndex = lastTokenIndex;
            child = isFunctionCall();
        }

        if (child == null) {
            tokenIndex = lastTokenIndex;
            child = isIdentifier();
        }

        if (child == null) {
            tokenIndex = lastTokenIndex;
            child = isSubExpression();
        }

        if (child == null) {
            tokenIndex = lastTokenIndex;
            child = isUnary();
        }


        if (child == null) {
            return null;
        } else {
            parent.appendChild(child);
        }


        return parent;
    }

    private Node isTerm() {
        Element parent = doc.createElement("Term");

        Node startFactor = isFactor();

        if (startFactor != null) {
            parent.appendChild(startFactor);
            while (isAMultiplicativeOp()) {
                parent.appendChild(isMultiplicativeOp());

                int lastTokenIndex = tokenIndex;
                int lineNumber = currentTokenLineNumber();

                Node factor = isFactor();

                if (factor == null) {
                    factor = doc.createElement("Factor");
                    factor.setTextContent("ERROR");
                    errorLogger(lineNumber, "Factor");
                    tokenIndex = lastTokenIndex;
                }

                parent.appendChild(factor);
            }

            return parent;
        }

        return null;
    }

    private Node isSimpleExpression() {
        Element parent = doc.createElement("SimpleExpression");

        Node startTerm = isTerm();

        if (startTerm != null) {
            parent.appendChild(startTerm);

            while (isAAdditiveOp()) {
                parent.appendChild(isAdditiveOp());

                int lastTokenIndex = tokenIndex;
                int lineNumber = currentTokenLineNumber();

                Node term = isTerm();

                if (term == null) {
                    term = doc.createElement("Term");
                    term.setTextContent("ERROR");
                    errorLogger(lineNumber, "Term");
                    tokenIndex = lastTokenIndex;
                }

                parent.appendChild(term);
            }

            return parent;
        }

        return null;
    }

    private Node isFormalParam() {
        Element parent = doc.createElement("FormalParam");

        Node identifier = isIdentifier();
        if (identifier != null) {
            parent.appendChild(identifier);
            int lastTokenIndex = tokenIndex;
            int lineNumber = currentTokenLineNumber();

            if (!isSymbol(":", true)) {
                tokenIndex = lastTokenIndex;
                errorLogger(lineNumber, ":");
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            Node type = isType();

            if (type == null) {
                type = doc.createElement("Type");
                type.setTextContent("ERROR");
                errorLogger(lineNumber, "Type");
                tokenIndex = lastTokenIndex;
            }

            parent.appendChild(type);

            return parent;


        }

        return null;
    }

    private Node isFormalParams() {
        Element parent = doc.createElement("FormalParams");

        int lastTokenIndex = tokenIndex;
        Node formalParam = isFormalParam();

        if (formalParam != null) {
            parent.appendChild(formalParam);

            int lineNumber;

            lastTokenIndex = tokenIndex;
            while (isSymbol(",", true)) {
                lastTokenIndex = tokenIndex;
                lineNumber = currentTokenLineNumber();

                formalParam = isFormalParam();

                if (formalParam == null) {
                    formalParam = doc.createElement("FormalParam");
                    formalParam.setTextContent("ERROR");
                    errorLogger(lineNumber, "Formal Param");
                    tokenIndex = lastTokenIndex;
                }

                lastTokenIndex = tokenIndex;

                parent.appendChild(formalParam);
            }

            tokenIndex = lastTokenIndex;

            return parent;

        }else{
            tokenIndex = lastTokenIndex;
        }

        return parent;
    }

    private Node isFunctionDecl() {
        Element parent = doc.createElement("FunctionDecl");


        if (isKeyword("function", false)) {
            int lastTokenIndex = tokenIndex;
            int lineNumber = currentTokenLineNumber();

            Node identifier = isIdentifier();

            if (identifier == null) {
                identifier = doc.createElement("Identifier");
                identifier.setTextContent("ERROR");
                errorLogger(lineNumber, "Identifier");
                tokenIndex = lastTokenIndex;
            }

            parent.appendChild(identifier);

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            if (!isSymbol("(", true)) {
                tokenIndex = lastTokenIndex;
                errorLogger(lineNumber, "(");
            }

            lastTokenIndex = tokenIndex;

            Node formalParams = isFormalParams();

            if (formalParams != null) {
                lastTokenIndex = tokenIndex;
                parent.appendChild(formalParams);
            }else {
                tokenIndex = lastTokenIndex;
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            if (!isSymbol(")", true)) {
                tokenIndex = lastTokenIndex;
                errorLogger(lineNumber, ")");
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            if (!isSymbol(":", true)) {
                tokenIndex = lastTokenIndex;
                errorLogger(lineNumber, ":");
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            Node type = isType();

            if (type == null) {
                type = doc.createElement("Type");
                type.setTextContent("ERROR");
                errorLogger(lineNumber, "Type");
                tokenIndex = lastTokenIndex;
            }

            parent.appendChild(type);

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            Node block = isBlock();

            if (block == null) {
                block = doc.createElement("Block");
                block.setTextContent("ERROR");
                errorLogger(lineNumber, "Block");
                tokenIndex = lastTokenIndex;
            }

            parent.appendChild(block);

            return parent;

        }

        return null;
    }

    private Node isAssignment() {
        Element parent = doc.createElement("Assignment");

        if (isKeyword("set", false)) {
            int lastTokenIndex = tokenIndex;
            int lineNumber = currentTokenLineNumber();

            Node identifier = isIdentifier();

            if (identifier == null) {
                identifier = doc.createElement("Identifier");
                identifier.setTextContent("ERROR");
                errorLogger(lineNumber, "Identifier");
                tokenIndex = lastTokenIndex;
            }

            parent.appendChild(identifier);

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            if (!isSymbol("<-", true)) {
                tokenIndex = lastTokenIndex;
                errorLogger(lineNumber, "<-");
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            Node expression = isExpression();

            if (expression == null) {
                expression = doc.createElement("Expression");
                expression.setTextContent("ERROR");
                errorLogger(lineNumber, "Expression");
                tokenIndex = lastTokenIndex;
            }

            parent.appendChild(expression);

            return parent;
        }

        return null;
    }

    private Node isExpression() {
        Element parent = doc.createElement("Expression");

        Node startSimpleExpression = isSimpleExpression();

        if (startSimpleExpression != null) {
            parent.appendChild(startSimpleExpression);

            if (isA(JParserSym.RELATIONAL_OP)) {
                tokenIndex--;
                parent.appendChild(isRelationalOp());

                int lastTokenIndex = tokenIndex;
                int lineNumber = currentTokenLineNumber();

                Node simpleExpression = isSimpleExpression();

                if (simpleExpression == null) {
                    simpleExpression = doc.createElement("SimpleExpression");
                    simpleExpression.setTextContent("ERROR");
                    errorLogger(lineNumber, "Simple Expression");
                    tokenIndex = lastTokenIndex;
                }

                parent.appendChild(simpleExpression);
            }else {
                tokenIndex--;
            }

            return parent;
        }

        return null;
    }


    private Node isVariableDecl() {
        Element parent = doc.createElement("VariableDecl");

        if (isKeyword("let", false)) {
            int lastTokenIndex = tokenIndex;
            int lineNumber = currentTokenLineNumber();

            Node identifier = isIdentifier();

            if (identifier == null) {
                identifier = doc.createElement("Identifier");
                identifier.setTextContent("ERROR");
                errorLogger(lineNumber, "Identifier");
                tokenIndex = lastTokenIndex;
            }

            parent.appendChild(identifier);

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            if (!isSymbol(":", true)) {
                tokenIndex = lastTokenIndex;
                errorLogger(lineNumber, ":");
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            Node type = isType();

            if (type == null) {
                type = doc.createElement("Type");
                type.setTextContent("ERROR");
                errorLogger(lineNumber, "Type");
                tokenIndex = lastTokenIndex;
            }

            parent.appendChild(type);

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            if (!isSymbol("=", true)) {
                tokenIndex = lastTokenIndex;
                errorLogger(lineNumber, "=");
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            Node expression = isExpression();

            if (expression == null) {
                expression = doc.createElement("Expression");
                expression.setTextContent("ERROR");
                errorLogger(lineNumber, "Expression");
                tokenIndex = lastTokenIndex;
            }

            parent.appendChild(expression);

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            if (!isKeyword("in", true)) {
                tokenIndex = lastTokenIndex;
                if (!isSymbol(";", true)) {
                    errorLogger(lineNumber, ";");
                    tokenIndex = lastTokenIndex;
                }
            } else {
                lastTokenIndex = tokenIndex;

                Node block = isBlock();

                if (block == null) {
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

    private Node isWriteStatement() {
        Element parent = doc.createElement("WriteStatement");

        if (isKeyword("write", false)) {
            int lastTokenIndex = tokenIndex;
            int lineNumber = currentTokenLineNumber();

            Node identifier = isIdentifier();

            if (identifier == null) {
                identifier = doc.createElement("Identifier");
                identifier.setTextContent("ERROR");
                errorLogger(lineNumber, "Identifier");
                tokenIndex = lastTokenIndex;
            }

            parent.appendChild(identifier);

            return parent;
        }

        return null;
    }

    private Node isReadStatement() {
        Element parent = doc.createElement("ReadStatement");

        if (isKeyword("read", false)) {
            int lastTokenIndex = tokenIndex;
            int lineNumber = currentTokenLineNumber();

            Node identifier = isIdentifier();

            if (identifier == null) {
                identifier = doc.createElement("Identifier");
                identifier.setTextContent("ERROR");
                errorLogger(lineNumber, "Identifier");
                tokenIndex = lastTokenIndex;
            }

            parent.appendChild(identifier);

            return parent;
        }

        return null;
    }

    private Node isIfStatement() {
        Element parent = doc.createElement("IfStatement");

        Node expression = null;
        Node statement = null;

        if (isKeyword("if", false)) {
            int lastTokenIndex = tokenIndex;
            int lineNumber = currentTokenLineNumber();

            if (!isSymbol("(", true)) {
                tokenIndex = lastTokenIndex;
                errorLogger(lineNumber, "(");
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            expression = isExpression();

            if (expression == null) {
                tokenIndex = lastTokenIndex;
                expression = doc.createElement("expression");
                expression.setTextContent("ERROR");
                errorLogger(lineNumber, "Expression");
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            if (!isSymbol(")", true)) {
                tokenIndex = lastTokenIndex;
                errorLogger(lineNumber, ")");
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            statement = isStatement();

            if (statement == null) {
                tokenIndex = lastTokenIndex;
                expression = doc.createElement("Statement");
                expression.setTextContent("ERROR");
                errorLogger(lineNumber, "Statement");
            }

            parent.appendChild(expression);
            parent.appendChild(statement);

            lastTokenIndex = tokenIndex;

            if (isKeyword("else", false)) {
                lastTokenIndex = tokenIndex;
                lineNumber = currentTokenLineNumber();

                statement = isStatement();

                if (statement == null) {
                    tokenIndex = lastTokenIndex;
                    expression = doc.createElement("Statement");
                    expression.setTextContent("ERROR");
                    errorLogger(lineNumber, "Statement");
                }

                parent.appendChild(statement);
            } else {
                tokenIndex = lastTokenIndex;
            }

            return parent;
        }

        return null;
    }

    private Node isWhileStatement() {
        Element parent = doc.createElement("WhileStatement");

        Node expression = null;
        Node statement = null;

        if (isKeyword("while", false)) {
            int lastTokenIndex = tokenIndex;
            int lineNumber = currentTokenLineNumber();

            if (!isSymbol("(", true)) {
                tokenIndex = lastTokenIndex;
                errorLogger(lineNumber, "(");
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            expression = isExpression();

            if (expression == null) {
                tokenIndex = lastTokenIndex;
                expression = doc.createElement("expression");
                expression.setTextContent("ERROR");
                errorLogger(lineNumber, "Expression");
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            if (!isSymbol(")", true)) {
                tokenIndex = lastTokenIndex;
                errorLogger(lineNumber, ")");
            }

            lastTokenIndex = tokenIndex;
            lineNumber = currentTokenLineNumber();

            statement = isStatement();

            if (statement == null) {
                tokenIndex = lastTokenIndex;
                expression = doc.createElement("Statement");
                expression.setTextContent("ERROR");
                errorLogger(lineNumber, "Statement");
            }

            parent.appendChild(expression);
            parent.appendChild(statement);

            return parent;
        }

        return null;
    }

    private Node isHaltStatement() {
        Element parent = doc.createElement("HaltStatement");

        if (isKeyword("halt", false)) {
            int lastTokenIndex = tokenIndex;
            int lineNumber;

            Node integerLiteral = isIntegerLiteral();

            if (integerLiteral == null) {
                tokenIndex = lastTokenIndex;

                lineNumber = currentTokenLineNumber();
                Node identifier = isIdentifier();

                if (identifier == null) {
                    return parent;
                }
                parent.appendChild(identifier);
            } else {
                parent.appendChild(integerLiteral);
            }

            return parent;
        }

        return null;
    }

    private Node isBlock() {
        Element parent = doc.createElement("Block");

        int currentIndex = tokenIndex;

        Node child = null;
        if (isSymbol("{", false)) {
            currentIndex = tokenIndex;
            child = isStatement();
            while (child != null) {
                currentIndex = tokenIndex;
                parent.appendChild(child);
                child = isStatement();
            }
            tokenIndex = currentIndex;
            int lineNumber = currentTokenLineNumber();
            if (!isSymbol("}", true)) {
                errorLogger(lineNumber, "}");
                tokenIndex = currentIndex;
            }
            return parent;
        }


        return null;
    }

    private boolean isSymbol(String symbol, boolean persist) {
        int type = currentTokenType();

        if (!isASymbol(symbol)) {
            int lineNumber = currentTokenLineNumber();
            if (!isDone() && persist) {
                if (!isASymbol(symbol)) {
                    return false;
                } else {
                    errorLogger(lineNumber, "Syntax error before '" + symbol + "'", false);
                    return true;
                }
            } else {
                return false;
            }
        }

        return true;
    }

    private boolean isASymbol(String symbol){
        int type = currentTokenType();

        tokenIndex++;

        if (symbol.equals("(") && type == JParserSym.BRACE_OPEN){
            return true;
        }else if (symbol.equals(")") && type == JParserSym.BRACE_CLOSE){
            return true;
        }else if (symbol.equals("}") && type == JParserSym.CURLY_BRACE_CLOSE){
            return true;
        }else if (symbol.equals("{") && type == JParserSym.CURLY_BRACE_OPEN){
            return true;
        }else if (symbol.equals(",") && type == JParserSym.COMMA){
            return true;
        }else if (symbol.equals(";") && type == JParserSym.SEMICOLON){
            return true;
        }else if (symbol.equals(":") && type == JParserSym.COLON){
            return true;
        }else if (symbol.equals("<-") && type == JParserSym.TO){
            return true;
        }else if (symbol.equals("=") && type == JParserSym.EQ){
            return true;
        }

        return false;
    }

    private boolean isKeyword(String keyword, boolean persist) {
        if (!isAKeyword(keyword)) {
            int lineNumber = currentTokenLineNumber();
            if (!isDone() && persist) {
                if (!isAKeyword(keyword)) {
                    return false;
                } else {
                    errorLogger(lineNumber, "Syntax error before '" + keyword + "'", false);
                    return true;
                }
            } else {
                return false;
            }
        }

        return true;
    }

    private boolean isAKeyword(String symbol){
        int type = currentTokenType();

        tokenIndex++;

        if (symbol.equals("not") && type == JParserSym.NOT){
            return true;
        }else if (symbol.equals("in") && type == JParserSym.IN){
            return true;
        }else if (symbol.equals("set") && type == JParserSym.SET){
            return true;
        }else if (symbol.equals("halt") && type == JParserSym.HALT){
            return true;
        }else if (symbol.equals("let") && type == JParserSym.LET){
            return true;
        }else if (symbol.equals("function") && type == JParserSym.FUNCTION){
            return true;
        }else if (symbol.equals("while") && type == JParserSym.WHILE){
            return true;
        }else if (symbol.equals("read") && type == JParserSym.READ){
            return true;
        }else if (symbol.equals("write") && type == JParserSym.WRITE){
            return true;
        }else if (symbol.equals("if") && type == JParserSym.IF){
            return true;
        }else if (symbol.equals("else") && type == JParserSym.ELSE){
            return true;
        }

        return false;
    }

    private Node isIntegerLiteral() {
        Element parent = doc.createElement("IntegerLiteral");

        if (!isA(JParserSym.INTEGER_LITERAL)) {
            return null;
        } else {
            parent.setTextContent(tokens.get(tokenIndex - 1).value.toString());
            parent.setAttribute("lineNumber", currentTokenLineNumber() + "");
        }

        return parent;
    }

    private Node isBooleanLiteral() {
        Element parent = doc.createElement("BooleanLiteral");

        if (!isA(JParserSym.BOOLEAN_LITERAL)) {
            return null;
        } else {
            parent.setTextContent(tokens.get(tokenIndex - 1).value.toString());
            parent.setAttribute("lineNumber", currentTokenLineNumber() + "");
        }

        return parent;
    }

    private Node isRealLiteral() {
        Element parent = doc.createElement("RealLiteral");

        if (!isA(JParserSym.REAL_LITERAL)) {
            return null;
        } else {
            parent.setTextContent(tokens.get(tokenIndex - 1).value.toString());
            parent.setAttribute("lineNumber", currentTokenLineNumber() + "");
        }

        return parent;
    }

    private Node isCharLiteral() {
        Element parent = doc.createElement("CharLiteral");

        if (!isA(JParserSym.CHAR_LITERAL)) {
            return null;
        } else {
            String text = tokens.get(tokenIndex - 1).value.toString();
            parent.setTextContent(text.substring(1, text.length() - 1));
            parent.setAttribute("lineNumber", currentTokenLineNumber() + "");
        }

        return parent;
    }

    private Node isStringLiteral() {
        Element parent = doc.createElement("StringLiteral");

        if (!isA(JParserSym.STRING_LITERAL)) {
            return null;
        } else {
            String text = tokens.get(tokenIndex - 1).value.toString();
            parent.setTextContent(text.substring(1, text.length() - 1));
            parent.setAttribute("lineNumber", currentTokenLineNumber() + "");
        }

        return parent;
    }

    private Node isUnitLiteral() {
        Element parent = doc.createElement("UnitLiteral");

        if (!isA(JParserSym.UNIT_LITERAL)) {
            return null;
        } else {
            parent.setTextContent(tokens.get(tokenIndex - 1).value.toString());
            parent.setAttribute("lineNumber", currentTokenLineNumber() + "");
        }

        return parent;
    }

    private Node isRelationalOp() {
        Element parent = doc.createElement("RelationalOp");

        if (!isA(JParserSym.RELATIONAL_OP)) {
            return null;
        } else {
            parent.setTextContent(tokens.get(tokenIndex - 1).value.toString());
            parent.setAttribute("lineNumber", currentTokenLineNumber() + "");
        }

        return parent;
    }

    private Node isMultiplicativeOp() {
        Element parent = doc.createElement("MultiplicativeOp");

        if (isAMultiplicativeOp()) {
            String sym = (currentTokenType() == JParserSym.MULTIPLICATION? "*" : (currentTokenType() == JParserSym.DIVISION? "/" : "and")) ;
            parent.setTextContent(sym);
            tokenIndex++;
            parent.setAttribute("lineNumber", currentTokenLineNumber() + "");
            return parent;
        }
        tokenIndex++;
        return null;
    }

    private boolean isAMultiplicativeOp(){
        return currentTokenType() == JParserSym.MULTIPLICATION || currentTokenType() == JParserSym.AND || currentTokenType() == JParserSym.DIVISION;
    }

    private Node isAdditiveOp() {
        Element parent = doc.createElement("AdditiveOp");

        if (isAAdditiveOp()) {
            String sym = (currentTokenType() == JParserSym.PLUS? "+" : (currentTokenType() == JParserSym.MINUS? "-" : "not")) ;
            parent.setTextContent(sym);
            tokenIndex++;
            //parent.setAttribute("lineNumber", currentTokenLineNumber() + "");
            return parent;
        }

        tokenIndex++;
        return null;
    }

    private boolean isAAdditiveOp(){
        return currentTokenType() == JParserSym.PLUS || currentTokenType() == JParserSym.MINUS || currentTokenType() == JParserSym.OR;
    }


    private Node isIdentifier() {
        Element parent = doc.createElement("Identifier");

        if (!isA(JParserSym.IDENTIFIER)) {
            return null;
        } else {
            parent.setTextContent(tokens.get(tokenIndex - 1).value.toString());
            parent.setAttribute("lineNumber", currentTokenLineNumber() + "");
        }

        return parent;
    }

    private Node isType() {
        Element parent = doc.createElement("Type");

        if (!isA(JParserSym.TYPE)) {
            return null;
        } else {
            parent.setTextContent(tokens.get(tokenIndex - 1).value.toString());
            parent.setAttribute("lineNumber", currentTokenLineNumber() + "");
        }

        return parent;
    }

    private boolean isA(int type) {
        if (isDone()) {
            tokenIndex++;
            return false;
        }
        return tokens.get(tokenIndex++).sym == type;
    }

    private boolean isAWith(int type, String str) {
        return tokens.get(tokenIndex).value.equals(str) & isA(type);
    }

    private int currentTokenLineNumber() {
        if (tokenIndex >= tokens.size()) return -1;
        return tokens.get(tokenIndex).left;
    }

    private int currentTokenType() {
        if (tokenIndex >= tokens.size()) return -1;
        return tokens.get(tokenIndex).sym;
    }

    private void errorLogger(int lineNumber, String expected) {
        errorLogger(lineNumber, expected, true);
    }

    private void errorLogger(int lineNumber, String expected, boolean exp) {
        if (useLineNumbers) {
            errorLog.add("Error: " + (exp ? "Expected: " : "") + expected + "  at line " + lineNumber);
        } else {
            errorLog.add("Error: " + (exp ? "Expected: " : "") + expected);
        }
        error = true;
    }

    private void dumpErrors() {
        for (int loops = 0; loops < errorLog.size(); loops++) {
            System.out.println(errorLog.get(loops));
        }
    }
}
