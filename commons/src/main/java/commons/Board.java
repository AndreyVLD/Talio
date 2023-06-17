package commons;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

@Entity
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String name;
    public Status status;     // to be determined if we use integer codes for status or strings
    public String password;
    public String color;    // to be determined if String or Color class

    /**
     * Dummy constructor for Person used by object mapper
     */
    @SuppressWarnings("unused")
    public Board(){

    }

    /**
     * Basic constructor for the Board
     * @param name - name of the board
     */

    public Board(String name){
        this.name = name;
        this.status = Status.ACTIVE;
    }

    /**
     * More advanced constructor for the Board
     * @param name - name of the board
     * @param password - password of the board
     */

    public Board(String name, String password){
        this(name);
        this.password=password;
    }

    /**
     * Checks if the 2 objects are equal
     * @param obj - another object compared to
     * @return - true if the 2 objects are equal
     */

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    /**
     * Generates the hashCode of this object
     * @return the hashCode
     */

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    /**
     * Returns a human-readable string representation of this object.
     * @return - a string
     */

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, MULTI_LINE_STYLE);
    }
}
