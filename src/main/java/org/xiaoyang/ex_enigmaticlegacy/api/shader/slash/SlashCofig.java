package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

public final class SlashCofig {

    public static final class Quick {
        public static final class WorldSlash {
            public static final float WIDTH_SCALE = 1.72f; // 世界斩宽度倍率
            public static final float ALPHA_SCALE = 1.0f; // 世界斩透明度倍率
            public static final float BLOOM_INTENSITY_SCALE = 0.80f; // 世界斩泛光强度倍率
            public static final float BLOOM_RADIUS_SCALE = 0.80f; // 世界斩泛光半径倍率

            private WorldSlash() {}
        }

        public static final class ScreenFx {
            public static final float UOM_SCALE = 1.0f; // 尾端屏幕特效倍率
            public static final float EDGE_MOTION_BLUR_SCALE = 1.0f; // 边缘动态模糊倍率
            public static final float PRESSURE_WARP_SCALE = 1.0f; // 屏幕径向压迫倍率

            private ScreenFx() {}
        }

        public static final class ScreenGlass {
            public static final float COUNT_SCALE = 1.2f; // 屏幕碎片数量倍率
            public static final float EXPLOSION_SCALE = 1.0f; // 屏幕碎片爆速倍率
            public static final float GRAVITY_SCALE = 1.0f; // 屏幕碎片重力倍率
            public static final float TUMBLE_SCALE = 1.0f; // 屏幕碎片翻滚倍率
            public static final float THICKNESS_SCALE = 1.0f; // 屏幕碎片厚度倍率

            private ScreenGlass() {}
        }

        public static final float WORLD_SLASH_WIDTH_SCALE = WorldSlash.WIDTH_SCALE;
        public static final float WORLD_SLASH_ALPHA_SCALE = WorldSlash.ALPHA_SCALE;
        public static final float WORLD_BLOOM_INTENSITY_SCALE = WorldSlash.BLOOM_INTENSITY_SCALE;
        public static final float WORLD_BLOOM_RADIUS_SCALE = WorldSlash.BLOOM_RADIUS_SCALE;
        public static final float UOM_SCREEN_FX_SCALE = ScreenFx.UOM_SCALE;
        public static final float EDGE_MOTION_BLUR_SCALE = ScreenFx.EDGE_MOTION_BLUR_SCALE;
        public static final float PRESSURE_WARP_SCALE = ScreenFx.PRESSURE_WARP_SCALE;
        public static final float GLASS_SHARD_COUNT_SCALE = ScreenGlass.COUNT_SCALE;
        public static final float GLASS_SHARD_EXPLOSION_SCALE = ScreenGlass.EXPLOSION_SCALE;
        public static final float GLASS_SHARD_GRAVITY_SCALE = ScreenGlass.GRAVITY_SCALE;
        public static final float GLASS_SHARD_TUMBLE_SCALE = ScreenGlass.TUMBLE_SCALE;
        public static final float GLASS_SHARD_THICKNESS_SCALE = ScreenGlass.THICKNESS_SCALE;

        private Quick() {}
    }

    public static final class Audio {
        public static final int SLASH_INTERVAL_TICKS = 1; // 斩间隔
        public static final float CUT_VOLUME = 0.85F; // 斩音量
        public static final float CUT_PITCH = 1.0F; // 斩音高
        public static final float FINAL_CUT_VOLUME = 1.00F; // 最后一斩原版 slash_cut 主响度
        public static final float FINAL_CUT_BOOST_VOLUME = 0.85F; // 同音高同步增益层
        public static final float FINAL_CUT_PITCH = 1.00F; // 严格保持 slash_cut 原音高
        public static final float BREAK_PREPARE_VOLUME = 0.70F; // RH2 音量
        public static final float BREAK_PREPARE_PITCH = 1.0F; // RH2 音高
        public static final float BREAK_PREPARE_PEAK_OFFSET_TICKS = 8.46876F; // RH2 峰值偏移
        public static final int SPACE_FRACTURE_TO_PRESSURE_DELAY_TICKS = 2; // 断裂至蓄压延迟
        public static final int PRESSURE_BED_FADE_IN_TICKS = 2; // 蓄压淡入时间
        public static final int PRESSURE_BED_RISE_TICKS = 14; // 蓄压增强时间
        public static final int PRESSURE_BED_FADE_OUT_TICKS = 2; // 蓄压淡出时间
        public static final int RANGE_EXIT_FADE_TICKS = 6; // 离开领域音频渐出
        public static final float PRESSURE_BED_START_VOLUME = 0.20F; // 蓄压起始音量
        public static final float PRESSURE_BED_END_VOLUME = 0.32F; // 蓄压峰值音量
        public static final float PRESSURE_BED_START_PITCH = 0.75F; // 蓄压起始音高
        public static final float PRESSURE_BED_END_PITCH = 0.90F; // 蓄压结束音高
        public static final float SPACE_FRACTURE_VOLUME = 0.72F; // 断裂音量
        public static final float SPACE_FRACTURE_PITCH = 1.0F; // 断裂音高
        public static final float FINAL_GLASS_TAIL_VOLUME = 1.04F; // 最后一斩玻璃尾音音量
        public static final float FINAL_GLASS_TAIL_PITCH = 0.90F; // 最后一斩玻璃尾音音高
        public static final float GLASS_BREAK_VOLUME = 0.22F; // 玻璃补音音量
        public static final float GLASS_BREAK_PITCH = 0.85F; // 玻璃补音音高

        private Audio() {}
    }

    public static final class WorldSlash {
        public static final class CommandDefaults {
            public static final int DEFAULT_SLASHES = 46; // 默认斩数
            public static final float DEFAULT_LENGTH = 80.0f; // 默认斩长
            public static final float DEFAULT_RADIUS = 10.0f; // 默认散布半径
            public static final int MIN_SLASHES = 1; // 最小斩数
            public static final int MAX_SLASHES = 46; // 最大斩数
            public static final float MIN_LENGTH = 2.0f; // 最小斩长
            public static final float MAX_LENGTH = 128.0f; // 最大斩长
            public static final float MIN_RADIUS = 0.0f; // 最小散布半径
            public static final float MAX_RADIUS = 24.0f; // 最大散布半径

            private CommandDefaults() {}
        }

        public static final class Timeline {
            public static final int PRE_CRACK_TICKS = 3; // 预裂时间
            public static final int SWEEP_TICKS = 4; // 扫切时间
            public static final int SETTLE_TICKS = 2; // 稳定时间
            public static final int LINE_REVEAL_TICKS = PRE_CRACK_TICKS + SWEEP_TICKS + SETTLE_TICKS; // 单斩入场时间
            public static final int LINE_HOLD_TICKS = 15; // 单斩保持时间
            public static final int FINAL_BURST_DELAY_TICKS = 10; // 最终爆发延迟
            public static final int FINAL_FADE_TICKS = 20; // 世界斩最终淡出时间

            private Timeline() {}
        }

        public static final class Palette {
            public static final int OUTER_COLOR_PRIMARY = 0x2DF2FF; // 主外光颜色
            public static final int OUTER_COLOR_ALT_A = 0x28C8FF; // 外光颜色 A
            public static final int OUTER_COLOR_ALT_B = 0x5DDDFF; // 外光颜色 B
            public static final int CORE_COLOR_PRIMARY = 0xFFFFFF; // 主核心颜色
            public static final int CORE_COLOR_ALT = 0xE5F8FF; // 备用核心颜色

            private Palette() {}
        }

