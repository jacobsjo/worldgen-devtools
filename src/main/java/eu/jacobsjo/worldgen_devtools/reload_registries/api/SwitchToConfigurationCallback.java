package eu.jacobsjo.worldgen_devtools.reload_registries.api;

public interface SwitchToConfigurationCallback {
    interface Callback {
        void handle();
    }
    void worldgenDevtools$onSwitchToConfiguration(SwitchToConfigurationCallback.Callback callback);
}
