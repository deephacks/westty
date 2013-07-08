#!/bin/bash

# compiling protobuf should be integrated into maven

protoc src/main/resources/META-INF/cluster.proto --java_out=src/main/java
protoc --descriptor_set_out=src/main/resources/META-INF/cluster.desc src/main/resources/META-INF/cluster.proto

protoc src/main/resources/META-INF/server.proto --java_out=src/main/java
protoc --descriptor_set_out=src/main/resources/META-INF/server.desc src/main/resources/META-INF/server.proto


