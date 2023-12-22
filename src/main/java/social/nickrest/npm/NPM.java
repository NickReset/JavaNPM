package social.nickrest.npm;

import lombok.Getter;
import lombok.Setter;
import social.nickrest.npm.module.InstalledNPMPackage;
import social.nickrest.npm.module.NPMPackage;

import java.io.File;

@Getter @Setter
public class NPM {

    public static final String BASE_URL = "https://registry.npmjs.org";

    private final File nodeModulesDir;
    private NPMLogger logger;

    public NPM(File nodeModulesDir) {
        this.nodeModulesDir = nodeModulesDir;
        this.logger = new DefualtNPMLogger();
    }

    public NPMPackage getPackage(String name) {
        return new NPMPackage(this, name);
    }

    public InstalledNPMPackage getInstalledPackage(String name) {
        File dir = new File(nodeModulesDir, name);

        if(!dir.exists()) return null;

        return new InstalledNPMPackage(this, dir);
    }

    public boolean isOutdated(String name) {
        InstalledNPMPackage installed = getInstalledPackage(name);

        if(installed == null) return false;

        return installed.getPackageJson() == null || !installed.getVersion().equals(getPackage(name).getVersion());
    }

    public boolean isInstalled(String name) {
        InstalledNPMPackage installed = getInstalledPackage(name);

        if(installed == null) return false;

        return installed.getPackageJson() != null;
    }

}
