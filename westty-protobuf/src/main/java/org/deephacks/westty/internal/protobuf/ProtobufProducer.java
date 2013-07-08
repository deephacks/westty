package org.deephacks.westty.internal.protobuf;

import org.deephacks.westty.protobuf.ProtobufSerializer;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class ProtobufProducer {

    @Inject
    private ProtobufExtension extension;

    @Produces
    @Singleton
    public ProtobufSerializer produceSerializer(){
        return extension.getSerializer();
    }

    @Produces
    @Singleton
    public ProtobufEndpoints produceEndpoints(){
        return extension.getEndpoints();
    }
}
