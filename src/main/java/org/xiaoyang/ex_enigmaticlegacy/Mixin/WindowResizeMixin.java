package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

@Mixin(Window.class)
public class WindowResizeMixin {
    @Inject(method = "onFramebufferResize", at = @At("RETURN"))
    private void ex_enigmaticlegacy$onFramebufferResize(long window, int width, int height, CallbackInfo ci) {
        if (Exe.POST != null) {
            Exe.POST.onFramebufferResize(width, height);
        }
    }
}
