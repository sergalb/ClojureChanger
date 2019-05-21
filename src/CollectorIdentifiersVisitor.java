import jdk.nashorn.api.tree.BlockTree;
import jdk.nashorn.api.tree.IdentifierTree;
import jdk.nashorn.api.tree.SimpleTreeVisitorES6;
import jdk.nashorn.api.tree.VariableTree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollectorIdentifiersVisitor extends SimpleTreeVisitorES6<Void, Pair> {
    private List<Set<String>> ownVariables;
    private Set<String> putedVariables;
    CollectorIdentifiersVisitor() {
        super();
        ownVariables = new ArrayList<>();
        putedVariables = new HashSet<>();
    }

    @Override
    public Void visitVariable(VariableTree node, Pair r) {
        node.getBinding().accept(this, new Pair(r.getVariables(), true));
        node.getInitializer().accept(this, r);
        return null;
    }

    @Override
    public Void visitIdentifier(IdentifierTree node, Pair r) {
        String name = node.getName();
        if (r.getFromVariableTree()) {
            ownVariables.get(ownVariables.size() - 1).add(name);
        } else if (!putedVariables.contains(name)){
            boolean isInternalVariable = false;
            for (Set set : ownVariables) {
                if (set.contains(name)) {
                    isInternalVariable = true;
                    break;
                }
            }
            if (!isInternalVariable) {
                r.getVariables().add(name);
                putedVariables.add(name);
            }
        }
        return null;
    }

    @Override
    public Void visitBlock(BlockTree node, Pair r) {
        ownVariables.add(new HashSet<>());
        node.getStatements().forEach((tree) -> {
            tree.accept(this, r);
        });
        ownVariables.remove(ownVariables.size() - 1);
        return null;
    }
}