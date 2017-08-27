# songbirdDbApi4j

[![Build Status](https://travis-ci.org/schnatterer/songbirdDbApi4j.svg?branch=master)](https://travis-ci.org/schnatterer/songbirdDbApi4j)
[![JitPack](https://jitpack.io/v/schnatterer/songbirdDbApi4j.svg)](https://jitpack.io/#schnatterer/songbirdDbApi4j)
[![License](https://img.shields.io/github/license/schnatterer/songbirdDbApi4j.svg)](LICENSE)
  
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
    
## Release notes
See [Releases] (https://github.com/schnatterer/songbirdDbApi4j/releases).

## Examples

This API is used in the following projects for example
- [songbirdDbTools](https://github.com/schnatterer/songbirdDbTools)
- [songbird2itunes](https://github.com/schnatterer/songbird2itunes)
