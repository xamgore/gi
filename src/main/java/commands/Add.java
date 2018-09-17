package commands;

import git.Git;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

@Command(description = "Add file to stage", mixinStandardHelpOptions = true)
public class Add implements Callable<Void> {
  private final Git git;

  public Add(final Git git) {
    this.git = git;
  }

  @Parameters(index = "0..*", arity = "1..*", description = "files to add to index")
  private List<File> files = null;

  @Override
  public Void call() {
    if (files != null) git.addToIndex(files);
    return null;
  }
}
