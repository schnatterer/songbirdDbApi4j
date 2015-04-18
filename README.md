# itunes4j
A java wrapper for the windows COM API of iTunes. That is, this will only work on windows.

## How to use
For now, this is not hosted on maven central, but on this very repository. To use it, add the following maven repository to your POM.xml

    <repositories>
        <repository>
            <id>itunes4j-mvn-repo</id>
            <url>https://raw.github.com/schnatterer/itunes4j/mvn-repo/</url>
        </repository>
    </repositories>
Then add the actual dependency

    <dependency>
        <groupId>info.schnatterer</groupId>
        <artifactId>itunes4j</artifactId>
        <version>1.0</version>
    </dependency>

See the [wiki](https://github.com/schnatterer/itunes4j/wiki) for details on how to use the API.
    
## History
###Version 1.0
Very first version that provides very basic functionality for adding tracks and playlists to iTunes.   
Tested and compiled against iTunes 12.1.2.27 64-Bit.