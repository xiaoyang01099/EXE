package org.xiaoyang.ex_enigmaticlegacy.Event;

import org.xiaoyang.ex_enigmaticlegacy.Client.particle.state.SparklingOutlineClientState;

// SparklingOutlineClientHandler 在客户端网络线程回调中更新闪闪果实火焰描边缓存。
public class SparklingOutlineClientHandler {
    // 应用服务端同步来的火焰描边状态。
    public static void apply(int entityId, boolean active, int durationTicks) {
        if (active) {
            SparklingOutlineClientState.activate(entityId, durationTicks);
            return;
        }
        SparklingOutlineClientState.deactivate(entityId);
    }
}
