package io.github.jjdelcerro.javarcs.lib.commands;

import io.github.jjdelcerro.javarcs.lib.RCSCommandOptions;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author jjdelcerro
 */
public interface MergeOptions extends RCSCommandOptions {

  String getBaseRevision();

  String getCompareRevision();

  List<String> getLabels();

  Path getWorkFilePath();

  boolean isEflag();

  boolean isEflagPlusOflag();

  boolean isPipeOut();

  boolean isQuiet();

  MergeOptions setCompareRevision(String compareRevision);

  MergeOptions setEflag(boolean eflag);

  MergeOptions setEflagPlusOflag(boolean eflagPlusOflag);

  MergeOptions setLabels(List<String> labels);

  MergeOptions setPipeOut(boolean pipeOut);

  MergeOptions setQuiet(boolean quiet);
  
}
