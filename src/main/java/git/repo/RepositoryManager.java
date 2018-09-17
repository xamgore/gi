package git.repo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public interface RepositoryManager {
  /**
   * Save string to the file
   */
  void saveString(String gitFolderPath, String content);

  /**
   * Load content of the file
   */
  String loadString(String gitFolderPath);

  /**
   * List all files in the git folder
   */
  Collection<Path> list(String gitFolderPath);

  Collection<File> listWorkingDir(File path);

  /**
   * Copy file <tt>originalPath</tt> to <tt>path</tt>.
   * Possibly with compression.
   */
  void writeContentTo(String gitFolderPath, Path originalPath);

  /**
   * Read content of the file, possibly with decompression
   */
  String read(Path path) throws IOException;

  void deleteWorkingDirFile(Path path);

  void deleteInnerFile(Path path);

  void updateFile(Path path, String content);

  boolean exists(String path);

  /**
   * Called once, on git init
   */
  void initialize();

  default void restoreBlobInWorkingDir(Path path, String blobId) {
    updateFile(path, Blob.getByIdentifier(blobId, this).getSource());
  }
}
