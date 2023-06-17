package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    String name1 = "Name 1";
    String name2 = "Name 2";
    Board a;
    Board b;

    @BeforeEach
    public void setup() {
        a = new Board(name1);
        b = new Board(name2);
    }
    @Test
    public void checkConstructor () {
        assertEquals("Name 1", a.name);
        a = new Board();
        assertNotNull(a);
        a = new Board(name1, "password");
        assertEquals("password", a.password);
    }
    @Test
    void testEquals() {
        assertEquals(a, a);
        assertNotEquals(a, b);
        b = new Board(name1);
        assertEquals(a, b);
    }

    @Test
    void testHashCode() {
        assertEquals(a.hashCode(), a.hashCode());
        assertNotEquals(a.hashCode(), b.hashCode());
        b = new Board(name1);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testToString() {
        var s = a.toString();
        assertTrue(s.contains("\n"));
        assertTrue(s.contains("Name 1"));
    }
}