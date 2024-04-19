import ro.cs.tao.utils.Crypto;
import ro.cs.tao.utils.FileUtilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class UnzipTest {

    public static void main(String[] args) throws IOException {
        /*final Path source = Paths.get("D:\\download");
        final Path destination = Paths.get("W:\\download");
        final List<Path> all = FileUtilities.listTree(source);
        final String filter = "2018\\12";
        final String filter2 = "eodata\\Sentinel-1\\SAR\\SLC\\" + filter;
        final List<Path> zipFiles = all.stream().filter(p -> p.toString().contains(filter) && p.getFileName().toString().endsWith(".zip")).collect(Collectors.toList());
        final int zipCount = zipFiles.size();
        final long minSize = 1024 * 1024 * 10;
        for (int i = 0; i < zipCount; i++) {
            try {
                final Path zipFile = zipFiles.get(i);
                //System.out.println(zipFile.getFileName() + " " + Files.size(zipFile) / 1024 + "kB");
                System.out.println(String.format("[%02d/%02d] Begin unzipping %s", i + 1, zipCount, zipFile));
                if (Files.size(zipFile) < minSize) {
                    System.err.println("File is too small, will skip");
                    continue;
                }
                FileUtilities.unzip(zipFile, destination, true);
                System.out.println("End unzipping " + zipFile);
                final List<Path> toMove = FileUtilities.listTree(destination.resolve(filter2))
                        .stream().filter(p -> {
                            final int idx = p.toString().indexOf(".SAFE");
                            return idx > 0 && p.toString().length() == idx + 5;
                        }).collect(Collectors.toList());
                for (Path path : toMove) {
                    Path relPath = Paths.get(destination.relativize(path).toString().replace(filter2, ""));
                    final int tokens = relPath.getNameCount();
                    Path newPath = destination.resolve("esp");
                    for (int j = 1; j < tokens - 1; j++) {
                        newPath = newPath.resolve(relPath.getName(j));
                    }
                    System.out.println(String.format("Moving %s to %s", path, newPath));
                    FileUtilities.move(path, newPath);
                }
                Files.delete(zipFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
        System.out.println(Crypto.decrypt("user", "user"));
        System.out.println(Crypto.encrypt("user", "user"));
    }
}
