package git.repo;

import git.GitException;
import git.Hasher;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dumped object file, saved in .git/objects
 */
public class Blob {
  private static final String BLOBS_PATH = "blobs/";
  private static final String BLOBS_FILE_PATH = BLOBS_PATH + "%s";

  private RepositoryManager repo;
  private String source = null;
  private String identifier;

  private Blob(RepositoryManager repo, String identifier) {
    this.identifier = identifier;
    this.repo = repo;
  }

  public static @NotNull Blob buildFrom(Path sourceFilePath, RepositoryManager repo) {
    try {
      String source = repo.read(sourceFilePath);
      String id = Hasher.hashHex(source);
      String path = String.format(BLOBS_FILE_PATH, id);
      repo.writeContentTo(path, sourceFilePath);
      return new Blob(repo, id);
    } catch (IOException e) {
      throw new GitException(e.getMessage(), e);
    }
  }

  public static @NotNull Blob getByIdentifier(String identifier, RepositoryManager repo) {
    return new Blob(repo, identifier);
  }

  public @NotNull String getSource() throws GitException {
    try {
      String path = String.format(BLOBS_FILE_PATH, getHash());
      return source = (source != null) ? source : repo.read(Paths.get(path));
    } catch (IOException e) {
      throw new GitException(e.getMessage(), e);
    }
  }

  public @NotNull String getHash() {
    return identifier != null ? identifier : Hasher.hashHex(getSource());
  }

  public static void cleanAllExcept(Set<Blob> usedBlobs, RepositoryManager repo) {
    Set<String> used = usedBlobs.stream()
        .map(Blob::getHash)
        .collect(Collectors.toSet());

    repo.list(BLOBS_PATH).stream()
        .filter(path -> !used.contains(path.getFileName().toString()))
        .forEach(repo::deleteWorkingDirFile);
  }
}
