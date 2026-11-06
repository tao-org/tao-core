package ro.cs.tao.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

/** Filesystem containment using path components and real paths, including existing symlink parents. */
public final class SafePaths {
    private SafePaths() { }

    public static Path resolve(Path root, String input) throws IOException {
        if (input == null || input.indexOf('\0') >= 0) throw new IOException("Invalid path");
        Path base = root.toAbsolutePath().normalize();
        Path supplied = Path.of(input);
        Path target = (supplied.isAbsolute() ? supplied : base.resolve(supplied)).toAbsolutePath().normalize();
        if (!target.startsWith(base)) throw new IOException("Path outside authorized workspace");
        Path realBase = base.toRealPath();
        Path ancestor = target;
        while (ancestor != null && !Files.exists(ancestor, LinkOption.NOFOLLOW_LINKS)) ancestor = ancestor.getParent();
        if (ancestor == null || !ancestor.toRealPath().startsWith(realBase)) {
            throw new IOException("Path outside authorized workspace");
        }
        // Reject links in the relative path as well as links pointing outside the root.
        Path cursor = base;
        for (Path component : base.relativize(target)) {
            cursor = cursor.resolve(component);
            if (Files.isSymbolicLink(cursor)) throw new IOException("Symbolic links are not allowed");
        }
        return target;
    }

    public static String fileName(String name) throws IOException {
        if (name == null || name.isBlank() || name.equals(".") || name.equals("..")
                || name.contains("/") || name.contains("\\") || name.contains(":") || name.indexOf('\0') >= 0) {
            throw new IOException("Invalid file name");
        }
        return name;
    }
}
