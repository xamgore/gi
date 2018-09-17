package git.revisions;

import git.branches.Branch;
import git.commits.Commit;
import org.jetbrains.annotations.NotNull;

public class Revision {
  private final Commit commit;
  private final Branch branch;

  public Revision(Commit commit, Branch branch) {
    this.commit = commit;
    this.branch = branch;
  }

  public boolean isBranch() {
    return branch != null;
  }

  public @NotNull Commit getCommit() {
    return commit;
  }

  public @NotNull Branch getBranch() {
    return branch;
  }
}
