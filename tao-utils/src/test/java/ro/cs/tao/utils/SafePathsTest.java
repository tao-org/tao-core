package ro.cs.tao.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class SafePathsTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();

    @Test public void permitsExistingAndNewChildren() throws Exception {
        Path root = temp.newFolder("root").toPath();
        Path child = Files.writeString(root.resolve("file.txt"), "data");
        assertEquals(child, SafePaths.resolve(root, "file.txt"));
        assertEquals(root.resolve("new/sub.txt"), SafePaths.resolve(root, "new/sub.txt"));
    }

    @Test public void rejectsTraversalAndSiblingPrefixes() throws Exception {
        Path root = temp.newFolder("root").toPath();
        Path sibling = temp.newFolder("root-other").toPath();
        assertThrows(IOException.class, () -> SafePaths.resolve(root, "../root-other/file"));
        assertThrows(IOException.class, () -> SafePaths.resolve(root, sibling.resolve("file").toString()));
    }

    @Test public void rejectsLinkedParentsWhenPlatformPermitsSymlinks() throws Exception {
        Path root = temp.newFolder("root").toPath();
        Path outside = temp.newFolder("outside").toPath();
        try { Files.createSymbolicLink(root.resolve("link"), outside); }
        catch (IOException | UnsupportedOperationException | SecurityException e) {
            org.junit.Assume.assumeNoException("Symlink creation is unavailable", e);
        }
        assertThrows(IOException.class, () -> SafePaths.resolve(root, "link/file"));
    }

    @Test public void rejectsUploadPathComponents() {
        for (String name : new String[]{"../file", "..\\file", "C:file", ".", "..", ""}) {
            assertThrows(IOException.class, () -> SafePaths.fileName(name));
        }
    }
}
