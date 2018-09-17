package commands;

import git.Git;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

@Command(description = "Commit stage to the repository", mixinStandardHelpOptions = true)
public class Rm implements Callable<Void> {
  private final Git git;

  public Rm(final Git git) {
    this.git = git;
  }

  @Parameters(index = "0..*", arity = "1..*", description = "files to remove from index")
  private List<File> files = null;

  @Override
  public Void call() {
    if (files != null)
      git.removeFromIndex(files);
    return null;
  }
}
