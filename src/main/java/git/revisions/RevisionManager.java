package git.revisions;

import git.GitException;
import git.branches.Branch;
import git.branches.BranchManager;
import git.commits.Commit;
import git.commits.CommitsManager;
import org.jetbrains.annotations.NotNull;

public class RevisionManager {
  private static final String HEAD = "HEAD";

  private final BranchManager branches;
  private final CommitsManager commits;
  private Revision head;

  public RevisionManager(BranchManager branches, CommitsManager commits) {
    this.branches = branches;
    this.commits = commits;
  }

  public void setHead(Revision head) {
    this.head = head;
  }

  public @NotNull Revision fromCommit(Commit commit) {
    return new Revision(commit, null);
  }

  public @NotNull Revision fromCommitId(String commitId) {
    return new Revision(commits.load(commitId), null);
  }

  public @NotNull Revision fromBranch(String name) {
    final Branch branch = branches.get(name);
    return new Revision(commits.load(branch.getCommitId()), branch);
  }

  public @NotNull Revision get(String hashOrBranchName) {
    if (HEAD.equals(hashOrBranchName)) {
      return head;
    }

    if (!exists(hashOrBranchName)) {
      throw new GitException(hashOrBranchName + " revision not found");
    }

    return branches.exists(hashOrBranchName)
        ? fromBranch(hashOrBranchName)
        : fromCommitId(hashOrBranchName);
  }

  public boolean exists(String hashOrBranchName) {
    return branches.exists(hashOrBranchName) || commits.exists(hashOrBranchName);
  }
}
