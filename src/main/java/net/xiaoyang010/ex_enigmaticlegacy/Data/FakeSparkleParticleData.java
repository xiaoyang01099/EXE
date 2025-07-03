package net.xiaoyang010.ex_enigmaticlegacy.Data;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;

public class FakeSparkleParticleData extends ParticleType<FakeSparkleParticleData> implements ParticleOptions {
    private final float size;
    private final float red;
    private final float green;
    private final float blue;
    private final int maxAge;
    private static final ParticleOptions.Deserializer<FakeSparkleParticleData> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        public FakeSparkleParticleData fromCommand(ParticleType<FakeSparkleParticleData> p_123846_, StringReader p_123847_) {
            return (FakeSparkleParticleData) p_123846_;
        }

        public FakeSparkleParticleData fromNetwork(ParticleType<FakeSparkleParticleData> p_123849_, FriendlyByteBuf p_123850_) {
            return (FakeSparkleParticleData) p_123849_;
        }
    };

    public FakeSparkleParticleData(){
        this(1.0f, 1.0f, 1.0f, 1.0f, 10);
    }

    public FakeSparkleParticleData(float size, float red, float green, float blue, int maxAge) {
        super(true, DESERIALIZER);
        this.size = size;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.maxAge = maxAge;
    }

    public static FakeSparkleParticleData fakeSpark(float size, float red, float green, float blue, int maxAge) {
        return new FakeSparkleParticleData(size, red, green, blue, maxAge);
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeFloat(size);
        buffer.writeFloat(red);
        buffer.writeFloat(green);
        buffer.writeFloat(blue);
        buffer.writeInt(maxAge);
    }

    @Override
    public String writeToString() {
        return String.format("fake_sparkle %.2f %.2f %.2f %.2f %d", size, red, green, blue, maxAge);
    }

    public float getSize() { return size; }
    public float getRed() { return red; }
    public float getGreen() { return green; }
    public float getBlue() { return blue; }
    public int getMaxAge() { return maxAge; }
    private final Codec<FakeSparkleParticleData> codec = Codec.unit(this::getType);
    public FakeSparkleParticleData getType() {
        return this;
    }

    public Codec<FakeSparkleParticleData> codec() {
        return this.codec;
    }
}