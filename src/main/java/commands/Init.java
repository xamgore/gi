package commands;

import git.Git;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(description = "Create an empty Gi repository or reinitialize an existing one")
public class Init implements Callable<Void> {
  private final Git git;

  public Init(final Git git) {
    this.git = git;
  }

  @Override
  public Void call() {
    git.init();
    System.err.println("Initialized empty gi repository");
    return null;
  }
}
