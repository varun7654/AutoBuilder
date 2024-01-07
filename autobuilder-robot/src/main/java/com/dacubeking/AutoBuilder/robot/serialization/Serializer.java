package com.dacubeking.AutoBuilder.robot.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import edu.wpi.first.math.spline.Spline.ControlVector;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.io.File;
import java.io.IOException;

public class Serializer {
    static ObjectMapper jsonObjectMapper = new ObjectMapper();
    static ObjectMapper msgPackObjectMapper = new ObjectMapper(new MessagePackFactory());

    static {
        // On the gui we overwrite the spline class with our own, so we need to ignore the spline class when deserializing on
        // the robot
        var splineFailureHandler = new DeserializationProblemHandler() {
            @Override
            public Object handleMissingInstantiator(DeserializationContext ctxt, Class<?> instClass, ValueInstantiator valueInsta,
                                                    JsonParser p, String msg) throws IOException {
                if (instClass.isAssignableFrom(ControlVector.class)) {
                    while (p.nextToken() != JsonToken.END_OBJECT) {
                    }
                    return null;
                }
                return super.handleMissingInstantiator(ctxt, instClass, valueInsta, p, msg);
            }
        };

        jsonObjectMapper.addHandler(splineFailureHandler);
        msgPackObjectMapper.addHandler(splineFailureHandler);
    }

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

    public static Object deserialize(String object, Class<?> serializableObject, boolean asJson) throws IOException {
        if (asJson) {
            return jsonObjectMapper.readValue(object, serializableObject);
        } else {
            return msgPackObjectMapper.readValue(object, serializableObject);
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

    public static Object deserializeFromFile(File file, Class<?> serializableObject, boolean asJson) throws IOException {
        if (asJson) {
            return jsonObjectMapper.readValue(file, serializableObject);
        } else {
            return msgPackObjectMapper.readValue(file, serializableObject);
        }
    }
}
