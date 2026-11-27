package core.game.node.entity.player.link;

import core.game.node.entity.player.Player;
import core.game.node.scenery.Scenery;
import core.game.system.task.Pulse;
import core.game.world.GameWorld;
import core.game.world.map.Location;
import core.game.world.map.RegionManager;
import core.game.world.update.flag.EntityFlag;
import core.game.world.update.flag.chunk.AnimateObjectUpdateFlag;
import core.game.world.update.flag.context.Animation;
import core.game.world.update.flag.context.Graphics;
import core.net.packet.PacketRepository;
import core.net.packet.out.Encoders578;
import core.net.packet.out.PacketContext.AccessMaskContext;
import core.net.packet.out.PacketContext.AnimateInterfaceContext;
import core.net.packet.out.PacketContext.AnimateObjectContext;
import core.net.packet.out.PacketContext.CSConfigContext;
import core.net.packet.out.PacketContext.ChildPositionContext;
import core.net.packet.out.PacketContext.ConfigContext;
import core.net.packet.out.PacketContext.DisplayModelContext;
import core.net.packet.out.PacketContext.DisplayModelContext.ModelType;
import core.net.packet.out.PacketContext.GameMessageContext;
import core.net.packet.out.PacketContext.InterfaceConfigContext;
import core.net.packet.out.PacketContext.InterfaceSetAngleContext;
import core.net.packet.out.PacketContext.InterfaceSetModelRotationContext;
import core.net.packet.out.PacketContext.MusicContext;
import core.net.packet.out.PacketContext.PlayerContext;
import core.net.packet.out.PacketContext.PositionedGraphicContext;
import core.net.packet.out.PacketContext.ResetInterfaceContext;
import core.net.packet.out.PacketContext.RunScriptContext;
import core.net.packet.out.PacketContext.StringContext;
import core.net.packet.out.PacketContext.SystemUpdateContext;
import core.net.packet.out.PacketContext.VarcUpdateContext;
import core.net.packet.out.PacketContext.WindowsPaneContext;
import core.tools.Log;

import static core.api.ContentAPIKt.log;
import static core.api.ContentAPIKt.setVarp;

/**
 * Represents the class used to dispatching packets.
 *
 * @author Emperor
 * @author Vexia
 */
public final class PacketDispatch {

    /**
     * The instance of the {@code Player}.
     */
    private final Player player;

    /**
     * The player context.
     */
    private final PlayerContext context;

    /**
     * Constructs a new {@code PacketDispatch} {@code Object}.
     *
     * @param player the player.
     */
    public PacketDispatch(Player player) {
        this.player = player;
        this.context = new PlayerContext(player);
    }

    /** Sends the client's signed run weight without changing the inventory calculation. */
    public void sendRunWeight() {
        PacketRepository.send(Encoders578.UPDATE_RUNWEIGHT, getContext());
    }

    /** Client-only spot animation; does not modify server-side entity masks. */
    public void sendEntitySpotAnimation(boolean npc, int index, int id, int delay, int height) {
        if (index < 0 || index >= (npc ? 32768 : 2048)) {
            throw new IllegalArgumentException("Invalid spot animation entity index");
        }
        sendGlobalSpotAnimation((npc ? 0x20000000 : 0x10000000) | index, id, delay, height);
    }

    /** Tile spot animations are immediate in the revision 578 client. */
    public void sendTileSpotAnimation(Location location, int id, int height) {
        if (location.getX() < 0 || location.getX() > 16383 || location.getY() < 0
                || location.getY() > 16383 || location.getZ() < 0 || location.getZ() > 3) {
            throw new IllegalArgumentException("Invalid spot animation tile");
        }
        sendGlobalSpotAnimation(0x40000000 | (location.getZ() << 28)
                | (location.getX() << 14) | location.getY(), id, 0, height);
    }

    private void sendGlobalSpotAnimation(int target, int id, int delay, int height) {
        PacketRepository.send(Encoders578.GLOBAL_SPOT_ANIMATION,
                new core.net.packet.out.PacketContext.GlobalSpotAnimationContext(player, target, id, delay, height));
    }

