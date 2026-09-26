package lykrast.turf;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.GrassColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = Turf.MODID, value = Dist.CLIENT)
public class TurfClient {
	//Put the client stuff in there so it doesn't crash the server
	//It took more than 8 months to report it crashes on server startup, I'm impressed honestly

	//Simplified grass color
	private static BlockColor grassB = (state, world, pos, tintIndex) -> world != null && pos != null ? BiomeColors.getAverageGrassColor(world, pos) : GrassColor.get(0.5, 1);
	private static ItemColor grassI = (stack, tintIndex) -> GrassColor.get(0.5, 1);

	//null for the default turf
	//Also I think I'm making about 4x as many objects as I actually need...
	//might see if I wanna clean it later out of good conscience, though I don't think it would matter much
	private static BlockColor colorB(TurfColor color) {
		if (color == null) return grassB;
		return (state, world, pos, tintIndex) -> color.getColor();
	}

	private static ItemColor colorI(TurfColor color) {
		if (color == null) return grassI;
		return (stack, tintIndex) -> color.getColor();
	}

	@SubscribeEvent
	public static void registerBlockColors(final RegisterColorHandlersEvent.Block event) {
		for (var t : Turf.blocksToColor) event.register(colorB(t.getB()), t.getA().get());
		//Don't need them after that, hopefully that lets them be garbage collected
		Turf.blocksToColor = null;
	}

	@SubscribeEvent
	public static void registerItemColors(final RegisterColorHandlersEvent.Item event) {
		for (var t : Turf.itemsToColor) event.register(colorI(t.getB()), t.getA().get());
		//No I'm keeping it for making the creative tab
		//Turf.itemsToColor = null;
	}

}
