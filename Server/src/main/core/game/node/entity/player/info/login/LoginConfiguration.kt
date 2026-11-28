package core.game.node.entity.player.info.login

import content.data.Quests

import content.data.GameAttributes
import content.data.GlobalStore
import core.ServerConstants
import core.api.*
import core.game.component.Component
import core.game.interaction.InteractionListeners
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.SpellBookManager
import core.game.node.entity.player.link.emote.Emotes
import core.game.world.GameWorld
import core.game.world.map.RegionManager
import core.game.world.repository.Repository
import core.game.world.update.UpdateSequence
import core.net.packet.PacketRepository
import core.net.packet.out.Encoders578
import core.net.packet.out.PacketContext.InterfaceContext
import core.plugin.Plugin
import core.tools.Log
import core.tools.colorize
import shared.consts.Components
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream

/**
 * Handles login configuration, including lobby setup and game world initialization.
 */
object LoginConfiguration {
    /**
     * List of login-related plugins.
     */
    var loginPlugins = mutableListOf<Plugin<Any>>()
    @JvmStatic
    fun configureLobby(player: Player) {
        player.updateSceneGraph(true)
        if (player.isArtificial || !isTutorialCompleted(player) || !player.isNotReconnecting()) configureGameWorld(player)
        else sendLobbyScreen(player)
    }

    @JvmStatic
    fun sendLobbyScreen(player: Player) {
        player.setAttribute("logging_in", true)
        Repository.lobbyPlayers.removeIf { it.name == player.name }
        Repository.lobbyPlayers.add(player)
        player.interfaceManager.openWindowsPane(Component(378))
        player.packetDispatch.sendLastLoginInfo()
        sendString(player, "Welcome to ${GameWorld.settings?.name}", 378, 4)
        sendIfaceSettings(player, 2, 18, 378, -1, -1)
    }

    /**
     * Configures the game world for the player.
     * @param player The player to configure the game world for.
     */
    @JvmStatic
    fun configureGameWorld(player: Player) {
        player.removeAttribute("logging_in")
        if (!player.isArtificial) {
            player.details.accountInfo.lastUsedIp = player.details.ipAddress
            player.details.setLastLogin(System.currentTimeMillis())
        }
        sendGameConfiguration(player)

        Repository.lobbyPlayers.remove(player)
        player.isPlaying = true

        UpdateSequence.renderablePlayers.add(player)
        RegionManager.move(player)
        player.musicPlayer.init()
        player.updateAppearance()

        checkEmotes(player)
        setupItems(player)
        setupSpellBook(player)
        setConfigs(player)
    }

