package io.github.jjdelcerro.javarcs.lib.impl.core.model;

import java.util.Objects;

/**
 * Representa una entrada de rama en un delta de RCS.
 */
public class RCSBranchEntry {
    private final RCSRevisionNumber branchNumber;

    public RCSBranchEntry(RCSRevisionNumber branchNumber) {
        this.branchNumber = Objects.requireNonNull(branchNumber, "Branch number cannot be null");
    }

    public RCSRevisionNumber getBranchNumber() {
        return branchNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RCSBranchEntry that = (RCSBranchEntry) o;
        return Objects.equals(branchNumber, that.branchNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(branchNumber);
    }

    @Override
    public String toString() {
        return "RCSBranchEntry{branchNumber=" + branchNumber + '}';
    }
}
