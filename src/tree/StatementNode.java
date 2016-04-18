package tree;

import java.util.ArrayList;
import java.util.List;

import syms.SymbolTable;
import syms.Scope;
import source.Position;
import syms.SymEntry;

/** 
 * class StatementNode - Abstract syntax tree representation of statements. 
 * @version $Revision: 22 $  $Date: 2014-05-20 15:14:36 +1000 (Tue, 20 May 2014) $
 * Classes defined within StatementNode extend it.
 */
public abstract class StatementNode {
    /** Position in the input source program (line and column number effectively).
     * All statements have a position within the original source code.
     */
    private Position pos;

    /** Constructor */
    protected StatementNode( Position pos ) {
        this.pos = pos;
    }
    public Position getPosition() {
        return pos;
    }
    
    /** All statement nodes provide an accept method to implement the visitor
     * pattern to traverse the tree.
     * @param visitor class implementing the details of the particular
     *  traversal.
     */
    public abstract void accept( StatementVisitor visitor );
    
    /** All statement nodes provide a genCode method to implement the visitor
     * pattern to traverse the tree for code generation.
     * @param visitor class implementing the code generation
     */
    public abstract Code genCode( StatementTransform<Code> visitor );
    
    /** Debugging output of a statement at an indent level */
    public abstract String toString( int level );
    
    /** Debugging output at level 0 */
    @Override
    public String toString() {
        return this.toString(0);
    }
    
    /** Returns a string with a newline followed by spaces of length 2n. */
    public static String newLine( int n ) {
       String indent = "\n";
       while( n > 0) {
           indent += "  ";
           n--;
       }
       return indent;
    }
    
    /** Tree node representing the main program. */
    public static class ProgramNode extends StatementNode {
        private SymbolTable baseSymbolTable;
        private BlockNode mainProc;

