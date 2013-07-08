#!/bin/bash

# compiling protobuf should be integrated into maven

protoc src/main/resources/META-INF/failure.proto --java_out=src/main/java
protoc --descriptor_set_out=src/main/resources/META-INF/failure.desc src/main/resources/META-INF/failure.proto

protoc src/main/resources/META-INF/void.proto --java_out=src/main/java
protoc --descriptor_set_out=src/main/resources/META-INF/void.desc src/main/resources/META-INF/void.proto

protoc src/test/resources/META-INF/create.proto --java_out=src/test/java
protoc --descriptor_set_out=src/test/resources/META-INF/create.desc src/test/resources/META-INF/create.proto

protoc src/test/resources/META-INF/delete.proto --java_out=src/test/java
protoc --descriptor_set_out=src/test/resources/META-INF/delete.desc src/test/resources/META-INF/delete.proto


