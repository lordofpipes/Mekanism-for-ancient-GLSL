package mekanism.common.lib.frequency;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import mekanism.api.NBTConstants;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.network.BasePacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class FrequencyType<FREQ extends Frequency> {

    private static final Map<String, FrequencyType<?>> registryMap = new HashMap<>();

    public static final FrequencyType<TeleporterFrequency> TELEPORTER = register("Teleporter",
          (key, uuid) -> new TeleporterFrequency((String) key, uuid),
          TeleporterFrequency::new,
          FrequencyManagerWrapper.Type.PUBLIC_PRIVATE,
          IdentitySerializer.NAME);
    public static final FrequencyType<InventoryFrequency> INVENTORY = register("Inventory",
          (key, uuid) -> new InventoryFrequency((String) key, uuid),
          InventoryFrequency::new,
          FrequencyManagerWrapper.Type.PUBLIC_PRIVATE,
          IdentitySerializer.NAME);
    public static final FrequencyType<SecurityFrequency> SECURITY = register("Security",
          (key, uuid) -> new SecurityFrequency(uuid),
          SecurityFrequency::new,
          FrequencyManagerWrapper.Type.PUBLIC_ONLY,
          IdentitySerializer.UUID);
    public static final FrequencyType<QIOFrequency> QIO = register("QIO",
          (key, uuid) -> new QIOFrequency((String) key, uuid),
          QIOFrequency::new,
          FrequencyManagerWrapper.Type.PUBLIC_PRIVATE,
          IdentitySerializer.NAME);

    public static void init() {
    }

    private final String name;
    private final BiFunction<Object, UUID, FREQ> creationFunction;
    private final Supplier<FREQ> baseCreationFunction;
    private final IdentitySerializer identitySerializer;
    private final FrequencyManagerWrapper<FREQ> managerWrapper;

    private static <FREQ extends Frequency> FrequencyType<FREQ> register(String name, BiFunction<Object, UUID, FREQ> creationFunction,
          Supplier<FREQ> baseCreationFunction, FrequencyManagerWrapper.Type managerType, IdentitySerializer identitySerializer) {
        FrequencyType<FREQ> type = new FrequencyType<>(name, creationFunction, baseCreationFunction, managerType, identitySerializer);
        registryMap.put(name, type);
        return type;
    }

    private FrequencyType(String name, BiFunction<Object, UUID, FREQ> creationFunction, Supplier<FREQ> baseCreationFunction,
          FrequencyManagerWrapper.Type managerType, IdentitySerializer identitySerializer) {
        this.name = name;
        this.creationFunction = creationFunction;
        this.baseCreationFunction = baseCreationFunction;
        this.managerWrapper = FrequencyManagerWrapper.create(this, managerType);
        this.identitySerializer = identitySerializer;
    }

    public String getName() {
        return name;
    }

    public FREQ create(CompoundTag tag) {
        FREQ freq = baseCreationFunction.get();
        freq.read(tag);
        return freq;
    }

    public FREQ create(Object key, UUID ownerUUID) {
        return creationFunction.apply(key, ownerUUID);
    }

    public FREQ create(FriendlyByteBuf packet) {
        FREQ freq = baseCreationFunction.get();
        freq.read(packet);
        return freq;
    }

    public FrequencyManagerWrapper<FREQ> getManagerWrapper() {
        return managerWrapper;
    }

    public FrequencyManager<FREQ> getManager(@Nullable UUID owner) {
        return owner == null ? getManagerWrapper().getPublicManager() : getManagerWrapper().getPrivateManager(owner);
    }

    @Nullable
    @Contract("null -> null")
    public FrequencyManager<FREQ> getFrequencyManager(@Nullable FREQ freq) {
        if (freq == null) {
            return null;
        } else if (freq.isPublic()) {
            return getManagerWrapper().getPublicManager();
        }
        return getManagerWrapper().getPrivateManager(freq.getOwner());
    }

    public FrequencyManager<FREQ> getManager(FrequencyIdentity identity, UUID owner) {
        return identity.isPublic() ? getManagerWrapper().getPublicManager() : getManagerWrapper().getPrivateManager(owner);
    }

    public FREQ getFrequency(FrequencyIdentity identity, UUID owner) {
        return getManager(identity, owner).getFrequency(identity.key());
    }

    public IdentitySerializer getIdentitySerializer() {
        return identitySerializer;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(name);
    }

    public static <FREQ extends Frequency> FrequencyType<FREQ> load(FriendlyByteBuf buf) {
        return (FrequencyType<FREQ>) registryMap.get(BasePacketHandler.readString(buf));
    }

    public static <FREQ extends Frequency> FrequencyType<FREQ> load(CompoundTag tag) {
        return (FrequencyType<FREQ>) registryMap.get(tag.getString(NBTConstants.TYPE));
    }

    public static void clear() {
        registryMap.values().forEach(type -> type.managerWrapper.clear());
    }
}