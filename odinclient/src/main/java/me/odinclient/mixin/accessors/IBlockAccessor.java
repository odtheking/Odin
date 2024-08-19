package me.odinclient.mixin.accessors;

import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Block.class)
public interface IBlockAccessor {

    @Accessor
    void setMinX(double maxY);

    @Accessor
    void setMinY(double maxY);

    @Accessor
    void setMinZ(double maxY);

    @Accessor
    void setMaxX(double maxY);

    @Accessor
    void setMaxY(double maxY);

    @Accessor
    void setMaxZ(double maxY);

}
