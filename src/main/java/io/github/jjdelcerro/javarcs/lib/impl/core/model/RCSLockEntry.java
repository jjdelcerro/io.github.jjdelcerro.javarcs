package io.github.jjdelcerro.javarcs.lib.impl.core.model;

import java.util.Objects;

/**
 * Representa una entrada de bloqueo en un archivo RCS.
 */
public class RCSLockEntry {
    private final String username;
    private final RCSRevisionNumber revisionNumber;

    public RCSLockEntry(String username, RCSRevisionNumber revisionNumber) {
        this.username = Objects.requireNonNull(username, "Username cannot be null");
        this.revisionNumber = Objects.requireNonNull(revisionNumber, "Revision number cannot be null");
    }

    public String getUsername() {
        return username;
    }

    public RCSRevisionNumber getRevisionNumber() {
        return revisionNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RCSLockEntry that = (RCSLockEntry) o;
        return Objects.equals(username, that.username) && Objects.equals(revisionNumber, that.revisionNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, revisionNumber);
    }

    @Override
    public String toString() {
        return "RCSLockEntry{username='" + username + "', revisionNumber=" + revisionNumber + '}';
    }
}
