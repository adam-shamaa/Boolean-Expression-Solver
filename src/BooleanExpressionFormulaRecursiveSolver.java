import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

abstract class Node<T> {
    protected BooleanExpressionFormulaRecursiveSolver.NodeType nodeType;
    protected T value;
    protected Node lChild;
    protected Node rChild;

    public Node(BooleanExpressionFormulaRecursiveSolver.NodeType nodeType, T value) {
        this.nodeType = nodeType;
        this.value = value;
    }

    public Node(BooleanExpressionFormulaRecursiveSolver.NodeType nodeType, T value, Node lChild, Node rChild) {
        this.nodeType = nodeType;
        this.value = value;
        this.lChild = lChild;
        this.rChild = rChild;
    }

    public BooleanExpressionFormulaRecursiveSolver.NodeType getType() {
        return this.nodeType;
    }

    public T getValue() {
        return this.value;
    }

    Node getLeftNode() {
        return this.lChild;
    }

    Node getRightChild() {
        return this.rChild;
    }
}

class ConstantNode extends Node<Boolean> {
    public ConstantNode(boolean val) {
        super(BooleanExpressionFormulaRecursiveSolver.NodeType.CONSTANT, val);
    }
    public ConstantNode(boolean val, Node lNode, Node rNode) {
        super(BooleanExpressionFormulaRecursiveSolver.NodeType.CONSTANT, val, lNode, rNode);
    }
}

class OperatorNode extends Node<BooleanExpressionFormulaRecursiveSolver.Operator> {
    public OperatorNode(BooleanExpressionFormulaRecursiveSolver.Operator operator) {
        super(BooleanExpressionFormulaRecursiveSolver.NodeType.OPERATOR, operator);
    }
    public OperatorNode(BooleanExpressionFormulaRecursiveSolver.Operator operator, Node lNode, Node rNode) {
        super(BooleanExpressionFormulaRecursiveSolver.NodeType.OPERATOR, operator, lNode, rNode);
    }
}

class VariableNode extends Node<Variable> {
    public VariableNode(Variable variable) {
        super(BooleanExpressionFormulaRecursiveSolver.NodeType.VARIABLE, variable);
    }
    public VariableNode(Variable variable, Node lNode, Node rNode) {
        super(BooleanExpressionFormulaRecursiveSolver.NodeType.VARIABLE, variable, lNode, rNode);
    }
}

class Variable {
    private int variableId;
    private int hashCode;

    public Variable(int variableId) {
        this.variableId = variableId;
        this.hashCode = Objects.hash(this.getVariableId());
    }

    public int getVariableId() {
        return variableId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;
        Variable that = (Variable) o;
        return this.getVariableId() == that.getVariableId();
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }
}

public class BooleanExpressionFormulaRecursiveSolver {
    enum NodeType {CONSTANT, OPERATOR, VARIABLE}
    enum Operator {AND, OR, NOT, IMPLICATION, MATERIAL_IMPLICATION}

    static boolean eval(Node node, Map<Variable, Boolean> interpretation) {
        switch (node.nodeType) {
            case CONSTANT:
                return ((ConstantNode) node).getValue();
            case OPERATOR:
                switch (((OperatorNode) node).getValue()) {
                    case AND :
                        return eval(node.getLeftNode(), interpretation) && eval(node.getRightChild(), interpretation);
                    case OR:
                        return eval(node.getLeftNode(), interpretation) || eval(node.getRightChild(), interpretation);
                    case NOT:
                        return !eval(node.getLeftNode(), interpretation);
                    case IMPLICATION:
                        return !eval(node.getLeftNode(), interpretation) || eval(node.getRightChild(), interpretation);
                    case MATERIAL_IMPLICATION:
                        return eval(node.getLeftNode(), interpretation) == eval(node.getRightChild(), interpretation);
                    default:
                        throw new IllegalArgumentException();
                }
            case VARIABLE:
                return interpretation.get(((VariableNode) node).getValue());
            default:
                throw new IllegalArgumentException();
        }
    }

