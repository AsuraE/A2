package source;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/** 
 * class Source - Handles the input character-by-character.
 * @version $Revision: 14 $  $Date: 2013-05-08 10:40:38 +1000 (Wed, 08 May 2013) $
 * To interface with JFlex this class has to extend java.io.Reader.
 */
public class Source extends java.io.Reader {

    /** Name of the input source file. */
    private String fileName;
    /** Buffered reader for input source file */
    private BufferedReader input;
    /** Provides the positions of the end of every line. */
    private LinePositions linePositions;
    /** Current position in the input source file. */
    private int currentPosn;

    public Source( String filename ) 
            throws java.io.IOException {
        this( new FileInputStream(filename), filename );
    }

    private Source( FileInputStream in, String inFile ) {
        input = new BufferedReader( new InputStreamReader( in ) );
        fileName = inFile;
        currentPosn = 0;
        linePositions = new LinePositions();
    }    
    public String getFileName() {
        return fileName;
    }
    /* Close input stream any flush out any error messages */
    public void close() throws IOException {
        input.close();
    }
    /** Get the line number containing pos. */
    public int getLineNumber( Position pos ) {
        return linePositions.getLineNumber( pos );
    }
    /** Get the position of the start of the line containing pos. */
    public Position getLineStart(Position pos) {
        return linePositions.getLineStart( pos );
    }
    /** Get the offset of pos from the start of the line containing it. */
    public int offset( Position pos ) {
        return linePositions.offset( pos );
    }
    /** Provides buffered read to JFlex.
     * getNextChar should be enough, but this is the interface JFlex wants.
     */
    public int read( char[] cbuf, int off, int len ) throws IOException {
        int nchars = input.read( cbuf, off, len );
        if( nchars < 0 ) {
            linePositions.add( new Position( currentPosn ) );
        } else {
            for( int i = 0; i < nchars; i++ ) {
                if( cbuf[off+i] == '\n' ) {
                    linePositions.add( new Position( currentPosn ) );
                }
                currentPosn++;
            }
        }
        return nchars;
    }
}
