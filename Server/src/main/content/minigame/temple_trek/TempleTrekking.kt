package content.minigame.temple_trek

import content.data.Quests

import core.api.hasRequirement
import core.api.isQuestComplete
import core.game.node.entity.player.Player

object TempleTrekking {
    fun hasRequirements(player: Player?): Boolean {
        if (player != null) {
            if (!isQuestComplete(player, Quests.PRIEST_IN_PERIL.questName)) return false
            if (!isQuestComplete(player, Quests.NATURE_SPIRIT.questName)) return false
            if (!hasRequirement(player, Quests.IN_SEARCH_OF_THE_MYREQUE.questName, false)) return false
            if (!hasRequirement(player, Quests.IN_AID_OF_THE_MYREQUE.questName, false)) return false
            if (!hasRequirement(player, Quests.DARKNESS_OF_HALLOWVALE.questName, false)) return false
            if (!hasRequirement(player, Quests.LEGACY_OF_SEERGAZE.questName, false)) return false
        }
        return true
    }

    val tentacleCombatEventRegion = 10318
    val logout = "temple-trekking-logout"
    val baseLocation = "temple-trekking:base_location"
    val templeTrekkingCombatSession = "temple-trekking:combat_session"
}
