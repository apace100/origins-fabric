package io.github.apace100.origins.components;

import com.google.common.collect.ImmutableList;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.power.Power;
import io.github.apace100.origins.power.PowerType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;

public class DummyOriginComponent implements OriginComponent {

	private static final DummyOriginComponent INSTANCE = new DummyOriginComponent();

	@Nonnull
	public static DummyOriginComponent getInstance() {
		return INSTANCE;
	}

	@Override
	public boolean hasOrigin(OriginLayer layer) {
		return true;
	}

	@Override
	public boolean hasAllOrigins() {
		return true;
	}

	@Override
	public HashMap<OriginLayer, Origin> getOrigins() {
		return new HashMap<>();
	}

	@Override
	public Origin getOrigin(OriginLayer layer) {
		return Origin.EMPTY;
	}

	@Override
	public boolean hadOriginBefore() {
		return false;
	}

	@Override
	public boolean hasPower(PowerType<?> powerType) {
		return false;
	}

	@Override
	public <T extends Power> T getPower(PowerType<T> powerType) {
		return null;
	}

	@Override
	public List<Power> getPowers() {
		return ImmutableList.of();
	}

	@Override
	public <T extends Power> List<T> getPowers(Class<T> powerClass) {
		return ImmutableList.of();
	}

	@Override
	public <T extends Power> List<T> getPowers(Class<T> powerClass, boolean includeInactive) {
		return ImmutableList.of();
	}

	@Override
	public void setOrigin(OriginLayer layer, Origin origin) {

	}

	@Override
	public void serverTick() {

	}

	@Override
	public void readFromNbt(CompoundTag compoundTag) {

	}

	@Override
	public void writeToNbt(CompoundTag compoundTag) {

	}

	@Override
	public void applySyncPacket(PacketByteBuf buf) {

	}

	@Override
	public void sync() {

	}
}