    /** Explicit client-only reset; does not change server animator state. */
    public void resetClientEntityAnimations() {
        PacketRepository.send(Encoders578.RESET_ENTITY_ANIMATIONS, getContext());
    }

    /** Caller must resend authoritative varps after resetting client values. */
    public void resetClientVarps() {
        PacketRepository.send(Encoders578.RESET_CLIENT_VARPS, getContext());
    }

    /** Runs the active root interface's dialog-abort hooks, without closing it. */
    public void runDialogAbortHooks() {
        PacketRepository.send(Encoders578.RUN_DIALOG_ABORT_HOOKS, getContext());
    }

    /** Sets a component colour using the client's packed 5-bit RGB format. */
    public void sendComponentColour(int interfaceId, int childId, int rgb555) {
        PacketRepository.send(Encoders578.IF_SETCOLOUR,
                new core.net.packet.out.PacketContext.ComponentValueContext(player, interfaceId, childId, rgb555));
    }

    /** Sets the unsigned 16-bit sprite ID used by revision 578. */
    public void sendComponentSprite(int interfaceId, int childId, int spriteId) {
        PacketRepository.send(Encoders578.IF_SETSPRITE,
                new core.net.packet.out.PacketContext.ComponentValueContext(player, interfaceId, childId, spriteId));
    }

    /** Displays the local player's full appearance (model type 5), not the chat head. */
    public void sendSelfBodyModel(int interfaceId, int childId) {
        PacketRepository.send(Encoders578.IF_SET_SELF_BODY_MODEL,
                new core.net.packet.out.PacketContext.ComponentValueContext(player, interfaceId, childId, 0));
    }

    /** Displays a client-visible player using its client index and exact client username hash. */
    public void sendPlayerBodyModel(int interfaceId, int childId, int playerIndex, int nameHash) {
        PacketRepository.send(Encoders578.IF_SET_PLAYER_BODY_MODEL,
                new core.net.packet.out.PacketContext.InterfacePlayerModelContext(player, interfaceId, childId, playerIndex, nameHash));
    }

    /** Displays three cache-backed identity kits, tinted by the local appearance. */
    public void sendIdentityKitModel(int interfaceId, int childId, int kit1, int kit2, int kit3) {
        PacketRepository.send(Encoders578.IF_SET_IDENTITY_KIT_MODEL,
                new core.net.packet.out.PacketContext.InterfaceKitModelContext(player, interfaceId, childId, kit1, kit2, kit3));
    }

    /** Animates a client-visible NPC; caller must establish visibility and a valid sequence. */
    public void sendDirectNpcAnimation(int npcIndex, int sequenceId, int delay) {
        PacketRepository.send(Encoders578.NPC_ANIMATION_DIRECT,
                new core.net.packet.out.PacketContext.DirectNpcAnimationContext(player, npcIndex, sequenceId, delay));
    }

    /** Updates one client clan member; rank -128 removes it and requires a null worldName. */
    public void sendClanMemberUpdate(String name, String alternateName, int world, int rank, String worldName) {
        PacketRepository.send(Encoders578.CLAN_MEMBER_INCREMENTAL,
                new core.net.packet.out.PacketContext.ClanMemberUpdateContext(player, name, alternateName, world, rank, worldName));
    }

    /** Sets the raw 24-bit component field exposed by client CS2 opcode 6705.
     * The caller must establish the content meaning; values are copied for the write queue. */
    public void sendComponent24BitValues(int interfaceId, int firstChild, int[] values) {
        if (values == null || values.length < 1 || values.length > 82) return;
        PacketRepository.send(Encoders578.IF_COMPONENT_24BIT_VALUES,
                new core.net.packet.out.PacketContext.Component24BitValuesContext(player, interfaceId, firstChild, values.clone()));
    }

    /** Sets the client entity rendering priority mode (0 or 1). */
    public void sendEntityRenderPriority(int mode) {
        PacketRepository.send(Encoders578.ENTITY_RENDER_PRIORITY,
                new core.net.packet.out.PacketContext.EntityRenderPriorityContext(player, mode));
    }

