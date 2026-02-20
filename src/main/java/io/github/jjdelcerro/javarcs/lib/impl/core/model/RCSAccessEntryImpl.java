package io.github.jjdelcerro.javarcs.lib.impl.core.model;

import io.github.jjdelcerro.javarcs.lib.RCSAccessEntry;
import java.util.Objects;

/**
 * Representa una entrada de acceso en un archivo RCS.
 */
public class RCSAccessEntryImpl implements RCSAccessEntry {
    private final String username;

    public RCSAccessEntryImpl(String username) {
        this.username = Objects.requireNonNull(username, "Username cannot be null");
    }

  @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RCSAccessEntryImpl that = (RCSAccessEntryImpl) o;
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
