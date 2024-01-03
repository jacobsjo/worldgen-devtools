package eu.jacobsjo.worldgen_devtools;

public interface SwitchToConfigurationCallback {
    interface Callback {
        void handle();
    }
    public void worldgenDevtools$onSwitchToConfiguration(SwitchToConfigurationCallback.Callback callback);

}
