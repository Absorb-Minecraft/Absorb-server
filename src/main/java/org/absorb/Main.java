package org.absorb;

import org.absorb.command.CommandManager;
import org.absorb.command.Commands;
import org.absorb.console.ConsoleSource;
import org.absorb.event.EventManager;
import org.absorb.files.ServerProperties;
import org.absorb.message.channel.ChannelManager;
import org.absorb.module.ModuleManager;
import org.absorb.module.loader.ModuleLoaders;
import org.absorb.module.loader.absorb.AbsorbModule;
import org.absorb.module.loader.absorb.AbsorbModuleLoader;
import org.absorb.net.NetManager;
import org.absorb.net.handler.NetHandler;
import org.absorb.register.AbsorbKey;
import org.absorb.register.RegistryManager;
import org.absorb.schedule.Schedule;
import org.absorb.schedule.ScheduleManager;
import org.absorb.utils.Identifiable;
import org.absorb.world.AbsorbWorld;
import org.absorb.world.AbsorbWorldBuilder;
import org.absorb.world.AbsorbWorldData;
import org.absorb.world.AbsorbWorldManager;
import org.absorb.world.type.WorldTypes;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.math.vector.Vector3i;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {

    public static boolean IS_RUNNING = true;

    public static void init() throws IOException {
        for (int i = 0; i < 25; i++) {
            System.out.println();
        }
        AbsorbManagers.instance = new AbsorbManagers();
        AbsorbManagers.instance.console = new ConsoleSource();
        AbsorbManagers.getConsole().setProgress(0, 5);
        AbsorbManagers.instance.registryManager = new RegistryManager();
        AbsorbManagers.instance.eventManager = new EventManager();
        AbsorbManagers.instance.moduleManager = new ModuleManager();
        AbsorbManagers.instance.scheduleManager = new ScheduleManager();
        AbsorbManagers.instance.channelManager = new ChannelManager();
        AbsorbManagers.instance.commandManager = new CommandManager();


        Commands.getAll();
        AbsorbManagers.getConsole().setProgress(1, 5);


        AbsorbManagers.instance.properties = new ServerProperties();
        AbsorbWorld world =
                new AbsorbWorldBuilder()
                        .setBlockMax(new Vector3i(600, 20, 600))
                        .setBlockMin(new Vector3i(-200, 0, -200))
                        .setWorldData(new AbsorbWorldData()
                                .setType(WorldTypes.OVERWORLD)
                                .setSeed(0)
                                .setKey(new AbsorbKey(Identifiable.MINECRAFT_HOST, "temp")))
                        .build();
        AbsorbManagers.instance.worldManager = new AbsorbWorldManager(world);

        System.out.println("Loaded world: " + world.getWorldData().getKey().asFormatted());
        AbsorbManagers.getConsole().setProgress(1, 4);

        AbsorbModuleLoader absorbModuleLoader = ModuleLoaders.ABSORB_MODULE;
        if (!AbsorbModuleLoader.MODULE_FOLDER.exists()) {
            AbsorbModuleLoader.MODULE_FOLDER.mkdirs();
        }
        @NotNull Collection<File> canLoad = absorbModuleLoader.getCanLoad();
        System.out.println("Found " + canLoad.size() + " compatible files");
        AbsorbManagers.getConsole().setProgress(2, 4);

        Set<AbsorbModule> loaded = canLoad.parallelStream().map(load -> {
            try {
                return absorbModuleLoader.create(load);
            } catch (IOException e) {
                e.printStackTrace();
                //noinspection ReturnOfNull
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
        System.out.println("Loading modules");
        AbsorbManagers.getConsole().setProgress(3, 4);

        loaded.parallelStream().forEach(module -> {
            try {
                System.out.println("Loading " + module.getDisplayName() + " Version: " + module.getVersion());
                absorbModuleLoader.load(module);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        ServerSocket socket = new ServerSocket(AbsorbManagers.getProperties().getPort());

        NetHandler handler = new NetHandler(socket);
        AbsorbManagers.instance.netManager = new NetManager(handler);

        AbsorbManagers.getConsole().runCommandScanner();

        System.out.println("Ready to accept players");
        AbsorbManagers.instance.console.removeProgress();
        handler.start();
        RegistryManager.getVanillaValues(Schedule.class).parallelStream().forEach(schedule -> AbsorbManagers.instance.scheduleManager.register(schedule));
        AbsorbManagers.getScheduleManager().runSchedulers();
    }

    public static void main(String[] args) {
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
