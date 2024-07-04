package org.darkrpa.mods.simplewhitelist.simplewhitelist;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.KickCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import org.slf4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("simplewhitelist")
public class SimpleWhitelist {

    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    private File archivoNombres;
    private static boolean isDisabled;

    public SimpleWhitelist() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // Some preinit code
        LOGGER.info("Iniciando whitelist");
        this.archivoNombres = new File("whitelistModificada.txt");

        if(!this.archivoNombres.exists()){
            try {
                boolean resultado = this.archivoNombres.createNewFile();
            } catch (IOException e) {
                SimpleWhitelist.LOGGER.warn("No se ha podido crear el archivo de la whitelist, desactivando");
                SimpleWhitelist.isDisabled = true;
            }
        }
    }

    private List<String> getPlayerList() throws IOException {
        ArrayList<String> listaJugadores = new ArrayList<>();
        if(SimpleWhitelist.isDisabled()) return null;

        BufferedReader lector = new BufferedReader(new FileReader(this.archivoNombres));
        String nombreActual = "";

        while((nombreActual = lector.readLine()) != null){
            listaJugadores.add(nombreActual.toLowerCase());
        }

        return listaJugadores;
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if(SimpleWhitelist.isDisabled()) return;
        ServerPlayer jugador = (ServerPlayer) event.getPlayer();
        String nombreJugador = jugador.getName().getString();
        try {
            List<String> listaWhitelist = this.getPlayerList();
            if(!listaWhitelist.contains(nombreJugador.toLowerCase())){
                jugador.connection.disconnect(Component.nullToEmpty("No estás en la whitelist"));
                SimpleWhitelist.LOGGER.warn(String.format("%s ha intentado entrar sin estar en la whitelist", jugador.getName().getString()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isDisabled(){
        return SimpleWhitelist.isDisabled;
    }
}
