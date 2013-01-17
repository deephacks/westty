/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deephacks.westty.protobuf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;

import org.deephacks.westty.protobuf.FailureMessages.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Message;

public class ProtobufSerializer {
    private static final Logger log = LoggerFactory.getLogger(ProtobufSerializer.class);
    private static final int BAD_REQUEST = 1;
    private HashMap<Integer, Method> numToMethod = new HashMap<Integer, Method>();
    private HashMap<String, Integer> protoToNum = new HashMap<String, Integer>();
    private static final String UNRECOGNIZED_PROTOCOL_MSG = "Unrecognized protocol.";

    public ProtobufSerializer() {
        registerResource("META-INF/failure.desc");
    }

    public void register(URL protodesc) {
        try {
            registerDesc(protodesc.getFile(), protodesc.openStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void register(File protodesc) {
        try {
            registerDesc(protodesc.getName(), new FileInputStream(protodesc));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerResource(String protodesc) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(protodesc);
        register(url);
    }

    private void registerDesc(String name, InputStream in) {
        try {
            FileDescriptorSet descriptorSet = FileDescriptorSet.parseFrom(in);
            for (FileDescriptorProto fdp : descriptorSet.getFileList()) {
                FileDescriptor fd = FileDescriptor.buildFrom(fdp, new FileDescriptor[] {});

                for (Descriptor desc : fd.getMessageTypes()) {
                    FieldDescriptor fdesc = desc.findFieldByName("protoType");
                    if (fdesc == null) {
                        throw new IllegalArgumentException(name
                                + ".proto file must define protoType field "
                                + "with unqiue number that identify proto type");
                    }
                    String packageName = fdp.getOptions().getJavaPackage();

                    if (Strings.isNullOrEmpty(packageName)) {
                        throw new IllegalArgumentException(name
                                + ".proto file must define java_package");
                    }
                    String simpleClassName = fdp.getOptions().getJavaOuterClassname();
                    if (Strings.isNullOrEmpty(simpleClassName)) {
                        throw new IllegalArgumentException(name
                                + " .proto file must define java_outer_classname");
                    }

                    String className = packageName + "." + simpleClassName + "$" + desc.getName();
                    Class<?> cls = Thread.currentThread().getContextClassLoader()
                            .loadClass(className);
                    protoToNum.put(desc.getFullName(), fdesc.getNumber());
                    numToMethod.put(fdesc.getNumber(), cls.getMethod("parseFrom", byte[].class));
                    log.debug("Registered protobuf resource {}.", name);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object read(byte[] bytes) throws Exception {
        try {
            ByteBuffer buf = ByteBuffer.wrap(bytes);
            Varint32 vint = new Varint32(buf);
            int protoTypeNum = vint.read();
            buf = vint.getByteBuffer();
            byte[] message = new byte[buf.remaining()];
            buf.get(message);
            Method m = numToMethod.get(protoTypeNum);
            if (m == null) {
                return Failure.newBuilder().setCode(BAD_REQUEST)
                        .setMsg("proto_type=" + protoTypeNum).build();
            }
            return m.invoke(null, message);
        } catch (Exception e) {
            return Failure.newBuilder().setCode(BAD_REQUEST).setMsg(UNRECOGNIZED_PROTOCOL_MSG)
                    .build();
        }
    }

    public byte[] write(Object proto) throws IOException {
        Message msg = (Message) proto;
        String protoName = msg.getDescriptorForType().getFullName();
        Integer num = protoToNum.get(protoName);
        byte[] msgBytes = msg.toByteArray();
        Varint32 vint = new Varint32(num);
        int vsize = vint.getSize();
        byte[] bytes = new byte[vsize + msgBytes.length];
        System.arraycopy(vint.write(), 0, bytes, 0, vsize);
        System.arraycopy(msgBytes, 0, bytes, vsize, msgBytes.length);
        return bytes;
    }
}
