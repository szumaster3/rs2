package core.game.dialogue

import core.api.sendInputDialogue
import core.game.node.entity.player.Player
import core.plugin.Initializable

/**
 * Represents the dialogue plugin used to automatically handle skill dialogues with creation amounts.
 */
@Initializable
class SkillDialoguePlugin(player: Player? = null) : Dialogue(player) {

    private lateinit var handler: SkillDialogueHandler

    override fun newInstance(player: Player?): Dialogue = SkillDialoguePlugin(player)

    override fun open(vararg args: Any?): Boolean {
        handler = args[0] as SkillDialogueHandler
        handler.display()
        handler.type?.let { player.interfaceManager.openChatbox(it.interfaceId) }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        val type = handler.type ?: return true
        val amount = type.getAmount(handler, buttonId)
        val index = type.getIndex(handler, buttonId)

        end()

        if (amount != -1) {
            handler.create(amount, index)
        } else {
            sendInputDialogue(player, true, "Enter the amount:") { value ->
                val inputAmount = when (value) {
                    is String -> value.toIntOrNull()
                    is Number -> value.toInt()
                    else -> null
                }

                if (inputAmount != null) {
                    handler.create(inputAmount, index)
                }
            }
        }

        return true
    }

    override fun getIds(): IntArray = intArrayOf(SkillDialogueHandler.SKILL_DIALOGUE)
}