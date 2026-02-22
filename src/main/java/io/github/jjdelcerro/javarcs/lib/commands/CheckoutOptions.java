package io.github.jjdelcerro.javarcs.lib.commands;

import io.github.jjdelcerro.javarcs.lib.RCSCommandOptions;
import java.io.PrintStream;
import java.nio.file.Path;

/**
 *
 * @author jjdelcerro
 */
public interface CheckoutOptions extends RCSCommandOptions {

  String getAuthor();

  String getDate();

  int getKeywordExpansionMode();

  String getRevision();

  String getState();

  Path getWorkFilePath();

  boolean isForce();

  boolean isLock();

  boolean isPipeOut();

  boolean isPreserveTime();

  boolean isQuiet();

  boolean isUnlock();

  CheckoutOptions setAuthor(String author);

  CheckoutOptions setDate(String date);

  CheckoutOptions setForce(boolean force);

  CheckoutOptions setKeywordExpansionMode(int keywordExpansionMode);

  CheckoutOptions setLock(boolean lock);

  CheckoutOptions setPipeOut(boolean pipeOut);

  CheckoutOptions setPreserveTime(boolean preserveTime);

  CheckoutOptions setQuiet(boolean quiet);

  CheckoutOptions setRevision(String revision);

  CheckoutOptions setState(String state);

  CheckoutOptions setUnlock(boolean unlock);
  
  public void setOutputStream(PrintStream out);

  public PrintStream getOutputStream();
}
