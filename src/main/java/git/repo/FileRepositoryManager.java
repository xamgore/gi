package git.repo;

import git.GitException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

public class FileRepositoryManager implements RepositoryManager {

  public static final String GIT = ".gi";

  private File getFile(String path) {
    return Paths.get(GIT, path).toFile();
  }

  /**
   * Save string to the file
   */
  @Override public void saveString(String gitFolderPath, String content) {
    try {
      FileUtils.writeStringToFile(getFile(gitFolderPath), content);
    } catch (IOException e) {
      throw new GitException(e.getMessage(), e);
    }
  }

  /**
   * Load content of the file
   */
  @Override public String loadString(String gitFolderPath) {
    try {
      File toRead = getFile(gitFolderPath);
      return toRead.exists()
          ? FileUtils.readFileToString(getFile(gitFolderPath))
          : null;
    } catch (IOException e) {
      throw new GitException(e.getMessage(), e);
    }
  }

  /**
   * List all files in the git folder
   */
  public Collection<Path> list(String gitFolderPath) {
    return FileUtils.listFiles(getFile(gitFolderPath), null, false)
        .stream().map(File::toPath).collect(Collectors.toList());
  }

  public Collection<File> listWorkingDir(File where) {
    IOFileFilter notGitFolder =
        new NotFileFilter(new NameFileFilter(FileRepositoryManager.GIT));
    return FileUtils.listFiles(where, FileFileFilter.FILE, notGitFolder);
  }

  /**
   * Copy file <tt>originalPath</tt> to <tt>path</tt>.
   * Possibly with compression.
   */
  @Override public void writeContentTo(String gitFolderPath, Path originalPath) {
    try {
      FileUtils.copyFile(originalPath.toFile(), getFile(gitFolderPath));
    } catch (IOException e) {
      throw new GitException(e.getMessage(), e);
    }
  }

  /**
   * Read content of the file, possibly with decompression
   */
  @Override public String read(Path path) {
    try {
      File gitFolderFile = Paths.get(GIT).resolve(path).toFile();
      File toOpen = gitFolderFile.exists() ? gitFolderFile : path.toFile();
      return FileUtils.readFileToString(toOpen);
    } catch (IOException e) {
      throw new GitException(e.getMessage(), e);
    }
  }

  @Override public void deleteWorkingDirFile(Path path) {
    try {
      FileUtils.forceDelete(path.toFile());
    } catch (IOException e) {
      throw new GitException(e.getMessage(), e);
    }
  }

  @Override public void deleteInnerFile(Path path) {
    deleteWorkingDirFile(Paths.get(GIT, path.toString()));
  }

  @Override public void updateFile(Path path, String content) {
    try {
      FileUtils.writeStringToFile(path.toFile(), content);
    } catch (IOException e) {
      throw new GitException(e.getMessage(), e);
    }
  }

  @Override public boolean exists(String path) {
    if (path.startsWith("commits/"))
      return getFile(path).exists();
    return Paths.get(path).toFile().exists();
  }

  @Override public void initialize() {
    Paths.get(GIT).toFile().mkdirs();
  }
}