        public ProgramNode( Position pos, SymbolTable baseSyms, 
                BlockNode mainProc ) {
            super( pos );
            this.baseSymbolTable = baseSyms;
            this.mainProc = mainProc;
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitProgramNode( this );
        }
        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitProgramNode( this );
        }
        public SymbolTable getBaseSymbolTable() {
            return baseSymbolTable;
        }
        public BlockNode getBlock() {
            return mainProc;
        }
        @Override
        public String toString( int level ) {
            return mainProc.toString(level);
        }
    }

    /** Node representing a Block consisting of declarations and
     * body of a procedure, function, or the main program. */
    public static class BlockNode extends StatementNode {
        protected DeclNode.DeclListNode procedures;
        protected StatementNode body;
        protected Scope blockLocals;

        /** Constructor for a block within a procedure */
        public BlockNode( Position pos, DeclNode.DeclListNode procedures, 
                StatementNode body) {
            super( pos );
            this.procedures = procedures;
            this.body = body;
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitBlockNode( this );
        }
        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitBlockNode( this );
        }

        public DeclNode.DeclListNode getProcedures() {
            return procedures;
        }
        public StatementNode getBody() {
            return body;
        }
        public Scope getBlockLocals() {
            return blockLocals;
        }
        public void setBlockLocals( Scope blockLocals ) {
            this.blockLocals = blockLocals;
        }
        @Override
        public String toString( int level ) {
            return getProcedures().toString(level+1) + 
                    newLine(level) + "BEGIN" + 
                    newLine(level+1) + body.toString(level+1) +
                    newLine(level) + "END";
        }
    }

    /** Statement node representing an erroneous statement. */
    public static class ErrorNode extends StatementNode {
        public ErrorNode( Position pos ) {
            super( pos );
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitStatementErrorNode( this );
        }
        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitStatementErrorNode( this );
        }
        @Override
        public String toString( int level) {
            return "ERROR";
        }
    }

    /** Tree node representing an assignment statement. */
    public static class AssignmentNode extends StatementNode {
        /** Tree node for expression on left hand side of an assignment. */
        private ExpNode lValue;
        /** Tree node for the expression to be assigned. */
        private ExpNode exp;

        public AssignmentNode( Position pos, ExpNode variable, ExpNode exp ) {
            super( pos );
            this.lValue = variable;
            this.exp = exp;
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitAssignmentNode( this );
        }
        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitAssignmentNode( this );
        }
        public ExpNode getVariable() {
            return lValue;
        }
        public void setVariable( ExpNode variable ) {
            this.lValue = variable;
        }
        public ExpNode getExp() {
            return exp;
        }
        public void setExp(ExpNode exp) {
            this.exp = exp;
        }
        public String getVariableName() {
            if( lValue instanceof ExpNode.VariableNode ) {
                return 
                    ((ExpNode.VariableNode)lValue).getVariable().getIdent();
            } else {
                return "<noname>";
            }
        }
        @Override
        public String toString( int level ) {
            return lValue.toString() + " := " + exp.toString();
        }
    }
    /** Tree node representing a "write" statement. */
    public static class WriteNode extends StatementNode {
        private ExpNode exp;

        public WriteNode( Position pos, ExpNode exp ) {
            super( pos );
            this.exp = exp;
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitWriteNode( this );
        }
        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitWriteNode( this );
        }
        public ExpNode getExp() {
            return exp;
        }
        public void setExp( ExpNode exp ) {
            this.exp = exp;
        }
        @Override
        public String toString( int level ) {
            return "WRITE " + exp.toString();
        }
    }
    
    /** Tree node representing a "call" statement. */
    public static class CallNode extends StatementNode {
        private String id;
        private SymEntry.ProcedureEntry procEntry;

        public CallNode( Position pos, String id ) {
            super( pos );
            this.id = id;
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitCallNode( this );
        }
        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitCallNode( this );
        }
        public String getId() {
            return id;
        }
        public SymEntry.ProcedureEntry getEntry() {
            return procEntry;
        }
        public void setEntry(SymEntry.ProcedureEntry entry) {
            this.procEntry = entry;
        }
        @Override
        public String toString( int level ) {
            String s = "CALL " + procEntry.getIdent() + "(";
            return s + ")";
        }
    }
    /** Tree node representing a statement list. */
    public static class ListNode extends StatementNode {
        private List<StatementNode> statements;
        
        public ListNode( Position pos ) {
            super( pos );
            this.statements = new ArrayList<StatementNode>();
        }
        public void addStatement( StatementNode s ) {
            statements.add( s );
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitStatementListNode( this );
        }
        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitStatementListNode( this );
        }
        public List<StatementNode> getStatements() {
            return statements;
        }
        @Override
        public String toString( int level) {
            String result = "";
            String sep = "";
            for( StatementNode s : statements ) {
                result += sep + s.toString( level );
                sep = ";" + newLine(level);
            }
            return result;
        }
    }
    /** Tree node representing an "if" statement. */
    public static class IfNode extends StatementNode {
        private ExpNode condition;
        private StatementNode thenStmt;
        private StatementNode elseStmt;

        public IfNode( Position pos, ExpNode condition, 
                StatementNode thenStmt, StatementNode elseStmt ) {
            super( pos );
            this.condition = condition;
            this.thenStmt = thenStmt;
            this.elseStmt = elseStmt;
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitIfNode( this );
        }
        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitIfNode( this );
        }
        public ExpNode getCondition() {
            return condition;
        }
        public void setCondition( ExpNode cond ) {
            this.condition = cond;
        }
        public StatementNode getThenStmt() {
            return thenStmt;
        }
        public StatementNode getElseStmt() {
            return elseStmt;
        }
        @Override
        public String toString( int level ) {
            return "IF " + condition.toString() + " THEN" + 
                        newLine(level+1) + thenStmt.toString( level+1 ) + 
                    newLine( level ) + "ELSE" + 
                        newLine(level+1) + elseStmt.toString( level+1 );
        }
    }

    /** Tree node representing a "while" statement. */
    public static class WhileNode extends StatementNode {
        private ExpNode condition;
        private StatementNode loopStmt;

        public WhileNode( Position pos, ExpNode condition, 
              StatementNode loopStmt ) {
            super( pos );
            this.condition = condition;
            this.loopStmt = loopStmt;
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitWhileNode( this );
        }
        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitWhileNode( this );
        }
        public ExpNode getCondition() {
            return condition;
        }
        public void setCondition( ExpNode cond ) {
            this.condition = cond;
        }
        public StatementNode getLoopStmt() {
            return loopStmt;
        }
        @Override
        public String toString( int level ) {
            return "WHILE " + condition.toString() + " DO" +
                newLine(level+1) + loopStmt.toString( level+1 );
        }
    }
    
    /** Tree node representing a "for" statement. */
    public static class ForNode extends StatementNode {
    	private ExpNode controlVariable;
    	private ExpNode c1;
    	private ExpNode c2;
    	private StatementNode sl;
    	
    	public ForNode( Position pos, ExpNode id, ExpNode c1, ExpNode c2, StatementNode sl ) {
    		super( pos );
    		this.controlVariable = id;
    		this.c1 = c1;
    		this.c2 = c2;
    		this.sl = sl;
    	}
		@Override
		public void accept(StatementVisitor visitor) {
			visitor.visitForNode( this );
		}
		@Override
		public Code genCode(StatementTransform<Code> visitor) {
			return visitor.visitForNode( this );
		}
		public ExpNode getCondition1() {
			return c1;
		}
		public ExpNode getCondition2() {
			return c2;
		}
		public void setCondition1( ExpNode c1 ) {
			this.c1 = c1;
		}
		public void setCondition2( ExpNode c2 ) {
			this.c2 = c2;
		}
		public StatementNode getLoopStmt() {
			return sl;
		}
		public ExpNode getControlVariable() {
			return controlVariable;
		}
		public void setControlVariable( ExpNode controlVariable ) {
			this.controlVariable = controlVariable;
		}
		@Override
		public String toString(int level) {
			return "";
		}
    }
}

