package git.trees;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import git.Hasher;
import git.repo.RepositoryManager;
import util.PathConverter;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class TreeManager {
  private static final String TREES_PATH = "trees/";
  private static final String TREES_FILE_PATH = TREES_PATH + "%s.json";
  private static final Gson GSON = new GsonBuilder()
      .registerTypeHierarchyAdapter(Path.class, new PathConverter())
      .create();

  private RepositoryManager repo;


  public TreeManager(RepositoryManager repo) {
    this.repo = repo;
  }

  public Tree createEmpty() {
    return dump(new Tree(repo, this, new HashMap<>()));
  }

  public Tree create() {
    return new Tree(repo, this, new HashMap<>());
  }

  public Tree dump(Tree tree) {
    String json = toJSON(tree);
    String path = String.format(TREES_FILE_PATH, getIdentifier(json));
    repo.saveString(path, json);
    return tree;
  }

  public Tree load(String identifier) {
    String path = String.format(TREES_FILE_PATH, identifier);
    String content = repo.loadString(path);
    return new Tree(repo, this, GSON.fromJson(content, new TypeToken<HashMap<Path, String>>() {}.getType()));
  }

  private String toJSON(Tree tree) {
    return GSON.toJson(tree.getBlobIds());
  }

  public String getIdentifier(Tree tree) {
    return getIdentifier(toJSON(tree));
  }

  private String getIdentifier(String jsonDump) {
    return Hasher.hashHex(jsonDump);
  }

  public void cleanAllExcept(Set<Tree> usedTrees) {
    Set<String> used = usedTrees.stream()
        .map(c -> c.getIdentifier() + ".json")
        .collect(Collectors.toSet());

    repo.list(TREES_PATH).stream()
        .filter(path -> !used.contains(path.getFileName().toString()))
        .forEach(repo::deleteWorkingDirFile);
  }
}
