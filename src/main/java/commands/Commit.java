package commands;

import git.Git;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(description = "Commit stage to the repository", mixinStandardHelpOptions = true)
public class Commit implements Callable<Void> {
  private final Git git;

  public Commit(final Git git) {
    this.git = git;
  }

  @Parameters(index = "0", description = "commit message")
  private String message = null;

  @Override
  public Void call() {
    git.commit(message);
    return null;
  }
}
