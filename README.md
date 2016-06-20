# songbirdDbApi4j
[![Build Status](https://jenkins.schnatterer.info/job/songbirdDbApi4j/badge/icon)](https://jenkins.schnatterer.info/job/songbirdDbApi4j/)

A java wrapper for songbird SQLite database.

## How to use
For now, this is not hosted on maven central, but on this very repository. To use it, you have two options:

1. Add this repo as maven repository
2. Use jitPack

See the [wiki](https://github.com/schnatterer/songbirdDbApi4j/wiki) for details on how to use the API or see the  [songbirdDbTools](https://github.com/schnatterer/songbirdDbTools) that makes use of this API.

### Add this repo as maven repository
Add the following maven repository to your POM.xml

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

### Use jitPack
Add the following maven repository to your POM.xml

    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
Then add the actual dependency

        <dependency>
            <groupId>com.github.schnatterer</groupId>
            <artifactId>songbirdDbApi4j</artifactId>
            <version>v.2.0</version>
        </dependency>
    
## History
###Version 1.0
Very first version that provides very basic functionality for reading playlists from the database.

## Examples

This API is used in the following projects for example
- [songbirdDbTools](https://github.com/schnatterer/songbirdDbTools)
- [songbird2itunes](https://github.com/schnatterer/songbird2itunes)
