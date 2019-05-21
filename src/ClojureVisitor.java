import jdk.nashorn.api.tree.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClojureVisitor extends SimpleTreeVisitorES6<StringBuilder, Boolean> {
    private final String LINE_SEPARATOR = "\n";
    //Queue<Pair> stack;
    private Map<String, FunctionProperties> changedFunction;
    StringBuilder newTopLevelFunctions;
    public ClojureVisitor() {
        super();
        changedFunction = new HashMap<>();
    }

    @Override
    public StringBuilder visitCompilationUnit(CompilationUnitTree node, Boolean r) {
        StringBuilder result = new StringBuilder();
        node.getSourceElements().forEach((tree) -> {
            result.append(tree.accept(this, false));
        });
        return result;
    }

    @Override
    public StringBuilder visitVariable(VariableTree node, Boolean r) {
        StringBuilder variableCode = new StringBuilder("var ");
        variableCode.append(node.getBinding().accept(this, r));
        variableCode.append(" = ");
        variableCode.append(node.getInitializer().accept(this, r));
        variableCode.append(";").append(LINE_SEPARATOR);
        return variableCode;
    }

    @Override
    public StringBuilder visitBlock(BlockTree node, Boolean r) {
        StringBuilder blockCode = new StringBuilder("{").append(LINE_SEPARATOR);
        node.getStatements().forEach((tree) -> {
            blockCode.append(tree.accept(this, r));
        });
        blockCode.append("}").append(LINE_SEPARATOR);
        return blockCode;
    }

    @Override
    public StringBuilder visitExpressionStatement(ExpressionStatementTree node, Boolean r) {
        return node.getExpression().accept(this, r).append(";").append(LINE_SEPARATOR);
    }

    @Override
    public StringBuilder visitAssignment(AssignmentTree node, Boolean r) {
        StringBuilder assigmentCode = node.getVariable().accept(this, r);
        assigmentCode.append(" = ");
        assigmentCode.append(node.getExpression().accept(this, r));
        return assigmentCode;
    }

    @Override
    public StringBuilder visitBinary(BinaryTree node, Boolean r) {
        StringBuilder binaryCode = node.getLeftOperand().accept(this, r);
        StringBuilder operator = new StringBuilder();
        switch (node.getKind()) {
            case PLUS:
                operator.append(" + ");
                break;
            default:
                //todo need in this?
                throw new IllegalArgumentException("Binary operator \"" + node.getKind() + "\" doesn't supported");
        }
        binaryCode.append(operator);
        binaryCode.append(node.getRightOperand().accept(this, r));
        return binaryCode;
    }

    //change old name to new name and add new arguments
    @Override
    public StringBuilder visitFunctionDeclaration(FunctionDeclarationTree node, Boolean isNestedFunction) {
        //todo correct arguments
        String functionName = node.getName().accept(this, isNestedFunction).toString();
        StringBuilder functionBody = node.getBody().accept(this, true);
        StringBuilder functionDeclaration = new StringBuilder("function ");
        List<? extends ExpressionTree> oldArguments = node.getParameters();
        if (!isNestedFunction) {
            functionDeclaration.append(functionName);
            functionDeclaration.append("(");
            functionDeclaration.append(printArguments(oldArguments, true));
            functionDeclaration.append(") ");
            functionDeclaration.append(functionBody);

        } else {

            changedFunction.put(functionName, new FunctionProperties(takeNewName(functionName), Collections.emptyList(), functionBody));
            return new StringBuilder();
        }

        return functionDeclaration;
    }

    @Override
    public StringBuilder visitFunctionCall(FunctionCallTree node, Boolean r) {
        StringBuilder functionName = node.getFunctionSelect().accept(this, r);
        if (!changedFunction.containsKey(functionName.toString())) {
            throw new IllegalArgumentException("Function " + functionName + "doesn't declared in current scope");
        }
        FunctionProperties properties = changedFunction.get(functionName.toString());
        StringBuilder functionCallCode = new StringBuilder(properties.getName());
        functionCallCode.append("(");
        functionCallCode.append(printArguments(node.getArguments(), r));
        if (!properties.newArguments.isEmpty()) {
            functionCallCode.append(", ");
        }
        //todo print arguments
        for (int i = 0; i < properties.newArguments.size(); ++i) {
            functionCallCode.append(properties.newArguments.get(i));
            if (i < properties.newArguments.size() - 1) {
                functionCallCode.append(", ");
            }
        }
        functionCallCode.append(")");
        return functionCallCode;
    }

    @Override
    public StringBuilder visitIdentifier(IdentifierTree node, Boolean r) {
        return new StringBuilder(node.getName());
    }

    @Override
    public StringBuilder visitLiteral(LiteralTree node, Boolean r) {
        return new StringBuilder(node.getValue().toString());
    }

    @Override
    public StringBuilder visitParenthesized(ParenthesizedTree node, Boolean r) {
        StringBuilder parenthesizedCode = new StringBuilder("(");
        parenthesizedCode.append(node.getExpression().accept(this, r));
        parenthesizedCode.append(")");
        return parenthesizedCode;
    }

    @Override
    public StringBuilder visitReturn(ReturnTree node, Boolean r) {
        StringBuilder returnCode = new StringBuilder("return ");
        returnCode.append(node.getExpression().accept(this, r));
        returnCode.append(";").append(LINE_SEPARATOR);
        return returnCode;
    }

    @Override
    public StringBuilder visitUnary(UnaryTree node, Boolean r) {
        //todo may be I need in it
        return super.visitUnary(node, r);
    }

    private StringBuilder printArguments(List<? extends ExpressionTree> arguments, Boolean r) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < arguments.size(); ++i) {
            result.append(arguments.get(i).accept(this, r));
            if (i < arguments.size() - 1) {
                result.append(", ");
            }
        }
        return result;
    }

    //todo realize
    private StringBuilder takeNewName(String oldName) {
        return new StringBuilder(oldName);
    }

    public class FunctionProperties {
        private StringBuilder name;
        private List<String> newArguments;
        private StringBuilder body;

        public FunctionProperties(StringBuilder name, List<String> newArguments, StringBuilder body) {
            this.name = name;
            this.newArguments = newArguments;
            this.body = body;
        }

        public StringBuilder getName() {
            return name;
        }

        public List<String> getNewArguments() {
            return newArguments;
        }

        public StringBuilder getBody() {
            return body;
        }
    }
}
