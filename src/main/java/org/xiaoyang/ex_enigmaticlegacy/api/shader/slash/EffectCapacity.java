package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

final class EffectCapacity {
    private EffectCapacity() {}

    static int oldestIndexToEvict(List<UUID> activeOwners, UUID incomingOwner, int maximumPerOwner) {
        if (activeOwners == null || activeOwners.isEmpty()) return -1;

        int safeMaximum = Math.max(1, maximumPerOwner);
        int ownedCount = 0;
        int oldestOwnedIndex = -1;
        for (int index = 0; index < activeOwners.size(); index++) {
            if (!Objects.equals(activeOwners.get(index), incomingOwner)) continue;
            if (oldestOwnedIndex < 0) oldestOwnedIndex = index;
            ownedCount++;
        }
        return ownedCount >= safeMaximum ? oldestOwnedIndex : -1;
    }
}
