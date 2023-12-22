package social.nickrest.npm.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

@UtilityClass
public class TarArchiveUtil {

    public void extract(File archive, File dest, Consumer<File> callback) {
        if(!dest.exists() && !dest.mkdirs()) {
            throw new RuntimeException("Failed to create directory");
        }

        InputStream is = IOUtils.getFileStream(archive);

        if (is == null) {
            throw new RuntimeException("Failed to get file stream");
        }

        try {
            TarArchiveInputStream tar = new TarArchiveInputStream(new GzipCompressorInputStream(is));

            ArchiveEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                Path extractTo = dest.toPath().resolve(entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(extractTo);
                } else {
                    if(extractTo.getParent() != null && !Files.exists(extractTo.getParent())) {
                        Files.createDirectories(extractTo.getParent());
                    }

                    if(Files.exists(extractTo) && !Files.isDirectory(extractTo)) {
                        try {
                            Files.delete(extractTo);
                        } catch (Exception ignored) {} // some other process already moved/deleted the file
                    }

                    Files.copy(tar, extractTo);
                }
            }
            tar.close();

            callback.accept(dest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
