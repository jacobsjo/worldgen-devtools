package eu.jacobsjo.worldgendevtools.externalprofiling.mixin;


import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.modmuss50.tracyutils.NameableZone;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.TracyZoneFiller;
import net.minecraft.util.profiling.Zone;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement$Placer")
public class JigsawPlacementPlacerMixin {

    @WrapMethod(method = "tryPlacingChildren")
    void tryPlacingChildren(PoolElementStructurePiece poolElementStructurePiece, MutableObject<VoxelShape> mutableObject, int depth, boolean bl, LevelHeightAccessor levelHeightAccessor, RandomState randomState, PoolAliasLookup poolAliasLookup, LiquidSettings liquidSettings, Operation<Void> original){
        try (Zone zone = Profiler.get().zone("tryPlacingChildren")) {
            if (zone.profiler instanceof TracyZoneFiller) {
                ((NameableZone) ((TracyZoneFiller) zone.profiler).activeZone()).tracy_utils$setName(poolElementStructurePiece.getElement().toString());
            }
            zone.addText("pos: " + poolElementStructurePiece.getPosition().toShortString());
            zone.addText("depth: " + depth);
            original.call(poolElementStructurePiece, mutableObject, depth, bl, levelHeightAccessor, randomState, poolAliasLookup, liquidSettings);
        }
    }
}
