package me.odinmain.events.impl

import net.minecraft.client.model.ModelBase
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

@Cancelable
data class RenderEntityModelEvent(
    var entity: EntityLivingBase,
    var limbSwing: Float,
    var limbSwingAmount: Float,
    var ageInTicks: Float,
    var headYaw: Float,
    var headPitch: Float,
    var scaleFactor: Float,
    var model: ModelBase
): Event()