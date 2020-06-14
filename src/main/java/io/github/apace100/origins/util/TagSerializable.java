package io.github.apace100.origins.util;

import net.minecraft.nbt.Tag;

public interface TagSerializable {

    Tag toTag();
    void fromTag(Tag tag);
}
