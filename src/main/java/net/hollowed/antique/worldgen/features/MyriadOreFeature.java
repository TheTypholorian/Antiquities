package net.hollowed.antique.worldgen.features;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

public class MyriadOreFeature extends Feature<MyriadOreFeatureConfig> {
	public MyriadOreFeature(Codec<MyriadOreFeatureConfig> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<MyriadOreFeatureConfig> context) {
		RandomSource random = context.random();
		BlockPos blockPos = context.origin();
		WorldGenLevel structureWorldAccess = context.level();
		MyriadOreFeatureConfig oreFeatureConfig = context.config();
		float f = random.nextFloat() * (float) Math.PI;
		float g = oreFeatureConfig.size() / 8.0F;
		int i = Mth.ceil((oreFeatureConfig.size() / 16.0F * 2.0F + 1.0F) / 2.0F);
		double d = blockPos.getX() + Math.sin(f) * g;
		double e = blockPos.getX() - Math.sin(f) * g;
		double h = blockPos.getZ() + Math.cos(f) * g;
		double j = blockPos.getZ() - Math.cos(f) * g;
		double l = blockPos.getY() + random.nextInt(3) - 2;
		double m = blockPos.getY() + random.nextInt(3) - 2;
		int n = blockPos.getX() - Mth.ceil(g) - i;
		int o = blockPos.getY() - 2 - i;
		int p = blockPos.getZ() - Mth.ceil(g) - i;
		int q = 2 * (Mth.ceil(g) + i);
		int r = 2 * (2 + i);

		for (int s = n; s <= n + q; s++) {
			for (int t = p; t <= p + q; t++) {
				if (o <= structureWorldAccess.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, s, t)) {
					return this.generateVeinPart(structureWorldAccess, random, oreFeatureConfig, d, e, h, j, l, m, n, o, p, q, r);
				}
			}
		}

