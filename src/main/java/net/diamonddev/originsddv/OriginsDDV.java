package net.diamonddev.originsddv;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.diamonddev.libgenetics.common.api.LibGeneticsEntrypointApi;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;

public class OriginsDDV implements ModInitializer, LibGeneticsEntrypointApi {
    @Override
    public void onInitialize() {

    }

    @Override
    public void addLibGeneticsCommandBranches(LiteralArgumentBuilder<ServerCommandSource> root, ArrayList<ArgumentBuilder<ServerCommandSource, ?>> branches) {

    }
}
