SimpleFluidTanks (SFT)
======================
SimpleFluidTanks (SFT) is a minecraft mod that adds multiblock fluid tanks which can be build in virtually any size and shape.

Download
========
Get the lastest version at [CurseForge](https://www.curseforge.com/minecraft/mc-mods/simplefluidtanks/files)

Design
======
This is supposed to serve as a rough overview of the mods inner workings.

The basic idea behind the design of SFT is not to use ticking tile entities but to precalculate the fluid distribution. Two algorithms are used to achieve this. What they do on a high level is simply assign a priority to each and every TankBlock in the multiblock structure. The TankBlocks themselves don't hold any fluid, it's all stored in the ValveBlock. They only store a fill percentage, the position of the ValveBlock they are connected to and some other data used for rendering. When fluid is added or drained, the fill percentages of the TankBlocks are updated using the precalculated priorities.

Compared to a system with ticking tile entities that constantly tries to rebalance the fluid, the precalculated approach scales better. This shows especially with large tanks. All the heavy lifting is done only once when the ValveBlock is placed. Fluid distribution itself is simple and fast.

Setting up Eclipse Workspace and Compiling
=========================================
- Clone the repo to the folder you want to work in (aka the working directory).
- Check out the branch you're interested in.
- Open build.gradle in an editor and find the Forge version the mod is compiled against (e.g. version = "1.7.10-10.13.0.1188")
- Go to http://files.minecraftforge.net/ and download the appropriate MDK (Forge Src zip before 1.8).
- Extract the zip to wherever you want but not to your working directory.
- Copy the eclipse folder into your working directory.
- Open up a console and execute "gradlew setupDecompWorkspace" then "gradlew eclipse" in your working directory.
- Your workspace is now located in "yourWorkingDir\eclipse". You can open it with Eclipse and hack away.
- To compile, simply type "gradlew build" in the console. The binary will be located in "yourWorkingDir\build\libs".