    /**
     * Sends initial game configuration settings to the player.
     * @param player The player to configure.
     */
    @JvmStatic
    fun sendGameConfiguration(player: Player) {
        player.interfaceManager.openWindowsPane(Component(if (player.session?.clientInfo?.isResizable == true) Components.TOPLEVEL_FULLSCREEN_746 else Components.TOPLEVEL_548))
        player.interfaceManager.openChatbox(Components.CHATDEFAULT_137)
        welcome(player)
        config(player)

        loginPlugins.forEach { plugin ->
            try {
                plugin.newInstance(player)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        refreshAppearance(player)
    }

    @JvmStatic
    fun welcome(player: Player) {
        if (player.isArtificial) return
        if(isTutorialCompleted(player)){
            sendMessage(player, "Welcome to ${ServerConstants.SERVER_NAME}.")
        }
        GlobalStore.check(player)

        if (player.details.isMuted) {
            sendMessage(player, "You are muted.")
            sendMessage(player, "To prevent further mutes, please read the rules.")
            openInterface(player, Components.QUICKCHAT_LOCKED_801)
        }
    }

    @JvmStatic
    fun config(player: Player) {
        if (!player.isArtificial) {
            log(LoginConfiguration::class.java, Log.INFO, "Configuring player ${player.username}")
        }
        player.interfaceManager.openDefaultTabs()
        player.interfaceManager.openInfoBars()
        player.inventory.refresh()
        player.equipment.refresh()
        player.skills.refresh()
        player.skills.configure()
        player.settings.update()
        player.interaction.setDefault()
        player.packetDispatch.sendRunEnergy()
        player.familiarManager.login()

        sendString(
            player,
            "Friends List - ${ServerConstants.SERVER_NAME} ${GameWorld.settings?.worldId}",
            Components.FRIENDS2_550,
            3,
        )
        sendString(
            player,
            "When you have finished playing ${ServerConstants.SERVER_NAME}, always use the button below to logout safely.",
            Components.LOGOUT_182,
            0,
        )

        player.questRepository.syncronizeTab(player)
        player.interfaceManager.close()
        player.emoteManager.refresh()
        if (!player.isArtificial) {
            log(LoginConfiguration::class.java, Log.INFO, "Finished configuring player ${player.username}")
        }
    }

    /**
     * Gets the last login message for a player.
     */
    @JvmStatic
    fun getLastLogin(player: Player): String {
        var lastIp =
            player.details.accountInfo.lastUsedIp
                .ifEmpty { player.details.ipAddress }
        player.details.accountInfo.lastUsedIp = player.details.ipAddress
        val timeAgo = calculateTimeAgo(player.details.lastLogin)
        return "You last logged in $timeAgo from: $lastIp"
    }

    /**
     * Calculates the time difference from the last login to the current time.
     * @param lastLoginTime The timestamp of the last login.
     * @return A formatted string representing how long ago the login occurred.
     */
    private fun calculateTimeAgo(lastLoginTime: Long): String {
        val lastLogin = Date(lastLoginTime)
        val current = Date()
        val diff = current.time - lastLogin.time
        val days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
        return when {
            days < 1 -> "earlier today"
            days == 1 -> "yesterday"
            else -> "$days days ago"
        }
    }

    /**
     * Determines if the player has completed the tutorial.
     * @param player The player to check.
     * @return True if the tutorial is completed, otherwise false.
     */
    private fun isTutorialCompleted(player: Player): Boolean = player.getAttribute(GameAttributes.TUTORIAL_COMPLETE, false)

    private fun Player.isNotReconnecting(): Boolean = getAttribute("login_type", LoginType.NORMAL_LOGIN) != LoginType.RECONNECT_TYPE

    private fun checkEmotes(player: Player) {
        if (player.globalData.getTestStage() == 3 && !hasEmote(player, Emotes.SAFETY_FIRST)) {
            unlockEmote(player, Emotes.SAFETY_FIRST)
        }
    }

    private fun setupItems(player: Player) {
        player.equipment.toArray().forEach { item ->
            if (item != null) {
                player.equipment.remove(item)
                if (!InteractionListeners.run(item.id, player, item, true) ||
                    !player.equipment.add(
                        item,
                        true,
                        false,
                    )
                ) {
                    sendMessage(player,
                        colorize(
                            "%RYou had items equipped in the wrong slots. They were moved out into your inventory.",
                        ),
                    )
                    addItemOrBank(player, item.id, item.amount)
                }
            }
        }
    }

    private fun setupSpellBook(player: Player) {
        val manager = player.spellBookManager
        val currentBook = SpellBookManager.SpellBook.forInterface(manager.spellBook) ?: SpellBookManager.SpellBook.MODERN

        when {
            currentBook == SpellBookManager.SpellBook.ANCIENT && !isQuestComplete(player, Quests.DESERT_TREASURE.questName) -> {
                sendMessage(player, colorize("%RAs you can no longer use Ancient Magic, you have been set back to Modern."))
                manager.setSpellBook(SpellBookManager.SpellBook.MODERN)
            }
            currentBook == SpellBookManager.SpellBook.LUNAR && !hasRequirement(player, Quests.LUNAR_DIPLOMACY.questName) -> {
                sendMessage(player, colorize("%RAs you can no longer use Lunar Magic, you have been set back to Modern."))
                manager.setSpellBook(SpellBookManager.SpellBook.MODERN)
            }
        }

        manager.update(player)
    }


    private fun setConfigs(player: Player) {
        setVarbit(player, 4322, 1)
        setVarbit(player, 3537, if(getQuestStage(player, Quests.OLAFS_QUEST.questName) != 3) 1 else 0)

        if (getAttribute(player, GameAttributes.KW_COMPLETE, false)) {
            setVarbit(player, 3909, 8)
        } else {
            setVarbit(player, 3909, 0)
        }

        if(hasRequirement(player, Quests.EAGLES_PEAK.questName, false))
            // setVarbit(player, 2780, 15)
            setVarbit(player, 3109, 3)
            // setVarbit(player, 4056, 199)
    }
}
