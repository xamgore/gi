package git.commits;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import git.Hasher;
import git.repo.RepositoryManager;
import git.trees.Tree;
import git.trees.TreeManager;
import org.jetbrains.annotations.NotNull;
import util.PathConverter;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class CommitsManager {
  private static final String COMMITS_PATH = "commits/";
  private static final String COMMITS_FILE_PATH = COMMITS_PATH + "%s.json";
  private static final Gson GSON = new GsonBuilder()
      .registerTypeHierarchyAdapter(Path.class, new PathConverter())
      .create();

  private RepositoryManager repo;
  private TreeManager trees;

  public CommitsManager(RepositoryManager repo, TreeManager trees) {
    this.repo = repo;
    this.trees = trees;
  }

  public Commit build(String message, Tree tree, LocalDateTime date, List<String> parentIds) {
    // contract: tree is dumped already
    return dump(new Commit(this, message, date, tree, parentIds));
  }

  public Commit newInitialCommit(Tree index) {
    return build("Initial commit", index, LocalDateTime.now(), new LinkedList<>());
  }

  public boolean exists(String commitIdOrPrefix) {
    return repo.exists(String.format(COMMITS_FILE_PATH, commitIdOrPrefix))
        || find(commitIdOrPrefix) != null;
  }

  public Commit dump(Commit commit) {
    String json = toJSON(commit);
    String path = String.format(COMMITS_FILE_PATH, getIdentifier(json));
    repo.saveString(path, json);
    return commit;
  }

  /**
   * @return filename of the commit's file, like hash123.json
   */
  private String find(String commitIdOrPrefix) {
    return repo.list(COMMITS_PATH).stream()
        .map(path -> path.getFileName().toString())
        .filter(name -> name.startsWith(commitIdOrPrefix))
        .max(Comparator.comparingInt(String::length))
        .orElse(null);
  }

  public Commit load(String commitId) {
    String path = COMMITS_PATH + find(commitId);
    String content = repo.loadString(path);
    Map<String, String> map = GSON.fromJson(content, new TypeToken<HashMap<String, String>>() {}.getType());

    String message = map.get("message");
    Tree tree = trees.load(map.get("tree"));
    LocalDateTime date = LocalDateTime.parse(map.get("date"));
    String parents = map.get("parents");
    List<String> parentIds = parents.isEmpty()
        ? new LinkedList<>() : asList(map.get("parents").split(","));
    return new Commit(this, message, date, tree, parentIds);
  }

  private String toJSON(Commit commit) {
    Map<String, Object> object = new HashMap<>();
    object.put("parents", String.join(",", commit.getParentIds()));
    object.put("message", commit.getMessage());
    object.put("date", commit.getDate().toString());
    object.put("tree", commit.getTree().getIdentifier());
    return GSON.toJson(object);
  }

  public String getIdentifier(Commit commit) {
    return getIdentifier(toJSON(commit));
  }

  private String getIdentifier(String jsonDump) {
    return Hasher.hashHex(jsonDump);
  }

  public @NotNull List<Commit> getParentsOf(Commit current) {
    return current.getParentIds().stream().map(this::load).collect(Collectors.toList());
  }

  /**
   * Traverse commits until root and collect to the set.
   */
  public Set<Commit> pickAllToRoot(Commit from) {
    return pickAllToRoot(new LinkedHashSet<>(), from);
  }

  /**
   * Traverse commits until root and collect to the set.
   */
  public Set<Commit> pickAllToRoot(List<Commit> from) {
    Set<Commit> visited = new LinkedHashSet<>();
    from.forEach(commit -> pickAllToRoot(visited, commit));
    return visited;
  }

  /**
   * Traverse commits until root and collect to the set.
   */
  private Set<Commit> pickAllToRoot(Set<Commit> visited, Commit from) {
    LinkedList<Commit> queue = new LinkedList<>();
    queue.add(from);

    while (!queue.isEmpty()) {
      Commit current = queue.pop();

      if (!visited.contains(current)) {
        visited.add(current);
        queue.addAll(getParentsOf(current));
      }
    }

    return visited;
  }

  public void cleanAllExcept(Set<Commit> usedCommits) {
    Set<String> used = usedCommits.stream()
        .map(c -> c.getIdentifier() + ".json")
        .collect(Collectors.toSet());

    repo.list(COMMITS_PATH).stream()
        .filter(path -> !used.contains(path.getFileName().toString()))
        .forEach(repo::deleteWorkingDirFile);
  }
}
