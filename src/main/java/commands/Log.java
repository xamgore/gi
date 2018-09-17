package commands;

import git.Git;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;


@Command(description = "Show commit logs")
public class Log implements Callable<Void> {
  private final Git git;

  public Log(final Git git) {
    this.git = git;
  }

  @Parameters(arity = "0..1", index = "0", defaultValue = "", description = "Branch name or revision's hash")
  private String from_revision = null;

  @Override
  public Void call() {
    System.out.println(git.log(from_revision));
    return null;
  }
}
