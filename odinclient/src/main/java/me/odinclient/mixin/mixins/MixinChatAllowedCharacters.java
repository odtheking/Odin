package me.odinclient.mixin.mixins;

import net.minecraft.util.ChatAllowedCharacters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ChatAllowedCharacters.class)
public class MixinChatAllowedCharacters {

    /**
     * @author Cezar
     * @reason Allow '§' in text fields
     */
    @Overwrite
    public static boolean isAllowedCharacter(char character) {
        return character >= ' ' && character != '\u007f';
    }

}
