package eu.jacobsjo.worldgendevtools.reloadregistries.impl;

import net.minecraft.network.chat.Component;

public class ComponentFormattedException extends IllegalStateException{
    Component message;
    public ComponentFormattedException(Component message){
        super(message.getString());

        this.message = message;
    }

    public Component getComponentMessage() {
        return message;
    }
}
