package commons;

public enum Status {
    ACTIVE,
    PLANNED,
    DONE,
    ARCHIVED,
    DELETED;

    /**
     * Get Status entry by name (case-insensitive)
     * @param name the name of the entry
     * @return the entry with that name or null if such does not exist
     */
    public static Status findByName(String name) {
        for (Status s : values()) {
            if (s.name().equalsIgnoreCase(name)) {
                return s;
            }
        }
        return null;
    }
}
