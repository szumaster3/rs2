package core.game.system.command.rottenpotato

import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.InputType
import core.game.node.entity.player.Player
import core.game.node.scenery.Scenery
import core.plugin.Initializable
import core.tools.colorize

@Initializable
class RPUseWithSceneryDialogue(player: Player? = null) : Dialogue(player) {

    val ID = 38575797

    private lateinit var scenery: Scenery
    private var rotation: Int = 0
    private var type: Int = 10

    override fun newInstance(player: Player?): Dialogue = RPUseWithSceneryDialogue(player)

    override fun open(vararg args: Any?): Boolean {
        scenery = args[0] as Scenery

        options(
            "Remove Scenery",
            "Transform Scenery (ID +1)",
            "Transform Scenery (Enter ID)",
            "Teleport to Scenery",
            "Animate Scenery"
        )
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (buttonId) {

            // Remove
            1 -> {
                removeScenery(scenery.asScenery())
                sendMessage(player, colorize("%RScenery removed."))
                end()
            }

            // Transform (+1 ID)
            2 -> {
                transformScenery(scenery.id + 1)
                end()
            }

            // Transform (custom ID)
            3 -> {
                end()
                sendInputDialogue(player, InputType.AMOUNT, "Enter scenery ID:") { value ->
                    val sceneryId = value.toString().toIntOrNull()

                    if (sceneryId == null || sceneryId < 0) {
                        sendMessage(player, colorize("%RInvalid scenery ID."))
                        return@sendInputDialogue
                    }

                    transformScenery(sceneryId)
                }
            }

            // Teleport
            4 -> {
                player.teleport(scenery.location)
                end()
            }

            // Animate
            5 -> {
                end()
                sendInputDialogue(player, InputType.AMOUNT, "Enter animation ID:") { value ->
                    val animationId = value.toString().toIntOrNull()

                    if (animationId == null || animationId < 0) {
                        sendMessage(player, colorize("%RInvalid animation ID."))
                        return@sendInputDialogue
                    }

                    animateScenery(scenery, animationId)
                    sendMessage(
                        player,
                        colorize("%BScenery animation set to %R$animationId%B.")
                    )
                }
            }
        }
        return true
    }

    private fun transformScenery(newId: Int) {
        val location = scenery.location

        removeScenery(scenery.asScenery())
        addScenery(newId, location, rotation, type)

        sendMessage(
            player,
            colorize("%BScenery transformed to %R$newId%B.")
        )
    }

    override fun getIds(): IntArray = intArrayOf(ID)
}