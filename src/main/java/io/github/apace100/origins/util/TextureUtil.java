package io.github.apace100.origins.util;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.Origin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.GuiAtlasManager;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class TextureUtil {

    private static final List<Identifier> LOGGED = new LinkedList<>();

    public static void resetLoggedIds() {
        LOGGED.clear();
    }

    public static Identifier getTexture(Origin origin, Identifier textureName) {
        Map<Identifier, Identifier> textures = origin.getTextures();
        Identifier strippedIdentifier = textureName.withPath(textureName.getPath().replace("textures/gui/sprites/", "").replace(".png", ""));
        if(textures.containsKey(strippedIdentifier)) {
            Identifier textureID = textures.get(strippedIdentifier);
            textureID = textureID.withPath("textures/gui/sprites/" + textureID.getPath() + ".png");
            Optional<Identifier> identifier = getOptionalId(textureID);
            if(identifier.isPresent()) {
                return identifier.get();
            }
        }
        return textureName;
    }

    public static Identifier getGUITexture(Origin origin, Identifier textureName) {
        if(origin == null) return textureName;
        Map<Identifier, Identifier> textures = origin.getTextures();
        if(textures.containsKey(textureName)) {
            Identifier textureID = textures.get(textureName);
            Optional<Identifier> identifier = getOptionalId(textureID);
            if(identifier.isPresent()) {
                return identifier.get();
            };
        }
        return textureName;
    }

    private static Optional<Identifier> getOptionalId(Identifier identifier) {
        GuiAtlasManager atlasManager = MinecraftClient.getInstance().getGuiAtlasManager();
        if(!atlasManager.getSprite(identifier).equals(atlasManager.getSprite(MissingSprite.getMissingSpriteId()))) {
            return Optional.of(identifier);
        } else if(!LOGGED.contains(identifier)){
            Origins.LOGGER.warn("Origin Screen is missing texture: \"" + identifier + "\". Are you missing a resource pack?");
            LOGGED.add(identifier);
        }
        return Optional.empty();
    }

}
