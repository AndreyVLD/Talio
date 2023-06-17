package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskListTest {

    TaskList a;
    TaskList b;
    String name1 = "Name 1";
    String name2 = "Name 2";
    Board board;

    @BeforeEach
    public void setup() {
        board = new Board("boardName");
        a = new TaskList(name1, board);
        b = new TaskList(name2, board);
    }
    @Test
    public void checkConstructor () {
        assertEquals("Name 1", a.name);
        a = new TaskList();
        assertNotNull(a);
    }
    @Test
    void testEquals() {
        assertEquals(a, a);
        assertNotEquals(a, b);
        b = new TaskList(name1, board);
        assertEquals(a, b);
    }

    @Test
    void testHashCode() {
        assertEquals(a.hashCode(), a.hashCode());
        assertNotEquals(a.hashCode(), b.hashCode());
        b = new TaskList(name1, board);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testToString() {
        var s = a.toString();
        assertTrue(s.contains("\n"));
        assertTrue(s.contains("Name 1"));
    }
}