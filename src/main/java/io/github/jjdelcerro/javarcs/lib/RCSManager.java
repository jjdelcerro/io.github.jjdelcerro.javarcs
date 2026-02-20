package io.github.jjdelcerro.javarcs.lib;

import io.github.jjdelcerro.javarcs.lib.commands.CheckinOptions;
import io.github.jjdelcerro.javarcs.lib.commands.CheckoutOptions;
import io.github.jjdelcerro.javarcs.lib.commands.CleanOptions;
import io.github.jjdelcerro.javarcs.lib.commands.DiffOptions;
import io.github.jjdelcerro.javarcs.lib.commands.IdentOptions;
import io.github.jjdelcerro.javarcs.lib.commands.LogOptions;
import io.github.jjdelcerro.javarcs.lib.commands.MergeOptions;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author jjdelcerro
 */
public interface RCSManager {

  public enum DiffFormat {
    NORMAL, CONTEXT, UNIFIED, RCSDIFF, BRIEF
  }

  public CheckinOptions createCheckinOptions(Path workFilePath);

  public CheckoutOptions createCheckoutOptions(Path workFilePath);

  public CleanOptions createCleanOptions(List<Path> filePaths);

  public DiffOptions createDiffOptions(List<Path> filePaths);

  public IdentOptions createIdentOptions(List<Path> filePaths);

  public LogOptions createLogOptions(Path workFilePath);

  public MergeOptions createMergeOptions(Path workFilePath, String baseRevision);

  public RCSCommand create(RCSCommandOptions options);
}