    /** Explicitly writes 24 caller-supplied bytes to the client's persistent random data file.
     * Not a login requirement; never called automatically by the local server. */
    public void sendClientRandomData(byte[] data) {
        if (data == null || data.length != 24) return;
        PacketRepository.send(Encoders578.CLIENT_RANDOM_DATA,
                new core.net.packet.out.PacketContext.ClientRandomDataContext(player, data));
    }

    /** Sets an opaque client settings cookie; an empty token clears it. No automatic caller. */
    public void sendClientSettingsCookie(String settings) {
        if (settings == null) return;
        PacketRepository.send(Encoders578.CLIENT_SETTINGS_COOKIE,
                new core.net.packet.out.PacketContext.ClientSettingsCookieContext(player, settings));
    }

    /** Opens an explicitly selected, trusted server URL. Never forward URL_REQUEST input here. */
    public void sendOpenUrl(String url) {
        if (url == null) return;
        PacketRepository.send(Encoders578.OPEN_URL,
                new core.net.packet.out.PacketContext.OpenUrlContext(player, url));
    }

    /** Sets a component font; -1 clears the font ID. */
    public void sendComponentFont(int interfaceId, int childId, int fontId) {
        PacketRepository.send(Encoders578.IF_SET_FONT,
                new core.net.packet.out.PacketContext.ComponentValueContext(player, interfaceId, childId, fontId));
    }

    /** Sets vertical scroll position; the client clamps it to the container bounds. */
    public void sendComponentScrollY(int interfaceId, int childId, int scrollY) {
        PacketRepository.send(Encoders578.IF_SET_SCROLL_Y,
                new core.net.packet.out.PacketContext.ComponentValueContext(player, interfaceId, childId, scrollY));
    }

    /** Removes a client-side item container, not the authoritative server inventory. */
    public void deleteClientContainer(int containerId, boolean split) {
        PacketRepository.send(Encoders578.DELETE_CLIENT_CONTAINER,
                new core.net.packet.out.PacketContext.DeleteClientContainerContext(player, containerId, split));
    }

    public void sendVarp(int index, int value) {
        PacketRepository.send(Encoders578.CONFIG, new ConfigContext(player, index, value));
    }

    public void sendVarcUpdate(short index, int value) {
        sendVarcUpdate((int) index, value);
    }

    public void sendVarcUpdate(int index, int value) {
        PacketRepository.send(Encoders578.VARC_UPDATE, new VarcUpdateContext(player, index, value));
    }

    public void sendVarcString(int index, String value) {
        PacketRepository.send(Encoders578.VARC_STRING_UPDATE,
                new core.net.packet.out.PacketContext.VarcStringUpdateContext(player, index, value));
    }

    /**
     * Send a game message.
     *
     * @param message The game message.
     */
    public void sendMessage(String message) {
        if (message == null) {
            return;
        }
        if (player.getAttribute("chat_filter") != null && !message.contains("<col=CC6600>") && !message.contains("<col=FFFF00>")) {
            return;
        }
        if (message.length() > 255) {
            log(this.getClass(), Log.ERR, "Message length out of bounds (" + message + ")!");
            message = message.substring(0, 255);
        }
        PacketRepository.send(Encoders578.MESSAGE_GAME, new GameMessageContext(player, message));
    }

    /**
     * Sends game messages.
     *
     * @param messages the messages.
     */
    public void sendMessages(final String... messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }

    /**
     * Send messages.
     *
     * @param messages the messages
     * @param ticks    the ticks
     */
    public void sendMessages(int ticks, final String... messages) {
        GameWorld.getPulser().submit(new Pulse(ticks, player) {
            @Override
            public boolean pulse() {
                for (String message : messages) {
                    sendMessage(message);
                }
                return true;
            }
        });
    }

    /**
     * Method used to send a game message on a tick.
     *
     * @param message the message.
     * @param ticks   the ticks.
     */
    public void sendMessage(final String message, int ticks) {
        GameWorld.getPulser().submit(new Pulse(ticks, player) {
            @Override
            public boolean pulse() {
                sendMessage(message);
                return true;
            }
        });
    }

