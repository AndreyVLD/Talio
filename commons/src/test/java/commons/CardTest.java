package commons;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    Card a;
    Card b;
    String title1 = "Title 1";
    String title2 = "Title 2";
    String description1 = "Description 1";
    String description2 = "Description 2";
    TaskList taskList;
    Board board;

    @BeforeEach
    public void setup() {
        board = new Board("boardName");
        taskList = new TaskList("listName", board);
        a = new Card(title1, description1, taskList);
        b = new Card(title2, description2, taskList);
    }
    @Test
    public void checkConstructor () {
        a = new Card(title1, description2, taskList);
        assertEquals("Title 1", a.title);
        assertEquals("Description 2", a.description);
        assertEquals(taskList, a.taskList);
        assertNotNull(new Card());
    }
    @Test
    void testEquals() {
        assertEquals(a, a);
        assertNotEquals(a, b);
        b = new Card(title1, description1, taskList);
        assertEquals(a, b);
    }

    @Test
    void testHashCode() {
        assertEquals(a.hashCode(), a.hashCode());
        assertNotEquals(a.hashCode(), b.hashCode());
        b = new Card(title1, description1, taskList);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testToString() {
        var s = a.toString();
        assertTrue(s.contains("\n"));
        assertTrue(s.contains("Title 1"));
    }
}