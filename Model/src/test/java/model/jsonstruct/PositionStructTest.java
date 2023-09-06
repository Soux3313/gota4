package model.jsonstruct;

import model.util.Position;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
/**
 * This class tests all methods of class PositionStruct
 */
public class PositionStructTest {

    /**
     * This method tests fromModel() in PositionStruct-class with an example
     */
    @Test
    public void fromModelTest(){
        // creating position and turning it into positionStruct
        Position pos = new Position(0, 1);
        PositionStruct posStruct = PositionStruct.fromModel(pos);
        // check values
        assertEquals(posStruct.column, Integer.valueOf(1));
        assertEquals(posStruct.row, Integer.valueOf(0));
    }

    /**
     * This method tests intoModel() in PositionStruct-class with an example
     * assuming fromModel() works
     */
    @Test
    public void intoModelTest(){
        // creating PositionStruct in order to reverse it
        Position pos = new Position(0, 1);
        PositionStruct posStruct = PositionStruct.fromModel(pos);
        // check values
        Position newPos = posStruct.intoModel();
        assertEquals(newPos.getColumn(), 1);
        assertEquals(newPos.getRow(), 0);
    }
}
