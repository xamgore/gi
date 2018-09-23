package commands;

import git.Git;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;


@Command(description = "Show commit logs")
public class Branch implements Callable<Void> {
  private final Git git;

  public Branch(final Git git) {
    this.git = git;
  }

  @Parameters(arity = "1", index = "0", description = "new branch name")
  private String branchName = null;

  @Parameters(arity = "0..1", index = "1", description = "revision or hash of commit")
  private String revision = null;

  @Option(names = "-d", description = "delete branch")
  private boolean delete;

  @Override
  public Void call() {
    if (delete) {
      git.deleteBranch(branchName);
    } else {
      git.createBranch(branchName, revision == null ? "HEAD" : revision);
    }
    return null;
  }
}
