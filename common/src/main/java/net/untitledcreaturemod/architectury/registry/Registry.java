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

package net.untitledcreaturemod.architectury.registry;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;

public interface Registry<T> extends Iterable<T> {
    default Supplier<T> delegate(Identifier id) {
        return delegateSupplied(id);
    }
    
    RegistrySupplier<T> delegateSupplied(Identifier id);
    
    default <E extends T> Supplier<E> register(Identifier id, Supplier<E> supplier) {
        return registerSupplied(id, supplier);
    }
    
    <E extends T> RegistrySupplier<E> registerSupplied(Identifier id, Supplier<E> supplier);
    
    @Nullable
    Identifier getId(T obj);
    
    int getRawId(T obj);
    
    Optional<RegistryKey<T>> getKey(T obj);
    
    @Nullable
    T get(Identifier id);
    
    @Nullable
    T byRawId(int rawId);
    
    boolean contains(Identifier id);
    
    boolean containsValue(T obj);
    
    Set<Identifier> getIds();
    
    Set<Map.Entry<RegistryKey<T>, T>> entrySet();
    
    RegistryKey<? extends net.minecraft.util.registry.Registry<T>> key();
}