    /**
     * Send a access mask.
     *
     * @param settingsHash The access mask settingsHash.
     * @param childId      The access mask child id.
     * @param interfaceId  The access mask interface Id.
     * @param offset       The access mask off set.
     * @param length       The access mask length.
     */
    public void sendIfaceSettings(int settingsHash, int childId, int interfaceId, int offset, int length) {
        PacketRepository.send(Encoders578.SET_INTERFACE_SETTINGS, new AccessMaskContext(player, settingsHash, childId, interfaceId, offset, length));
    }

    /**
     * Send a windowns pane.
     *
     * @param windowId The windows pane id.
     * @param type     The windowns pane type.
     */
    public void sendWindowsPane(int windowId, int type) {
        PacketRepository.send(Encoders578.IF_OPENTOP, new WindowsPaneContext(player, windowId, type));
    }

    /**
     * sends the system update packet.
     *
     * @param time the amount of time.
     */
    public void sendSystemUpdate(int time) {
        PacketRepository.send(Encoders578.UPDATE_REBOOT_TIMER, new SystemUpdateContext(player, time));
    }

    /**
     * Sends music packet.
     *
     * @param musicId The music id.
     */
    public void sendMusic(int musicId) {
        PacketRepository.send(Encoders578.MUSIC_PACKET, new MusicContext(player, musicId));
    }

    /**
     * Sends the temporary music packet.
     *
     * @param musicId The music id.
     */
    public void sendTempMusic(int musicId) {
        PacketRepository.send(Encoders578.MUSIC_PACKET, new MusicContext(player, musicId, true));
    }

    /**
     * Sends a client script config to the player.
     *
     * @param id    The id to set.
     * @param value The value of the config.
     */
    public void sendScriptConfig(int id, int value, String types, Object... parameters) {
        PacketRepository.send(Encoders578.CS_CONFIG_PACKET, new CSConfigContext(player, id, value, types, parameters));
    }

    /**
     * Send a run script.
     *
     * @param id      The run script id.
     * @param string  The run script string.
     * @param objects The run scripts objects.
     */
    public void sendRunScript(int id, String string, Object... objects) {
        PacketRepository.send(Encoders578.RUNCLIENTSCRIPT, new RunScriptContext(player, id, string, objects));
    }

    /**
     * Send a StringPacket.
     *
     * @param string      The string.
     * @param interfaceId The interface id.
     * @param lineId      The line id.
     */
    public void sendString(String string, int interfaceId, int lineId) {
        PacketRepository.send(Encoders578.IF_SETTEXT, new StringContext(player, string, interfaceId, lineId));
    }

    /**
     * Send a update packet for the amount of run energy.
     */
    public void sendRunEnergy() {
        PacketRepository.send(Encoders578.UPDATE_RUNENERGY, getContext());
    }

    /**
     * Send the logout packet.
     */
    public void sendLogout() {
        PacketRepository.send(Encoders578.LOGOUT, getContext());
    }

    /**
     * Send the interface animation packet.
     *
     * @param animationId The animation id.
     * @param interfaceId The interface id.
     * @param childId     The child id.
     */
    public void sendAnimationInterface(int animationId, int interfaceId, int childId) {
        PacketRepository.send(Encoders578.IF_SETANIM, new AnimateInterfaceContext(player, animationId, interfaceId, childId));
    }

    /**
     * Send the player on interface packet.
     *
     * @param interfaceId The interface id.
     * @param childId     The child id.
     */
    public void sendPlayerOnInterface(int interfaceId, int childId) {
        // fixme right now for iface 68-71 the player is massive
        // The zoom for the other windows is 2150
        // These four interfaces need a zoom of 796. The player model packet has no zoom field.
        PacketRepository.send(Encoders578.DISPLAY_MODEL, new DisplayModelContext(player, interfaceId, childId));
    }

    /**
     * Send the non-player character on interface packet.
     *
     * @param npcId       The non-player character's id.
     * @param interfaceId The interface id.
     * @param childId     The child id.
     */
    public void sendNpcOnInterface(int npcId, int interfaceId, int childId) {
        PacketRepository.send(Encoders578.DISPLAY_MODEL, new DisplayModelContext(player, npcId, interfaceId, childId));
    }

    public void sendModelOnInterface(int modelID, int interfaceId, int childId, int zoom) {
        PacketRepository.send(Encoders578.DISPLAY_MODEL, new DisplayModelContext(player, ModelType.MODEL, modelID, 0, interfaceId, childId, zoom));
    }

