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

package net.untitledcreaturemod.architectury.hooks;

import me.shedaniel.architectury.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class ScreenHooks {
    private ScreenHooks() {
    }
    
    @ExpectPlatform
    public static List<AbstractButtonWidget> getButtons(Screen screen) {
        throw new AssertionError();
    }
    
    @ExpectPlatform
    public static <T extends AbstractButtonWidget> T addButton(Screen screen, T widget) {
        throw new AssertionError();
    }
    
    @ExpectPlatform
    public static <T extends Element> T addChild(Screen screen, T listener) {
        throw new AssertionError();
    }
}
