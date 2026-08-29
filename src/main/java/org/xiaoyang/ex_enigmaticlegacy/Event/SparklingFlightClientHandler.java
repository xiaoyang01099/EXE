package org.xiaoyang.ex_enigmaticlegacy.Event;

import org.xiaoyang.ex_enigmaticlegacy.Client.particle.state.SparklingFlightClientState;

// SparklingFlightClientHandler 在客户端网络回调中应用闪闪果实加速飞行状态。
public class SparklingFlightClientHandler {
    // 应用服务端同步的开始或停止状态。
    public static void apply(int entityId, boolean boosting, boolean horizontalPose, double maxSpeed) {
        SparklingFlightClientState.apply(entityId, boosting, horizontalPose, maxSpeed);
    }
}
