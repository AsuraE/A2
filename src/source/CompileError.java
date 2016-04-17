package source;

/**
 * class CompilerError -  Represents a single error.
 * @version $Revision: 14 $  $Date: 2013-05-08 10:40:38 +1000 (Wed, 08 May 2013) $
 * An error can consist of 
 * - an error message string,
 * - the severity, and
 * - the position in the souce input of the error.
 * If no position can be assigned to the error then
 * Position.NO_POSITION is used.
 * @see pl0.source.Severity
 * @see pl0.source.Position
 */
public class CompileError implements Comparable<CompileError> {
    /** The error message */
    private String message;
    /** The error's severity */
    private Severity severity;
    /** The position in the input source, or NO_POSITION */
    private Position position;
    
    public CompileError( String message, Severity severity, Position pos ) {
        this.message = message;
        this.severity = severity;
        this.position = pos;
    }

    /** Ordering of errors is based on their position.
     * @see java.lang.Comparable#compareTo(T)
     */
    public int compareTo( CompileError that ) {
        return this.position.compareTo( that.position );
    }
    public Position getPosition() {
        return position;
    }
    public Severity getSeverity() {
        return severity;
    }
    public String toString() {
        return severity.toString() + ": " + message;
    }
}
