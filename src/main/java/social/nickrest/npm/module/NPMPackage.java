package social.nickrest.npm.module;

import lombok.Getter;
import org.json.simple.JSONObject;
import social.nickrest.npm.NPM;
import social.nickrest.npm.util.IOUtils;
import social.nickrest.npm.util.TarArchiveUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Getter
public class NPMPackage {

    public static List<String> beingInstalled = new ArrayList<>();

    private final NPM parent;
    private final NPMPackageData data;
    private final JSONObject response;

    public boolean await = false;

    public NPMPackage(NPM parent, NPMPackageData data) {
        this.parent = parent;
        this.data = data;
        this.response = NPMStaticPackage.getPackageData(data.name(), data.version());
    }

    public NPMPackage(NPM parent, String name) {
        this(parent, new NPMPackageData(NPMStaticPackage.getLatestVersion(name), name));
    }

    public NPMPackage install(Consumer<NPMPackage> callback) {
        if((parent.isInstalled(data.name()) && !parent.isOutdated(data.name())) || beingInstalled.contains(data.name())) return this;

        String threadName = "NPM-Install-" + data.name() + "@" + data.version();

        Thread installThread = new Thread(() -> {
            JSONObject dist = (JSONObject) response.get("dist");

            if (dist == null) {
                parent.getLogger().warn(String.format("\"dist\" object not found for %s@%s", data.name(), data.version()));
                return;
            }

            try {
                NPMStaticPackage.downloadTarball(dist.get("tarball").toString(), new File(parent.getNodeModulesDir(), String.format("%s-%s.tgz", data.name(), data.version())),
                        (file -> TarArchiveUtil.extract(file, new File(String.format("%s\\%s", parent.getNodeModulesDir(), data.name())), (f) -> {
                    if(file.exists()) {
                        try {
                            Files.delete(file.toPath());
                        } catch (Exception e) {
                            parent.getLogger().error("Failed to delete " + f.getAbsolutePath(), e);
                        }
                    }

                    File moveTo = Objects.requireNonNull(f.listFiles())[0];
                    Path path = Paths.get(moveTo.getAbsolutePath()), parentPath = f.toPath();

                    if(!moveTo.isDirectory()) {
                        parent.getLogger().error("Failed to move " + moveTo.getAbsolutePath() + " to " + parent.getNodeModulesDir().getAbsolutePath());
                        return;
                    }

                    try {
                        for(Path p : Files.newDirectoryStream(path)) {
                            Files.move(p, parentPath.resolve(p.getFileName()));
                        }

                        IOUtils.deleteDirectoryAndContents(path.toFile());
                    } catch (Exception e) {
                        parent.getLogger().error("Failed to move " + moveTo.getAbsolutePath() + " to " + parent.getNodeModulesDir().getAbsolutePath(), e);
                        return;
                    }

                    callback.accept(this);
                    beingInstalled.remove(data.name());
                })));
            } catch (Exception e) {
                parent.getLogger().error("Failed to download " + data.name() + "@" + data.version() + " tarball", e);
            }
        });

        if(this.hasDependencies()) {
            for(NPMPackage dependency : this.getDependencies()
                    .stream()
                    .filter(dependency -> !parent.isInstalled(dependency.getName()) || parent.isOutdated(dependency.getName()))
                    .toArray(NPMPackage[]::new)) {

                dependency.install((npmPackage) -> parent.getLogger().info(String.format("Installed %s@%s", npmPackage.getName(), npmPackage.getVersion())));
            }
        }

        beingInstalled.add(data.name());
        installThread.setName(threadName);
        installThread.start();

        if(await) {
            try {
                installThread.join();
            } catch (Exception e) {
                parent.getLogger().error("Failed to join " + threadName, e);
            }
        }

        return this;
    }

    public NPMPackage install() {
        return install(npmPackage -> parent.getLogger().info(String.format("Installed %s@%s", npmPackage.getName(), npmPackage.getVersion())));
    }

    public ArrayList<NPMPackage> getDependencies() {
        ArrayList<NPMPackage> dependencies = new ArrayList<>();

        JSONObject dependenciesObject = (JSONObject) response.get("dependencies");

        if(dependenciesObject == null) {
            return dependencies;
        }

        for(Object name : dependenciesObject.keySet()) {
            String toVersion = dependenciesObject.get(name).toString();

            if(toVersion.contains("*") || toVersion.contains(">=") || toVersion.contains("^") || toVersion.contains("~")) {
                toVersion = NPMStaticPackage.getLatestVersion(name.toString());
            }

            dependencies.add(new NPMPackage(parent, new NPMPackageData(toVersion, name.toString())));
        }

        return dependencies;
    }

    public NPMPackage await() {
        this.await = true;
        return this;
    }

    public boolean hasDependencies() {
        return !getDependencies().isEmpty();
    }

    public String getName() {
        return data.name();
    }

    public String getVersion() {
        return data.version();
    }

    @Override
    public String toString() {
        return String.format("%s@%s", data.name(), data.version());
    }
}
