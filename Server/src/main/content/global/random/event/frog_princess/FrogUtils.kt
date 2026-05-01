package content.global.random.event.frog_princess

import content.data.GameAttributes
import content.data.RandomEvent
import core.api.*
import core.game.dialogue.FaceAnim
import core.game.interaction.QueueStrength
import core.game.node.Node
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.world.update.flag.context.Animation
import shared.consts.*

/**
 * Utils for Frog random event.
 */
object FrogUtils {
    const val FROG_APPEARANCE_NPC = NPCs.FROG_2473
    private const val FROG_PRINCE_NPC = NPCs.FROG_PRINCE_2474
    private const val FROG_PRINCESS_NPC = NPCs.FROG_PRINCESS_2475
    const val TRANSFORM_INTO_FROG = Animations.MORPH_TO_FROG_2377
    const val TRANSFORM_INTO_HUMAN = Animations.MORPH_FROM_FROG_2375
    const val FROG_KISS_ANIM = Animations.FROG_KISS_2374
    private const val HUMAN_KISS_ANIM = Animations.HUMAN_BLOW_KISS_1374
    private const val HUMAN_KISS_GFX = Graphics.KISS_EMOTE_ORIGINAL_1702

    /**
     * Cleans up the event.
     *
     * @param player The player.
     */
    fun cleanup(player: Player) {
        player.properties.teleportLocation = getAttribute(player, RandomEvent.save(), null)
        clearLogoutListener(player, RandomEvent.logout())
        restoreTabs(player)
        resetAnimator(player)
        clearLogoutListener(player, RandomEvent.logout())
        removeAttributes(player, GameAttributes.KTF_KISS_FAIL)
    }

    /**
     * Handles kiss the frog interaction.
     *
     * @param player The player.
     * @param node The npc.
     */
    fun kissTheFrog(player: Player, node: Node) {
        val npc = node as NPC
        val royalCouple = if (player.isMale) FROG_PRINCESS_NPC else FROG_PRINCE_NPC
        player.lock(18)
        queueScript(player,1,QueueStrength.SOFT) { stage ->
            when (stage) {
                0 -> {
                    face(player, npc, 3)
                    face(npc, player, 3)
                    npc.animate(Animation(FROG_KISS_ANIM))
                    player.animate(Animation(Animations.HUMAN_KISS_THE_FROG_2376))
                    return@queueScript delayScript(player, 3)
                }
                1 -> {
                    visualize(npc, TRANSFORM_INTO_HUMAN, Graphics.SPELL_SPLASH_85)
                    return@queueScript delayScript(player, 2)
                }
                2 -> {
                    transformNpc(npc, royalCouple, 100)
                    return@queueScript delayScript(player, 2)
                }
                3 -> {
                    sendNPCDialogueLines(player, royalCouple, FaceAnim.HAPPY, false, "Thank you so much, ${player.username}.", "I must return to my fairy tale kingdom now, but I will", "leave you a reward for your kindness.")
                    return@queueScript delayScript(player, 1)
                }
                4 -> {
                    visualize(npc, HUMAN_KISS_ANIM, HUMAN_KISS_GFX)
                    return@queueScript delayScript(player, 3)
                }
                5 -> {
                    visualize(npc, HUMAN_KISS_ANIM, HUMAN_KISS_GFX)
                    return@queueScript delayScript(player, 3)
                }
                6 -> {
                    npc.reTransform()
                    cleanup(player)
                    openInterface(player, Components.FADE_FROM_BLACK_170)
                    addItemOrDrop(player, Items.FROG_TOKEN_6183)
                    sendMessage(player, "You've received a frog token!")
                    return@queueScript stopExecuting(player)
                }
                else -> return@queueScript stopExecuting(player)
            }
        }
    }
}