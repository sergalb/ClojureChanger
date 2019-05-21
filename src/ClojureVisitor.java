import jdk.nashorn.api.tree.*;

import java.util.*;

public class ClojureVisitor extends SimpleTreeVisitorES6<StringBuilder, Boolean> {
    private final String LINE_SEPARATOR = "\n";
    //Queue<Pair> stack;
    private Map<String, FunctionProperties> changedFunction;
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
        result.append(printNewFunctions());
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

    @Override
    public StringBuilder visitFunctionDeclaration(FunctionDeclarationTree node, Boolean isNestedFunction) {
        String functionName = node.getName().accept(this, isNestedFunction).toString();
        StringBuilder functionBody = node.getBody().accept(this, true);
        List<String> oldArguments = getListArguments(node.getParameters(), isNestedFunction);

        StringBuilder functionDeclaration = new StringBuilder("function ");
        if (!isNestedFunction) {
            functionDeclaration.append(functionName);
            functionDeclaration.append("(");
            functionDeclaration.append(printArguments(oldArguments));
            functionDeclaration.append(") ");
            functionDeclaration.append(functionBody);
            return functionDeclaration;
        } else {
            List<String> internalUsedVariables = new ArrayList<>();
            node.accept(new CollectorIdentifiersVisitor(), new Pair(internalUsedVariables, false));
            changedFunction.put(functionName, new FunctionProperties(internalUsedVariables, functionBody));
            return new StringBuilder();
        }
    }

    @Override
    public StringBuilder visitFunctionCall(FunctionCallTree node, Boolean r) {
        StringBuilder functionName = node.getFunctionSelect().accept(this, r);
        StringBuilder functionCallCode = new StringBuilder(functionName).append("(");
        List<String> requiredArguments = getListArguments(node.getArguments(), r);
        functionCallCode.append(printArguments(requiredArguments));
        if (changedFunction.containsKey(functionName.toString())) {
            List<String> newArguments = changedFunction.get(functionName.toString()).getArguments();
            List<String> additionalArguments = newArguments.subList(requiredArguments.size(), newArguments.size());
            if (!requiredArguments.isEmpty() && !additionalArguments.isEmpty()) {
                functionCallCode.append(", ");
            }
            functionCallCode.append(printArguments(additionalArguments));
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

    private StringBuilder printArguments(List<String> arguments) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < arguments.size(); ++i) {
            result.append(arguments.get(i));
            if (i < arguments.size() - 1) {
                result.append(", ");
            }
        }
        return result;
    }

    private List<String> getListArguments(List<? extends ExpressionTree> arguments, Boolean r) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < arguments.size(); ++i) {
            result.add(arguments.get(i).accept(this, r).toString());
        }
        return result;
    }

    private StringBuilder printNewFunctions() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, FunctionProperties> newFunction: changedFunction.entrySet()) {
            result.append(newFunction.getKey()).append("(");
            FunctionProperties functionProperties = newFunction.getValue();
            result.append(printArguments(functionProperties.getArguments()));
            result.append(") ").append(functionProperties.getBody());
        }
        return result;
    }

    public class FunctionProperties {
        private List<String> arguments;
        private StringBuilder body;

        public FunctionProperties(List<String> arguments, StringBuilder body) {
            this.arguments = arguments;
            this.body = body;
        }

        public List<String> getArguments() {
            return arguments;
        }

        public StringBuilder getBody() {
            return body;
        }


    }
}
