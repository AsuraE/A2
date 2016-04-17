package source;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 
 * class LinePositions - tracks the positions of lines within text file.
 * @version $Revision: 14 $  $Date: 2013-05-08 10:40:38 +1000 (Wed, 08 May 2013) $
 */

public class LinePositions {

    private List<Position> lineEnds;
    
    LinePositions() {
        this.lineEnds = new ArrayList<Position>();
        this.lineEnds.add( new Position( -1 ) );
    }
    /** Add an end-of-line position.
     * @requires the new position greater than or equal to previous last position.
     */
    void add( Position p ) {
        assert endLast().compareTo( p ) <= 0;
        // Add line only if nonempty
        if( endLast().compareTo( p ) != 0 ) {
            lineEnds.add( p );
        }
    }
    /** Retrieve the line number on which the given position occurs.
     * @requires the position is not greater than the end of the last line.
     */
    int getLineNumber( Position p ) {
        if( endLast().compareTo( p ) < 0 ) {
            return lineEnds.size();
        }
        int index = Collections.binarySearch( lineEnds, p );
        if( 0 <= index ) {
            return index;
        } else {
            return -(index+1);
        }
    }
    /** Get the position of the start of the line that contains position p.
     */
    Position getLineStart( Position p ) {
        Position endPrevious = lineEnds.get( getLineNumber( p ) - 1 );
        return new Position( endPrevious.getIndex() + 1 );
    }
    /** Get the offset of position p from the start of the line on which
     * it occurs.
     */
    int offset( Position p ) {
        return p.offset( getLineStart( p ) );
    }
    /** Get the position of the end of the last line. */
    Position endLast() {
        return lineEnds.get( lineEnds.size() - 1 );
    }
}
