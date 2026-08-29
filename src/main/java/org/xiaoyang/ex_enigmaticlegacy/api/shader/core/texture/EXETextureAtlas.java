package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;

@OnlyIn(value = Dist.CLIENT)
public class EXETextureAtlas {
    public static TextureAtlas EXE_TOOL_ATLAS;
    public static final ResourceLocation EXE_TOOL_ATLAS_LOCATION = new ResourceLocation(Exe.MODID, "exe_tool_atlas.png"); // Exe 自定义图集位置。

    public static final ResourceLocation TRAIL1_TEXTURE = new ResourceLocation(Exe.MODID, "atlases/trial_1"); // 飞剑拖尾基础贴图。
    public static final ResourceLocation TRAIL1_NOISE_TEXTURE = new ResourceLocation(Exe.MODID, "atlases/trial_1_noise"); // 飞剑拖尾噪声贴图。
    public static final ResourceLocation fx_noise015 = new ResourceLocation(Exe.MODID, "atlases/fx_noise015"); // 火焰描边噪声贴图。
    public static final ResourceLocation noise_002_128x = new ResourceLocation(Exe.MODID, "atlases/noise_002_128x"); // Miao 火焰描边噪声贴图。
    public static final ResourceLocation cellnoise_a = new ResourceLocation(Exe.MODID, "atlases/cellnoise_a"); // 通用细胞噪声贴图。
    public static final ResourceLocation T_FX_TILE_0012_TEXTURE = new ResourceLocation(Exe.MODID, "atlases/t_fx_tile_0012"); // 手持飞剑流动效果第一张噪声贴图。
    public static final ResourceLocation T_FX_TILE_0137_MOON_TEXTURE = new ResourceLocation(Exe.MODID, "atlases/tile_0137_moon"); // 手持飞剑 RG UV 扰动噪声贴图。
    public static final ResourceLocation multi_gradient = new ResourceLocation(Exe.MODID, "atlases/multi_gradient"); // 通用多色渐变贴图。
    public static final ResourceLocation BLUE_GRADIENT_TEXTURE = new ResourceLocation(Exe.MODID, "atlases/blue_gradient"); // 剑气蓝白渐变贴图。
    public static final ResourceLocation yellow_gradient = new ResourceLocation(Exe.MODID, "atlases/yellow_gradient"); // 黄色渐变贴图。
    public static final ResourceLocation SWORD_AURA_TEXTURE = new ResourceLocation(Exe.MODID, "atlases/sword1"); // 剑气 OBJ 贴图。
    public static final ResourceLocation BATTO_TEX_B_TEXTURE = new ResourceLocation(Exe.MODID, "atlases/t_noise_1_128x"); // 拔刀斩 daoguang 暖色自发光贴图。
    public static final ResourceLocation BATTO_MASK_TEXTURE = new ResourceLocation(Exe.MODID, "atlases/t_radial_mask_128x"); // 拔刀斩 daoguang 透明遮罩贴图。
    public static final ResourceLocation LIGHTNING_TEXTURE = new ResourceLocation(Exe.MODID, "atlases/lightning_128x"); // 闪电主纹理 sprite，透明背景由 Alpha 控制形状。
    public static final ResourceLocation LIGHTNING_NOISE_TEXTURE = new ResourceLocation(Exe.MODID, "atlases/noise_076_128x"); // 闪电扰动噪声 sprite，使用 G 通道驱动 UV 偏移。
    public static final ResourceLocation noise_092_128x = new ResourceLocation(Exe.MODID, "atlases/noise_092_128x"); // noise_092_128x 噪声贴图。
    public static final ResourceLocation tex_pattern66 = new ResourceLocation(Exe.MODID, "atlases/tex_pattern66"); // tex_pattern66 法阵能量基础纹理。
    public static final ResourceLocation tex_pattern59 = new ResourceLocation(Exe.MODID, "atlases/tex_pattern59"); // tex_pattern59 法阵能量 UV 扰动纹理。
    public static final ResourceLocation LIGHTNING_NOISE_TEXTURE_ALT = noise_092_128x; // 闪电备用扰动噪声 sprite，和 noise_076 随机使用。
    public static final ResourceLocation trail_2 = new ResourceLocation(Exe.MODID, "atlases/trail_2"); // trail_2 冲击波纹理。
    public static final ResourceLocation CIRCLE_SHOCKWAVE_TEXTURE = new ResourceLocation(Exe.MODID, "atlases/trail_3"); // 法阵冲击波 trail_3 sprite。
    public static final ResourceLocation ex_wave1 = new ResourceLocation(Exe.MODID, "atlases/ex_wave1"); // ex_wave1 EX 剑气黄色主体纹理。
    public static final ResourceLocation ex_wave2 = new ResourceLocation(Exe.MODID, "atlases/ex_wave2"); // ex_wave2 EX 剑气橙色主体纹理。
    public static final ResourceLocation noise_054 = new ResourceLocation(Exe.MODID, "atlases/noise_054"); // noise_054 EX 剑气 UV 扰动纹理。
    public static final ResourceLocation AI_STAR_TEXTURE = new ResourceLocation(Exe.MODID, "atlases/ai_star"); // ai_star 星星材质粒子贴图，使用 R 通道控制透明度。
    public static final ResourceLocation T_FX_TILE_0016_TEXTURE = new ResourceLocation(Exe.MODID, "atlases/t_fx_tile_0016"); // t_fx_tile_0016 上升冲击波圆台主体纹理。

    public static void init() {
        EXE_TOOL_ATLAS = new TextureAtlas(EXE_TOOL_ATLAS_LOCATION);
        Minecraft.getInstance().getTextureManager().register(EXE_TOOL_ATLAS_LOCATION, EXE_TOOL_ATLAS);
    }

    public static void applyLinearFilter(boolean useMipmap) {
        if (EXE_TOOL_ATLAS == null) return;
        int textureId = EXE_TOOL_ATLAS.getId();
        if (textureId <= 0) return;

        int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
                useMipmap ? GL11.GL_LINEAR_MIPMAP_LINEAR : GL11.GL_LINEAR);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

        if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
            float maxAnisotropy = GL11.glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT,
                    Math.min(4.0F, maxAnisotropy));
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousTexture);
    }


    public static TextureAtlasSprite getTextureLocation(ResourceLocation name) {
        return EXE_TOOL_ATLAS.getSprite(name);
    }
}

