package commands;

import git.Git;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(description = "Create an empty Gi repository or reinitialize an existing one")
public class Status implements Callable<Void> {
  private final Git git;

  public Status(final Git git) {
    this.git = git;
  }

  @Override
  public Void call() {
    git.status();
    return null;
  }
}
