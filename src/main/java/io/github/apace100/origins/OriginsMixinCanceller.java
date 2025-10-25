package io.github.apace100.origins;

import com.bawnorton.mixinsquared.api.MixinCanceller;
import net.fabricmc.loader.api.FabricLoader;

import java.util.List;

public class OriginsMixinCanceller implements MixinCanceller {

    private final List<String> cancelMixins = List.of(
            "com.jamieswhiteshirt.reachentityattributes.mixin.client.ClientPlayerInteractionManagerMixin",
            "com.jamieswhiteshirt.reachentityattributes.mixin.client.GameRendererMixin",
            "com.jamieswhiteshirt.reachentityattributes.mixin.ServerPlayNetworkHandlerMixin",
            "com.jamieswhiteshirt.reachentityattributes.mixin.ItemMixin"
    );
    @Override
    public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
        if(FabricLoader.getInstance().isModLoaded("connectormod") && cancelMixins.contains(mixinClassName)) {
            return true;
        }
        return false;
    }
}
