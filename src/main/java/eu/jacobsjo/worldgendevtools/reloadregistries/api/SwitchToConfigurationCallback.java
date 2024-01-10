package eu.jacobsjo.worldgendevtools.reloadregistries.api;

public interface SwitchToConfigurationCallback {
    interface Callback {
        void handle();
    }
    void worldgenDevtools$onSwitchToConfiguration(SwitchToConfigurationCallback.Callback callback);
}
