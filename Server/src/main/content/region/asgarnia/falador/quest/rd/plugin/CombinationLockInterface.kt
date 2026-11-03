package content.region.asgarnia.falador.quest.rd.plugin

import content.region.asgarnia.falador.quest.rd.RecruitmentDrive
import content.region.asgarnia.falador.quest.rd.dialogue.SirReenItchoodDialogue
import core.api.*
import core.game.interaction.InterfaceListener
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import shared.consts.Components
import shared.consts.NPCs
import core.api.getAttribute
import core.api.removeAttribute
import core.api.setAttribute

/**
 * Handles the combination lock interface for the Recruitment Drive quest.
 */
class CombinationLockInterface : InterfaceListener {

    /**
     * Defines listeners for the combination lock interface.
     */
    override fun defineInterfaceListeners() {
        onOpen(Components.RD_COMBOLOCK_285) { player, _ ->
            initializeLocks(player)
            return@onOpen true
        }

        onClose(Components.RD_COMBOLOCK_285) { player, _ ->
            clearLocks(player)
            return@onClose true
        }

        on(Components.RD_COMBOLOCK_285) { player, _, _, buttonID, _, _ ->
            handleButtonPress(player, buttonID)
            return@on true
        }
    }

    /**
     * Initializes all lock dials for the player to the default value.
     */
    private fun initializeLocks(player: Player) {
        lockArray.forEach { lock -> setAttribute(player, lock, INITIAL_VALUE) }
    }

    /**
     * Clears all lock-related attributes for the player.
     */
    private fun clearLocks(player: Player) {
        lockArray.forEach { lock -> removeAttribute(player, lock) }
    }

    /**
     * Handles button presses on the interface.
     */
    private fun handleButtonPress(player: Player, buttonID: Int) {
        if (buttonID in 10..17) {
            adjustLockValue(player, buttonID)
        } else if (buttonID == 18) {
            validateAnswer(player)
        }
    }

    /**
     * Adjusts a specific lock dial based on the button pressed.
     */
    private fun adjustLockValue(player: Player, buttonID: Int) {
        val position = (buttonID - 10) / 2
        val direction = (buttonID - 10) % 2
        var newValue = getAttribute(player, lockArray[position], INITIAL_VALUE) + if (direction == 0) -1 else 1

        newValue = when {
            newValue < LOWER_BOUND -> UPPER_BOUND
            newValue > UPPER_BOUND -> LOWER_BOUND
            else -> newValue
        }

        setAttribute(player, lockArray[position], newValue)
        sendString(player, newValue.toChar().toString(), Components.RD_COMBOLOCK_285, position + 6)
    }

    /**
     * Validates the player's combination against the correct answer.
     */
    private fun validateAnswer(player: Player) {
        val answer = lockArray.joinToString("") {
            getAttribute(player, it, INITIAL_VALUE).toChar().toString()
        }
        closeInterface(player)
        if (answers[getAttribute(player, SirReenItchoodDialogue.ATTRIBUTE_CLUE, 0)] == answer) {
            if (!getAttribute(player, RecruitmentDrive.stageFail, false)) {
                setAttribute(player, RecruitmentDrive.stagePass, true)
            }
            playJingle(player, 159)
            sendNPCDialogue(player, NPCs.SIR_REN_ITCHOOD_2287, "Your wit is sharp, your brains quite clear; You solved my puzzle with no fear. At puzzles I rank you quite the best, now enter the portal for your next test.")
        } else {
            setAttribute(player, RecruitmentDrive.stageFail, true)
            openDialogue(player, SirReenItchoodDialogue(2), NPC(NPCs.SIR_REN_ITCHOOD_2287))
        }
    }

    companion object {
        /**
         * Default ASCII value for lock dials (`A`).
         */
        private const val INITIAL_VALUE = 65

        /**
         * Minimum ASCII value for a lock dial (`A`).
         */
        private const val LOWER_BOUND = 65

        /**
         * Maximum ASCII value for a lock dial (`Z`).
         */
        private const val UPPER_BOUND = 90

        /**
         * Attributes for each of the four lock dials.
         */
        private val lockArray = arrayOf("rd:lock1", "rd:lock2", "rd:lock3", "rd:lock4")

        /**
         * Correct answers for the combination lock puzzles.
         */
        private val answers = arrayOf("BITE", "FISH", "LAST", "MEAT", "RAIN", "TIME")
    }
}
