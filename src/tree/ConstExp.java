package tree;

import source.ErrorHandler;
import source.Errors;
import source.Position;
import syms.Predefined;
import syms.SymEntry;
import syms.Scope;
import syms.Type;

/** Constant expressions tree structures used to evaluate
 * compile-time constant expressions.
 * @version $Revision: 22 $  $Date: 2014-05-20 15:14:36 +1000 (Tue, 20 May 2014) $
 */
public abstract class ConstExp {
    /** Position in the source code of the expression */
    protected Position pos;
    /** Status of constant for resolving references */
    private enum Status{ Unresolved, Resolving, Resolved } 
    /** Resolved if expression has been evaluated */
    protected Status status;
    /** Scope in which expression should be evaluated */
    protected Scope scope;
    /** Type of the expression */
    protected Type type;
    /** Value of the expression */
    protected int value;
    /** Error handler */
    protected Errors errors = ErrorHandler.getErrorHandler();
    /** Constructor used by subclass constructors only */
    protected ConstExp( Position pos, Status status, Scope scope, 
            Type type, int value ) {
        this.pos = pos;
        this.status = status;
        this.scope = scope;
        this.type = type;
        this.value = value;
    }
    /** Constructor for an initially unevaluated expression */
    protected ConstExp( Position pos, Status status, Scope scope ) {
        /* Defaults to error type and silly value */
        this(pos, status, scope, Type.ERROR_TYPE, 0x80808080 );
    }
    public Type getType() {
        if( status == Status.Unresolved ) {
            evaluate();
        }
        return type;
    }
    public int getValue() {
        if( status == Status.Unresolved ) {
            evaluate();
        }
        return value;
    }

    /** Evaluate a constant and store value and type.
     * Overridden in sub-types where necessary. */
    protected void evaluate() {
        // Default for expression that do not need evaluating.
    }

    /** For handling erroneous constant expressions */
    public static class ErrorNode extends ConstExp {
        
        public ErrorNode( Position pos, Scope scope ) {
            super( pos, Status.Resolved, scope );
        }
    }
    /** A constant expression consisting of a number */
    public static class NumberNode extends ConstExp {
        
        public NumberNode(Position pos, Scope scope, Type type, int value) {
            super( pos, Status.Resolved, scope, type, value );
        }
    }
    /** A constant expression consisting of a negated constant expression */
    public static class NegateNode extends ConstExp {
        private ConstExp subExp;
        
        public NegateNode( Position pos, Scope scope, ConstExp subExp ) {
            super( pos, Status.Unresolved, scope );
            this.subExp = subExp;
        }
        @Override
        protected void evaluate() {
            type = subExp.getType();
            if( type != Predefined.INTEGER_TYPE ){
                errors.error( "can only negate an integer", pos );
            } else {
                value = -subExp.getValue();
            }
            status = Status.Resolved;
        }
    }
    /** A constant expression consisting of a reference to an identifier */
    public static class ConstIdNode extends ConstExp {
        private String id;
        
        public ConstIdNode( Position pos, Scope scope, String id ) {
            super( pos, Status.Unresolved, scope );
            this.id = id;
        }
        /** In evaluating a constant expression consisting of an identifier
         * we need to be careful in case it is circularly defined.
         * To handle this we set its status to Resolving while its value 
         * is being calculated, so that if during the calculation we try
         * to calculate the value of the same identifier again, we report an
         * error. 
         */
        @Override
        protected void evaluate() {
//          System.out.println( " ConstExp Resolving " + id + " " + status );
            switch( status ) {
            case Unresolved:
                status = Status.Resolving;
                SymEntry entry = scope.lookup( id );
                if( entry != null && entry instanceof SymEntry.ConstantEntry ) {
                    SymEntry.ConstantEntry constEntry =
                        (SymEntry.ConstantEntry)entry;
                    type = constEntry.getType();
                    value = constEntry.getValue();
                    status = Status.Resolved;
                } else {
                    errors.error( "Constant identifier expected", pos );
                }
                break;
            case Resolving:
                errors.error( id + " is circularly defined", pos );
                /* Will resolve to error type and silly value.
                 * Set to Resolved to avoid repeated attempts to
                 * resolve the unresolvable, and hence avoid
                 * unnecessary repeated error messages. */
                status = Status.Resolved;
                break;
            case Resolved:
                /* Already resolved */
                break;
            }
        }
    }       
}
