package io.github.apace100.origins.power;

public interface Active {

    void onUse();
    String getKey();
    void setKey(String key);

    enum KeyType {
        PRIMARY, SECONDARY
    }
}