        public static final class Overall {
            public static final float FLICKER_BASE = 0.96f; // 闪烁基础亮度
            public static final float FLICKER_RANDOM = 0.0f; // 闪烁浮动幅度
            public static final float FLICKER_SPEED = 1.70f; // 闪烁速度

            private Overall() {}
        }

        public static final class PurpleLayer {
            public static final float WIDTH = 0.50f; // 第二外层宽度
            public static final float ALPHA = 0.55f; // 第二外层透明度
            public static final float SOFT_EDGE = 0.80f; // 第二外层柔边

            private PurpleLayer() {}
        }

        public static final class WhiteCore {
            public static final float WIDTH = 0.232f; // 白芯宽度
            public static final float ALPHA = 0.94f; // 白芯透明度
            public static final float SOFT_EDGE = 0.624f; // 白芯柔边
            public static final int BLACK_COLOR = 0x000000; // 黑芯颜色
            public static final float BLACK_WIDTH = 0.32f; // 黑芯宽度
            public static final float BLACK_ALPHA = 1; // 黑芯透明度
            public static final float BLACK_SOFT_EDGE = 0.10f; // 黑芯柔边
            public static final float BLACK_DEPTH_BIAS = 0.0f; // 黑芯深度偏移

            private WhiteCore() {}
        }

        public static final class Shape {
            public static final int TAPER_SEGMENTS = 20; // 锥形细分数
            public static final float TAPER_POWER = 2.45f; // 两端尖锐度
            public static final float TAPER_BELLY_MIN = 0.42f; // 中段最小宽度
            public static final float TAPER_MID_WIDTH_BOOST = 1.60f; // 中段宽度增幅
            public static final float TAPER_MID_WIDTH_POWER = 1.10f; // 中段宽度曲线
            public static final float ALPHA_POWER = 1.24f; // 两端淡出曲线
            public static final float ALPHA_MULTIPLIER = 1.02f; // 整体透明度倍率

            private Shape() {}
        }

        public static final class Appearance {
            public static final int PRE_CRACK_COLOR = 0x001018; // 预裂颜色
            public static final int PRE_CRACK_GLOW_COLOR = 0x005870; // 预裂光颜色
            public static final float PRE_CRACK_WIDTH = 0.13f; // 预裂宽度
            public static final float PRE_CRACK_ALPHA = 0.72f; // 预裂透明度
            public static final float PRE_CRACK_GLOW_WIDTH = 0.30f; // 预裂光宽度
            public static final float PRE_CRACK_GLOW_ALPHA = 0.16f; // 预裂光强度
            public static final float SWEEP_HEAD_LENGTH = 0.085f; // 扫切头长度
            public static final float SWEEP_HEAD_OUTER_WIDTH = 1.18f; // 扫切头外光宽度
            public static final float SWEEP_HEAD_CORE_WIDTH = 0.44f; // 扫切头白芯宽度
            public static final float SWEEP_HEAD_OUTER_ALPHA = 0.88f; // 扫切头外光强度
            public static final float SWEEP_HEAD_CORE_ALPHA = 1.00f; // 扫切头白芯强度
            public static final float SWEEP_AFTERIMAGE_LENGTH = 0.16f; // 扫切余辉长度
            public static final float SWEEP_AFTERIMAGE_WIDTH = 1.46f; // 扫切余辉宽度
            public static final float SWEEP_AFTERIMAGE_ALPHA = 0.18f; // 扫切余辉强度
            public static final float SWEEP_RESIDUAL_ALPHA = 1.00f; // 残留斩痕透明度
            public static final float SETTLE_BLOOM_PULSE = 0.18f; // 稳定泛光脉冲

            private Appearance() {}
        }

        public static final class Placement {
            public static final float INNER_RADIUS_SCALE = 0.08f; // 内层半径倍率
            public static final float OUTER_RADIUS_SCALE = 1.00f; // 外层半径倍率
            public static final float RADIAL_CURVE_POWER = 1.00f; // 径向分布曲线
            public static final float SPATIAL_STEP_RATIO = 0.618034f; // 球面黄金步长
            public static final float SPHERICAL_PHASE_RADIANS = 0.72f; // 球面旋转相位
            public static final int DIRECTION_FAMILIES = 4; // 方向族数量
            public static final float DIRECTION_DRIFT_RADIANS = 0.16f; // 方向族层间旋转
            public static final float DIRECTION_JITTER_RADIANS = 0.18f; // 方向随机偏角
            public static final float RADIAL_SKEW = 0.24f; // 径向倾斜幅度
            public static final float INNER_LENGTH_BOOST = 0.14f; // 内层长度增幅
            public static final float INNER_WIDTH_BOOST = 0.02f; // 内层宽度增幅
            public static final int ACCENT_PAIR_INTERVAL = 4; // 强调斩间隔
            public static final float ACCENT_LENGTH_BOOST = 0.18f; // 强调斩长度增幅
            public static final float ACCENT_WIDTH_BOOST = 0.025f; // 强调斩宽度增幅
            public static final float MIRROR_SECONDARY_LENGTH_SCALE = 0.94f; // 镜像副斩长度倍率
            public static final float MIRROR_SECONDARY_WIDTH_SCALE = 0.90f; // 镜像副斩宽度倍率

            private Placement() {}
        }

        public static final class FinalStrike {
            public static final int WINDUP_DELAY_TICKS = 5; // 最后一斩前的短促蓄势停顿
            public static final float LENGTH_SCALE = 2.25f; // 巨型斩长度倍率
            public static final float WIDTH = 1.72f; // 巨型斩基础宽度
            public static final float CENTER_HEIGHT_RADIUS_SCALE = 0.28f; // 斩面中心向上偏移
            public static final float CENTER_FORWARD_RADIUS_SCALE = 0.16f; // 斩面中心向前压入
            public static final float LEFT_COMPONENT = 0.72f; // 右上至左下的向左分量
            public static final float DOWN_COMPONENT = 1.00f; // 右上至左下的下劈分量
            public static final float FORWARD_COMPONENT = 0.14f; // 斩入世界纵深的分量
            public static final float SCREEN_IMPACT_SCALE = 1.78f; // 沿用单斩冲击时的最终压迫倍率
            public static final float CAMERA_IMPACT_SCALE = 2.60f; // 沿用单斩震动时的最终重量倍率

            public static final class GroundImpact {
                public static final boolean ENABLED = true;
                public static final float DURATION_TICKS = 18.0f;
                public static final float START_RADIUS = 0.35f;
                public static final float MAX_RADIUS_SCALE = 1.35f;
                public static final float MIN_MAX_RADIUS = 8.0f;
                public static final float MAX_RADIUS = 22.0f;
                public static final float START_WIDTH = 2.40f;
                public static final float END_WIDTH_SCALE = 0.42f;
                public static final float OUTER_WIDTH_SCALE = 0.28f;
                public static final float OUTER_ALPHA = 1.00f;
                public static final float CORE_ALPHA = 1.00f;
                public static final float BLACK_ALPHA = 0.70f;
                public static final float SOFT_EDGE = 0.96f;
                public static final float CORE_SOFT_EDGE = 0.90f;
                public static final float BLACK_SOFT_EDGE = 0.78f;
                public static final float BLOOM_WIDTH_SCALE = 0.48f;
                public static final float BLOOM_ALPHA = 0.82f;
                public static final float GROUND_OFFSET = 0.055f;
                public static final int GROUND_RETRY_TICKS = 2;
                public static final boolean DAMAGE_ENABLED = true;
                public static final float MIN_DAMAGE_HEARTS = 1.00f;
                public static final float MAX_DAMAGE_HEARTS = 6.56f;
                public static final float COLLISION_PADDING = 0.30f;
                public static final float VERTICAL_REACH_BELOW = 2.00f;
                public static final float VERTICAL_REACH_ABOVE = 5.00f;
                public static final double VERTICAL_KNOCKBACK = 0.32;
                public static final double HORIZONTAL_KNOCKBACK = 0.10;

