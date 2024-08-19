allprojects {
    // set version inside gradle.properties
    version = project.findProperty("version") as String
}
