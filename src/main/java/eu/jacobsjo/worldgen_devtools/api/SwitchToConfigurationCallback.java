package eu.jacobsjo.worldgen_devtools.api;

public interface SwitchToConfigurationCallback {
    interface Callback {
        void handle();
    }
    void worldgenDevtools$onSwitchToConfiguration(SwitchToConfigurationCallback.Callback callback);
}
