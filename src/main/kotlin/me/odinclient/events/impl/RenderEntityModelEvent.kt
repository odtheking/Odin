package me.odinclient.events.impl

import net.minecraft.client.model.ModelBase
import net.minecraft.entity.EntityLivingBase

/**
 * @see me.odinclient.mixin.MixinRendererLivingEntity.renderModel
 */
class RenderEntityModelEvent(
    var entity: EntityLivingBase,
    var limbSwing: Float,
    var limbSwingAmount: Float,
    var ageInTicks: Float,
    var headYaw: Float,
    var headPitch: Float,
    var scaleFactor: Float,
    var model: ModelBase
)