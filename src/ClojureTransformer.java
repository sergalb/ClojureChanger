import jdk.nashorn.api.tree.*;
import java.io.File;

// Simple example that prints warning on 'with' statements
public class ClojureTransformer {
    static public class MyVisitor extends SimpleTreeVisitorES6<Void, Void>{
        @Override
        public Void visitIdentifier(IdentifierTree node, Void r) {
            System.out.println(node.getName());
            return super.visitIdentifier(node, r);
        }

        @Override
        public Void visitBinary(BinaryTree node, Void r) {
            System.out.println(node.getKind().toString());
            return super.visitBinary(node, r);
        }

        @Override
        public Void visitAssignment(AssignmentTree node, Void r) {
            node.getVariable().accept(this, r);
            System.out.println("=");
            node.getExpression().accept(this, r);
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        // Create a new parser instance
        Parser parser = Parser.create();
        File sourceFile = new File(args[0]);

        // Parse given source File using parse method.
        // Pass a diagnostic listener to print error messages.
        CompilationUnitTree cut = parser.parse(sourceFile,
                (d) -> { System.out.println(d); });

        if (cut != null) {
            // call Tree.accept method passing a SimpleTreeVisitor
            System.out.println(cut.accept(new ClojureVisitor(), null));
        }
    }
}
 