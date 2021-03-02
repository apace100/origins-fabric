package io.github.apace100.origins.power;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class PowerTypeRegistry {
    private static HashMap<Identifier, PowerType<?>> idToPower = new HashMap<>();

    public static <T extends Power> PowerType<T> register(Identifier id, PowerType<T> powerType) {
        if(idToPower.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate power type id tried to register: '" + id.toString() + "'");
        }
        idToPower.put(id, powerType);
        return powerType;
    }

    protected static <T extends Power> PowerType<T> update(Identifier id, PowerType<T> powerType) {
        if(idToPower.containsKey(id)) {
            PowerType<?> old = idToPower.get(id);
            idToPower.remove(id);
        }
        return register(id, powerType);
    }

    public static int size() {
        return idToPower.size();
    }

    public static Stream<Identifier> identifiers() {
        return idToPower.keySet().stream();
    }

    public static Iterable<Map.Entry<Identifier, PowerType<?>>> entries() {
        return idToPower.entrySet();
    }

    public static Iterable<PowerType<?>> values() {
        return idToPower.values();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Power> PowerType<T> get(Identifier id) {
        if(!idToPower.containsKey(id)) {
            throw new IllegalArgumentException("Could not get power type from id '" + id.toString() + "', as it was not registered!");
        }
        return (PowerType<T>) idToPower.get(id);
    }

    public static Identifier getId(PowerType<?> powerType) {
        return powerType.getIdentifier();
    }

    public static boolean contains(Identifier id) {
        return idToPower.containsKey(id);
    }

    public static void clear() {
        idToPower.clear();
    }

    public static void reset() {
        clear();
    }
}
