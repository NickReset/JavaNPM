package social.nickrest.npm.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@UtilityClass
public class IOUtils {

    public static String readConnection(@NonNull URLConnection connection) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            return response.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void downloadConnection(HttpURLConnection connection, File directory) {
        try {
            FileUtils.copyInputStreamToFile(connection.getInputStream(), directory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static InputStream getFileStream(File file) {
        try {
            URL url = file.toURI().toURL();
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Accept", "application/octet-stream");

            return connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void deleteDirectoryAndContents(File file) {
        try {
            FileUtils.deleteDirectory(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
