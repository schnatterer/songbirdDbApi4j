# songbirdDbApi4j
A java wrapper for songbird SQLite database.

## How to use
For now, this is not hosted on maven central, but on this very repository. To use it, add the following maven repository to your POM.xml

    <repositories>
        <repository>
            <id>songbirdDbApi4j-mvn-repo</id>
            <url>https://raw.github.com/schnatterer/songbirdDbApi4j/mvn-repo/</url>
        </repository>
    </repositories>
Then add the actual dependency

    <dependency>
        <groupId>info.schnatterer</groupId>
        <artifactId>songbirdDbApi4j</artifactId>
        <version>1.0</version>
    </dependency>

See the [wiki](https://github.com/schnatterer/songbirdDbApi4j/wiki) for details on how to use the API or see the  [songbirdDbTools](https://github.com/schnatterer/songbirdDbTools) that makes use of this API.
    
## History
###Version 1.0
Very first version that provides very basic functionality for reading playlists from the database.