    static public void runTests() {
        // Create Graph
        // represents the expression:
        //  (((x1 <-> ~x2) -> x3) and (x4))
        OperatorNode rootNode =
                new OperatorNode(Operator.OR,
                    new OperatorNode(Operator.AND,
                        new OperatorNode(Operator.IMPLICATION,
                            new OperatorNode(Operator.MATERIAL_IMPLICATION,
                                new VariableNode(new Variable(1)),
                                new OperatorNode(Operator.NOT,
                                        new VariableNode(new Variable(2)),
                                        null
                                )
                            ),
                            new VariableNode(new Variable(3))
                        ),
                        new VariableNode(new Variable(4))
                    ),
                    new ConstantNode(false)
                );

        // Run tests:
            // Simply runs for each possible interpretation
            // interpretation definition & expected evaluation combined into single print statement
            // should print true for each test if it passes
        // X1=F x2=F x3=F x4=F
        System.out.println(eval(rootNode,
                    new HashMap<Variable, Boolean>(){{
                        put(new Variable(1), false);
                        put(new Variable(2), false);
                        put(new Variable(3), false);
                        put(new Variable(4), false);
                    }}) == false);

        // X1=F x2=F x3=F x4=T
        System.out.println(eval(rootNode,
                new HashMap<Variable, Boolean>(){{
                    put(new Variable(1), false);
                    put(new Variable(2), false);
                    put(new Variable(3), false);
                    put(new Variable(4), true);
                }}) == true);

        // X1=F x2=F x3=T x4=F
        System.out.println(eval(rootNode,
                new HashMap<Variable, Boolean>(){{
                    put(new Variable(1), false);
                    put(new Variable(2), false);
                    put(new Variable(3), true);
                    put(new Variable(4), false);
                }}) == false);

        // X1=F x2=F x3=T x4=T
        System.out.println(eval(rootNode,
                new HashMap<Variable, Boolean>(){{
                    put(new Variable(1), false);
                    put(new Variable(2), false);
                    put(new Variable(3), true);
                    put(new Variable(4), true);
                }}) == true);

        // X1=F x2=T x3=F x4=F
        System.out.println(eval(rootNode,
                new HashMap<Variable, Boolean>(){{
                    put(new Variable(1), false);
                    put(new Variable(2), true);
                    put(new Variable(3), false);
                    put(new Variable(4), false);
                }}) == false);

        // X1=F x2=T x3=F x4=T
        System.out.println(eval(rootNode,
                new HashMap<Variable, Boolean>(){{
                    put(new Variable(1), false);
                    put(new Variable(2), true);
                    put(new Variable(3), false);
                    put(new Variable(4), true);
                }}) == false);

        // X1=F x2=T x3=T x4=F
        System.out.println(eval(rootNode,
                new HashMap<Variable, Boolean>(){{
                    put(new Variable(1), false);
                    put(new Variable(2), true);
                    put(new Variable(3), true);
                    put(new Variable(4), false);
                }}) == false);

        // X1=F x2=T x3=T x4=T
        System.out.println(eval(rootNode,
                new HashMap<Variable, Boolean>(){{
                    put(new Variable(1), false);
                    put(new Variable(2), true);
                    put(new Variable(3), true);
                    put(new Variable(4), true);
                }}) == true);

        // X1=T x2=F x3=F x4=F
        System.out.println(eval(rootNode,
                new HashMap<Variable, Boolean>(){{
                    put(new Variable(1), true);
                    put(new Variable(2), false);
                    put(new Variable(3), false);
                    put(new Variable(4), false);
                }}) == false);

        // X1=T x2=F x3=F x4=T
        System.out.println(eval(rootNode,
                new HashMap<Variable, Boolean>(){{
                    put(new Variable(1), true);
                    put(new Variable(2), false);
                    put(new Variable(3), false);
                    put(new Variable(4), true);
                }}) == false);

        // X1=T x2=F x3=T x4=F
        System.out.println(eval(rootNode,
                new HashMap<Variable, Boolean>(){{
                    put(new Variable(1), true);
                    put(new Variable(2), false);
                    put(new Variable(3), true);
                    put(new Variable(4), false);
                }}) == false);

        // X1=T x2=F x3=T x4=T
        System.out.println(eval(rootNode,
                new HashMap<Variable, Boolean>(){{
                    put(new Variable(1), true);
                    put(new Variable(2), false);
                    put(new Variable(3), true);
                    put(new Variable(4), true);
                }}) == true);

        // X1=T x2=T x3=F x4=F
        System.out.println(eval(rootNode,
                new HashMap<Variable, Boolean>(){{
                    put(new Variable(1), true);
                    put(new Variable(2), true);
                    put(new Variable(3), false);
                    put(new Variable(4), false);
                }}) == false);

        // X1=T x2=T x3=F x4=T
        System.out.println(eval(rootNode,
                new HashMap<Variable, Boolean>(){{
                    put(new Variable(1), true);
                    put(new Variable(2), true);
                    put(new Variable(3), false);
                    put(new Variable(4), true);
                }}) == true);

        // X1=T x2=T x3=T x4=F
        System.out.println(eval(rootNode,
                new HashMap<Variable, Boolean>(){{
                    put(new Variable(1), true);
                    put(new Variable(2), true);
                    put(new Variable(3), true);
                    put(new Variable(4), false);
                }}) == false);

        // X1=T x2=T x3=T x4=T
        System.out.println(eval(rootNode,
                new HashMap<Variable, Boolean>(){{
                    put(new Variable(1), true);
                    put(new Variable(2), true);
                    put(new Variable(3), true);
                    put(new Variable(4), true);
                }}) == true);
    }

    public static void main(String[] args) {
        runTests();
    }
}