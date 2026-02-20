package io.github.jjdelcerro.javarcs.lib.impl.core.model;

import java.util.Objects;

/**
 * Representa una entrada de símbolo en un archivo RCS.
 */
public class RCSSymbolEntry {
    private final String name;
    private final RCSRevisionNumber revisionNumber;

    public RCSSymbolEntry(String name, RCSRevisionNumber revisionNumber) {
        this.name = Objects.requireNonNull(name, "Symbol name cannot be null");
        this.revisionNumber = Objects.requireNonNull(revisionNumber, "Revision number cannot be null");
    }

    public String getName() {
        return name;
    }

    public RCSRevisionNumber getRevisionNumber() {
        return revisionNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RCSSymbolEntry that = (RCSSymbolEntry) o;
        return Objects.equals(name, that.name) && Objects.equals(revisionNumber, that.revisionNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, revisionNumber);
    }

    @Override
    public String toString() {
        return "RCSSymbolEntry{name='" + name + "', revisionNumber=" + revisionNumber + '}';
    }
}
