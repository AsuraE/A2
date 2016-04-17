package syms;

import machine.StackMachine;
import source.Position;
import syms.Type.FunctionType;
import syms.Type.ProductType;
import syms.Type.ScalarType;

/**
 * class Predefined - handles the predefined types and symbols.
 * @version $Revision: 14 $  $Date: 2013-05-08 10:40:38 +1000 (Wed, 08 May 2013) $
 */

public class Predefined {
    /** Predefined integer type. */
    public static ScalarType INTEGER_TYPE;
    /** Predefined boolean type. */
    public static ScalarType BOOLEAN_TYPE;
    public static Type.ProductType PAIR_INTEGER_TYPE;   
    public static Type.ProductType PAIR_BOOLEAN_TYPE;           
    public static Type.FunctionType ARITHMETIC_BINARY;
    public static Type.FunctionType INT_RELATIONAL_TYPE;    
    public static Type.FunctionType LOGICAL_BINARY;
    public static Type.FunctionType ARITHMETIC_UNARY;   
    public static Type.FunctionType LOGICAL_UNARY;
    
    public static void addPredefinedEntries( SymbolTable symtab ) {
        Scope predefined = symtab.getCurrentScope();
        // Define types needed for predefined entries
        /** Predefined integer type. */
        INTEGER_TYPE = new ScalarType( "int", StackMachine.SIZE_OF_INT, 
                        Integer.MIN_VALUE, Integer.MAX_VALUE ) { };
        /** Predefined boolean type. */
        BOOLEAN_TYPE = new ScalarType( "boolean", StackMachine.SIZE_OF_BOOLEAN, 
                    StackMachine.FALSE_VALUE, StackMachine.TRUE_VALUE ) {
            };
        PAIR_INTEGER_TYPE = new ProductType( INTEGER_TYPE, INTEGER_TYPE );                  
        PAIR_BOOLEAN_TYPE = new ProductType( BOOLEAN_TYPE, BOOLEAN_TYPE );
        ARITHMETIC_BINARY = new FunctionType( PAIR_INTEGER_TYPE, INTEGER_TYPE );
        INT_RELATIONAL_TYPE = new FunctionType(PAIR_INTEGER_TYPE, BOOLEAN_TYPE);
        LOGICAL_BINARY = new FunctionType( PAIR_BOOLEAN_TYPE, BOOLEAN_TYPE );
        ARITHMETIC_UNARY = new FunctionType( INTEGER_TYPE, INTEGER_TYPE );
        LOGICAL_UNARY = new FunctionType( BOOLEAN_TYPE, BOOLEAN_TYPE );
        // Add predefined symbols to predefined scope
        predefined.addType( "int", Position.NO_POSITION, INTEGER_TYPE );
        predefined.addType( "boolean", Position.NO_POSITION, BOOLEAN_TYPE );
        predefined.addConstant("false", Position.NO_POSITION, BOOLEAN_TYPE, 
                StackMachine.FALSE_VALUE );
        predefined.addConstant("true", Position.NO_POSITION, BOOLEAN_TYPE, 
                StackMachine.TRUE_VALUE );
        predefined.addOperator("-_", Position.NO_POSITION, ARITHMETIC_UNARY );
        predefined.addOperator("_+_", Position.NO_POSITION, ARITHMETIC_BINARY );
        predefined.addOperator("_-_", Position.NO_POSITION, ARITHMETIC_BINARY );
        predefined.addOperator("_*_", Position.NO_POSITION, ARITHMETIC_BINARY );
        predefined.addOperator("_/_", Position.NO_POSITION, ARITHMETIC_BINARY );
        predefined.addOperator("_=_", Position.NO_POSITION, INT_RELATIONAL_TYPE );
        predefined.addOperator("_=_", Position.NO_POSITION, LOGICAL_BINARY );
        predefined.addOperator("_!=_", Position.NO_POSITION, INT_RELATIONAL_TYPE );
        predefined.addOperator("_!=_", Position.NO_POSITION, LOGICAL_BINARY );
        predefined.addOperator("_>_", Position.NO_POSITION, INT_RELATIONAL_TYPE);
        predefined.addOperator("_<_", Position.NO_POSITION, INT_RELATIONAL_TYPE);
        predefined.addOperator("_>=_", Position.NO_POSITION, INT_RELATIONAL_TYPE);
        predefined.addOperator("_<=_", Position.NO_POSITION, INT_RELATIONAL_TYPE);
    }
}
