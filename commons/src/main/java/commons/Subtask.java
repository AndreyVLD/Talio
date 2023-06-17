package commons;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

@Entity
public class Subtask implements Comparable<Subtask> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String title;
    public Integer index;
    @Enumerated(EnumType.STRING)
    public Status status;

    @ManyToOne
    public Card card;

    /**
     * Dummy constructor for card
     */
    @SuppressWarnings("unused")
    public Subtask() {
    }

    /**
     * @param title Title of the card
     * @param card The card it belongs to
     */
    public Subtask(String title, Card card) {
        this.title = title;
        this.card = card;
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

    /**
     * Compares this subtask to another one based on their indexes in the nested task list
     * Potentially throws IllegalArgumentException if the subtasks are don't belong to the same card
     * or NullPointerException if the objects miss important attributes
     * @param subtask the other subtask
     * @return the result of the comparison
     */
    @Override
    public int compareTo(Subtask subtask) {
        if (subtask.status != Status.ACTIVE || this.status != Status.ACTIVE
            || subtask.card != this.card)
            throw new IllegalArgumentException();
        return Integer.compare(this.index, subtask.index);
    }
}