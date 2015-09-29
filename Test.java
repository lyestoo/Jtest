import java.io.File;
import java.io.IOException;
import java.util.List;


import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffConfig;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

// Simple example that shows how to diff a single file between two commits when
// the file may have been renamed.
public class DiffRenamedFile {
    private static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException,
            MissingObjectException,
            IncorrectObjectTypeException {
        // from the commit we can build the tree which allows us to construct the TreeParser
        RevWalk walk = new RevWalk(repository);
        RevCommit commit = walk.parseCommit(ObjectId.fromString(objectId));
        RevTree tree = walk.parseTree(commit.getTree().getId());

        CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
        ObjectReader oldReader = repository.newObjectReader();
            oldTreeParser.reset(oldReader, tree.getId());


        walk.dispose();

        return oldTreeParser;
    }

    private static DiffEntry diffFile(Repository repo, String oldCommit,
                                      String newCommit, String path) throws IOException, GitAPIException {
        Config config = new Config();
        config.setBoolean("diff", null, "renames", true);
        DiffConfig diffConfig = config.get(DiffConfig.KEY);
        List<DiffEntry> diffList = new Git(repo).diff().
                setOldTree(prepareTreeParser(repo, oldCommit)).
                setNewTree(prepareTreeParser(repo, newCommit)).
                setPathFilter(FollowFilter.create(path, diffConfig)).
                call();
        if (diffList.size() == 0)
            return null;
        if (diffList.size() > 1)
            throw new RuntimeException("invalid diff");
        return diffList.get(0);
    }

    public static void main(String args[])
            throws IOException, GitAPIException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.setGitDir(new File("C:/Users/Elyes/Desktop/Hexel/Hexel/.git"))
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build();

        RevWalk walk = new RevWalk(repository);

        AnyObjectId  headId = repository.resolve(Constants.HEAD);
        RevCommit root = walk.parseCommit(headId);
        walk.sort(RevSort.REVERSE);
        walk.markStart(root);

        String oldHash = walk.next().getName();
        System.out.println(oldHash);
        AbstractTreeIterator oldTreeParser;
        AbstractTreeIterator newTreeParser;
        for (RevCommit rev : walk){

            oldTreeParser = prepareTreeParser(repository, oldHash);
            newTreeParser = prepareTreeParser(repository, rev.name());
            List<DiffEntry> diffs= new Git(repository).diff()
                    .setNewTree(newTreeParser)
                    .setOldTree(oldTreeParser)
                    .call();
            System.out.println("-------------" + rev.getShortMessage()+"-------------");
            for (DiffEntry entry : diffs) {

                if(entry.getNewPath().contains(".java") && !entry.getOldPath().equals("/dev/null") )
                {
                    DiffEntry diff = diffFile(repository, oldHash, rev.name(), entry.getNewPath());
                    DiffFormatter formatter = new DiffFormatter(System.out);
                    formatter.setRepository(repository);
                    //formatter.format(diff);

                    if(diff.getChangeType().equals("RENAME")){
                       System.out.println(formatter.toFileHeader(diff));
                    }


                }
            }
            oldHash = rev.getId().getName();
        }

        // Diff README.md between two commits. The file is named README.md in
        // the new commit (5a10bd6e), but was named "jgit-cookbook README.md" in
        // the old commit (2e1d65e4).
        DiffEntry diff = diffFile(repository,
                "52e3f855900fc0c72751f42524617ffbc133801c",
                "48d39bb3f80a61725c7d7bfce1f319821d00c1b3",
                "src/main/java/Hexel/blocks/BlockDirt.java");
        //90b94fd2c651939d63ec6f0eafe8b6de2a3b525b
        // Display the diff.
        DiffFormatter formatter = new DiffFormatter(System.out);
        formatter.setRepository(repository);
        //formatter.format(diff);
        System.out.println( diff.getChangeType());
        System.out.println(formatter.toFileHeader(diff));
    }
}