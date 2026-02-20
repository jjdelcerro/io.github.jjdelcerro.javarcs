package io.github.jjdelcerro.javarcs.lib.impl.core.model;

import io.github.jjdelcerro.javarcs.lib.RCSSymbolEntry;
import java.util.Objects;

/**
 * Representa una entrada de símbolo en un archivo RCS.
 */
public class RCSSymbolEntryImpl implements RCSSymbolEntry {
    private final String name;
    private final RCSRevisionNumberImpl revisionNumber;

    public RCSSymbolEntryImpl(String name, RCSRevisionNumberImpl revisionNumber) {
        this.name = Objects.requireNonNull(name, "Symbol name cannot be null");
        this.revisionNumber = Objects.requireNonNull(revisionNumber, "Revision number cannot be null");
    }

  @Override
    public String getName() {
        return name;
    }

  @Override
    public RCSRevisionNumberImpl getRevisionNumber() {
        return revisionNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RCSSymbolEntryImpl that = (RCSSymbolEntryImpl) o;
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
