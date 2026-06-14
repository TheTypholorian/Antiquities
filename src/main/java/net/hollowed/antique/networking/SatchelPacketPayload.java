package net.hollowed.antique.networking;

import net.hollowed.antique.Antiquities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record SatchelPacketPayload(boolean bool) implements CustomPacketPayload {

    public static final Type<SatchelPacketPayload> ID = new Type<>(Antiquities.id("satchel_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SatchelPacketPayload> CODEC = StreamCodec.ofMember(SatchelPacketPayload::write, SatchelPacketPayload::new);

    public SatchelPacketPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readBoolean());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(bool);
    }

    @SuppressWarnings("all")
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }

}
