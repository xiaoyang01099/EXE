package org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword;

// SwordAuraVisualConfig 保存飞剑剑气视觉静态调参，避免服务端实体直接引用客户端渲染类。
public class SwordAuraVisualConfig {
    public static int PREVIEW_LIFE_TICKS = 200; // testitem 静态预览剑气自动清理时间。
    public static double OBJ_BASE_SCALE = 0.012D; // OBJ 模型基础缩放，抵消建模软件导出的较大坐标。
    public static double OBJ_START_SCALE = 0.66D; // 剑气刚生成时的视觉缩放倍率。
    public static double OBJ_END_SCALE = 2.0D; // 剑气生命周期末尾的视觉缩放倍率。
    public static double OBJ_SIDE_OFFSET = 0.0D; // OBJ 沿剑气横向的局部偏移。
    public static double OBJ_UP_OFFSET = 0.0D; // OBJ 沿剑气厚度方向的局部偏移。
    public static double OBJ_FORWARD_OFFSET = 0.0D; // OBJ 沿剑气飞行方向的局部偏移。
    public static double OBJ_FORWARD_SIGN = -1.0D; // OBJ 局部前后方向符号，-1 用于修正模型发射方向反向。
    public static double OBJ_YAW_DEGREES = 0.0D; // OBJ 局部水平修正角。
    public static double OBJ_PITCH_DEGREES = 0.0D; // OBJ 局部俯仰修正角。
    public static double OBJ_ROLL_DEGREES = 0.0D; // OBJ 局部翻滚修正角。
    public static double OBJ_ALPHA = 1.0D; // OBJ 可见颜色透明度。
    public static double BLOOM_STRENGTH_SCALE = 1.05D; // shader bloom 整体强度倍率。
    public static double REVEAL_COMPLETE_PROGRESS = 0.32D; // 剑气从左到右划出显现所占生命周期比例。
    public static double LIFE_SCALE = 0.5D; // 剑气有效存在时间倍率，配合速度倍率保持移动距离。
    public static double MAX_ROLL_DEGREES = 360.0D; // 剑气最大视觉旋转角度。
    public static double TRAIL_BACK_OFFSET = 0.28D; // 拖尾粒子相对剑气中心的后方偏移，降低贴近玩家视角的遮挡。
    public static double TRAIL_SIDE_OFFSET = 0.88D; // 左右两组拖尾粒子的横向偏移。
    public static double TRAIL_SIDE_DIRECTION_SCALE = 0.38D; // 左右拖尾向两侧扩散的方向强度。
    public static int TRAIL_SIDE_RATE = 18; // 左右拖尾每秒发射数量。


    public SwordAuraVisualConfig() {
    }
}