    public void sendRepositionOnInterface(int interfaceId, int childId, int positionX, int positionY) {
        PacketRepository.send(Encoders578.IF_SETPOSITION, new ChildPositionContext(player, interfaceId, childId, positionX, positionY));
    }

    public void sendAngleOnInterface(int interfaceId, int childId, int zoom, int pitch, int yaw) {
        PacketRepository.send(Encoders578.IF_SETANGLE, new InterfaceSetAngleContext(player, pitch, zoom, yaw, interfaceId, childId));
    }

    /**
     * Send the item on interface packet.
     *
     * @param itemId      The item id.
     * @param amount      The item amount.
     * @param interfaceId The interface id.
     * @param childId     The child id.
     */
    public void sendItemOnInterface(int itemId, int amount, int interfaceId, int childId) {
        PacketRepository.send(Encoders578.DISPLAY_MODEL, new DisplayModelContext(player, ModelType.ITEM, itemId, amount, interfaceId, childId));
    }

    /**
     * Send the item on interface packet.
     *
     * @param itemId      The item id.
     * @param zoom        the zoom.
     * @param interfaceId The interface id.
     * @param childId     The child id.
     */
    public void sendItemZoomOnInterface(int itemId, int zoom, int interfaceId, int childId) {
        PacketRepository.send(Encoders578.DISPLAY_MODEL, new DisplayModelContext(player, ModelType.ITEM, itemId, 0, interfaceId, childId, zoom));
    }

    public void sendInterSetItemsOptionsScript(int interfaceId, int componentId, int key, int width, int height, String... options) {
        sendInterSetItemsOptionsScript(interfaceId, componentId, key, false, width, height, options);
    }

    public void sendInterSetItemsOptionsScript(int interfaceId, int componentId, int key, boolean negativeKey, int width, int height, String... options) {
        Object[] parameters = new Object[6 + options.length];
        int index = 0;
        for (int count = options.length - 1; count >= 0; count--)
            parameters[index++] = options[count];
        parameters[index++] = -1; // dunno but always this
        parameters[index++] = 0;// dunno but always this, maybe startslot?
        parameters[index++] = height;
        parameters[index++] = width;
        parameters[index++] = key;
        parameters[index++] = interfaceId << 16 | componentId;
        sendRunScript(negativeKey ? 695 : 150, parameters);
        // name says*/
    }

    public void sendRunScript(int scriptId, Object... params) {
        String parameterTypes = "";
        if (params != null) {
            for (int count = params.length - 1; count >= 0; count--) {
                if (params[count] instanceof String)
                    parameterTypes += "s"; // string
                else
                    parameterTypes += "i"; // integer
            }
        }
        sendRunScript(scriptId, parameterTypes, params);
    }


    /**
     * Send the item on interface packet.
     *
     * @param itemId      The item id.
     * @param amount      The amount.
     * @param zoom        the zoom.
     * @param interfaceId The interface id.
     * @param childId     The child id.
     */
    public void sendItemZoomOnInterface(int itemId, int amount, int zoom, int interfaceId, int childId) {
        PacketRepository.send(Encoders578.DISPLAY_MODEL, new DisplayModelContext(player, ModelType.ITEM, itemId, 0, interfaceId, childId, amount));
    }

    /**
     * Send the interface config packet.
     *
     * @param interfaceId The interface id.
     * @param childId     The child id.
     * @param hide        If the component should be hidden.
     */
    public void sendInterfaceConfig(int interfaceId, int childId, boolean hide) {
        PacketRepository.send(Encoders578.IF_SETHIDE, new InterfaceConfigContext(player, interfaceId, childId, hide));
    }

    /**
     * Send a animation update flag mask.
     *
     * @param id The animation id.
     */
    public void sendAnimation(int id) {
        player.getUpdateMasks().register(EntityFlag.Animate, new Animation(id));
    }

    /**
     * Send a animation update flag mask.
     *
     * @param id    The animation id.
     * @param delay The animation delay.
     */
    public void sendAnimation(int id, int delay) {
        player.getUpdateMasks().register(EntityFlag.Animate, new Animation(id, delay));
    }

