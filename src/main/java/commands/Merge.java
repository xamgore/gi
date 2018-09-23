package commands;

import git.Git;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;


@Command(description = "Show commit logs")
public class Merge implements Callable<Void> {
  private final Git git;

  public Merge(final Git git) {
    this.git = git;
  }

  @Parameters(arity = "1", index = "0", description = "branch to merge")
  private String branchName = null;

  @Option(names = "--force", description = "force merging")
  private boolean force;

  @Override
  public Void call() {
    git.merge(branchName, force);
    return null;
  }
}
