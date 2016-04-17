package source;

/** 
 * class Position - Position in the source file.
 * @version $Revision: 14 $  $Date: 2013-05-08 10:40:38 +1000 (Wed, 08 May 2013) $
 * The index is the character position in the file, starting from zero.
 */
public class Position implements Comparable<Position> {
    
    public static final Position NO_POSITION = 
        new Position( Integer.MAX_VALUE );

    int index;
    
    public Position( int pos ) {
        this.index = pos;
    }
    /** Position are ordered by their indices. */
    public int compareTo( Position that ) {
        if( this.index < that.index ) {
            return -1;
        } else if( this.index == that.index ) {
            return 0;
        } else {
            return 1;
        }
    }
    public boolean equals( Position that ) {
        return this.index == that.index;
    }
    public int getIndex() {
        return index;
    }
    public int offset( Position start ) {
        return this.index - start.index;
    }
}
