package com.dacubeking.autobuilder.gui.net;

import com.dacubeking.autobuilder.gui.serialization.path.Autonomous;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.io.File;
import java.io.IOException;

public class Serializer {
    static ObjectMapper jsonObjectMapper = new ObjectMapper();
    static ObjectMapper msgPackObjectMapper = new ObjectMapper(new MessagePackFactory());

    public static String serializeToString(Object obj, boolean asJson) throws IOException {
        jsonObjectMapper.configure(SerializationFeature.INDENT_OUTPUT, false);
        return jsonObjectMapper.writeValueAsString(obj);
    }

    public static void serializeToFile(Object obj, File file, boolean asJson) throws IOException {
        if (asJson) {
            jsonObjectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            jsonObjectMapper.writeValue(file, obj);
        } else {
            msgPackObjectMapper.writeValue(file, obj);
        }
    }

    public static Autonomous deserializeAutoFromFile(File file) throws IOException {
        try {
            return msgPackObjectMapper.readValue(file, Autonomous.class);
        } catch (StreamReadException | MismatchedInputException e) {
            try {
                return jsonObjectMapper.readValue(file, Autonomous.class);
            } catch (StreamReadException | MismatchedInputException ex) {
                e.printStackTrace();
                throw ex;
            }
        }
    }

    public static Object deserialize(String object, Class<?> serializableObject) throws IOException {
        try {
            return msgPackObjectMapper.readValue(object, serializableObject);
        } catch (StreamReadException | MismatchedInputException e) {
            try {
                return jsonObjectMapper.readValue(object, serializableObject);
            } catch (StreamReadException | MismatchedInputException ex) {
                e.printStackTrace();
                throw ex;
            }
        }
    }

    public static Autonomous deserializeAuto(String object) throws IOException, ClassNotFoundException {
        try {
            return msgPackObjectMapper.readValue(object, Autonomous.class);
        } catch (StreamReadException | MismatchedInputException e) {
            try {
                return jsonObjectMapper.readValue(object, Autonomous.class);
            } catch (StreamReadException | MismatchedInputException ex) {
                e.printStackTrace();
                throw ex;
            }
        }
    }

    public static Object deserializeFromFile(File file, Class<?> serializableObject) throws IOException {
        try {
            return msgPackObjectMapper.readValue(file, serializableObject);
        } catch (StreamReadException | MismatchedInputException e) {
            try {
                return jsonObjectMapper.readValue(file, serializableObject);
            } catch (StreamReadException | MismatchedInputException ex) {
                e.printStackTrace();
                throw ex;
            }
        }
    }
}