                private GroundImpact() {}
            }

            private FinalStrike() {}
        }

        public static final class Gameplay {
            public static final float CONTACT_DAMAGE = 0.5f; // 单次接触伤害
            public static final int CONTACT_DAMAGE_INTERVAL_TICKS = 10; // 接触伤害间隔
            public static final float CONTACT_PADDING = 0.08f; // 接触判定容差
            public static final float GENERIC_DAMAGE_CHANCE = 0.50f; // 通用伤害概率

            private Gameplay() {}
        }

        public static final class Corrosion {
            public static final int DURATION_TICKS = 100; // 侵蚀持续时间
            public static final float CURRENT_HEALTH_DAMAGE_CHANCE = 0.90f; // 比例伤害概率
            public static final float CURRENT_HEALTH_DAMAGE_FRACTION = 0.60f; // 当前生命扣除比例
            public static final float HEALTH_CAP = 20.0f; // 特殊分支生命上限
            public static final float WHITE_BURST_CENTER_KILL_CHANCE = 0.50f; // 领域中心白爆机制杀概率
            public static final float WHITE_BURST_EDGE_KILL_CHANCE = 0.05f; // 领域边缘白爆机制杀概率
            public static final int DOMAIN_EFFECT_REFRESH_TICKS = 40; // 服务端效果安全刷新时长
            public static final int DOMAIN_PULSE_INTERVAL_TICKS = 20; // 侵蚀脉冲间隔
            public static final float DOMAIN_HEALTH_LOSS = 0.5f; // 每次脉冲扣除生命
            public static final float DOMAIN_FOOD_LOSS = 0.8f; // 每次脉冲扣除可见食物值
            public static final double DOMAIN_ATTACK_DAMAGE_REDUCTION = 0.30; // 攻击伤害降低比例
            public static final int NAUSEA_AMPLIFIER = 0; // 恶心效果等级
            public static final int PARTICLE_EMIT_TICKS = 80; // 粒子生成时间
            public static final int PARTICLE_INTERVAL_TICKS = 2; // 粒子生成间隔
            public static final int PARTICLES_PER_EMISSION = 3; // 单次粒子数
            public static final float PARTICLE_SPREAD_SCALE = 0.34f; // 粒子散布倍率
            public static final float PARTICLE_SIZE_SCALE = 1.45f; // 粒子尺寸倍率

            private Corrosion() {}
        }

        public static final class PlayerPressure {
            public static final float MOVEMENT_SPEED_MULTIPLIER = 0.25f; // 移动速度倍率
            public static final int MOVEMENT_SPEED_FADE_IN_TICKS = 20; // 减速渐入时间
            public static final int MOVEMENT_SPEED_RECOVERY_TICKS = 30; // 减速恢复时间
            public static final int VIGNETTE_FADE_IN_TICKS = 20; // 晕影渐入时间
            public static final int VIGNETTE_FADE_OUT_TICKS = 30; // 晕影渐出时间
            public static final float VIGNETTE_INNER_RADIUS = 0.58f; // 晕影内半径
            public static final float VIGNETTE_OUTER_RADIUS = 1.50f; // 晕影外半径
            public static final float VIGNETTE_MAX_ALPHA = 0.72f; // 晕影最大透明度

            private PlayerPressure() {}
        }

        public static final class Domain {
            public static final boolean ENABLED = true; // 启用地面领域
            public static final int FILL_COLOR = Palette.OUTER_COLOR_PRIMARY; // 径向品红
            public static final float VISIBILITY_EDGE_FADE_FRACTION = 0.10f; // 边界渐隐比例
            public static final int SEGMENTS = 64; // 领域细分数
            public static final int GROUND_RETRY_TICKS = 2; // 地面重试间隔
            public static final float GROUND_SEARCH_ABOVE = 1.0f; // 地面搜索高度
            public static final float GROUND_OFFSET = 0.025f; // 地面高度偏移
            public static final float INTRO_TICKS = 5.0f; // 领域入场时间
            public static final float FADE_TICKS = 12.0f; // 领域淡出时间
            public static final float FILL_CENTER_ALPHA = 0.32f; // 径向中心透明度
            public static final float RING_BASE_WIDTH = 0.62f; // 环形斩基础宽度
            public static final boolean PARTICLES_ENABLED = true; // 启用侵蚀粒子
            public static final int PARTICLE_INTERVAL_TICKS = 1; // 粒子生成间隔
            public static final int PARTICLE_MIN_COUNT = 3; // 单次最少粒子
            public static final int PARTICLE_MAX_COUNT = 12; // 单次最多粒子
            public static final float PARTICLE_COUNT_PER_RADIUS = 0.60f; // 半径粒子倍率
            public static final float PARTICLE_SIZE_SCALE = 1.65f; // 粒子尺寸倍率
            public static final int PARTICLE_STOP_EARLY_TICKS = 20; // 提前停止时间
            public static final float PARTICLE_GROUND_SEARCH_ABOVE = 1.5f; // 粒子上方搜索
            public static final float PARTICLE_GROUND_SEARCH_BELOW = 4.0f; // 粒子下方搜索
            public static final float PARTICLE_GROUND_OFFSET = 0.055f; // 粒子离地高度
            public static final int SATURATION_INTERVAL_TICKS = 16; // 饱和度脉冲间隔
            public static final float SATURATION_ONE_POINT_LOSS = 1.0f; // 一级饱和度损失
            public static final float SATURATION_TWO_POINT_LOSS = 2.0f; // 二级饱和度损失
            public static final float SATURATION_THREE_POINT_LOSS = 3.0f; // 三级饱和度损失
            public static final float SATURATION_ONE_POINT_CHANCE = 0.80f; // 一级触发率
            public static final float SATURATION_TWO_POINT_CHANCE = 0.50f; // 二级触发率
            public static final float SATURATION_THREE_POINT_CHANCE = 0.32f; // 三级触发率

            private Domain() {}
        }

        public static final class SlashParticles {
            public static final float SIZE_SCALE = 0.12f; // 单斩粒子尺寸倍率

            private SlashParticles() {}
        }

        public static final class TrueDemonParticles {
            public static final int FINAL_BURST_MIN_COUNT = 250; // 爆发粒子最小数
            public static final int FINAL_BURST_MAX_COUNT = 532; // 爆发粒子最大数
            public static final float FINAL_BURST_COUNT_PER_RADIUS = 128; // 爆发粒子半径增量
            public static final float FINAL_BURST_SPAWN_RADIUS = 5; // 爆发粒子半径
            public static final float FINAL_BURST_SPEED_MIN = 4f; // 爆发粒子最小速度
            public static final float FINAL_BURST_SPEED_RANDOM = 8f; // 爆发粒子速度浮动
            public static final int FINAL_STAR_MIN_COUNT = 72; // 星粒子最小数
            public static final int FINAL_STAR_MAX_COUNT = 144; // 星粒子最大数
            public static final float FINAL_STAR_COUNT_PER_RADIUS = 18f; // 星粒子半径增量
            public static final float FINAL_STAR_SPAWN_RADIUS_SCALE = 0.75f; // 星粒子半径倍率
            public static final float FINAL_STAR_SPAWN_RADIUS_MAX = 7.0f; // 星粒子半径上限
            public static final float FINAL_STAR_SPEED_MIN = 0.075f; // 星粒子最小速度
            public static final float FINAL_STAR_SPEED_RANDOM = 0.18f; // 星粒子速度浮动

