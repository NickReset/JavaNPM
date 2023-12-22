package social.nickrest.npm.module;

import lombok.Getter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import social.nickrest.npm.NPM;
import social.nickrest.npm.util.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.util.function.Consumer;

@Getter
public class InstalledNPMPackage {

    private final NPM parent;
    private final File dir;

    private JSONObject packageJson;
    private NPMPackageData data;

    public InstalledNPMPackage(NPM parent, File dir) {
        this.parent = parent;
        this.dir = dir;

        StringBuilder builder = new StringBuilder();

        File packageJson = new File(dir, "package.json");
        try(FileReader reader = new FileReader(packageJson)) {
            int i;
            while((i = reader.read()) != -1) {
                builder.append((char) i);
            }

            this.packageJson = (JSONObject) new JSONParser().parse(builder.toString());
            this.data = new NPMPackageData((String) this.packageJson.get("version"), (String) this.packageJson.get("name"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void uninstall(Consumer<InstalledNPMPackage> callback) {
        IOUtils.deleteDirectoryAndContents(dir);

        callback.accept(this);
    }

    public void uninstall() {
        uninstall((p) -> parent.getLogger().info(String.format("Uninstalled %s@%s", p.getName(), p.getVersion())));
    }

    public String getVersion() {
        return this.data.version();
    }

    public String getName() {
        return this.data.name();
    }
}
