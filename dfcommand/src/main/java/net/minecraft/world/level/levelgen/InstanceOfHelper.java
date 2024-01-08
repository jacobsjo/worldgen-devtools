package net.minecraft.world.level.levelgen;

public class InstanceOfHelper {
    public static boolean isInstanceOfEndIslandDensityFunction(DensityFunction densityFunction){
        return densityFunction instanceof DensityFunctions.EndIslandDensityFunction;
    }
}
