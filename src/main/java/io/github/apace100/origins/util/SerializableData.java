package io.github.apace100.origins.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import net.minecraft.network.PacketByteBuf;

import java.util.HashMap;
import java.util.Map;

public class SerializableData {

    private HashMap<String, Entry<?>> dataFields = new HashMap<>();

    public SerializableData add(String name, SerializableDataType<?> type) {
        dataFields.put(name, new Entry<>(type));
        return this;
    }

    public <T> SerializableData add(String name, SerializableDataType<T> type, T defaultValue) {
        dataFields.put(name, new Entry<>(type, defaultValue));
        return this;
    }

    public void write(PacketByteBuf buffer, Instance instance) {
        dataFields.forEach((name, entry) -> {
            boolean isPresent = instance.get(name) != null;
            if(entry.hasDefault && entry.defaultValue == null) {
                buffer.writeBoolean(isPresent);
            }
            if(isPresent) {
                entry.dataType.send(buffer, instance.get(name));
            }
        });
    }

    public Instance read(PacketByteBuf buffer) {
        Instance instance = new Instance();
        dataFields.forEach((name, entry) -> {
            boolean isPresent = true;
            if(entry.hasDefault && entry.defaultValue == null) {
                isPresent = buffer.readBoolean();
            }
            instance.set(name, isPresent ? entry.dataType.receive(buffer) : null);
        });
        return instance;
    }

    public Instance read(JsonObject jsonObject) {
        Instance instance = new Instance();
        try {
            dataFields.forEach((name, entry) -> {
                if(!jsonObject.has(name)) {
                    if(entry.hasDefault) {
                        instance.set(name, entry.defaultValue);
                    } else {
                        throw new JsonSyntaxException("JSON requires field: " + name);
                    }
                } else {
                    instance.set(name, entry.dataType.read(jsonObject.get(name)));
                }
            });
        } catch(JsonParseException | ClassCastException e) {
            throw new JsonSyntaxException("A problem occured while reading data of "
                + getClass().getSimpleName() + ": " + e.getMessage());
        }
        return instance;
    }

    public class Instance {
        private HashMap<String, Object> data = new HashMap<>();

        Instance() {

        }

        public void debugPrint() {
            Origins.LOGGER.info("Debug Printing Data Instance:");
            Origins.LOGGER.info("-----------------------------");
            for (Map.Entry<String, Object> entry: data.entrySet()) {
                Origins.LOGGER.info(entry.getKey() + "\t=\t" + entry.getValue().toString());
            }
            Origins.LOGGER.info("-----------------------------");
        }

        public boolean isPresent(String name) {
            if(dataFields.containsKey(name)) {
                Entry<?> entry = dataFields.get(name);
                if(entry.hasDefault && entry.defaultValue == null) {
                    return get(name) != null;
                }
            }
            return true;
        }

        public void set(String name, Object value) {
            this.data.put(name, value);
        }

        public Object get(String name) {
            if(!data.containsKey(name)) {
                throw new RuntimeException("Tried to get field \"" + name + "\" from data, which did not exist.");
            }
            return data.get(name);
        }

        public int getInt(String name) {
            return (int)get(name);
        }

        public boolean getBoolean(String name) {
            return (boolean)get(name);
        }

        public float getFloat(String name) {
            return (float)get(name);
        }

        public double getDouble(String name) {
            return (double)get(name);
        }

        public String getString(String name) {
            return (String)get(name);
        }
    }

    private static class Entry<T> {
        public final SerializableDataType<T> dataType;
        public final Object defaultValue;
        public final boolean hasDefault;

        public Entry(SerializableDataType<T> dataType) {
            this.dataType = dataType;
            this.defaultValue = null;
            this.hasDefault = false;
        }

        public Entry(SerializableDataType<T> dataType, T defaultValue) {
            this.dataType = dataType;
            this.defaultValue = defaultValue;
            this.hasDefault = true;
        }
    }
}
