package me.odinclient.mixin.mixins;

import me.odinmain.events.dsl.EventDSL;
import me.odinmain.events.dsl.Listener;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(EventBus.class)
public class MixinEventBus {

    @Shadow
    private ConcurrentHashMap<Object, ArrayList<IEventListener>> listeners;

    @Final
    @Shadow
    private int busID;

    @Inject(method = "register(Ljava/lang/Object;)V", at = @At("RETURN"), remap = false)
    private void onRegister(Object obj, CallbackInfo ci) {
        if (obj instanceof EventDSL) {
            ArrayList<IEventListener> list = listeners.computeIfAbsent(obj, k -> new ArrayList<>());
            ArrayList<Listener<?>> dslListeners = ((EventDSL) obj).getListeners();
            list.addAll(dslListeners);

            for (Listener<?> listener : dslListeners) {
                try {
                    Event event = listener.getEvent().getConstructor().newInstance();
                    event.getListenerList().register(busID, EventPriority.NORMAL, listener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
