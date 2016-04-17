package source;

/**
 * interface Errors - interface to allow reporting of compilation
 *      errors and other messages. Use flush() to cause output.
 * @version $Revision: 14 $  $Date: 2013-05-08 10:40:38 +1000 (Wed, 08 May 2013) $
 */
public interface Errors {

    /** Signal an error at the given position */
    public void error( String m, Position pos );

    /** Signal a fatal error at the given position */
    public void fatal( String m, Position pos );
    
    /** Output debugging message if debug turned on */
    public void debugMessage( String msg );
    
    /** Increment debug level for indenting messages */
    public void incDebug();
    
    /** Decrement debug level for indenting messages */
    public void decDebug();
    
    /** Report error is assert condition fails */
    public void checkAssert( boolean condition, String m, Position pos );
    
    /** Print immediately a summary of all errors reported */
    public void errorSummary();

    /** List impending error messages, and clear accumulated errors. */
    public void flush();

    /** Return whether any errors have been reported at all */
    public boolean hadErrors();
    
    /** Print line to output stream */
    public void println( String msg );
    
}
