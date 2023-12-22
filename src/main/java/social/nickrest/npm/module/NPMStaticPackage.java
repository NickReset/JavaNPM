package social.nickrest.npm.module;

import lombok.experimental.UtilityClass;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import social.nickrest.npm.NPM;
import social.nickrest.npm.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

@UtilityClass
public class NPMStaticPackage {

    public String getLatestVersion(String packageName) {
        try {
            URL url = new URL(String.format("%s/%s", NPM.BASE_URL, packageName));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            String response = IOUtils.readConnection(connection);
            JSONObject json = (JSONObject) new JSONParser().parse(response);
            JSONObject distTags = (JSONObject) json.get("dist-tags");

            return distTags.get("latest").toString();
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failed to get latest version", e);
        }
    }

    public JSONObject getPackageData(String name, String version) {
        try {
            if(version.equalsIgnoreCase("*")) {
                version = getLatestVersion(name);
            }

            URL url = new URL(String.format("%s/%s/%s", NPM.BASE_URL, name, version));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            String response = IOUtils.readConnection(connection);
            return (JSONObject) new JSONParser().parse(response);
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failed to get package data", e);
        }
    }

    public void downloadTarball(String tarball, File file, Consumer<File> consumer) {
        try {
            URL url = new URL(tarball);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            IOUtils.downloadConnection(connection, file);

            consumer.accept(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to download tarball", e);
        }
    }

}
