package io.github.apace100.origins.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.function.Function;

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

    public <T> SerializableData addFunctionedDefault(String name, SerializableDataType<T> type, Function<Instance, T> defaultFunction) {
        dataFields.put(name, new Entry<>(type, defaultFunction));
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
                    if(entry.hasDefault()) {
                        instance.set(name, entry.getDefault(instance));
                    } else {
                        throw new JsonSyntaxException("JSON requires field: " + name);
                    }
                } else {
                    instance.set(name, entry.dataType.read(jsonObject.get(name)));
                }
            });
        } catch(JsonParseException | ClassCastException e) {
            throw new JsonSyntaxException(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        return instance;
    }

    public class Instance {
        private HashMap<String, Object> data = new HashMap<>();

        public Instance() {

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

        public Identifier getId(String name) {
            return (Identifier)get(name);
        }

        public EntityAttributeModifier getModifier(String name) {
            return (EntityAttributeModifier)get(name);
        }
    }

    private static class Entry<T> {
        public final SerializableDataType<T> dataType;
        public final T defaultValue;
        private final Function<Instance, T> defaultFunction;
        private final boolean hasDefault;
        private final boolean hasDefaultFunction;

        public Entry(SerializableDataType<T> dataType) {
            this.dataType = dataType;
            this.defaultValue = null;
            this.defaultFunction = null;
            this.hasDefault = false;
            this.hasDefaultFunction = false;
        }

        public Entry(SerializableDataType<T> dataType, T defaultValue) {
            this.dataType = dataType;
            this.defaultValue = defaultValue;
            this.defaultFunction = null;
            this.hasDefault = true;
            this.hasDefaultFunction = false;
        }

        public Entry(SerializableDataType<T> dataType, Function<Instance, T> defaultFunction) {
            this.dataType = dataType;
            this.defaultValue = null;
            this.defaultFunction = defaultFunction;
            this.hasDefault = false;
            this.hasDefaultFunction = true;
        }

        public boolean hasDefault() {
            return hasDefault || hasDefaultFunction;
        }

        public T getDefault(Instance dataInstance) {
            if(hasDefaultFunction) {
                return defaultFunction.apply(dataInstance);
            } else if(hasDefault) {
                return defaultValue;
            } else {
                throw new IllegalStateException("Tried to access default value of serializable data entry, when no default was provided.");
            }
        }
    }
}
