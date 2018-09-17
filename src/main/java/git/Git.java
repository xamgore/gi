package git;

import git.branches.BranchManager;
import git.commits.Commit;
import git.commits.CommitsManager;
import git.repo.Blob;
import git.repo.RepositoryManager;
import git.revisions.Revision;
import git.revisions.RevisionManager;
import git.trees.Tree;
import git.trees.TreeManager;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import static util.Terminal.*;

public class Git {
  private static final String MASTER = "master";
  private static final String INDEX = "INDEX";
  private static final String HEAD = "HEAD";
  private static final String REF = "ref: ";
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("E MMM d HH:mm:ss yyyy");
  public static File ROOT = Paths.get(".").toAbsolutePath().normalize().toFile();

  public static Path toRelativePath(File file) {
    return ROOT.toPath().relativize(file.getAbsoluteFile().toPath().normalize());
  }

  private Revision head;
  private Tree index;

  private RepositoryManager repo;
  private RevisionManager revisions;
  private BranchManager branches;
  private TreeManager trees;
  private CommitsManager commits;


  public Git(RepositoryManager repo) {
    this.repo = repo;

    trees = new TreeManager(repo);
    commits = new CommitsManager(repo, trees);
    branches = new BranchManager(repo);
    revisions = new RevisionManager(branches, commits);

    loadHeadFromRepository();
    loadIndexFromRepository();
    revisions.setHead(head);
  }


  public void init() {
    repo.initialize();

    // new empty index
    index = trees.createEmpty();
    setNewGlobalIndexPointer();

    Commit initial = commits.newInitialCommit(index);
    branches.set(MASTER, initial);
    setAsHead(revisions.fromBranch(MASTER));
  }

  /**
   * Add files files passed by parameter <tt>files</tt> to the index,
   * so that they will appear in a new revision.<br>
   */
  public void addToIndex(List<File> files) {
    files.forEach(index::add);
    trees.dump(index);
    setNewGlobalIndexPointer();
  }

  /**
   * Remove files files passed by parameter <tt>files</tt> from the index,
   * they won't be tracked.<br>
   */
  public void removeFromIndex(List<File> files) {
    files.forEach(index::remove);
    trees.dump(index);
    setNewGlobalIndexPointer();
  }

  /**
   * Make a new revision, that contains files from the index.<br>
   * Moves the HEAD pointer and the current branch label.
   *
   * @param message short string that describes the message
   */
  public void commit(String message) {
    if (!head.getCommit().getTree().differs(index)) {
      System.out.println("Nothing to commit");
      return;
    }

    String parentCommitHash = head.getCommit().getIdentifier();
    Commit fresh = commits.build(message, index, LocalDateTime.now(), parentCommitHash);

    if (head.isBranch()) {
      head.getBranch().pinTo(fresh);
    } else {
      setAsHead(revisions.fromCommit(fresh));
    }
  }

  /**
   * Update index & file system as they are in the revision.<br>
   * Moves the current branch label.
   *
   * @param hashOrBranchName hash or branch of the revision to checkout
   */
  public void reset(String hashOrBranchName) throws GitException {
    if (!head.isBranch()) {
      throw new GitException("Can't reset in detached state");
    }

    Commit toCommit = revisions.get(hashOrBranchName).getCommit();
    Tree futureTree = toCommit.getTree();

    index.migrateTo(futureTree);
    index = futureTree;
    setNewGlobalIndexPointer();

    // move branch label
    head.getBranch().pinTo(toCommit);

    cleanUnreachableCommits();
  }

  public void status() {
    System.out.println(head.isBranch()
      ? "On branch " + head.getBranch().getName()
      : "Detached HEAD");

    Tree headTree = head.getCommit().getTree();
    Collection<Path> newFiles = index.getPathsThatAreNotIn(headTree);
    Collection<Path> modified = index.intersectPathWithDifferentVersions(headTree);
    Collection<Path> deleted = headTree.getPathsThatAreNotIn(index);

    Tree current = trees.create().addAll(repo.listWorkingDir(ROOT));
    Collection<Path> notStaged = index.intersectPathWithDifferentVersions(current);
    Collection<Path> removed = index.getPathsThatAreNotIn(current);
    Collection<Path> untracked = current.getPathsThatAreNotIn(index);

    if (!newFiles.isEmpty() || !modified.isEmpty() || !deleted.isEmpty()) {
      System.out.println("Changes to be commited:\n");
      display(newFiles, GREEN, "new file:  ");
      display(modified, GREEN, "modified:  ");
      display(deleted, GREEN, "deleted:   ");
      System.out.println();
    }

    if (!notStaged.isEmpty() || !removed.isEmpty()) {
      System.out.println("Changes not staged for commit:\n");
      display(notStaged, RED, "modified:  ");
      display(removed, RED, "deleted:   ");
      System.out.println();
    }

    if (!untracked.isEmpty()) {
      System.out.println("Untracked files:\n");
      display(untracked, RED, "");
      System.out.println();
    }
  }