            private TrueDemonParticles() {}
        }

        public static final class WorldGlassShards {
            public static final boolean ENABLED = true; // 启用世界碎片
            public static final int COUNT = 160; // 世界碎片数量
            public static final int LIFETIME_TICKS = 60; // 世界碎片寿命
            public static final float SPAWN_RADIUS = 1.32f; // 世界碎片生成半径
            public static final float SIZE_MIN = 0.18f; // 世界碎片最小尺寸
            public static final float SIZE_MAX = 0.82f; // 世界碎片最大尺寸
            public static final float EXPLOSION_SPEED = 0.18f; // 世界碎片爆速
            public static final float EXPLOSION_RANDOM = 0.24f; // 世界碎片速度浮动
            public static final float UPWARD_BIAS = 0.06f; // 世界碎片上升偏移
            public static final float GRAVITY = 0.0065f; // 世界碎片重力
            public static final float TUMBLE_SPEED = 0.18f; // 世界碎片翻滚速度
            public static final float ALPHA = 0.88f; // 世界碎片透明度
            public static final float ALPHA_FADE_START = 0.60f; // 世界碎片淡出起点
            public static final float REFRACTION = 7.0f; // 世界碎片折射强度
            public static final float EDGE_HIGHLIGHT = 0.72f; // 世界碎片边缘高光
            public static final float THICKNESS = 0.075f; // 世界碎片厚度
            public static final float MIRROR_STRENGTH = 0.78f; // 世界碎片反射强度

            private WorldGlassShards() {}
        }

        public static final class Bloom {
            public static final boolean ENABLED = true; // 启用世界斩泛光
            public static final int BLUR_ITERATIONS = 4; // 泛光模糊次数
            public static final float MASK_WIDTH = 0.62f; // 泛光遮罩宽度
            public static final float MASK_ALPHA = 0.62f; // 泛光遮罩强度
            public static final float BLUR_RADIUS = 3f; // 泛光半径
            public static final float INTENSITY = 1.032f; // 泛光强度
            public static final float COMPOSITE_ALPHA = 0.50f; // 泛光合成透明度

            private Bloom() {}
        }

