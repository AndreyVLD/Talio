package commons;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

@Entity
public class TaskList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String name;

    @Enumerated(EnumType.STRING)
    public Status status;

    public String color;    // to be determined if String or Color class
    public Integer index;

    @ManyToOne
    public Board board;


    /**
     * Dummy constructor for Person used by object mapper
     */
    @SuppressWarnings("unused")
    public TaskList(){

    }

    /**
     * Basic constructor for the List
     * @param name - name of the list.
     * @param board - the board that contains this List.
     */
    public TaskList(String name, Board board){
        this.name = name;
        this.board = board;
        this.status = Status.ACTIVE;
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
