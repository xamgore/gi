package git;

import commands.*;
import git.repo.FileRepositoryManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Paths;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.RunAll;

@Command(name = "gi", mixinStandardHelpOptions = true, version = "gi version 0.2")
public class Main implements Runnable {
  public static void main(String[] args) {
    Git git = new Git(new FileRepositoryManager());

    CommandLine cli = new CommandLine(new Main())
        .addSubcommand("init", new Init(git))
        .addSubcommand("add", new Add(git))
        .addSubcommand("rm", new Rm(git))
        .addSubcommand("status", new Status(git))
        .addSubcommand("commit", new Commit(git))
        .addSubcommand("reset", new Reset(git))
        .addSubcommand("log", new Log(git))
        .addSubcommand("checkout", new Checkout(git));

    try {
      cli.parseWithHandler(new RunAll(), args);
    } catch (CommandLine.ExecutionException e) {
      System.err.println(e.getCause().getMessage());
    }
  }

  @Override
  public void run() {}
}
