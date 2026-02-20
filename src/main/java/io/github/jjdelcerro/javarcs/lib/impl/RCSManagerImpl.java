package io.github.jjdelcerro.javarcs.lib.impl;

import io.github.jjdelcerro.javarcs.lib.RCSCommand;
import io.github.jjdelcerro.javarcs.lib.RCSCommandOptions;
import io.github.jjdelcerro.javarcs.lib.RCSManager;
import io.github.jjdelcerro.javarcs.lib.commands.CheckinOptions;
import io.github.jjdelcerro.javarcs.lib.commands.CheckoutOptions;
import io.github.jjdelcerro.javarcs.lib.commands.CleanOptions;
import io.github.jjdelcerro.javarcs.lib.commands.DiffOptions;
import io.github.jjdelcerro.javarcs.lib.commands.IdentOptions;
import io.github.jjdelcerro.javarcs.lib.commands.LogOptions;
import io.github.jjdelcerro.javarcs.lib.commands.MergeOptions;
import io.github.jjdelcerro.javarcs.lib.impl.core.commands.CheckinCommand;
import io.github.jjdelcerro.javarcs.lib.impl.core.commands.CheckinOptionsImpl;
import io.github.jjdelcerro.javarcs.lib.impl.core.commands.CheckoutCommand;
import io.github.jjdelcerro.javarcs.lib.impl.core.commands.CheckoutOptionsImpl;
import io.github.jjdelcerro.javarcs.lib.impl.core.commands.CleanCommand;
import io.github.jjdelcerro.javarcs.lib.impl.core.commands.CleanOptionsImpl;
import io.github.jjdelcerro.javarcs.lib.impl.core.commands.DiffCommand;
import io.github.jjdelcerro.javarcs.lib.impl.core.commands.DiffOptionsImpl;
import io.github.jjdelcerro.javarcs.lib.impl.core.commands.IdentCommand;
import io.github.jjdelcerro.javarcs.lib.impl.core.commands.IdentOptionsImpl;
import io.github.jjdelcerro.javarcs.lib.impl.core.commands.LogCommand;
import io.github.jjdelcerro.javarcs.lib.impl.core.commands.LogOptionsImpl;
import io.github.jjdelcerro.javarcs.lib.impl.core.commands.MergeCommand;
import io.github.jjdelcerro.javarcs.lib.impl.core.commands.MergeOptionsImpl;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author jjdelcerro
 */
public class RCSManagerImpl implements RCSManager {

  @Override
  public CheckinOptions createCheckinOptions(Path workFilePath) {
    return new CheckinOptionsImpl(workFilePath);
  }

  @Override
  public CheckoutOptions createCheckoutOptions(Path workFilePath) {
    return new CheckoutOptionsImpl(workFilePath);
  }

  @Override
  public CleanOptions createCleanOptions(List<Path> filePaths) {
    return new CleanOptionsImpl(filePaths);
  }

  @Override
  public DiffOptions createDiffOptions(List<Path> filePaths) {
    return new DiffOptionsImpl(filePaths);
  }

  @Override
  public IdentOptions createIdentOptions(List<Path> filePaths) {
    return new IdentOptionsImpl(filePaths);
  }

  @Override
  public LogOptions createLogOptions(Path workFilePath) {
    return new LogOptionsImpl(workFilePath);
  }

  @Override
  public MergeOptions createMergeOptions(Path workFilePath, String baseRevision) {
    return new MergeOptionsImpl(workFilePath, baseRevision);
  }

  @Override
  public RCSCommand create(RCSCommandOptions options) {
    if( options instanceof CheckinOptions ) {
      return new CheckinCommand();
    }
    if( options instanceof CheckoutOptions ) {
      return new CheckoutCommand();
    }
    if( options instanceof CleanOptions ) {
      return new CleanCommand();
    }
    if( options instanceof DiffOptions ) {
      return new DiffCommand();
    }
    if( options instanceof IdentOptions ) {
      return new IdentCommand();
    }
    if( options instanceof LogOptions ) {
      return new LogCommand();
    }
    if( options instanceof MergeOptions ) {
      return new MergeCommand();
    }
    throw new IllegalArgumentException("Can't create command from "+options.getClass().getName()+".");
  }

}
