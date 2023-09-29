package com.dacubeking.AutoBuilder.robot.serialization;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.jetbrains.annotations.NotNull;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.io.File;
import java.io.IOException;

public class Serializer {
    static @NotNull ObjectMapper jsonObjectMapper = new ObjectMapper();
    static @NotNull ObjectMapper msgPackObjectMapper = new ObjectMapper(new MessagePackFactory());

    public static String serializeToString(Object obj, boolean asJson) throws IOException {
        jsonObjectMapper.configure(SerializationFeature.INDENT_OUTPUT, false);
        return jsonObjectMapper.writeValueAsString(obj);
    }

    public static void serializeToFile(Object obj, File file, boolean asJson) throws IOException {
        jsonObjectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        jsonObjectMapper.writeValue(file, obj);
    }

    public static Autonomous deserializeAutoFromFile(File file) throws IOException {
        try {
            return msgPackObjectMapper.readValue(file, Autonomous.class);
        } catch (StreamReadException e) {
            try {
                return jsonObjectMapper.readValue(file, Autonomous.class);
            } catch (StreamReadException ex) {
                e.printStackTrace();
                throw ex;
            }
        }
    }

    public static Object deserialize(String object, Class<?> serializableObject, boolean asJson) throws IOException {
        if (asJson) {
            return jsonObjectMapper.readValue(object, serializableObject);
        } else {
            return msgPackObjectMapper.readValue(object, serializableObject);
        }
    }

    public static Object deserializeFromFile(File file, Class<?> serializableObject, boolean asJson) throws IOException {
        if (asJson) {
            return jsonObjectMapper.readValue(file, serializableObject);
        } else {
            return msgPackObjectMapper.readValue(file, serializableObject);
        }
    }
}