        public static final int DEFAULT_SLASHES = CommandDefaults.DEFAULT_SLASHES;
        public static final float DEFAULT_LENGTH = CommandDefaults.DEFAULT_LENGTH;
        public static final float DEFAULT_RADIUS = CommandDefaults.DEFAULT_RADIUS;
        public static final int MIN_SLASHES = CommandDefaults.MIN_SLASHES;
        public static final int PRE_CRACK_TICKS = Timeline.PRE_CRACK_TICKS;
        public static final int SWEEP_TICKS = Timeline.SWEEP_TICKS;
        public static final int SETTLE_TICKS = Timeline.SETTLE_TICKS;
        public static final int LINE_REVEAL_TICKS = Timeline.LINE_REVEAL_TICKS;
        public static final int LINE_HOLD_TICKS = Timeline.LINE_HOLD_TICKS;
        public static final int FINAL_BURST_DELAY_TICKS = Timeline.FINAL_BURST_DELAY_TICKS;
        public static final int FINAL_FADE_TICKS = Timeline.FINAL_FADE_TICKS;
        public static final int MAX_SLASHES = CommandDefaults.MAX_SLASHES;
        public static final int TAPER_SEGMENTS = Shape.TAPER_SEGMENTS;
        public static final float MIN_LENGTH = CommandDefaults.MIN_LENGTH;
        public static final float MAX_LENGTH = CommandDefaults.MAX_LENGTH;
        public static final float MIN_RADIUS = CommandDefaults.MIN_RADIUS;
        public static final float MAX_RADIUS = CommandDefaults.MAX_RADIUS;
        public static final boolean WORLD_SHARDS_ENABLED = WorldGlassShards.ENABLED;
        public static final int WORLD_SHARD_COUNT = WorldGlassShards.COUNT;
        public static final int WORLD_SHARD_LIFETIME_TICKS = WorldGlassShards.LIFETIME_TICKS;
        public static final float WORLD_SHARD_SPAWN_RADIUS = WorldGlassShards.SPAWN_RADIUS;
        public static final float WORLD_SHARD_SIZE_MIN = WorldGlassShards.SIZE_MIN;
        public static final float WORLD_SHARD_SIZE_MAX = WorldGlassShards.SIZE_MAX;
        public static final float WORLD_SHARD_EXPLOSION_SPEED = WorldGlassShards.EXPLOSION_SPEED;
        public static final float WORLD_SHARD_EXPLOSION_RANDOM = WorldGlassShards.EXPLOSION_RANDOM;
        public static final float WORLD_SHARD_UPWARD_BIAS = WorldGlassShards.UPWARD_BIAS;
        public static final float WORLD_SHARD_GRAVITY = WorldGlassShards.GRAVITY;
        public static final float WORLD_SHARD_TUMBLE_SPEED = WorldGlassShards.TUMBLE_SPEED;
        public static final float WORLD_SHARD_ALPHA = WorldGlassShards.ALPHA;
        public static final float WORLD_SHARD_ALPHA_FADE_START = WorldGlassShards.ALPHA_FADE_START;
        public static final float WORLD_SHARD_REFRACTION = WorldGlassShards.REFRACTION;
        public static final float WORLD_SHARD_EDGE_HIGHLIGHT = WorldGlassShards.EDGE_HIGHLIGHT;
        public static final float WORLD_SHARD_THICKNESS = WorldGlassShards.THICKNESS;
        public static final float WORLD_SHARD_MIRROR_STRENGTH = WorldGlassShards.MIRROR_STRENGTH;
        public static final int OUTER_COLOR_PRIMARY = Palette.OUTER_COLOR_PRIMARY;
        public static final int OUTER_COLOR_ALT_A = Palette.OUTER_COLOR_ALT_A;
        public static final int OUTER_COLOR_ALT_B = Palette.OUTER_COLOR_ALT_B;
        public static final int CORE_COLOR_PRIMARY = Palette.CORE_COLOR_PRIMARY;
        public static final int CORE_COLOR_ALT = Palette.CORE_COLOR_ALT;
        public static final float FLICKER_BASE = Overall.FLICKER_BASE;
        public static final float FLICKER_RANDOM = Overall.FLICKER_RANDOM;
        public static final float FLICKER_SPEED = Overall.FLICKER_SPEED;
        public static final float MAIN_OUTER_WIDTH = PurpleLayer.WIDTH;
        public static final float MAIN_OUTER_ALPHA = PurpleLayer.ALPHA;
        public static final float MAIN_OUTER_SOFT_EDGE = PurpleLayer.SOFT_EDGE;
        public static final float MAIN_CORE_WIDTH = WhiteCore.WIDTH;
        public static final float MAIN_CORE_ALPHA = WhiteCore.ALPHA;
        public static final float MAIN_CORE_SOFT_EDGE = WhiteCore.SOFT_EDGE;
        public static final int MAIN_BLACK_CORE_COLOR = WhiteCore.BLACK_COLOR;
        public static final float MAIN_BLACK_CORE_WIDTH = WhiteCore.BLACK_WIDTH;
        public static final float MAIN_BLACK_CORE_ALPHA = WhiteCore.BLACK_ALPHA;
        public static final float MAIN_BLACK_CORE_SOFT_EDGE = WhiteCore.BLACK_SOFT_EDGE;
        public static final float MAIN_BLACK_CORE_DEPTH_BIAS = WhiteCore.BLACK_DEPTH_BIAS;
        public static final int PRE_CRACK_COLOR = Appearance.PRE_CRACK_COLOR;
        public static final int PRE_CRACK_GLOW_COLOR = Appearance.PRE_CRACK_GLOW_COLOR;
        public static final float PRE_CRACK_WIDTH = Appearance.PRE_CRACK_WIDTH;
        public static final float PRE_CRACK_ALPHA = Appearance.PRE_CRACK_ALPHA;
        public static final float PRE_CRACK_GLOW_WIDTH = Appearance.PRE_CRACK_GLOW_WIDTH;
        public static final float PRE_CRACK_GLOW_ALPHA = Appearance.PRE_CRACK_GLOW_ALPHA;
        public static final float SWEEP_HEAD_LENGTH = Appearance.SWEEP_HEAD_LENGTH;
        public static final float SWEEP_HEAD_OUTER_WIDTH = Appearance.SWEEP_HEAD_OUTER_WIDTH;
        public static final float SWEEP_HEAD_CORE_WIDTH = Appearance.SWEEP_HEAD_CORE_WIDTH;
        public static final float SWEEP_HEAD_OUTER_ALPHA = Appearance.SWEEP_HEAD_OUTER_ALPHA;
        public static final float SWEEP_HEAD_CORE_ALPHA = Appearance.SWEEP_HEAD_CORE_ALPHA;
        public static final float SWEEP_AFTERIMAGE_LENGTH = Appearance.SWEEP_AFTERIMAGE_LENGTH;
        public static final float SWEEP_AFTERIMAGE_WIDTH = Appearance.SWEEP_AFTERIMAGE_WIDTH;
        public static final float SWEEP_AFTERIMAGE_ALPHA = Appearance.SWEEP_AFTERIMAGE_ALPHA;
        public static final float SWEEP_RESIDUAL_ALPHA = Appearance.SWEEP_RESIDUAL_ALPHA;
        public static final float SETTLE_BLOOM_PULSE = Appearance.SETTLE_BLOOM_PULSE;
        public static final int TRUE_DEMON_FINAL_PARTICLE_MIN_COUNT = TrueDemonParticles.FINAL_BURST_MIN_COUNT;
        public static final int TRUE_DEMON_FINAL_PARTICLE_MAX_COUNT = TrueDemonParticles.FINAL_BURST_MAX_COUNT;
        public static final float TRUE_DEMON_FINAL_PARTICLE_COUNT_PER_RADIUS = TrueDemonParticles.FINAL_BURST_COUNT_PER_RADIUS;
        public static final float TRUE_DEMON_FINAL_PARTICLE_SPAWN_RADIUS = TrueDemonParticles.FINAL_BURST_SPAWN_RADIUS;
        public static final float TRUE_DEMON_FINAL_PARTICLE_SPEED_MIN = TrueDemonParticles.FINAL_BURST_SPEED_MIN;
        public static final float TRUE_DEMON_FINAL_PARTICLE_SPEED_RANDOM = TrueDemonParticles.FINAL_BURST_SPEED_RANDOM;
        public static final int TRUE_DEMON_FINAL_STAR_MIN_COUNT = TrueDemonParticles.FINAL_STAR_MIN_COUNT;
        public static final int TRUE_DEMON_FINAL_STAR_MAX_COUNT = TrueDemonParticles.FINAL_STAR_MAX_COUNT;
        public static final float TRUE_DEMON_FINAL_STAR_COUNT_PER_RADIUS = TrueDemonParticles.FINAL_STAR_COUNT_PER_RADIUS;
        public static final float TRUE_DEMON_FINAL_STAR_SPAWN_RADIUS_SCALE = TrueDemonParticles.FINAL_STAR_SPAWN_RADIUS_SCALE;
        public static final float TRUE_DEMON_FINAL_STAR_SPAWN_RADIUS_MAX = TrueDemonParticles.FINAL_STAR_SPAWN_RADIUS_MAX;
        public static final float TRUE_DEMON_FINAL_STAR_SPEED_MIN = TrueDemonParticles.FINAL_STAR_SPEED_MIN;
        public static final float TRUE_DEMON_FINAL_STAR_SPEED_RANDOM = TrueDemonParticles.FINAL_STAR_SPEED_RANDOM;
        public static final float TAPER_POWER = Shape.TAPER_POWER;
        public static final float TAPER_BELLY_MIN = Shape.TAPER_BELLY_MIN;
        public static final float TAPER_MID_WIDTH_BOOST = Shape.TAPER_MID_WIDTH_BOOST;
        public static final float TAPER_MID_WIDTH_POWER = Shape.TAPER_MID_WIDTH_POWER;
        public static final float ALPHA_POWER = Shape.ALPHA_POWER;
        public static final float ALPHA_MULTIPLIER = Shape.ALPHA_MULTIPLIER;
        public static final boolean BLOOM_ENABLED = Bloom.ENABLED;
        public static final int BLOOM_BLUR_ITERATIONS = Bloom.BLUR_ITERATIONS;
        public static final float BLOOM_MASK_WIDTH = Bloom.MASK_WIDTH;
        public static final float BLOOM_MASK_ALPHA = Bloom.MASK_ALPHA;
        public static final float BLOOM_BLUR_RADIUS = Bloom.BLUR_RADIUS;
        public static final float BLOOM_INTENSITY = Bloom.INTENSITY;
        public static final float BLOOM_COMPOSITE_ALPHA = Bloom.COMPOSITE_ALPHA;

        private WorldSlash() {}
    }

    public static final class ScreenBreak {
        public static final class Timeline {
            public static final int DURATION_TICKS = 50; // 屏幕碎裂时间
            public static final int SHARD_MAX_LIFETIME_TICKS = 96; // 屏幕碎片最长寿命
            public static final float SHARD_FADE_START_TICKS = 52.0f; // 屏幕碎片淡出起点
            public static final float SHARD_FADE_TICKS = 22.0f; // 屏幕碎片淡出时间

            private Timeline() {}
        }

        public static final class GlassShards {
            public static final int CELLS = 164; // Voronoi 碎片数
            public static final float EDGE_VISIBILITY = 0.50f; // 碎片边缘可见度
            public static final float MIRROR_STRENGTH = 1.50f; // 碎片反射强度
            public static final float LAUNCH_START_TICKS = 15.0f; // 碎片飞散起点
            public static final float LAUNCH_SPREAD_TICKS = 2.5f; // 碎片飞散窗口
            public static final float CENTER_PULL_PIXELS = 22.0f; // 碎片中心收缩位移
            public static final float GRAVITY = 7.2f; // 碎片重力
            public static final float DEPTH_SPEED = 1.20f; // 碎片纵深速度
            public static final float PERSPECTIVE_FOCAL = 900.0f; // 碎片透视焦距
            public static final float TUMBLE_SPEED = 1.0f; // 碎片翻滚速度
            public static final float THICKNESS_PIXELS = 5.0f; // 碎片厚度

            private GlassShards() {}
        }

        public static final class GlassLighting {
            public static final float BLOOM_PIXELS = 2.0f; // 碎片泛光宽度
            public static final float BLOOM_ALPHA = 0.40f; // 碎片泛光强度
            public static final float MOTION_BLUR_TICKS = 2.0f; // 碎片拖尾时间
            public static final float MOTION_BLUR_ALPHA = 0.12f; // 碎片拖尾强度
            public static final float CRACK_LIGHT_LENGTH_PIXELS = 5.0f; // 裂缝透光长度
            public static final float CRACK_LIGHT_WIDTH_PIXELS = 5.0f; // 裂缝透光宽度
            public static final float CRACK_LIGHT_ALPHA = 1.0001f; // 裂缝透光强度

