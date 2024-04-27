package eu.jacobsjo.worldgendevtools.worldgensettings.api;

@SuppressWarnings("unused")
public enum GenerationOptions {
    NOISE_ONLY(false, false, false),
    SURFACE(true, false, false),
    SURFACE_AND_CARVERS(true, true, false),
    ALL(true, true,true);

    public final boolean surface;
    public final boolean carvers;
    public final boolean features;

    GenerationOptions(boolean surface, boolean carvers, boolean features){
        this.surface = surface;
        this.carvers = carvers;
        this.features = features;
    }
}