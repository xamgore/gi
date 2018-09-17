package git.branches;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import git.commits.Commit;
import git.commits.CommitsManager;
import git.repo.RepositoryManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BranchManager implements Iterable<Map.Entry<String, String>> {
  private static final String BRANCHES_PATH = "branches.json";
  private static final Gson GSON = new Gson();

  private Map<String, String> branches;
  private RepositoryManager repo;

  public BranchManager(RepositoryManager repo) {
    this.repo = repo;
    loadBranches();
  }

  public boolean exists(String branchName) {
    return branches.containsKey(branchName);
  }

  public Branch get(String branchName) {
    return exists(branchName) ? new Branch(this, branchName, branches.get(branchName)) : null;
  }

  /**
   * @return a list of commits, where branches point
   */
  public List<Commit> getItsCommits(CommitsManager manager) {
    return branches.values().stream().map(manager::load).collect(Collectors.toList());
  }

  public void set(String branchName, Commit commit) {
    branches.put(branchName, commit.getIdentifier());
    dumpBranches();
  }

  public void delete(String branchName) {
    branches.remove(branchName);
    dumpBranches();
  }

  private void dumpBranches() {
    repo.saveString(BRANCHES_PATH, GSON.toJson(branches));
  }

  private void loadBranches() {
    String content = repo.loadString(BRANCHES_PATH);
    branches = GSON.fromJson(content == null || content.isEmpty() ? "{}" : content,
        new TypeToken<HashMap<String, String>>() {}.getType());
  }

  public void pinTo(Branch branch, Commit toCommit) {
    set(branch.getName(), toCommit);
  }

  @NotNull @Override public Iterator<Map.Entry<String, String>> iterator() {
    return branches.entrySet().iterator();
  }

  @NotNull public Stream<Map.Entry<String, String>> stream() {
    return branches.entrySet().stream();
  }
}
