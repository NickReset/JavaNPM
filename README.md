# Example

## Install a package
when initializing the NPM class you can specify the directory where the node_modules folder is located
```java
NPM npm = new NPM(new File("node_modules"));
    npm.getPackage("express")
    .await()
    .install((npmPackage) -> 
        npm.getLogger().info(String.format("Installed %s@%s", npmPackage.getName(), npmPackage.getVersion())
    ));
```
the output will be "Installed express@${latestVersion}"

## Uninstall a package
```java
NPM npm = new NPM(new File("node_modules"));

InstalledNPMPackage express = npm.getInstalledPackage("express");
if (express != null) {
    express.uninstall((npmPackage) ->
        npm.getLogger().info(String.format("Uninstalled %s@%s", npmPackage.getName(), npmPackage.getVersion())
    ));
}
```
the output will be "Uninstalled express@${latestVersion}"

## check if a package is installed
```java
NPM npm = new NPM(new File("node_modules"));

if(npm.isInstalled("express")) {
    npm.getLogger().info("Express is installed!");
}
```
the output will either be "Express is installed" or nothing