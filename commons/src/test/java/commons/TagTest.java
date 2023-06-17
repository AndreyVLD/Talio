package commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TagTest {

    @Test
    void testConstructorBasic(){
        Tag tag = new Tag();
        assertNotNull(tag);
    }

    @Test
    void testConstructor(){
        Tag tag = new Tag("Work","Blue");
        assertEquals("Work",tag.name);
        assertEquals("Blue",tag.color);
    }
    @Test
    void testEquals() {
        Tag tag1 = new Tag("Work","Blue");
        Tag tag2 = new Tag("Work","Blue");
        assertEquals(tag1,tag2);
    }

    @Test
    void testHashCodeSame() {
        Tag tag1 = new Tag("Work","Blue");
        Tag tag2 = new Tag("Work","Blue");
        assertEquals(tag1.hashCode(),tag2.hashCode());
    }

    @Test
    void testToString() {
        Tag tag1 = new Tag("Work","Blue");
        assertTrue(tag1.toString().contains("color=Blue"));
        assertTrue(tag1.toString().contains("name=Work"));
    }

    @Test
    void testNotEquals() {
        Tag tag1 = new Tag("Work", "Blue");
        Tag tag2 = new Tag("Personal", "Green");
        assertNotEquals(tag1, tag2);
    }
}