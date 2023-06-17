package commons;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

@Entity
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String name;
    public String color;

    @Enumerated(EnumType.STRING)
    public Status status;

    @ManyToOne
    public Board board;


    /**
     * Dummy constructor for Tags used for Object Mapper
     */
    @SuppressWarnings("unused")
    public Tag() {
    }

    /**
     * Actual constructor for Tag
     * @param name -  name of the Tag
     * @param color -  the color of the Tag
     */

    public Tag(String name, String color) {
        this.name = name;
        this.color = color;
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
