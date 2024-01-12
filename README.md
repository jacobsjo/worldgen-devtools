# Worldgen Devtools

This is a mod that provides tools simlifying the creation of worldgen datapacks and mods.  

## Advanced /reload
Needs be enabled per world using `/gamerule reloadRegistries true`.

**Warning: This tool might corrupt your worlds, only enable in testing worlds**

Allows the reloading of registry content (such as all worldgen data) using the `/reload` command, or when enabling or disabling a datapack using the `/datapack` command. Without this mod, the world needs to be reloaded for changes to take effect.

### Client Syncronization
Some reloaded data affects the client. In singleplayer, biome colors are only applied when reloading the chunks (as with `F3+A`). In multiplayer, dimension and biome effects need to be synconized. This is done automatically after each reload.
If this behaviour is undesireble, it can be disabled using `/gamerule syncClients false`.

### Current known limitations

- `/locate` sometimes doesn't find stuctures that are in loaded chunks during a reload.
- When `size_horizontal` is changed in the `noise_settings` the world crashes when loading new chunks.
- If the reload fails (i.e. because of errors in the datapack), the world state is indeterministic. The "keeping old data" message is lying.

## Registered vanilla comands
Some testing commands exist in vanilla, but aren't enabled. This mod enables:

### `/resetchunks [<range>]`
Resets the chunks surrounding the player in the specified range - regenerating it with the current worldgen data and settings. Defaults to `0` (i.e. only the current chunk). Does not reset structure positions, but does regenerate structure pieces.

### `/chase`
Provides options to syncronize of the player positions between two different instances of Minecraft. Usefull for comparing differences in worldgen.

## Colored Jigsaw Blocks
Colors Jigsaw blocks based on their specified "Name" or "Target Name". If a "Name" other than `minecraft:empty` is set, the jigsaw block is colored based on the hash of the "Name".
Otherwise, if a "Target Name" other than `minecraft:empty` is set, the jigsaw block is colored based on the hash of the "Target Name".

If both "Name" and "Target Name" are set to `minecraft:empty` the jigsaw block is rendered *deactivated*, with grayed out arrows and connectors.

In the inventory, if a jigsaw block has stored NBT (Shift+Pick), then it is rendered with the associated color and a tooltip indicated the "Name", "Target Pool", and "Target Name".

## World generation settings
A set of gamerules to disable parts of the world generation process:

### `/gamerule maxChunkStatus`
Controlls which steps of chunk generation to process.
- `NOISE_ONLY`: Generates stone, air, water and lava only.
- `SURFACE`: Applies the surface rules.
- `SURFACE_AND_CARVERS`: Applies surface rules, and carvers.
- `ALL`. Applies surface rules, carvers, and features.

### `/gamerule applyProcessorLists`
Controlls whether to apply the processor lists specified in the template pools.

### `/gamerule applyGravityProcessor`
Controlls whether structure pieces which are set to `"projection": "terrain_matching"` should have a `Gravity` processor applies. This 
processor is responsible for moving each column of blocks up or down to match the terrain.

### `/gamerule keepJigsaws`
Controlls whether to replace the jigsaw blocks with their "Turns Into" block after generation.

## `/getDensity` command
Command to debug the values of density functions are any position.

### `/getDensity density_function <df> [<pos>]`
Returns the value of the density function `df` at the specified postion (or the position of the player)

### `/getDensity noise_router <entry> [<pos>]`
Returns the value of the density function specified in the given entry of the noise router of the current dimension at the specified postion (or the position of the player)
