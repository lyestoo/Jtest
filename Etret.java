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

  
}