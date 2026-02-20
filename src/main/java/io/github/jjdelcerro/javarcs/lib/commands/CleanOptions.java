package io.github.jjdelcerro.javarcs.lib.commands;

import io.github.jjdelcerro.javarcs.lib.RCSCommandOptions;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author jjdelcerro
 */
public interface CleanOptions extends RCSCommandOptions {

  List<Path> getFilePaths();

  int getKeywordExpansionMode();

  String getRevision();

  boolean isDryRun();

  boolean isPreserveTime();

  boolean isQuiet();

  boolean isUnlock();

  CleanOptions setDryRun(boolean dryRun);

  CleanOptions setKeywordExpansionMode(int keywordExpansionMode);

  CleanOptions setPreserveTime(boolean preserveTime);

  CleanOptions setQuiet(boolean quiet);

  CleanOptions setRevision(String revision);

  CleanOptions setUnlock(boolean unlock);
  
}
