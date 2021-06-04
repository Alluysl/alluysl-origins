package alluysl.alluyslorigins;

import net.fabricmc.api.ModInitializer;
import alluysl.alluyslorigins.power.AlluyslOriginsPowers;

public class AlluyslOrigins implements ModInitializer {

	public static final String MODID = "alluyslorigins";

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		System.out.println("[Alluysl's Origins] Mod initialized.");

		AlluyslOriginsPowers.register();
	}
}
