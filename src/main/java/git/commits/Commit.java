package git.commits;

import git.trees.Tree;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class Commit {
  public static String ROOT_COMMIT_ID = "";

  private String parentId;  // todo: list of parents
  private String message;
  private LocalDateTime date;
  private Tree tree;

  private CommitsManager manager;


  public Commit(CommitsManager manager, String message, LocalDateTime date, Tree tree, String parentId) {
    this.manager = manager;
    this.parentId = parentId;
    this.message = message;
    this.date = date;
    this.tree = tree;
  }

  public @NotNull String getIdentifier() {
    return manager.getIdentifier(this);
  }

  public String getParentId() {
    return parentId;
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
    return ROOT_COMMIT_ID.equals(parentId);
  }

  @Override public boolean equals(Object obj) {
    return (obj instanceof Commit) && getIdentifier().equals(((Commit) obj).getIdentifier());
  }

  @Override public int hashCode() {
    return this.getIdentifier().hashCode();
  }
}
