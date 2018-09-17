package commands;

import git.Git;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(description = "Reset current HEAD to the specified state")
public class Reset implements Callable<Void> {
  private final Git git;

  public Reset(final Git git) {
    this.git = git;
  }

  @CommandLine.Parameters(index = "0", description = "Branch name or revision's hash")
  private String to_revision = null;

  @Override
  public Void call() {
    git.reset(to_revision);
    return null;
  }
}