  private void display(Collection<Path> paths, String color, String title) {
    if (!paths.isEmpty()) {
      paths.forEach(p -> System.out.println(c(color, "    " + title + p.toString())));
    }
  }

  /**
   * Get detailed history of commits from the revision to the root.
   *
   * @param hashOrBranchName hash / branch of the revision to log from
   * @return log in string representation
   */
  public String log(String hashOrBranchName) {
    Revision fromWhere = hashOrBranchName.isEmpty() ? head : revisions.get(hashOrBranchName);
    return log(fromWhere);
  }

  /**
   * Get detailed history of commits from the revision to the root.
   *
   * @param fromRevision the revision to log from
   * @return log in string representation
   */
  private String log(Revision fromRevision) {
    StringBuilder result = new StringBuilder();

    commits
        .pickAllToRoot(fromRevision.getCommit()).stream()
        .sorted(Comparator.comparing(Commit::getDate).reversed())
        .map(this::formatCommit)
        .forEach(result::append);

    return result.toString();
  }

  private String formatCommit(Commit commit) {
    String currentId = commit.getIdentifier();

    String labels = branches.stream()
        .filter(e -> e.getValue().equals(currentId))
        .map(Entry::getKey)
        .collect(Collectors.joining(", "));

    String branchLabels = labels.isEmpty() ? "" : " (" + labels + ")";

    boolean isHead = head.getCommit().equals(commit);
    String headLabel = isHead ? " [HEAD]" : "";

    String date = commit.getDate().format(DATE_FORMATTER);

    return String.format("%s%s%s\nDate: %s\n\n    %s\n\n",
        c(YELLOW, "commit " + currentId),
        c(CYAN, headLabel), c(GREEN, branchLabels),
        date, commit.getMessage());
  }

  /**
   * Clean objects, that are unreachable from all branches
   * or HEAD. This includes: commits, trees, blobs.
   */
  private void cleanUnreachableCommits() {
    Set<Commit> usedCommits = commits.pickAllToRoot(branches.getItsCommits(commits));

    Set<Tree> usedTrees = usedCommits.stream()
        .map(Commit::getTree)
        .collect(Collectors.toSet());

    Set<Blob> usedBlobs = usedTrees.stream()
        .flatMap(t -> t.getBlobIds().values().stream())
        .map(blobId -> Blob.getByIdentifier(blobId, repo))
        .collect(Collectors.toSet());

    commits.cleanAllExcept(usedCommits);
    trees.cleanAllExcept(usedTrees);
    Blob.cleanAllExcept(usedBlobs, repo);
  }

  /**
   * Update index & file system as they are in the revision.
   *
   * @param hashOrBranchName hash or branch of the revision to checkout
   */
  public void checkout(String hashOrBranchName) {
    Revision toRevision = revisions.get(hashOrBranchName);
    Tree futureTree = toRevision.getCommit().getTree();

    index.migrateTo(futureTree);
    index = futureTree;
    setNewGlobalIndexPointer();

    setAsHead(toRevision);
  }

  public void checkoutFiles(String hashOrBranchName, List<File> files) {
    Revision toRevision = revisions.get(hashOrBranchName);
    Tree futureTree = toRevision.getCommit().getTree();

    index.migrateFilesTo(futureTree, files);
    setNewGlobalIndexPointer();
  }

  /**
   * Sets the passed revision as the new HEAD.<br>
   * This information is saved to a file in the repository.
   */
  private void setAsHead(Revision revision) {
    String content = revision.isBranch()
        ? REF + revision.getBranch().getName()
        : revision.getCommit().getIdentifier();

    repo.saveString(HEAD, content);
  }

  /**
   * Initializes, where the HEAD points.<br>
   * Information is got from a file in the repository.
   */
  private void loadHeadFromRepository() {
    String content = repo.loadString(HEAD);

    String hashOrBranchName = content != null && content.startsWith(REF)
        ? content.substring(REF.length()) : content;

    head = hashOrBranchName == null || hashOrBranchName.isEmpty()
        ? null : revisions.get(hashOrBranchName);
  }

  /**
   * Initializes the index tree.<br>
   * Information is got from a file in the repository.
   */
  private void loadIndexFromRepository() {
    String indexTreePath = repo.loadString(INDEX);
    if (indexTreePath != null && !indexTreePath.isEmpty()) {
      index = trees.load(indexTreePath);
    }
  }

  private void setNewGlobalIndexPointer() {
    repo.saveString(INDEX, trees.getIdentifier(index));
  }
}
