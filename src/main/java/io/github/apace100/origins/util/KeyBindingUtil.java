package io.github.apace100.origins.util;

import io.github.apace100.origins.mixin.KeyBindingAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class KeyBindingUtil {

    /**
     *  Get the localized name of the keybind from the specified ID. If no such keybind exists or if the keybind is
     *  not bound to any key, use the specified ID instead.
     *
     *  @param id   The ID of the keybind to get the localized name of.
     *  @return     Either a {@linkplain Text text} that is localized, or a {@linkplain net.minecraft.text.TranslatableTextContent translatable text}
     *                  that contains the specified ID.
     */
    public static Text getLocalizedName(String id) {

        KeyBinding keyBinding = KeyBindingAccessor.getKeysById().get(id);
        if (keyBinding == null || keyBinding.isUnbound()) {
            return Text.translatable(id).styled(style -> style.withItalic(true));
        }

        return keyBinding.getBoundKeyLocalizedText();

    }

}
