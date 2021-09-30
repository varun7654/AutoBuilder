package me.varun.autobuilder.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import me.varun.autobuilder.serialization.Autonomous;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Serializer {
    static ObjectMapper objectMapper = new ObjectMapper();
    static {
        //objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    public static String serializeToString(Autonomous obj) throws IOException {
        String data = objectMapper.writeValueAsString(obj);
        System.out.println(data);
        return data;
    }

    public static void serializeToFile(Autonomous obj, File file) throws IOException {
        objectMapper.writeValue(file, obj);
    }

    public static Autonomous deserializeFromFile(File file) throws IOException {
        return objectMapper.readValue(file, Autonomous.class);
    }

    public static Object deserialize(String object) throws IOException, ClassNotFoundException {
       return objectMapper.readValue(object, Autonomous.class);
    }

}
