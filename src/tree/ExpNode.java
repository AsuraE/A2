package tree;

import java.util.Arrays;
import java.util.List;

import source.Position;
import syms.Predefined;
import syms.SymEntry;
import syms.Type;

/** 
 * class ExpNode - Abstract Syntax Tree representation of expressions.
 * @version $Revision: 22 $  $Date: 2014-05-20 15:14:36 +1000 (Tue, 20 May 2014) $
 * Abstract class representing expressions.
 * Actual expression nodes extend ExpNode.
 * All expression nodes have a position and a type.
 */
public abstract class ExpNode {
    /** Position in the source code of the expression */
    protected Position pos;
    /** Type of the expression (determined by static checker) */
    protected Type type;
    
    /** Constructor when type is known */
    protected ExpNode( Position pos, Type type) {
        this.pos = pos;
        this.type = type;
    }
    /** Constructor when type as yet unknown */
    protected ExpNode( Position pos ) {
        this( pos, Type.ERROR_TYPE );
    }
    public Type getType() {
        return type;
    }
    public void setType( Type type ) {
        this.type = type;
    }
    public Position getPosition() {
        return pos;
    }
    
    /** Each subclass of ExpNode must provide a transform method
     * to do type checking and transform the expression node to 
     * handle type coercions, etc.
     * @param visitor object that implements a traversal.
     * @return transformed expression node
     */
    public abstract ExpNode transform( ExpTransform<ExpNode> visitor );

    /** Each subclass of ExpNode must provide a genCode method
     * to visit the expression node to handle code generation.
     * @param visitor object that implements a traversal.
     * @return generated code
     */
    public abstract Code genCode( ExpTransform<Code> visitor );
        
    /** Tree node representing an erroneous expression. */
    public static class ErrorNode extends ExpNode {
        
        public ErrorNode( Position pos ) {
            super( pos, Type.ERROR_TYPE );
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitErrorExpNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitErrorExpNode( this );
        }
        @Override
        public String toString() {
            return "ErrorNode";
        }
    }

    /** Tree node representing a constant within an expression. */
    public static class ConstNode extends ExpNode {
        /** constant's value */
        private int value;

        public ConstNode( Position pos, Type type, int value ) {
            super( pos, type );
            this.value = value;
        }
        public int getValue() {
            return value;
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitConstNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitConstNode( this );
        }
        @Override
        public String toString( ) {
            return Integer.toString(value);
        }
    }

    /** Identifier node is used until the identifier can be resolved 
     * to be either a constant or a variable during the static 
     * semantics check phase. 
     */
    public static class IdentifierNode extends ExpNode {
        /** Name of the identifier */
        private String id;
        
        public IdentifierNode( Position pos, String id ) {
            super( pos );
            this.id = id;
        }
        public String getId() {
            return id;
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitIdentifierNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitIdentifierNode( this );
        }
        @Override
        public String toString() {
            return id;
        }
    }
    /** Tree node representing a variable. */
    public static class VariableNode extends ExpNode {
        /** Symbol table entry for the variable */
        protected SymEntry.VarEntry variable;
    
        public VariableNode( Position pos, SymEntry.VarEntry variable ) {
            super( pos, variable.getType() );
            this.variable = variable;
        }
        public SymEntry.VarEntry getVariable() {
            return variable;
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitVariableNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitVariableNode( this );
        }
        @Override
        public String toString( ) {
            return "VariableNode(" + variable + ")";
        }
    }
    /** Tree node representing a "read" expression. */
    public static class ReadNode extends ExpNode {

        public ReadNode( Position pos ) {
            super( pos, Predefined.INTEGER_TYPE );
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitReadNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitReadNode( this );
        }
        @Override
        public String toString( ) {
            return "Read";
        }
    }
    /** Tree node for an operator. */
    public static class OperatorNode extends ExpNode {
        /** Operator, e.g. binary or unary operator */
        private Operator op;
        /** Argument(s) for operator. If more than one argument then this is
         * an ArgumentsNode 
         */
        private ExpNode arg;
        