            private GlassLighting() {}
        }

        public static final class BreakBurst {
            public static final float IMPACT_PRE_TICKS = 2.0f; // 白爆前冲击时间
            public static final float IMPACT_POST_TICKS = 3.0f; // 白爆后冲击时间
            public static final float IMPACT_STRENGTH = 1.0f; // 冲击帧总强度
            public static final float IMPACT_INVERT_STRENGTH = 0.92f; // 冲击帧反相强度
            public static final float IMPACT_THRESHOLD_STRENGTH = 1.0f; // 冲击帧阈值强度
            public static final float IMPACT_COMPRESSION = 0.13f; // 冲击帧压缩强度
            public static final float IMPACT_CORE_ALPHA = 1.16f; // 冲击帧白核强度
            public static final float IMPACT_VIGNETTE_STRENGTH = 0.88f; // 冲击帧压暗强度
            public static final float DURATION_TICKS = 9.0f; // 白爆持续时间
            public static final float FLASH_ALPHA = 0.48f; // 白闪强度
            public static final float CORE_ALPHA = 0.54f; // 中心白光强度
            public static final float RING_ALPHA = 0.50f; // 白环强度
            public static final float RAY_ALPHA = 0.34f; // 光刺强度
            public static final float START_RADIUS = 0.035f; // 白环起始半径
            public static final float END_RADIUS = 1.18f; // 白环结束半径
            public static final float RING_WIDTH = 0.624f; // 白环宽度
            public static final float RING_CORE_FRACTION = 0.12f; // 白环实心比例

            private BreakBurst() {}
        }

        public static final class ScreenFx {
            public static final float UOM_DURATION_TICKS = 40.0f; // UOM 持续时间
            public static final float UOM_RATE = 20f; // UOM 闪烁频率
            public static final float UOM_STRENGTH = 1.32f; // UOM 强度
            public static final float UOM_EDGE_WEIGHT = 0.92f; // UOM 边缘权重
            public static final float UOM_CONTRAST = 2.10f; // UOM 对比度
            public static final float EDGE_MOTION_BLUR_VELOCITY_PIXELS = 26f; // 边缘模糊采样距离
            public static final float EDGE_MOTION_BLUR_INTENSITY = 0.82f; // 边缘模糊强度
            public static final float EDGE_MOTION_BLUR_FOCUS_RADIUS = 0.024f; // 边缘模糊清晰半径
            public static final float EDGE_MOTION_BLUR_RAMP_IN_TICKS = 6.0f; // 边缘模糊渐入时间
            public static final float EDGE_MOTION_BLUR_FADE_TICKS = 14f; // 边缘模糊渐出时间
            public static final float PRESSURE_WARP_STRENGTH = 0.92f; // 径向压迫强度
            public static final float PRESSURE_SHOCKWAVE_STRENGTH = 1.06f; // 压迫冲击波强度
            public static final float UOM_FINAL_SCALE = 1.0f; // 最终 UOM 倍率
            public static final float UOM_FINAL_CONTRAST_SCALE = 0.44f; // 最终 UOM 对比度
            public static final float UOM_CENTER_FLASH = 3.24f; // UOM 中心白闪
            public static final float ANIME_IMPACT_DURATION_TICKS = 10.0f; // 动漫冲击帧时间
            public static final float ANIME_IMPACT_CONTRAST_SCALE = 1.0f; // 动漫冲击帧对比度
            public static final float ANIME_IMPACT_THRESHOLD_STRENGTH = 1.08f; // 动漫冲击帧阈值
            public static final float ANIME_IMPACT_INVERT_STRENGTH = 0.96f; // 动漫冲击帧反相
            public static final float ANIME_IMPACT_MONO_STRENGTH = 1.0f; // 动漫冲击帧黑白
            public static final float ANIME_IMPACT_WHITE_FLASH_STRENGTH = 0.82f; // 动漫冲击帧白闪
            public static final float ANIME_IMPACT_CENTER_FLASH_SCALE = 0.30f; // 动漫冲击帧中心白闪
            public static final float UOM_VIGNETTE_STRENGTH = 0.50f; // UOM 晕影强度

            private ScreenFx() {}
        }

        public static final class ScreenChroma {
            public static final float EDGE_START = 0.10f; // 色散起始位置
            public static final float EDGE_END = 0.90f; // 色散最大位置

            private ScreenChroma() {}
        }

        public static final class TopChroma {
            public static final boolean ENABLED = true; // 启用覆盖全部次元斩层的最终色散
            public static final float OFFSET_PIXELS = 3.56f; // 屏幕边缘 RGB 分离像素
            public static final float CENTER_CLEAR_RADIUS = 0; // 中心色散透明半径
            public static final float EDGE_FULL_RADIUS = 1; // 以屏幕短边半径计的满强度位置

            private TopChroma() {}
        }

        public static final class PerSlashImpact {
            public static final boolean ENABLED = true; // 启用单斩冲击
            public static final float DURATION_TICKS = 3.0f; // 单斩冲击时间
            public static final float ATTACK_TICKS = 0.25f; // 单斩冲击起峰时间
            public static final float CAMERA_AMPLITUDE_DEGREES = 0.13f; // 单斩震动角度
            public static final float CAMERA_MAX_ANGLE_DEGREES = 0.20f; // 单斩震动上限
            public static final float CAMERA_YAW_WEIGHT = 0.72f; // 单斩水平震动权重
            public static final float CAMERA_PITCH_WEIGHT = 0.90f; // 单斩垂直震动权重
            public static final float CAMERA_ROLL_WEIGHT = 0.20f; // 单斩侧倾权重
            public static final float CHROMA_OFFSET_PIXELS = 3.20f; // 色散偏移
            public static final float CHROMA_MIX = 0.68f; // 色散混合强度
            public static final float CHROMA_CENTER_STRENGTH = 0.38f; // 中心色散强度
            public static final float CHROMA_EDGE_START = 0.02f; // 色散增强起点
            public static final float CHROMA_EDGE_END = 0.60f; // 色散满强度位置
            public static final float PRESSURE_COMPRESSION = 0.0065f; // 单斩压缩比例
            public static final float PRESSURE_EDGE_DARKEN = 0.11f; // 单斩边缘压暗
            public static final float PRESSURE_CONTRAST = 0.10f; // 单斩对比增强

            private PerSlashImpact() {}
        }

