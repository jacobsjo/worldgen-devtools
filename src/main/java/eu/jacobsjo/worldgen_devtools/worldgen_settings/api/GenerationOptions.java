package eu.jacobsjo.worldgen_devtools.worldgen_settings.api;

public enum GenerationOptions {
    @SuppressWarnings("unused")
    NOISE_ONLY(false, false, false),
    @SuppressWarnings("unused")
    SURFACE(true, false, false),
    @SuppressWarnings("unused")
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