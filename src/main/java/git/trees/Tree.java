package git.trees;

import git.Git;
import git.repo.Blob;
import git.repo.RepositoryManager;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Tree {
  private Map<Path, String> pathToBlobId;
  private RepositoryManager repo;
  private TreeManager manager;

  public Tree(RepositoryManager repo, TreeManager manager, Map<Path, String> pathToBlobId) {
    this.repo = repo;
    this.pathToBlobId = pathToBlobId;
    this.manager = manager;
  }

  public void add(File file) {
    if (file.isDirectory()) {
      repo.listWorkingDir(file).forEach(this::add);
    } else {
      Path path = Git.toRelativePath(file);
      if (repo.exists(path.toString())) {
        Blob blob = Blob.buildFrom(path, repo);
        pathToBlobId.put(path, blob.getHash());
      } else {
        remove(file);
      }
    }
  }

  public Tree addAll(Collection<File> paths) {
    paths.forEach(this::add);
    return this;
  }

  /**
   * Remove the blob from index, but not from pathToBlobId
   */
  public void remove(File file) {
    pathToBlobId.remove(Git.toRelativePath(file));
  }

  public boolean has(Path path) {
    return pathToBlobId.containsKey(path);
  }

  public Map<Path, String> getBlobIds() {
    return pathToBlobId;
  }

  public String getIdentifier() {
    return manager.getIdentifier(this);
  }

  /**
   * Check the difference between trees, and delete / update pathToBlobId in current.
   * There are three kinds of differences:
   * <li>there is a file in the current tree (but not in the future), it will be deleted</li>
   * <li>there is a file in the future tree (but not in the current), it will be restored</li>
   * <li>there is the same file in the current file & in the future, the future version will be chosen</li>
   */
  public void migrateTo(Tree futureTree) {
    // traverse the old tree, delete files missing in the new
    pathToBlobId.forEach((path, blobId) -> {
      if (!futureTree.has(path)) {
        repo.deleteWorkingDirFile(path);
      }
    });

    // traverse the new tree, create or update files here
    futureTree.pathToBlobId.forEach((path, blobId) -> {
      pathToBlobId.put(path, blobId);
      repo.restoreBlobInWorkingDir(path, blobId);
    });
  }

  public void migrateFilesTo(Tree futureTree, List<File> files) {
    files.forEach(file -> {
      Path path = Git.toRelativePath(file);
      if (futureTree.has(path)) {
        String blobId = futureTree.pathToBlobId.get(path);
        pathToBlobId.put(path, blobId);
        repo.restoreBlobInWorkingDir(path, blobId);
      } else {
        repo.deleteWorkingDirFile(path);
      }
    });
  }

  public Collection<Path> getPathsThatAreNotIn(Tree other) {
    Set<Path> here = new HashSet<>(this.pathToBlobId.keySet());
    here.removeAll(other.pathToBlobId.keySet());
    return here;
  }

  public Collection<Path> intersectPathWithDifferentVersions(Tree other) {
    HashSet<Entry<Path, String>> entries = new HashSet<>(this.pathToBlobId.entrySet());

    entries.removeIf(entry ->
        !other.pathToBlobId.containsKey(entry.getKey()) ||
            other.pathToBlobId.get(entry.getKey()).equals(entry.getValue()));

    return entries.stream().map(Entry::getKey).collect(Collectors.toList());
  }

  public boolean differs(Tree other) {
    return !this.getIdentifier().equals(other.getIdentifier());
  }

  public HashSet<Path> merge(Tree theirs, Tree common) {
    HashSet<Path> union = new HashSet<>(pathToBlobId.keySet());
    union.addAll(theirs.pathToBlobId.keySet());
    union.addAll(common.pathToBlobId.keySet());

    HashSet<Path> conflicts = new HashSet<>();

    for (Path path : union) {
      if (!same(path, theirs, common) && same(path, this, common)) {
        // theirs have more actual version
        if (theirs.has(path)) {
          String blobId = theirs.pathToBlobId.get(path);
          pathToBlobId.put(path, blobId);
          repo.restoreBlobInWorkingDir(path, blobId);
        } else {
          repo.deleteWorkingDirFile(path);
          remove(path.toFile());
        }
      } else if (!same(path, theirs, common) && !same(path, this, common) && !same(path, this, theirs)) {
        conflicts.add(path);
      }
    }

    return conflicts;
  }

  private String get(Path path) {
    return pathToBlobId.get(path);
  }

  private boolean same(Path path, Tree first, Tree second) {
    return first.has(path) && second.has(path) &&
        first.get(path).equals(second.get(path));
  }
}
