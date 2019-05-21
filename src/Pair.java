import java.util.List;

public class Pair {
    List<String> variables;
    Boolean fromVariableTree;

    public Pair(List<String> variables, Boolean fromVariableTree) {
        this.variables = variables;
        this.fromVariableTree = fromVariableTree;
    }

    public List<String> getVariables() {
        return variables;
    }

    public void setVariables(List<String> variables) {
        this.variables = variables;
    }

    public Boolean getFromVariableTree() {
        return fromVariableTree;
    }

    public void setFromVariableTree(Boolean fromVariableTree) {
        this.fromVariableTree = fromVariableTree;
    }
}
