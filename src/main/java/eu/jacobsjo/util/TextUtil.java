package eu.jacobsjo.util;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;

public class TextUtil {

    private static final Language language = Language.getInstance();


    public static MutableComponent translatable(String key) {
        return translatable(key, TranslatableContents.NO_ARGS);
    }

    public static MutableComponent translatable(String key, Object... args){
        return Component.translatableWithFallback(key, language.getOrDefault(key), args);
    }
}
