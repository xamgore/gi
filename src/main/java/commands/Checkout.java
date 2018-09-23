package commands;

import git.Git;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

@Command(description = "Switch branches or restore working treeHash files", mixinStandardHelpOptions = true)
public class Checkout implements Callable<Void> {
  private final Git git;

  public Checkout(final Git git) {
    this.git = git;
  }

  @Parameters(index = "0", description = "Branch name or revision's hash")
  private String revision = null;

  @Parameters(index = "1..*", arity = "0..*", description = "files to reset")
  private List<File> files = null;

  @Override
  public Void call() {
    if (files == null) {
      git.checkout(revision);
    } else {
      if ("--".equals(revision))
        revision = "HEAD";

      git.checkoutFiles(revision, files);
    }

    return null;
  }
}