		return false;
	}

	protected boolean generateVeinPart(
			WorldGenLevel world,
			RandomSource random,
			MyriadOreFeatureConfig config,
			double startX,
			double endX,
			double startZ,
			double endZ,
			double startY,
			double endY,
			int x,
			int y,
			int z,
			int horizontalSize,
			int verticalSize
	) {
		int i = 0;
		BitSet bitSet = new BitSet(horizontalSize * verticalSize * horizontalSize);
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
		int j = config.size();
		double[] ds = new double[j * 4];

		for (int k = 0; k < j; k++) {
			float f = (float)k / j;
			double d = Mth.lerp(f, startX, endX);
			double e = Mth.lerp(f, startY, endY);
			double g = Mth.lerp(f, startZ, endZ);
			double h = random.nextDouble() * j / 16.0;
			double l = ((Mth.sin((float) Math.PI * f) + 1.0F) * h + 1.0) / 2.0;
			ds[k * 4] = d;
			ds[k * 4 + 1] = e;
			ds[k * 4 + 2] = g;
			ds[k * 4 + 3] = l;
		}

		for (int k = 0; k < j - 1; k++) {
			if (!(ds[k * 4 + 3] <= 0.0)) {
				for (int m = k + 1; m < j; m++) {
					if (!(ds[m * 4 + 3] <= 0.0)) {
						double d = ds[k * 4] - ds[m * 4];
						double e = ds[k * 4 + 1] - ds[m * 4 + 1];
						double g = ds[k * 4 + 2] - ds[m * 4 + 2];
						double h = ds[k * 4 + 3] - ds[m * 4 + 3];
						if (h * h > d * d + e * e + g * g) {
							if (h > 0.0) {
								ds[m * 4 + 3] = -1.0;
							} else {
								ds[k * 4 + 3] = -1.0;
							}
						}
					}
				}
			}
		}

		try (BulkSectionAccess chunkSectionCache = new BulkSectionAccess(world)) {
			for (int mx = 0; mx < j; mx++) {
				double d = ds[mx * 4 + 3];
				if (!(d < 0.0)) {
					double e = ds[mx * 4];
					double g = ds[mx * 4 + 1];
					double h = ds[mx * 4 + 2];
					int n = Math.max(Mth.floor(e - d), x);
					int o = Math.max(Mth.floor(g - d), y);
					int p = Math.max(Mth.floor(h - d), z);
					int q = Math.max(Mth.floor(e + d), n);
					int r = Math.max(Mth.floor(g + d), o);
					int s = Math.max(Mth.floor(h + d), p);

					for (int t = n; t <= q; t++) {
						double u = (t + 0.5 - e) / d;
						if (u * u < 1.0) {
							for (int v = o; v <= r; v++) {
								double w = (v + 0.5 - g) / d;
								if (u * u + w * w < 1.0) {
									for (int aa = p; aa <= s; aa++) {
										double ab = (aa + 0.5 - h) / d;
										if (u * u + w * w + ab * ab < 1.0 && !world.isOutsideBuildHeight(v)) {
											int ac = t - x + (v - y) * horizontalSize + (aa - z) * horizontalSize * verticalSize;
											if (!bitSet.get(ac)) {
												bitSet.set(ac);
												mutable.set(t, v, aa);
												if (world.ensureCanWrite(mutable)) {
													LevelChunkSection chunkSection = chunkSectionCache.getSection(mutable);
													if (chunkSection != null) {
														int ad = SectionPos.sectionRelative(t);
														int ae = SectionPos.sectionRelative(v);
														int af = SectionPos.sectionRelative(aa);
														BlockState blockState = chunkSection.getBlockState(ad, ae, af);

														for (MyriadOreFeatureConfig.Target target : config.targets()) {
															if (shouldPlace(blockState, chunkSectionCache::getBlockState, random, config, target.target, mutable)) {
																chunkSection.setBlockState(ad, ae, af, target.state, false);
																i++;

																placeClusters(chunkSectionCache, t, v, aa, target.clusterState, chunkSectionCache::getBlockState, random, config, target.clusterTarget, mutable);
																break;
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		return i > 0;
	}

	public static void placeClusters(BulkSectionAccess chunkSectionCache, int x1, int y1, int z1, BlockState cluster, Function<BlockPos, BlockState> posToState, RandomSource random, MyriadOreFeatureConfig config, RuleTest target, BlockPos.MutableBlockPos pos) {
		for (Direction direction : Direction.values()) {
			BlockPos blockPos = new BlockPos(x1, y1, z1).offset(direction.getUnitVec3i());

			LevelChunkSection chunkSection = chunkSectionCache.getSection(blockPos);
			if (chunkSection != null) {

				int x = SectionPos.sectionRelative(blockPos.getX());
				int y = SectionPos.sectionRelative(blockPos.getY());
				int z = SectionPos.sectionRelative(blockPos.getZ());

				cluster = cluster.setValue(AmethystClusterBlock.FACING, direction)
						.setValue(AmethystClusterBlock.WATERLOGGED, chunkSection.getFluidState(x, y, z).getType() == Fluids.WATER);

				if (shouldPlace(chunkSection.getBlockState(x, y, z), posToState, random, config, target, pos) && Math.random() < 0.3) {
					chunkSection.setBlockState(x, y, z, cluster, false);
				}
			}
		}
	}

	public static boolean shouldPlace(
			BlockState state, Function<BlockPos, BlockState> posToState, RandomSource random, MyriadOreFeatureConfig config, RuleTest target, BlockPos.MutableBlockPos pos
	) {
		if (!target.test(state, random)) {
			return false;
		} else {
			return shouldNotDiscard(random, config.discardOnAirChance()) || !isAdjacentToAir(posToState, pos);
		}
	}

	protected static boolean shouldNotDiscard(RandomSource random, float chance) {
		if (chance <= 0.0F) {
			return true;
		} else {
			return !(chance >= 1.0F) && random.nextFloat() >= chance;
		}
	}
}
