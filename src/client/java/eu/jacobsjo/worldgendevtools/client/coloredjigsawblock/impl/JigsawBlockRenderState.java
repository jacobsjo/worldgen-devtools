package eu.jacobsjo.worldgendevtools.client.coloredjigsawblock.impl;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.FrontAndTop;

public class JigsawBlockRenderState extends BlockEntityRenderState {
    public int argb = 0;
    public FrontAndTop orientation = FrontAndTop.UP_NORTH;
}
