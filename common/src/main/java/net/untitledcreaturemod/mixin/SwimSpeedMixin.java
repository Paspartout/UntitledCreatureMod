package net.untitledcreaturemod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.untitledcreaturemod.creature.pelican.Pelican;
import net.untitledcreaturemod.creature.toad.Toad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class SwimSpeedMixin extends Entity {
    public SwimSpeedMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyVariable(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getDepthStrider(Lnet/minecraft/entity/LivingEntity;)I", ordinal = 0), ordinal = 1)
    public float mixin(float speed) {
        EntityType<?> type = getType();
        // TODO: Investigate if this is fast enough
        if (type == Toad.TOAD.get()) {
            return speed * Toad.SWIM_SPEED;
        } else if (type == Pelican.PELICAN.get()) {
            return speed * Pelican.SWIM_SPEED;
        }
        return speed;
    }
}
