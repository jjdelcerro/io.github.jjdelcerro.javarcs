package io.github.jjdelcerro.javarcs.lib.impl.core.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Representa un delta (revisión) dentro de un archivo RCS. Corresponde a la
 * estructura `rcs_delta` en la implementación C.
 */
public class RCSDelta {

  private final RCSRevisionNumber revisionNumber;
  private RCSRevisionNumber nextRevision; // rd_next
  private Date date; // rd_date
  private String author; // rd_author
  private String state; // rd_state
  private String commitId; // rd_commitid
  private String logMessage; // rd_log
  private String locker; // rd_locker
  private byte[] deltaText; // rd_text
  private List<RCSBranchEntry> branches; // rd_branches

  // rd_flags (RCS_RD_DEAD, RCS_RD_SELECT)
  private boolean isDead;
  private boolean isSelected;

  /**
   * Constructor para un RCSDelta.
   *
   * @param revisionNumber El número de revisión de este delta.
   */
  public RCSDelta(RCSRevisionNumber revisionNumber) {
    this.revisionNumber = Objects.requireNonNull(revisionNumber, "Revision number cannot be null");
    this.branches = new ArrayList<>();
    // Valores por defecto (adaptados de la implementación C si aplica)
    this.state = "Exp"; // RCS_STATE_EXP
  }

  public RCSRevisionNumber getRevisionNumber() {
    return revisionNumber;
  }

  public RCSRevisionNumber getNextRevision() {
    return nextRevision;
  }

  public void setNextRevision(RCSRevisionNumber nextRevision) {
    this.nextRevision = nextRevision;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getCommitId() {
    return commitId;
  }

  public void setCommitId(String commitId) {
    this.commitId = commitId;
  }

  public String getLogMessage() {
    return logMessage;
  }

  public void setLogMessage(String logMessage) {
    this.logMessage = logMessage;
  }

  public String getLocker() {
    return locker;
  }

  public void setLocker(String locker) {
    this.locker = locker;
  }

  public byte[] getDeltaText() {
    return deltaText;
  }

  public void setDeltaText(byte[] deltaText) {
    this.deltaText = deltaText;
  }

  public List<RCSBranchEntry> getBranches() {
    return branches;
  }

  public void addBranch(RCSBranchEntry branch) {
    this.branches.add(branch);
  }

  public boolean isDead() {
    return isDead;
  }

  public void setDead(boolean dead) {
    isDead = dead;
  }

  public boolean isSelected() {
    return isSelected;
  }

  public void setSelected(boolean selected) {
    isSelected = selected;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RCSDelta rcsDelta = (RCSDelta) o;
    return isDead == rcsDelta.isDead && isSelected == rcsDelta.isSelected && Objects.equals(revisionNumber, rcsDelta.revisionNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(revisionNumber, isDead, isSelected);
  }

  @Override
  public String toString() {
    return "RCSDelta{"
            + "revisionNumber=" + revisionNumber
            + ", nextRevision=" + nextRevision
            + ", date=" + date
            + ", author='" + author + "'"
            + ", state='" + state + "'"
            + ", commitId='" + commitId + "'"
            + ", logMessage='" + (logMessage != null ? logMessage.substring(0, Math.min(logMessage.length(), 20)) + "..." : "null") + "'"
            + ", locker='" + locker + "'"
            + ", branches=" + branches.size()
            + ", isDead=" + isDead
            + ", isSelected=" + isSelected
            + '}';
  }
}
