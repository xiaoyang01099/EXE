package org.xiaoyang.ex_enigmaticlegacy.api.shader.coffin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class CoffinData extends SavedData {
    public final Map<UUID, Entry> entries = new LinkedHashMap<>();

    public static final class Entry {
        public UUID target, attacker;
        public ResourceLocation dimension;
        public Vec3 anchor;
        public float yaw, pitch, health, width, height;
        public boolean player;
        public int age, form;
        int missingTicks;

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("Target", target);
            tag.putUUID("Attacker", attacker);
            tag.putString("Dimension", dimension.toString());
            tag.putDouble("X", anchor.x); tag.putDouble("Y", anchor.y); tag.putDouble("Z", anchor.z);
            tag.putFloat("Yaw", yaw); tag.putFloat("Pitch", pitch); tag.putFloat("Health", health);
            tag.putFloat("Width", width); tag.putFloat("Height", height);
            tag.putBoolean("Player", player); tag.putInt("Age", age);
            tag.putInt("Form",form);
            return tag;
        }

        static Entry load(CompoundTag tag) {
            Entry entry = new Entry();
            entry.target = tag.getUUID("Target"); entry.attacker = tag.getUUID("Attacker");
            entry.dimension = new ResourceLocation(tag.getString("Dimension"));
            entry.anchor = new Vec3(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"));
            entry.yaw = tag.getFloat("Yaw"); entry.pitch = tag.getFloat("Pitch");
            entry.health = Math.max(0.01f, tag.getFloat("Health"));
            entry.width = tag.getFloat("Width"); entry.height = tag.getFloat("Height");
            entry.form=tag.getInt("Form")==1?1:0;
            entry.player = tag.getBoolean("Player"); entry.age = Math.max(0,Math.min(CoffinExecutions.duration(entry.form), tag.getInt("Age")));
            return entry;
        }
    }

    public static CoffinData load(CompoundTag tag) {
        CoffinData data = new CoffinData();
        ListTag entries = tag.getList("Executions", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = Entry.load(entries.getCompound(i));
            data.entries.put(entry.target, entry);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        entries.values().forEach(entry -> list.add(entry.save()));
        tag.put("Executions", list);
        return tag;
    }
}
