package source;

import junit.framework.TestCase;

/**
 * class LinePositionsTest - Junit test of LinePositions class.
 * @version $Revision: 14 $  $Date: 2013-05-08 10:40:38 +1000 (Wed, 08 May 2013) $
 */
public class LinePositionsTest extends TestCase {

    private LinePositions lp;
    
    public LinePositionsTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        lp = new LinePositions();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        lp = null;
    }

    /*
     * Test method for 'pl0.source.LinePositions.LinePositions()'
     */
    public void testLinePositions() {
        assertEquals( 0, lp.getLineNumber( new Position( -1 ) ) );
    }

    /*
     * Test method for 'pl0.source.LinePositions.add(Position)'
     */
    public void testAdd() {
        lp.add( new Position( 3 ) );
        assertEquals( 1, lp.getLineNumber( new Position( 0 ) ) );
        assertEquals( 1, lp.getLineNumber( new Position( 1 ) ) );
        assertEquals( 1, lp.getLineNumber( new Position( 2 ) ) );
        assertEquals( 1, lp.getLineNumber( new Position( 3 ) ) );
        lp.add( new Position( 5 ) );
        assertEquals( 1, lp.getLineNumber( new Position( 0 ) ) );
        assertEquals( 1, lp.getLineNumber( new Position( 3 ) ) );
        assertEquals( 2, lp.getLineNumber( new Position( 4 ) ) );
        assertEquals( 2, lp.getLineNumber( new Position( 5 ) ) );
        lp.add( new Position( 7 ) );
        assertEquals( 1, lp.getLineNumber( new Position( 0 ) ) );
        assertEquals( 1, lp.getLineNumber( new Position( 3 ) ) );
        assertEquals( 2, lp.getLineNumber( new Position( 5 ) ) );
        assertEquals( 3, lp.getLineNumber( new Position( 6 ) ) );
        assertEquals( 3, lp.getLineNumber( new Position( 7 ) ) );
    }

    /*
     * Test method for 'pl0.source.LinePositions.getLineStart(Position)'
     */
    public void testGetLineStart() {
        lp.add( new Position( 3 ) );
        assertEquals( 0, lp.getLineStart( new Position( 0 ) ).getIndex() );
        assertEquals( 0, lp.getLineStart( new Position( 1 ) ).getIndex() );
        assertEquals( 0, lp.getLineStart( new Position( 2 ) ).getIndex() );
        assertEquals( 0, lp.getLineStart( new Position( 3 ) ).getIndex() );
        lp.add( new Position( 5 ) );
        assertEquals( 0, lp.getLineStart( new Position( 3 ) ).getIndex() );
        assertEquals( 4, lp.getLineStart( new Position( 4 ) ).getIndex() );
        assertEquals( 4, lp.getLineStart( new Position( 5 ) ).getIndex() );
        lp.add( new Position( 7 ) );
        assertEquals( 0, lp.getLineStart( new Position( 3 ) ).getIndex() );
        assertEquals( 4, lp.getLineStart( new Position( 5 ) ).getIndex() );
        assertEquals( 6, lp.getLineStart( new Position( 6 ) ).getIndex() );
        assertEquals( 6, lp.getLineStart( new Position( 7 ) ).getIndex() );
    }

    /*
     * Test method for 'pl0.source.LinePositions.offset(Position)'
     */
    public void testOffset() {
        lp.add(new Position(3));
        assertEquals(0, lp.offset(new Position(0)));
        assertEquals(1, lp.offset(new Position(1)));
        assertEquals(2, lp.offset(new Position(2)));
        assertEquals(3, lp.offset(new Position(3)));
        lp.add(new Position(5));
        assertEquals(3, lp.offset(new Position(3)));
        assertEquals(0, lp.offset(new Position(4)));
        assertEquals(1, lp.offset(new Position(5)));
        lp.add(new Position(7));
        assertEquals(3, lp.offset(new Position(3)));
        assertEquals(1, lp.offset(new Position(5)));
        assertEquals(0, lp.offset(new Position(6)));
        assertEquals(1, lp.offset(new Position(7)));
    }

    /*
     * Test method for 'pl0.source.LinePositions.endLast()'
     */
    public void testEndLast() {
        assertEquals( -1, lp.endLast().getIndex() );
        lp.add( new Position( 3 ) );
        assertEquals( 3, lp.endLast().getIndex() );
        lp.add( new Position( 5 ) );
        assertEquals( 5, lp.endLast().getIndex() );
        lp.add( new Position( 7 ) );
        assertEquals( 7, lp.endLast().getIndex() );
    }

}
