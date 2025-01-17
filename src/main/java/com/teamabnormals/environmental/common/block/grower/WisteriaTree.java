package com.teamabnormals.environmental.common.block.grower;

import com.teamabnormals.environmental.common.levelgen.util.WisteriaColor;
import com.teamabnormals.environmental.core.registry.EnvironmentalFeatures.EnvironmentalConfiguredFeatures;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.Random;

public class WisteriaTree extends AbstractTreeGrower {
	private final WisteriaColor color;

	public WisteriaTree(WisteriaColor colorIn) {
		color = colorIn;
	}

	@Nullable
	protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(Random random, boolean beehive) {
		return this.getFeatureForColor(this.color, random.nextInt(10) == 0, beehive).getHolder().get();
	}

	private RegistryObject<ConfiguredFeature<TreeConfiguration, ?>> getFeatureForColor(WisteriaColor color, boolean big, boolean beehive) {
		return !big ? switch (color) {
			case PURPLE -> beehive ? EnvironmentalConfiguredFeatures.BIG_PURPLE_WISTERIA_BEES_005 : EnvironmentalConfiguredFeatures.BIG_PURPLE_WISTERIA;
			case WHITE -> beehive ? EnvironmentalConfiguredFeatures.BIG_WHITE_WISTERIA_BEES_005 : EnvironmentalConfiguredFeatures.BIG_WHITE_WISTERIA;
			case PINK -> beehive ? EnvironmentalConfiguredFeatures.BIG_PINK_WISTERIA_BEES_005 : EnvironmentalConfiguredFeatures.BIG_PINK_WISTERIA;
			case BLUE -> beehive ? EnvironmentalConfiguredFeatures.BIG_BLUE_WISTERIA_BEES_005 : EnvironmentalConfiguredFeatures.BIG_BLUE_WISTERIA;
		} : switch (color) {
			case PURPLE -> beehive ? EnvironmentalConfiguredFeatures.PURPLE_WISTERIA_BEES_005 : EnvironmentalConfiguredFeatures.PURPLE_WISTERIA;
			case WHITE -> beehive ? EnvironmentalConfiguredFeatures.WHITE_WISTERIA_BEES_005 : EnvironmentalConfiguredFeatures.WHITE_WISTERIA;
			case PINK -> beehive ? EnvironmentalConfiguredFeatures.PINK_WISTERIA_BEES_005 : EnvironmentalConfiguredFeatures.PINK_WISTERIA;
			case BLUE -> beehive ? EnvironmentalConfiguredFeatures.BLUE_WISTERIA_BEES_005 : EnvironmentalConfiguredFeatures.BLUE_WISTERIA;
		};
	}
}