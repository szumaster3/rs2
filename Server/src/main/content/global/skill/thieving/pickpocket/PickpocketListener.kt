package content.global.skill.thieving.pickpocket

import content.data.Quests

import content.global.plugins.item.equipment.GlovesOfSilence
import content.global.skill.thieving.blackjack.BlackjackUtils
import content.global.skill.thieving.pickpocket.loot.FremennikCitizenLootTable
import core.api.*
import core.api.utils.WeightBasedTable
import core.game.container.impl.EquipmentContainer
import core.game.interaction.Clocks
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.combat.DeathTask
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.diary.DiaryType
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import core.game.world.map.zone.ZoneBorders
import core.game.world.update.flag.context.Animation
import core.tools.RandomFunction
import shared.consts.*

class PickpocketListener : InteractionListener {

    override fun defineListeners() {
        on(IntType.NPC, "pickpocket", "pick-pocket") { player, node ->
            val npc = node.asNpc()
            val shown = npc.definition.getChildNPC(player)
            val data = Pickpocket.forID(shown.id) ?: return@on false
            val npcName = shown.name.lowercase()

            getBlockReason(player, npc, npcName, data)?.let {
                sendMessage(player, it)
                return@on true
            }

            if (npc.id in FremennikCitizenLootTable.NPC_ID && !isQuestComplete(player, Quests.THE_FREMENNIK_TRIALS.questName)) {
                data.message?.let { npc.sendChat(it) }
                sendMessage(player, "They are too suspicious of you for you to get close enough to steal from them.")
                return@on true
            }

            val knockedOut = BlackjackUtils.canPickpocket(npc)
            val delay = if (knockedOut) 1 else 2

            delayClock(player, Clocks.SKILLING, delay)
            animate(player, Animation(if (knockedOut) Animations.HUMAN_PICKUPFLOOR_827 else Animations.HUMAN_PICKPOCKET_881))
            sendMessage(player, "You attempt to pick the $npcName's pocket.")

            if (npc.id == NPCs.ARDOUGNE_GUARD_32 && npc.location.isInRegion(10547)) finishDiaryTask(player, DiaryType.ARDOUGNE, 0, 1)
            val loot = if (knockedOut) data.loot.roll() else pickpocketRoll(player, data.low, data.high, data.loot)

            if (loot == null) {
                onFail(player, npc, npcName, data)
            } else {
                onSuccess(player, npc, npcName, data, loot, delay)
                if (knockedOut) BlackjackUtils.onPickpocket(npc)
            }

            return@on true
        }
    }

    /**
     * Returns the message explaining why the player can't pickpocket the npc right now, or `null` if they can.
     */
    private fun getBlockReason(player: Player, npc: NPC, npcName: String, data: Pickpocket): String? = when {
        isStunned(player) -> "You're stunned!"
        player.inCombat() -> "You can't do this while in combat."
        getStatLevel(player, Skills.THIEVING) < data.requiredLevel -> "You need a Thieving level of ${data.requiredLevel} to do that."
        DeathTask.isDead(npc) -> "Too late, $npcName is already dead."
        npc.id == NPCs.CURATOR_646 && hasAnItem(player, Items.DISPLAY_CABINET_KEY_4617).container != null -> "You have no reason to do that."
        !data.loot.canRoll(player) -> "You don't have enough inventory space to do that."
        else -> null
    }

    private fun onFail(player: Player, npc: NPC, npcName: String, data: Pickpocket) {
        npc.faceLocation(player.location)
        npc.animator.animate(Animation(Animations.HUMAN_UNARMEDPUNCH_422))
        data.message?.let { npc.sendChat(it) }
        sendMessage(player, "You fail to pick the $npcName's pocket.")
        playHurtAudio(player, 20)
        stun(player, data.stunTime)
        impact(player, RandomFunction.random(data.stunDamageMin, data.stunDamageMax))
        sendMessage(player, "You feel slightly concussed from the blow.")
        damageGloves(player)
        npc.face(null)
    }

    private fun onSuccess(player: Player, npc: NPC, npcName: String, data: Pickpocket, loot: List<Item>, delay: Int) {
        lock(player, delay)
        playAudio(player, Sounds.PICK_2581)
        loot.forEach { player.inventory.add(it) }

        sendMessage(player, if (npc.id == NPCs.CURATOR_646) "You steal a tiny key." else "You pick the $npcName's pocket.")
        rewardXP(player, Skills.THIEVING, data.xp)
        if (npc.id in intArrayOf(NPCs.MASTER_FARMER_1_2234, NPCs.MASTER_FARMER_2_2235) && npc.location.isInRegion(10548)) {
            finishDiaryTask(player, DiaryType.ARDOUGNE, 1, 12)
        }
        if (npc.id == NPCs.YANILLE_WATCHMAN_34 && inEquipment(player, Items.GLOVES_OF_SILENCE_10075)) {
            finishDiaryTask(player, DiaryType.ARDOUGNE, 2, 6)
        }

        when {
            inBorders(player, VARROCK_PALACE) && npc.id == NPCs.FAI_VARROCK_GUARD_CAPTAIN_5920 -> {
                finishDiaryTask(player, DiaryType.VARROCK, 1, 12)
                setVarbit(player, 4018, 1, true)
            }
            inBorders(player, FALADOR) && npc.id in FALADOR_GUARDS -> {
                finishDiaryTask(player, DiaryType.FALADOR, 1, 6)
                setVarbit(player, 5712, 1, true)
            }
        }
    }

    /**
     * Gloves of silence may break when the wearer gets caught.
     */
    private fun damageGloves(player: Player) {
        val gloves = player.equipment[EquipmentContainer.SLOT_HANDS]
            ?.takeIf { it.id == Items.GLOVES_OF_SILENCE_10075 } ?: return
        if (GlovesOfSilence.onFailedPickpocket(player, gloves)) {
            sendMessage(player, "Your gloves of silence fall apart.")
        }
    }

    companion object {
        private val VARROCK_PALACE = ZoneBorders(3201, 3456, 3227, 3468)
        private val FALADOR = ZoneBorders(2934, 3307, 3399, 3399)
        private val FALADOR_GUARDS = intArrayOf(NPCs.GUARD1_9, NPCs.FAI_FALADOR_GUARD3_3230, NPCs.FAI_FALADOR_GUARD1_3228, NPCs.FAI_FALADOR_GUARD2_3229)

        /**
         * Rolls for a pickpocket attempt.
         *
         * @param player The [Player] attempting to pickpocket.
         * @param low The minimum base success rate.
         * @param high The maximum base success rate.
         * @param table The [WeightBasedTable] loot for a pickpocket.
         * @return An [ArrayList] of [Item]s if the pickpocket succeeds, or `null` if the attempt fails.
         */
        @JvmStatic
        fun pickpocketRoll(player: Player, low: Double, high: Double, table: WeightBasedTable): ArrayList<Item>? {
            var successMod = 0.0
            if (inEquipment(player, Items.GLOVES_OF_SILENCE_10075)) {
                successMod += 5
            }
            val chance = RandomFunction.randomDouble(1.0, 100.0)
            val failThreshold = RandomFunction.getSkillSuccessChance(low, high, getStatLevel(player, Skills.THIEVING)) + successMod
            return if (chance > failThreshold) null else table.roll()
        }
    }
}