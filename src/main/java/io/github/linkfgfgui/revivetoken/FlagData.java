package io.github.linkfgfgui.revivetoken;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

public record FlagData(boolean reviveTip, Vec3 respawnPos) {
    public static final Codec<FlagData> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    Codec.BOOL
                            .fieldOf("reviveTip")
                            .forGetter(FlagData::reviveTip),
                    Vec3.CODEC
                            .fieldOf("respawnPos")
                            .forGetter(FlagData::respawnPos)
            ).apply(inst, FlagData::new)
    );

    public FlagData clear() { return new FlagData(false,Vec3.ZERO); }
}