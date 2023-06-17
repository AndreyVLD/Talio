package commons;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;


import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

@Entity
public class Card implements Comparable<Card> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String title;
    public String description;
    public String color;
    public String createdBy;
    public Integer index;
    public Integer doneSubtasks;
    public Integer totalSubtasks;
    @Enumerated(EnumType.STRING)
    public Status status;

    @ManyToOne
    public TaskList taskList;

    @ManyToMany
    public Set<Tag> tags;

    /**
     * Dummy constructor for card
     */
    @SuppressWarnings("unused")
    public Card() {
    }

    /**
     * @param title Title of the card
     * @param description A description of the task
     * @param taskList The list it belongs to
     */
    public Card(String title, String description, TaskList taskList) {
        this.title = title;
        this.description = description;
        this.taskList = taskList;
        this.status = Status.ACTIVE;
        this.tags = new HashSet<>();
        this.doneSubtasks = 0;
        this.totalSubtasks = -1;
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
     * Compares this card to another one based on their indexes in the task list
     * Potentially throws IllegalArgumentException if the cards are not in the same list
     * or NullPointerException if the objects miss important attributes
     * @param card the other card
     * @return the result of the comparison
     */
    @Override
    public int compareTo(Card card) {
        if (card.status != Status.ACTIVE || this.status != Status.ACTIVE
            || card.taskList != this.taskList)
            throw new IllegalArgumentException();
        return Integer.compare(this.index, card.index);
    }
}