        public static final class ScreenDistortion {
            public static final int LINE_COUNT = 16; // 裂线数量上限
            public static final float PRE_WIDTH_PIXELS = 32f; // 预破裂带宽度
            public static final float PRE_STRENGTH_PIXELS = 42f; // 预破裂偏移强度
            public static final float BREAK_WIDTH_PIXELS = 125f; // 正式破裂带宽度
            public static final float BREAK_STRENGTH_PIXELS = 46f; // 正式破裂偏移强度
            public static final float MAX_STRENGTH_PIXELS = 56f; // 偏移强度上限
            public static final float CENTER_FADE_INNER_RADIUS = 0.055f; // 中心衰减内半径
            public static final float CENTER_FADE_OUTER_RADIUS = 0.25f; // 中心衰减外半径
            public static final float CENTER_FADE_MIN_STRENGTH = 0.12f; // 中心最低偏移强度
            public static final float VORONOI_BLEND_AMOUNT = 0.95f; // Voronoi 混合强度
            public static final float VORONOI_CELL_SCALE = 8; // Voronoi 单元密度
            public static final float VORONOI_WARP_PIXELS = 50; // Voronoi 偏移强度
            public static final float VORONOI_CRACK_WIDTH_PIXELS = 1.24f; // Voronoi 裂纹宽度
            public static final float VORONOI_CRACK_ALPHA = 1; // Voronoi 裂纹强度
            public static final float VORONOI_CRACK_COLOR = 0; // Voronoi 裂纹颜色
            public static final float VORONOI_CRACK_REVEAL_THRESHOLD = 0.01f; // Voronoi 裂纹阈值
            public static final float VORONOI_CENTER_INNER_RADIUS = 0.12f; // Voronoi 中心内半径
            public static final float VORONOI_CENTER_OUTER_RADIUS = 1.16f; // Voronoi 中心外半径
            public static final float VORONOI_CENTER_FALLOFF_POWER = 1.25f; // Voronoi 径向衰减
            public static final float VORONOI_SPREAD_START_RADIUS = 0.024f; // Voronoi 扩散起点
            public static final float VORONOI_SPREAD_END_RADIUS = 1.42f; // Voronoi 扩散终点
            public static final float VORONOI_SPREAD_TICKS = 15; // Voronoi 扩散时间
            public static final float VORONOI_SPREAD_FEATHER_RADIUS = 0.075f; // Voronoi 扩散柔边
            private ScreenDistortion() {}
        }

        public static final class CameraShake {
            public static float AMPLITUDE_DEGREES = 0.20f; // 镜头震动角度
            public static final int DURATION_TICKS = 50; // 镜头震动时间
            public static final float FREQUENCY = 160f; // 镜头震动频率
            public static final float MAX_ANGLE_DEGREES = 0.32f; // 镜头震动上限
            public static final float ROUGHNESS = 0.92f; // 镜头震动粗糙度
            public static final float YAW_WEIGHT = 0.65f; // 水平震动权重
            public static final float PITCH_WEIGHT = 0.55f; // 垂直震动权重
            public static final float ROLL_WEIGHT = 0.12f; // 侧倾震动权重

            private CameraShake() {}
        }

        public static final class ScreenFreeze {
            public static final boolean ENABLED = true; // 启用画面冻结
            public static final int RELEASE_AFTER_GLASS_BREAK_TICKS = 20; // 冻结解除延迟
            public static final float DESATURATION = 1.0f; // 冻结去色强度
            public static final float CONTRAST = 1.08f; // 冻结对比度
            public static final float BRIGHTNESS = 0.92f; // 冻结亮度
            public static final float VIGNETTE_STRENGTH = 0.18f; // 冻结暗角强度

            private ScreenFreeze() {}
        }

