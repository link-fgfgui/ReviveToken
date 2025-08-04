package io.github.linkfgfgui.revivetoken.mixin;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Screen.class)
public interface ScreenAccessorMixin {

    @Accessor("children")
    List<GuiEventListener> getChildren();

    @Accessor("renderables")
    List<Renderable> getRenderables();

    @Accessor("width")
    int getWidth();

    @Accessor("height")
    int getHeight();

}
