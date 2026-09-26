package lykrast.turf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Turf.MODID)
public class Turf {
	public static final String MODID = "turf";

	public Turf(IEventBus bus) {
		BLOCKS.register(bus);
		ITEMS.register(bus);
		CREATIVE_MODE_TABS.register(bus);
	}

	// Generic DeferredRegisters use Supplier-based register(), which keeps the
	// original 1.20.1 registration code working unchanged on 1.21.1.
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, MODID);
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, MODID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

	private static Supplier<? extends Item> turfItem;

	//This is full of duct tape
	public static List<Tuple<Supplier<? extends Block>, TurfColor>> blocksToColor = new ArrayList<>();
	public static List<Tuple<Supplier<? extends Item>, TurfColor>> itemsToColor = new ArrayList<>();

	static {
		//null for the default turf
		DeferredHolder<Block, Block> turf = makeTurfBlock("turf", () -> new Block(grassProperties()), null);
		//1.21 note: StairBlock now takes a direct BlockState instead of a Supplier<BlockState>.
		//The outer registration supplier is still deferred, so turf.get() is safe here (turf registers first).
		makeTurfBlock("turf_stairs", () -> new StairBlock(turf.get().defaultBlockState(), grassProperties()), null);
		makeTurfBlock("turf_slab", () -> new SlabBlock(grassProperties()), null);
		makeTurfBlock("turf_wall", () -> new WallBlock(grassProperties()), null);

		for (TurfColor color : TurfColor.values()) {
			if (!color.shouldRegister()) continue;
			String name = color.getName();
			MapColor matColor = color.getMapColor();

			DeferredHolder<Block, Block> dyed = makeTurfBlock(name + "_turf", () -> new Block(grassProperties(matColor)), color);
			makeTurfBlock(name + "_turf_stairs", () -> new StairBlock(dyed.get().defaultBlockState(), grassProperties(matColor)), color);
			makeTurfBlock(name + "_turf_slab", () -> new SlabBlock(grassProperties(matColor)), color);
			makeTurfBlock(name + "_turf_wall", () -> new WallBlock(grassProperties(matColor)), color);
		}
	}

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TURF_TAB = CREATIVE_MODE_TABS.register("turf",
			() -> CreativeModeTab.builder()
					.title(Component.translatable("itemGroup.turf"))
					.icon(() -> turfItem.get().getDefaultInstance())
					.displayItems((parameters, output) -> itemsToColor.forEach(t -> output.accept(t.getA().get())))
					.build());

	private static <T extends Block> DeferredHolder<Block, T> makeTurfBlock(String name, Supplier<T> block, TurfColor color) {
		DeferredHolder<Block, T> reggedBlock = BLOCKS.register(name, block);
		DeferredHolder<Item, BlockItem> reggedItem = ITEMS.register(name, () -> new BlockItem(reggedBlock.get(), new Item.Properties()));
		blocksToColor.add(new Tuple<>(reggedBlock, color));
		itemsToColor.add(new Tuple<>(reggedItem, color));

		//So uh the way I did this I can't cleanly extract the turf block item without rewritting this
		//so instead here's a hack cause I know I'm making the normal turf first
		if (turfItem == null) turfItem = reggedItem;

		return reggedBlock;
	}

	private static BlockBehaviour.Properties grassProperties() {
		//Grass ticks randomly, I don't want that but there's no method to turn it off, so just copying stuff manually
		return BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).strength(0.6F).sound(SoundType.GRASS);
	}

	private static BlockBehaviour.Properties grassProperties(MapColor color) {
		//Grass ticks randomly, I don't want that but there's no method to turn it off, so just copying stuff manually
		return BlockBehaviour.Properties.of().mapColor(color).strength(0.6F).sound(SoundType.GRASS);
	}
}
