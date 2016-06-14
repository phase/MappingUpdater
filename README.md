# Mapping Updater
This is a tool to create new mappings based off [jar profiles](https://minecraft16.ml/profiles/).

## Usage
```
mvn package
java -jar target/mapping-updater-0.0.0.jar <old_profile.json> <new_profile.json> <old_mappings.srg> <output.srg>
```