    /**
     * Send a graphic update flag mask.
     *
     * @param id The graphic id.
     */
    public void sendGraphic(int id) {
        player.getUpdateMasks().register(EntityFlag.SpotAnim, new Graphics(id));
    }

    /**
     * Sends the positioned graphic.
     *
     * @param id       the id.
     * @param height   the height.
     * @param delay    the delay.
     * @param location the location.
     */
    public void sendPositionedGraphic(int id, int height, int delay, Location location) {
        PacketRepository.send(Encoders578.POSITIONED_GRAPHIC, new PositionedGraphicContext(player, new Graphics(id, height, delay), location, 0, 0));
    }

    /**
     * Sends a global graphic.
     *
     * @param id       the id.
     * @param location the location.
     */
    public void sendGlobalPositionGraphic(int id, Location location) {
        for (Player player : RegionManager.getLocalPlayers(location)) {
            player.getPacketDispatch().sendPositionedGraphic(id, 0, 0, location);
        }
    }

    /**
     * Sends the positioned graphic.
     *
     * @param graphics the graphics.
     * @param location the location.
     */
    public void sendPositionedGraphics(Graphics graphics, Location location) {
        PacketRepository.send(Encoders578.POSITIONED_GRAPHIC, new PositionedGraphicContext(player, graphics, location, 0, 0));
    }

    /**
     * Method used to send animate model rotation.
     *
     * @param id          the interface id.
     * @param componentId the child component.
     * @param pitch       the pitch rotation value.
     * @param yaw         the yaw rotation value.
     */
    public void sendInterfaceAnimateRotation(int id, int componentId, int pitch, int yaw) {
        PacketRepository.send(Encoders578.INTERFACE_SET_MODEL_ROTATION, new InterfaceSetModelRotationContext(player, id, componentId, pitch, yaw));
    }

    /**
     * Method used to send an object animation.
     *
     * @param object    the object.
     * @param animation the animation.
     */
    public void sendSceneryAnimation(Scenery object, Animation animation) {
        animation = new Animation(animation.getId(), animation.getDelay(), animation.getPriority());
        animation.setObject(object);
        RegionManager.getRegionChunk(object.getLocation()).flag(new AnimateObjectUpdateFlag(animation));
    }

    /**
     * Method used to send an object animation.
     *
     * @param object    the object.
     * @param animation the animation.
     * @param global    if the animation is global or not.
     */
    public void sendSceneryAnimation(Scenery object, Animation animation, boolean global) {
        if (global) {
            sendSceneryAnimation(object, animation);
            return;
        }
        animation.setObject(object);
        PacketRepository.send(Encoders578.ANIMATE_OBJECT_PACKET, new AnimateObjectContext(player, animation));
    }

    /**
     * Send a graphic update flag mask.
     *
     * @param id     The graphic id.
     * @param height The graphic height.
     */
    public void sendGraphic(int id, int height) {
        player.getUpdateMasks().register(EntityFlag.SpotAnim, new Graphics(id, height));
    }

    public void sendVarClient(int id, int value, boolean cs2) {
        PacketRepository.send(Encoders578.CONFIG, new ConfigContext(player, id, value, cs2));
    }

    public void sendLeftShiftedVarbit(int varpIndex, int offset, int value) {
        setVarp(player, varpIndex, (value << offset));
    }

    public void sendRightShiftedVarbit(int varpIndex, int offset, int value) {
        setVarp(player, varpIndex, (value >> offset));
    }


    /**
     * Gets the player.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the context.
     *
     * @return The context.
     */
    public PlayerContext getContext() {
        return context;
    }

    public void sendScriptConfigs(int id, int value, String type, Object... params) {
        PacketRepository.send(Encoders578.CS_CONFIG_PACKET, new CSConfigContext(player, id, value, type, params));
    }

    public void resetInterface(int id) {
        PacketRepository.send(Encoders578.RESET_INTERFACE, new ResetInterfaceContext(player, id));
    }

    public void sendLastLoginInfo() {
        PacketRepository.send(Encoders578.LAST_LOGIN_INFO, new PlayerContext(player));
    }

}
