package git.branches;

import git.commits.Commit;

public class Branch {
  private BranchManager manager;
  private String commitId;
  private String name;

  public Branch(BranchManager manager, String branchName, String commitId) {
    this.manager = manager;
    this.commitId = commitId;
    name = branchName;
  }

  public void pinTo(Commit commit) {
    this.commitId = commit.getIdentifier();
    manager.set(name, commit);
  }

  public String getName() {
    return name;
  }

  public String getCommitId() {
    return commitId;
  }
}
