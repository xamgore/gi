package git.commits;

import git.trees.Tree;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

public class Commit {
  public static String ROOT_COMMIT_ID = "";

  private List<String> parentIds;
  private String message;
  private LocalDateTime date;
  private Tree tree;

  private CommitsManager manager;


  public Commit(CommitsManager manager, String message, LocalDateTime date, Tree tree, List<String> parentIds) {
    this.parentIds = new LinkedList<>(parentIds);
    this.manager = manager;
    this.message = message;
    this.date = date;
    this.tree = tree;
  }

  public @NotNull String getIdentifier() {
    return manager.getIdentifier(this);
  }

  public List<String> getParentIds() {
    return parentIds;
  }

  public @NotNull LocalDateTime getDate() {
    return date;
  }

  public @NotNull String getMessage() {
    return message;
  }

  public Tree getTree() {
    return tree;
  }

  public boolean isRoot() {
    return parentIds.isEmpty();
  }

  @Override public boolean equals(Object obj) {
    return (obj instanceof Commit) && getIdentifier().equals(((Commit) obj).getIdentifier());
  }

  @Override public int hashCode() {
    return this.getIdentifier().hashCode();
  }
}
