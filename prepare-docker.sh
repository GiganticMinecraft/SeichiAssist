# sbt assembly
mkdir -p docker/spigot/plugins
cp localDependencies/*.jar docker/spigot/plugins/
cp target/build/*.jar docker/spigot/plugins/