package net.xiaoyang010.ex_enigmaticlegacy.Init;

import morph.avaritia.util.InfinityDamageSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;


public class ModDamageSources {
    public static final DamageSource ABSOLUTE = new DamageSource("absolute")
            .bypassArmor()
            .bypassMagic()
            .bypassInvul()
            .setIsFire()
            .damageHelmet()
            .setMagic();

    public static DamageSource causeAbsoluteDamage(Entity attacker) {
        return new EntityDamageSource("absolute", attacker)
                .bypassArmor()
                .bypassMagic()
                .bypassInvul()
                .setMagic();
    }

    public static class DamageSourceTrueDamage extends DamageSource {
        private final Entity entity;

        public DamageSourceTrueDamage(Entity entity) {
            super("trueDamage");
            this.entity = entity;
        }

        @Override
        public Entity getEntity() {
            return this.entity;
        }

        @Override
        public DamageSource bypassArmor() {
            return super.bypassArmor();
        }

        @Override
        public DamageSource bypassInvul() {
            return super.bypassInvul();
        }

        @Override
        public DamageSource bypassMagic() {
            return super.bypassMagic();
        }

        @Override
        public boolean isBypassArmor() {
            return true;
        }

        @Override
        public boolean isBypassInvul() {
            return true;
        }

        @Override
        public boolean isBypassMagic() {
            return true;
        }

        @Override
        public boolean isMagic() {
            return true;
        }
    }

    // 灵魂吸取伤害源
    public static class DamageSourceSoulDrain extends DamageSource {
        private final Entity entity;

        public DamageSourceSoulDrain(Entity entity) {
            super("soulAttack");
            this.entity = entity;
        }

        @Override
        public Entity getEntity() {
            return this.entity;
        }

        @Override
        public DamageSource bypassArmor() {
            return super.bypassArmor();
        }

        @Override
        public DamageSource bypassInvul() {
            return super.bypassInvul();
        }

        @Override
        public boolean isBypassArmor() {
            return true;
        }

        @Override
        public boolean isBypassInvul() {
            return true;
        }

        @Override
        public boolean isMagic() {
            return true;
        }
    }

    // 真实伤害源（无实体）
    public static class DamageSourceTrueDamageUndef extends DamageSource {
        public DamageSourceTrueDamageUndef() {
            super("trueDamageUndef");
        }

        @Override
        public DamageSource bypassArmor() {
            return super.bypassArmor();
        }

        @Override
        public DamageSource bypassInvul() {
            return super.bypassInvul();
        }

        @Override
        public DamageSource bypassMagic() {
            return super.bypassMagic();
        }

        @Override
        public boolean isBypassArmor() {
            return true;
        }

        @Override
        public boolean isBypassInvul() {
            return true;
        }

        @Override
        public boolean isBypassMagic() {
            return true;
        }

        @Override
        public boolean isMagic() {
            return true;
        }
    }

    // 遗忘伤害源
    public static class DamageSourceOblivion extends DamageSource {
        public DamageSourceOblivion() {
            super("attackOblivion");
        }

        @Override
        public boolean isMagic() {
            return true;
        }
    }

    // 命运伤害源
    public static class DamageSourceFate extends DamageSource {
        public DamageSourceFate() {
            super("attackFate");
        }

        @Override
        public DamageSource bypassArmor() {
            return super.bypassArmor();
        }

        @Override
        public DamageSource bypassInvul() {
            return super.bypassInvul();
        }

        @Override
        public DamageSource bypassMagic() {
            return super.bypassMagic();
        }

        @Override
        public boolean isBypassArmor() {
            return true;
        }

        @Override
        public boolean isBypassInvul() {
            return true;
        }

        @Override
        public boolean isBypassMagic() {
            return true;
        }

        @Override
        public boolean isMagic() {
            return true;
        }
    }

    // 雷电伤害源
    public static class DamageSourceTLightning extends DamageSource {
        private final Entity entity;

        public DamageSourceTLightning(Entity entity) {
            super("attackLightning");
            this.entity = entity;
        }

        @Override
        public Entity getEntity() {
            return this.entity;
        }
    }

    // 暗物质伤害源
    public static class DamageSourceDarkMatter extends DamageSource {
        private final Entity entity;

        public DamageSourceDarkMatter(Entity entity) {
            super("attackDarkMatter");
            this.entity = entity;
        }

        @Override
        public Entity getEntity() {
            return this.entity;
        }

        @Override
        public DamageSource bypassArmor() {
            return super.bypassArmor();
        }

        @Override
        public boolean isBypassArmor() {
            return true;
        }

        @Override
        public boolean isMagic() {
            return true;
        }
    }

    // 魔法伤害源
    public static class DamageSourceMagic extends DamageSource {
        private final Entity entity;

        public DamageSourceMagic(Entity entity) {
            super("Magic");
            this.entity = entity;
        }

        @Override
        public Entity getEntity() {
            return this.entity;
        }

        @Override
        public boolean isMagic() {
            return true;
        }
    }

    // 叠加态伤害源（无实体）
    public static class DamageSourceSuperposition extends DamageSource {
        public DamageSourceSuperposition() {
            super("superpositionedDamage");
        }
    }

    // 叠加态伤害源（带实体）
    public static class DamageSourceSuperpositionDefined extends DamageSource {
        private final Entity entity;

        public DamageSourceSuperpositionDefined(Entity entity) {
            super("superpositionedDamage");
            this.entity = entity;
        }

        @Override
        public Entity getEntity() {
            return this.entity;
        }
    }

    public static class DamageSourceParadox extends DamageSource {
        private final Entity entity;

        public DamageSourceParadox(Entity entity) {
            super("paradox");
            this.entity = entity;
        }

        @Override
        public Entity getEntity() {
            return this.entity;
        }

        @Override
        public DamageSource bypassArmor() {
            return super.bypassArmor();
        }

        @Override
        public boolean isBypassArmor() {
            return true;
        }

        @Override
        public boolean isMagic() {
            return true;
        }
    }

    // 悖论反射伤害源（对使用者）
    public static class DamageSourceParadoxReflection extends DamageSource {
        public DamageSourceParadoxReflection() {
            super("paradox_reflection");
        }

        @Override
        public DamageSource bypassArmor() {
            return super.bypassArmor();
        }

        @Override
        public boolean isBypassArmor() {
            return true;
        }

        @Override
        public boolean isMagic() {
            return true;
        }
    }
}