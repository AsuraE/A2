package tree;
import java.util.List;
import java.util.ListIterator;

import machine.Operation;
import machine.StackMachine;
import source.Errors;
import syms.Scope;
import syms.SymEntry;
import syms.Type;
import tree.StatementNode.*;

/** class CodeGenerator implements code generation using the
 * visitor pattern to traverse the abstract syntax tree.
 * @version $Revision: 22 $  $Date: 2014-05-20 15:14:36 +1000 (Tue, 20 May 2014) $ 
 */
public class CodeGenerator implements DeclVisitor, StatementTransform<Code>,
                    ExpTransform<Code> {
    /** Current static level of nesting into procedures. */
    private int staticLevel;
    
    /** Table of code for each procedure */
    private Procedures procedures;
    
    /** Error message handler */
    private Errors errors;
    
    public CodeGenerator(Errors errors) {
        super();
        this.errors = errors;
    }

    /*-------------------- Main Method to start code generation --------*/

    /** Main generate code for this tree. */
    public Procedures generateCode( ProgramNode node ) {
        staticLevel = 1;        // Main program is at static level 1
        procedures = new Procedures();
        /* Generate the code for the main program and all procedures */
        Code mainCode = visitProgramNode( node );
        /* Add the dummy environment to the table of procedures */
        Scope mainScope = node.getBlock().getBlockLocals();
        /* Save code for main program */
        procedures.addProcedure( mainScope.getProcEntry(), mainCode );
        return procedures;
    }
    
    /* -------------------- Visitor methods ----------------------------*/
    
    /** Generate the code for the main program. */
    public Code visitProgramNode( ProgramNode node ) {
        Code code = new Code();
        /* Generate the prefix code main stack frame.
         * Place dummy static and dynamic links on stack.
         * The stack machine begins execution with the frame pointer
         * equal to the stack pointer (both 0).
         * Hence the first value pushed is at the location
         * addressed by the frame pointer (fp). */
        code.generateOp( Operation.ZERO );  /* Push dummy static link */
        code.generateOp( Operation.ZERO );  /* Push dummy dynamic link */
        /* place return address from main program on stack:
         * a return address of 0 will terminate stack machine execution. */
        code.generateOp( Operation.ZERO );
        /* generate code for body of program */
        code.append( visitBlockNode( node.getBlock() ) );
        return code;
    }

    /** Generate code for a block. */
    public Code visitBlockNode( BlockNode node ) {
        /** Generate code to allocate space for local variables on
         * procedure entry.
         */
        Code code = new Code();
        code.genAllocStack( node.getBlockLocals().getVariableSpace() );
        /* Generate the code for the body */
        code.append( node.getBody().genCode( this ) );
        code.generateOp( Operation.RETURN );
        /** Generate code for local procedures. */
        /* Static level is one greater for the procedures. */
        staticLevel++;
        node.getProcedures().accept(this);
        staticLevel--;
        return code;
    }

    /** Code generation for a declaration list */
    public void visitDeclListNode( DeclNode.DeclListNode node ) {
        for( DeclNode decl : node.getDeclarations() ) {
            decl.accept( this );
        }
    }

    /** Generate code for a single procedure. */
    public void visitProcedureNode( DeclNode.ProcedureNode node ) {
        // Generate code for the block
        Code code = visitBlockNode( node.getBlock() );
        procedures.addProcedure( node.getProcEntry(), code );
    }
    /*************************************************
     *  Statement node code generation visit methods
     *************************************************/
    /** Code generation for an erroneous statement should not be attempted. */
    public Code visitStatementErrorNode( StatementNode.ErrorNode node ) {
        errors.fatal( "PL0 Internal error: generateCode for Statement Error Node",
                node.getPosition() );
        return null;
    }

    /** Code generation for an assignment statement. */
    public Code visitAssignmentNode(StatementNode.AssignmentNode node) {
        /* Generate code to evaluate the expression */
        Code code = node.getExp().genCode( this );
        /* Generate the code to load the address of the variable */
        code.append( node.getVariable().genCode( this ) );
        /* Generate the store based on the type/size of value */
        code.append( genStore( 
                (Type.ReferenceType)node.getVariable().getType() ) );
        return code;
    }
    /** Generate a store instruction based on the size of values of the type */
    private Code genStore( Type.ReferenceType refType ) {
        Code code = new Code();
        int size = refType.getBaseType().getSpace();
        if (size == 1) {
            /* For an expression that can fit in a single word,
             *  store that into the variable.
             */
            code.generateOp( Operation.STORE_FRAME );
        } else {
            /* For the assignment of one multi-word variable to another 
             * generate a STORE_MULTI instruction to store the entire value.
             */
            code.genLoadConstant(size);
            code.generateOp(Operation.STORE_MULTI);
        }
        return code;
    }
    /** Generate code for a "write" statement. */
    public Code visitWriteNode( StatementNode.WriteNode node ) {
        Code code = node.getExp().genCode( this );
        code.generateOp( Operation.WRITE );
        return code;
    }
    /** Generate code for a "call" statement. */
    public Code visitCallNode( StatementNode.CallNode node ) {
        SymEntry.ProcedureEntry proc = node.getEntry();
        Code code = new Code();
        /* Generate the call instruction. The second parameter is the
         * procedure's symbol table entry. The actual address is resolved 
         * at load time.
         */
        code.genCall( staticLevel - proc.getLevel(), proc );
        return code;
    }
    /** Generate code for a statement list */
    public Code visitStatementListNode( StatementNode.ListNode node ) {
        Code code = new Code();
        for( StatementNode s : node.getStatements() ) {
            code.append( s.genCode( this ) );
        }
        return code;
    }

    /** Generate code for an "if" statement. */
    public Code visitIfNode(StatementNode.IfNode node) {
        /* Generate code to evaluate the condition and then and else parts */
        Code code = node.getCondition().genCode( this );
        Code thenCode = node.getThenStmt().genCode( this );
        Code elseCode = node.getElseStmt().genCode( this );
        /* Append a branch over then part code */
        code.genJumpIfFalse( thenCode.size() + Code.SIZE_JUMP_ALWAYS );
        /* Next append the code for the then part */
        code.append( thenCode );
        /* Append branch over the else part */
        code.genJumpAlways( elseCode.size() );
        /* Finally append the code for the else part */
        code.append( elseCode );
        return code;
    }
 
    /** Generate code for a "while" statement. */
    public Code visitWhileNode(StatementNode.WhileNode node) {
        /* Generate the code to evaluate the condition. */
        Code code = node.getCondition().genCode( this );
        /* Generate the code for the loop body */
        Code bodyCode = node.getLoopStmt().genCode( this );
        /* Add a branch over the loop body on false.
         * The offset is the size of the loop body code plus 
         * the size of the branch to follow the body.
         */
        code.genJumpIfFalse( bodyCode.size() + Code.SIZE_JUMP_ALWAYS );
        /* Append the code for the body */
        code.append( bodyCode );
        /* Add a branch back to the condition.
         * The offset is the total size of the current code plus the
         * size of a Jump Always (being generated).
         */
        code.genJumpAlways( -(code.size() + Code.SIZE_JUMP_ALWAYS) );
        return code;
    }
    
    /** Generate code for a "for" statement */
	public Code visitForNode(ForNode node) {
		/* New empty codes */
		Code code = new Code();
		Code conditionCode = new Code();
		Code bodyCode = new Code();
		/* Generate the code to evaluate the start condition */
		code.append( node.getCondition1().genCode( this ) );
		/* Generate the code to store the control variable at the start condition */
		code.append( node.getControlVariable().genCode( this ) );
		code.append( genStore( (Type.ReferenceType)node.getControlVariable().getType() ) );
		/* Generate the code to evaluate the end condition */
		code.append( node.getCondition2().genCode( this ) );
		/* Generate code for comparison */
		/* Load control variable */
		conditionCode.append( node.getControlVariable().genCode( this ) );
		conditionCode.append( genLoad( (Type.ReferenceType)node.getControlVariable().getType() ) );
		/* Evaluate end condition */
		conditionCode.append( node.getCondition2().genCode( this ) );
		/* Generate less operation */
		conditionCode.generateOp( Operation.LESS );
		
		conditionCode.generateOp( Operation.DUP );
		conditionCode.generateOp( Operation.WRITE);
		
		bodyCode.append( node.getControlVariable().genCode( this ) );
		bodyCode.append( genLoad( (Type.ReferenceType)node.getControlVariable().getType() ) );
		bodyCode.generateOp( Operation.WRITE);
		
		/* Generate the code for the loop body */
		bodyCode.append( node.getLoopStmt().genCode( this ) );
		
		/* Increment the control variable */
		bodyCode.append( node.getControlVariable().genCode( this ) );
		bodyCode.append( genLoad( (Type.ReferenceType)node.getControlVariable().getType() ) );
		bodyCode.genLoadConstant( 1 );
		bodyCode.generateOp( Operation.ADD );		
		/* Store the incremented value */
		bodyCode.append( node.getControlVariable().genCode( this ) );
		bodyCode.append( genStore( (Type.ReferenceType)node.getControlVariable().getType() ) );	
		
		bodyCode.append( node.getControlVariable().genCode( this ) );
		bodyCode.append( genLoad( (Type.ReferenceType)node.getControlVariable().getType() ) );
		bodyCode.generateOp( Operation.WRITE);
		
		/* Append the condition code */
		code.append( conditionCode );
		/* Add a branch over the loop body if false */
		code.genJumpIfFalse( bodyCode.size() + Code.SIZE_JUMP_ALWAYS );
		/* Append the body code to the main code */
		code.append( bodyCode );
		/* Jump back to start of code execution */
		code.genJumpAlways( -( bodyCode.size() + Code.SIZE_JUMP_IF_FALSE + Code.SIZE_JUMP_ALWAYS + conditionCode.size() ) );
		return code;
	}
    /*************************************************
     *  Expression node code generation visit methods
     *************************************************/
    /** Code generation for an erroneous expression should not be attempted. */
    public Code visitErrorExpNode( ExpNode.ErrorNode node ) { 
        errors.fatal( "PL0 Internal error: generateCode for ErrorExpNode",
                node.getPosition() );
        return null;
    }

    /** Generate code for a constant expression. */
    public Code visitConstNode( ExpNode.ConstNode node ) {
        Code code = new Code();
        if( node.getValue() == 0 ) {
            code.generateOp( Operation.ZERO );
        } else if( node.getValue() == 1 ) {
            code.generateOp( Operation.ONE );
        } else {
            code.genLoadConstant( node.getValue() );
        }
        return code;
    }

    /** Generate code for a "read" expression. */
    public Code visitReadNode( ExpNode.ReadNode node ) {
        Code code = new Code();
        code.generateOp( Operation.READ );
        return code;
    }
    
    /** Generate code for a binary expression. */
    public Code visitOperatorNode( ExpNode.OperatorNode node ) {
        Code code;
        ExpNode args = node.getArg();
        switch ( node.getOp() ) {
        case ADD_OP:
            code = args.genCode( this );
            code.generateOp(Operation.ADD);
            break;
        case SUB_OP:
            code = args.genCode( this );
            code.generateOp(Operation.NEGATE);
            code.generateOp(Operation.ADD);
            break;
        case MUL_OP:
            code = args.genCode( this );
            code.generateOp(Operation.MPY);
            break;
        case DIV_OP:
            code = args.genCode( this );
            code.generateOp(Operation.DIV);
            break;
        case EQUALS_OP:
            code = args.genCode( this );
            code.generateOp(Operation.EQUAL);
            break;
        case LESS_OP:
            code = args.genCode( this );
            code.generateOp(Operation.LESS);
            break;
        case NEQUALS_OP:
            code = args.genCode( this );
            code.generateOp(Operation.EQUAL);
            code.genBoolNot();
            break;
        case LEQUALS_OP:
            code = args.genCode( this );
            code.generateOp(Operation.LESSEQ);
            break;
        case GREATER_OP:
            /* Generate argument values in reverse order and use LESS */
            code = genArgsInReverse( (ExpNode.ArgumentsNode)args );
            code.generateOp(Operation.LESS);
            break;
        case GEQUALS_OP:
            /* Generate argument values in reverse order and use LESSEQ */
            code = genArgsInReverse( (ExpNode.ArgumentsNode)args );
            code.generateOp(Operation.LESSEQ);
            break;
        case NEG_OP:
            code = args.genCode( this );
            code.generateOp(Operation.NEGATE);
            break;
        default:
            errors.fatal("PL0 Internal error: Unknown operator",
                    node.getPosition() );
            code = null;
        }
        return code;
    }

    /** Generate the code to load arguments (in order) */
    public Code visitArgumentsNode( ExpNode.ArgumentsNode node ) {
        Code code = new Code();
        for( ExpNode exp : node.getArgs() ) {
            code.append( exp.genCode( this ) );
        }
        return code;
    }
    /** Generate operator operands in reverse order */
    private Code genArgsInReverse( ExpNode.ArgumentsNode args ) {
        List<ExpNode> argList = args.getArgs();
        Code code = new Code();
        for( int i = argList.size()-1; 0 <= i; i-- ) {
            code.append( argList.get(i).genCode( this ) );
        }
        return code;
    }
    /** Generate code to dereference an RValue. */
    public Code visitDereferenceNode( ExpNode.DereferenceNode node ) {
        Code code = node.getLeftValue().genCode( this );
        code.append( genLoad( node.getType() ) );
        return code;
    }
    /** Generate the load instruction depending on size */
    private Code genLoad( Type type ) {
        Code code = new Code();
        if( type.getSpace() == 1 ) {
            /* A single word value is loaded with LOAD_FRAME */
            code.generateOp( Operation.LOAD_FRAME );
        } else {
            /* A multi-word value is loaded with LOAD_MULTI */
            code.genLoadConstant( type.getSpace() );
            code.generateOp( Operation.LOAD_MULTI );
        }
        return code;
    }

    /** Generate code for an identifier. */
    public Code visitIdentifierNode(ExpNode.IdentifierNode node) {
        /** Visit the corresponding constant or variable node. */
        errors.fatal("Internal error: code generator called on IdentifierNode",
                node.getPosition() );
        return null;
    }
    /** Generate code for a variable (Exp) reference. */
    public Code visitVariableNode( ExpNode.VariableNode node ) {
        SymEntry.VarEntry var = node.getVariable();
        Code code = new Code();
        code.genMemRef( staticLevel - var.getLevel(), var.getOffset() );
        return code;
    }
    /** Generate code to perform a bounds check on a subrange. */
    public Code visitNarrowSubrangeNode(ExpNode.NarrowSubrangeNode node) {
        Code code = node.getExp().genCode( this );
        code.genBoundsCheck(node.getSubrangeType().getLower(), 
                node.getSubrangeType().getUpper());
        return code;
    }

    /** Generate code to widen a subrange to an integer. */
    public Code visitWidenSubrangeNode(ExpNode.WidenSubrangeNode node) {
        // Widening doesn't require anything extra
        return node.getExp().genCode( this );
    }
    
	/** Generate code for an array node */
	public Code visitArrayNode(ExpNode.ArrayNode node) {
		// TODO
		return null;
	}
}