        public OperatorNode( Position pos, Operator op, ExpNode arg ) {
            super( pos );
            this.op = op;
            this.arg = arg;
        }
        public Operator getOp() {
            return op;
        }
        public ExpNode getArg() {
            return arg;
        }
        public void setArg( ExpNode arg ) {
            this.arg = arg;
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitOperatorNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitOperatorNode( this );
        }
        @Override
        public String toString() {
            return op.toString() + "(" + arg + ")";
        }
    }

    /** Tree node for a list of arguments */
    public static class ArgumentsNode extends ExpNode {
        /** List of arguments */
        private List<ExpNode> args;
        
        /** @requires args is non-empty */
        public ArgumentsNode( Type.ProductType t, List<ExpNode> args ) {
            super( args.get(0).getPosition(), t );
            this.args = args;
        }
        /** @requires args is non-empty */
        public ArgumentsNode( List<ExpNode> args ) {
            super( args.get(0).getPosition() );
            this.args = args;
        }
        /** @requires exps is non-empty */
        public ArgumentsNode( ExpNode... exps ) {
            this( Arrays.asList( exps ) );
        }
        public List<ExpNode> getArgs() {
            return args;
        }
        public void setArgs( List<ExpNode> args ) {
            this.args = args;
        }
        @Override
        public ExpNode transform(ExpTransform<ExpNode> visitor ) {
            return visitor.visitArgumentsNode( this );
        }
        @Override
        public Code genCode(ExpTransform<Code> visitor ) {
            return visitor.visitArgumentsNode( this );
        }
        @Override
        public String toString() {
            return args.toString();
        }
    }

    /** Tree node for dereferencing an LValue.
     * A Dereference node references an ExpNode node and represents the
     * dereferencing of the "address" given by the leftValue to give
     * the value at that address.
     */
    public static class DereferenceNode extends ExpNode {
        /** LValue to be dereferenced */
        private ExpNode leftValue;

        public DereferenceNode( Type type, ExpNode exp ) {
            super( exp.getPosition(), type );
            this.leftValue = exp;
        }
        public ExpNode getLeftValue() {
            return leftValue;
        }
        public void setLeftValue( ExpNode leftValue ) {
            this.leftValue = leftValue;
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitDereferenceNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitDereferenceNode( this );
        }
        @Override
        public String toString( ) {
            return "Dereference(" + leftValue + ")";
        }
    }

    /** Tree node representing a coercion that narrows a subrange */
    public static class NarrowSubrangeNode extends ExpNode {
        /** Expression to be narrowed */
        private ExpNode exp;

        /* @requires type instance of Type.SubrangeType */
        public NarrowSubrangeNode( Position pos, Type.SubrangeType type, 
                ExpNode exp )
        {
            super( pos, type );
            this.exp = exp;
        }
        public Type.SubrangeType getSubrangeType() {
            return (Type.SubrangeType)getType();
        }
        public ExpNode getExp() {
            return exp;
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitNarrowSubrangeNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitNarrowSubrangeNode( this );
        }
        @Override
        public String toString() {
            return "NarrowSubrange(" + exp + ":" + getType() + ")";
        }
    }
    
    /** Tree node representing a widening of a subrange */
    public static class WidenSubrangeNode extends ExpNode {
        /** Expression to be widened */
        private ExpNode exp;

        /* @requires exp.getType() instanceof Type.SubrangeType */
        public WidenSubrangeNode( Position pos, Type type, ExpNode exp ) {
            super( pos, type );
            assert exp.getType() instanceof Type.SubrangeType;
            this.exp = exp;
        }
        public ExpNode getExp() {
            return exp;
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitWidenSubrangeNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitWidenSubrangeNode( this );
        }
        @Override
        public String toString() {
            return "WidenSubrange(" + exp + ":" + getType() + ")";
        }
    }
}
