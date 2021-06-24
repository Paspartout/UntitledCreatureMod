/*
 * This file is part of architectury.
 * Copyright (C) 2020, 2021 architectury
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package net.untitledcreaturemod.architectury.mixin.forge;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.extensions.IForgeItem;
import net.untitledcreaturemod.architectury.extensions.ItemExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemExtension.class)
public interface MixinItemExtension extends IForgeItem {
    @Override
    default void onArmorTick(ItemStack stack, World world, PlayerEntity player) {
        ((ItemExtension) this).tickArmor(stack, player);
    }
    
    @Nullable
    @Override
    default EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return ((ItemExtension) this).getCustomEquipmentSlot(stack);
    }
}
