package io.github.jjdelcerro.javarcs.lib.commands;

import io.github.jjdelcerro.javarcs.lib.RCSCommandOptions;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author jjdelcerro
 */
public interface IdentOptions extends RCSCommandOptions {

  List<Path> getFilePaths();

  boolean isQuiet();

  IdentOptions setQuiet(boolean quiet);
  
}
