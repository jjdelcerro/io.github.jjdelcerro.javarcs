package io.github.jjdelcerro.javarcs.lib.impl.core.model;

import java.util.Objects;

/**
 * Representa una entrada de acceso en un archivo RCS.
 */
public class RCSAccessEntry {
    private final String username;

    public RCSAccessEntry(String username) {
        this.username = Objects.requireNonNull(username, "Username cannot be null");
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RCSAccessEntry that = (RCSAccessEntry) o;
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return "RCSAccessEntry{username='" + username + "'}";
    }
}
