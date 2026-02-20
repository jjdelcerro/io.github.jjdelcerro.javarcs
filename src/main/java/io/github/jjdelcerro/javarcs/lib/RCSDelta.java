package io.github.jjdelcerro.javarcs.lib;

import java.util.Date;

/**
 *
 * @author jjdelcerro
 */
public interface RCSDelta {

  String getAuthor();

  String getCommitId();

  Date getDate();

  byte[] getDeltaText();

  String getLocker();

  String getLogMessage();

  RCSRevisionNumber getNextRevision();

  RCSRevisionNumber getRevisionNumber();

  String getState();

  boolean isDead();

  boolean isSelected();

}
