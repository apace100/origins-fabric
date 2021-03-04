package io.github.apace100.origins.power;

public interface Active {

    void onUse();
    Key getKey();
    void setKey(Key key);

    class Key {

        public String key = "key.origins.primary_active";
        public boolean continuous = false;
    }
}
