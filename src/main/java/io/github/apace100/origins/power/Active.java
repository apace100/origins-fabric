package io.github.apace100.origins.power;

public interface Active {

    void onUse();
    KeyType getKey();
    void setKey(KeyType type);

    enum KeyType {
        PRIMARY, SECONDARY
    }
}
