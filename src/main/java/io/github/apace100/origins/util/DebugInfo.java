package io.github.apace100.origins.util;

import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;

public class DebugInfo {

    public static void printRegistrySizes(String at) {
        printInfo(new String[] {
            "Registry Size at " + at,
            "Origins: " + OriginRegistry.size(),
            "Layers:  " + OriginLayers.size(),
            "Powers:  " + PowerTypeRegistry.size()
        });
    }
    private static void printInfo(String[] lines) {
        int longest = 0;
        for(int i = 0; i < lines.length; i++) {
            if(lines[i].length() > longest)
                longest = lines[i].length();
            lines[i] = "| " + lines[i];
        }
        String border = "+";
        for(int i = 0; i < longest + 2; i++) {
            border += "-";
        }
        border += "+";
        Origins.LOGGER.info(border);
        for(int i = 0; i < lines.length; i++) {
            while(lines[i].length() < longest + 3)
                lines[i] += " ";
            lines[i] += "|";
            Origins.LOGGER.info(lines[i]);
        }
        Origins.LOGGER.info(border);
    }
}