        public static final int DURATION_TICKS = Timeline.DURATION_TICKS;
        public static final int SHARD_MAX_LIFETIME_TICKS = Timeline.SHARD_MAX_LIFETIME_TICKS;
        public static final float SHARD_FADE_START_TICKS = Timeline.SHARD_FADE_START_TICKS;
        public static final float SHARD_FADE_TICKS = Timeline.SHARD_FADE_TICKS;
        public static final int SHARD_CELLS = GlassShards.CELLS;
        public static final float EDGE_VISIBILITY = GlassShards.EDGE_VISIBILITY;
        public static final float SHARD_MIRROR_STRENGTH = GlassShards.MIRROR_STRENGTH;
        public static final float SHARD_LAUNCH_START_TICKS = GlassShards.LAUNCH_START_TICKS;
        public static final float SHARD_LAUNCH_SPREAD_TICKS = GlassShards.LAUNCH_SPREAD_TICKS;
        public static final float SHARD_CENTER_PULL_PIXELS = GlassShards.CENTER_PULL_PIXELS;
        public static final float SHARD_GRAVITY = GlassShards.GRAVITY;
        public static final float SHARD_DEPTH_SPEED = GlassShards.DEPTH_SPEED;
        public static final float SHARD_PERSPECTIVE_FOCAL = GlassShards.PERSPECTIVE_FOCAL;
        public static final float SHARD_TUMBLE_SPEED = GlassShards.TUMBLE_SPEED;
        public static final float SHARD_THICKNESS_PIXELS = GlassShards.THICKNESS_PIXELS;
        public static final float SHARD_BLOOM_PIXELS = GlassLighting.BLOOM_PIXELS;
        public static final float SHARD_BLOOM_ALPHA = GlassLighting.BLOOM_ALPHA;
        public static final float SHARD_MOTION_BLUR_TICKS = GlassLighting.MOTION_BLUR_TICKS;
        public static final float SHARD_MOTION_BLUR_ALPHA = GlassLighting.MOTION_BLUR_ALPHA;
        public static final float SHARD_CRACK_LIGHT_LENGTH_PIXELS = GlassLighting.CRACK_LIGHT_LENGTH_PIXELS;
        public static final float SHARD_CRACK_LIGHT_WIDTH_PIXELS = GlassLighting.CRACK_LIGHT_WIDTH_PIXELS;
        public static final float SHARD_CRACK_LIGHT_ALPHA = GlassLighting.CRACK_LIGHT_ALPHA;
        public static final float BREAK_BURST_IMPACT_PRE_TICKS = BreakBurst.IMPACT_PRE_TICKS;
        public static final float BREAK_BURST_IMPACT_POST_TICKS = BreakBurst.IMPACT_POST_TICKS;
        public static final float BREAK_BURST_IMPACT_STRENGTH = BreakBurst.IMPACT_STRENGTH;
        public static final float BREAK_BURST_IMPACT_INVERT_STRENGTH = BreakBurst.IMPACT_INVERT_STRENGTH;
        public static final float BREAK_BURST_IMPACT_THRESHOLD_STRENGTH = BreakBurst.IMPACT_THRESHOLD_STRENGTH;
        public static final float BREAK_BURST_IMPACT_COMPRESSION = BreakBurst.IMPACT_COMPRESSION;
        public static final float BREAK_BURST_IMPACT_CORE_ALPHA = BreakBurst.IMPACT_CORE_ALPHA;
        public static final float BREAK_BURST_IMPACT_VIGNETTE_STRENGTH = BreakBurst.IMPACT_VIGNETTE_STRENGTH;
        public static final float BREAK_BURST_DURATION_TICKS = BreakBurst.DURATION_TICKS;
        public static final float BREAK_BURST_FLASH_ALPHA = BreakBurst.FLASH_ALPHA;
        public static final float BREAK_BURST_CORE_ALPHA = BreakBurst.CORE_ALPHA;
        public static final float BREAK_BURST_RING_ALPHA = BreakBurst.RING_ALPHA;
        public static final float BREAK_BURST_RAY_ALPHA = BreakBurst.RAY_ALPHA;
        public static final float BREAK_BURST_START_RADIUS = BreakBurst.START_RADIUS;
        public static final float BREAK_BURST_END_RADIUS = BreakBurst.END_RADIUS;
        public static final float BREAK_BURST_RING_WIDTH = BreakBurst.RING_WIDTH;
        public static final float BREAK_BURST_RING_CORE_FRACTION = BreakBurst.RING_CORE_FRACTION;
        public static final float UOM_DURATION_TICKS = ScreenFx.UOM_DURATION_TICKS;
        public static final float UOM_RATE = ScreenFx.UOM_RATE;
        public static final float UOM_STRENGTH = ScreenFx.UOM_STRENGTH;
        public static final float UOM_EDGE_WEIGHT = ScreenFx.UOM_EDGE_WEIGHT;
        public static final float UOM_CONTRAST = ScreenFx.UOM_CONTRAST;
        public static final float EDGE_MOTION_BLUR_VELOCITY_PIXELS = ScreenFx.EDGE_MOTION_BLUR_VELOCITY_PIXELS;
        public static final float EDGE_MOTION_BLUR_INTENSITY = ScreenFx.EDGE_MOTION_BLUR_INTENSITY;
        public static final float EDGE_MOTION_BLUR_FOCUS_RADIUS = ScreenFx.EDGE_MOTION_BLUR_FOCUS_RADIUS;
        public static final float EDGE_MOTION_BLUR_RAMP_IN_TICKS = ScreenFx.EDGE_MOTION_BLUR_RAMP_IN_TICKS;
        public static final float EDGE_MOTION_BLUR_FADE_TICKS = ScreenFx.EDGE_MOTION_BLUR_FADE_TICKS;
        public static final float PRESSURE_WARP_STRENGTH = ScreenFx.PRESSURE_WARP_STRENGTH;
        public static final float PRESSURE_SHOCKWAVE_STRENGTH = ScreenFx.PRESSURE_SHOCKWAVE_STRENGTH;
        public static final float UOM_FINAL_SCALE = ScreenFx.UOM_FINAL_SCALE;
        public static final float UOM_FINAL_CONTRAST_SCALE = ScreenFx.UOM_FINAL_CONTRAST_SCALE;
        public static final float UOM_CENTER_FLASH = ScreenFx.UOM_CENTER_FLASH;
        public static final float ANIME_IMPACT_DURATION_TICKS = ScreenFx.ANIME_IMPACT_DURATION_TICKS;
        public static final float ANIME_IMPACT_CONTRAST_SCALE = ScreenFx.ANIME_IMPACT_CONTRAST_SCALE;
        public static final float ANIME_IMPACT_THRESHOLD_STRENGTH = ScreenFx.ANIME_IMPACT_THRESHOLD_STRENGTH;
        public static final float ANIME_IMPACT_INVERT_STRENGTH = ScreenFx.ANIME_IMPACT_INVERT_STRENGTH;
        public static final float ANIME_IMPACT_MONO_STRENGTH = ScreenFx.ANIME_IMPACT_MONO_STRENGTH;
        public static final float ANIME_IMPACT_WHITE_FLASH_STRENGTH = ScreenFx.ANIME_IMPACT_WHITE_FLASH_STRENGTH;
        public static final float ANIME_IMPACT_CENTER_FLASH_SCALE = ScreenFx.ANIME_IMPACT_CENTER_FLASH_SCALE;
        public static final float UOM_VIGNETTE_STRENGTH = ScreenFx.UOM_VIGNETTE_STRENGTH;
        public static final float SCREEN_CHROMA_EDGE_START = ScreenChroma.EDGE_START;
        public static final float SCREEN_CHROMA_EDGE_END = ScreenChroma.EDGE_END;
        public static final float DISTORTION_PRE_WIDTH_PIXELS = ScreenDistortion.PRE_WIDTH_PIXELS;
        public static final float DISTORTION_PRE_STRENGTH_PIXELS = ScreenDistortion.PRE_STRENGTH_PIXELS;
        public static final float DISTORTION_BREAK_WIDTH_PIXELS = ScreenDistortion.BREAK_WIDTH_PIXELS;
        public static final float DISTORTION_BREAK_STRENGTH_PIXELS = ScreenDistortion.BREAK_STRENGTH_PIXELS;
        public static final float DISTORTION_MAX_STRENGTH_PIXELS = ScreenDistortion.MAX_STRENGTH_PIXELS;
        public static final float DISTORTION_CENTER_FADE_INNER_RADIUS = ScreenDistortion.CENTER_FADE_INNER_RADIUS;
        public static final float DISTORTION_CENTER_FADE_OUTER_RADIUS = ScreenDistortion.CENTER_FADE_OUTER_RADIUS;
        public static final float DISTORTION_CENTER_FADE_MIN_STRENGTH = ScreenDistortion.CENTER_FADE_MIN_STRENGTH;
        public static final float VORONOI_BLEND_AMOUNT = ScreenDistortion.VORONOI_BLEND_AMOUNT;
        public static final float VORONOI_CELL_SCALE = ScreenDistortion.VORONOI_CELL_SCALE;
        public static final float VORONOI_WARP_PIXELS = ScreenDistortion.VORONOI_WARP_PIXELS;
        public static final float VORONOI_CRACK_WIDTH_PIXELS = ScreenDistortion.VORONOI_CRACK_WIDTH_PIXELS;
        public static final float VORONOI_CRACK_ALPHA = ScreenDistortion.VORONOI_CRACK_ALPHA;
        public static final float VORONOI_CRACK_COLOR = ScreenDistortion.VORONOI_CRACK_COLOR;
        public static final float VORONOI_CRACK_REVEAL_THRESHOLD = ScreenDistortion.VORONOI_CRACK_REVEAL_THRESHOLD;
        public static final float VORONOI_CENTER_INNER_RADIUS = ScreenDistortion.VORONOI_CENTER_INNER_RADIUS;
        public static final float VORONOI_CENTER_OUTER_RADIUS = ScreenDistortion.VORONOI_CENTER_OUTER_RADIUS;
        public static final float VORONOI_CENTER_FALLOFF_POWER = ScreenDistortion.VORONOI_CENTER_FALLOFF_POWER;
        public static final float VORONOI_SPREAD_START_RADIUS = ScreenDistortion.VORONOI_SPREAD_START_RADIUS;
        public static final float VORONOI_SPREAD_END_RADIUS = ScreenDistortion.VORONOI_SPREAD_END_RADIUS;
        public static final float VORONOI_SPREAD_TICKS = ScreenDistortion.VORONOI_SPREAD_TICKS;
        public static final float VORONOI_SPREAD_FEATHER_RADIUS = ScreenDistortion.VORONOI_SPREAD_FEATHER_RADIUS;
        public static final int DISTORTION_LINE_COUNT = ScreenDistortion.LINE_COUNT;
        public static final float CAMERA_SHAKE_AMPLITUDE_DEGREES = CameraShake.AMPLITUDE_DEGREES;
        public static final int CAMERA_SHAKE_DURATION_TICKS = CameraShake.DURATION_TICKS;
        public static final float CAMERA_SHAKE_FREQUENCY = CameraShake.FREQUENCY;
        public static final float CAMERA_SHAKE_MAX_ANGLE_DEGREES = CameraShake.MAX_ANGLE_DEGREES;
        public static final float CAMERA_SHAKE_ROUGHNESS = CameraShake.ROUGHNESS;
        public static final float CAMERA_SHAKE_YAW_WEIGHT = CameraShake.YAW_WEIGHT;
        public static final float CAMERA_SHAKE_PITCH_WEIGHT = CameraShake.PITCH_WEIGHT;
        public static final float CAMERA_SHAKE_ROLL_WEIGHT = CameraShake.ROLL_WEIGHT;
        public static final boolean SCREEN_FREEZE_ENABLED = ScreenFreeze.ENABLED;
        public static final int SCREEN_FREEZE_RELEASE_AFTER_GLASS_BREAK_TICKS = ScreenFreeze.RELEASE_AFTER_GLASS_BREAK_TICKS;
        public static final float SCREEN_FREEZE_DESATURATION = ScreenFreeze.DESATURATION;
        public static final float SCREEN_FREEZE_CONTRAST = ScreenFreeze.CONTRAST;
        public static final float SCREEN_FREEZE_BRIGHTNESS = ScreenFreeze.BRIGHTNESS;
        public static final float SCREEN_FREEZE_VIGNETTE_STRENGTH = ScreenFreeze.VIGNETTE_STRENGTH;

        private ScreenBreak() {}
    }

    private SlashCofig() {}
}
