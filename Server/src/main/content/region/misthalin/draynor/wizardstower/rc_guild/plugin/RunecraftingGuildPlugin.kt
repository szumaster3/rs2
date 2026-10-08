package content.region.misthalin.draynor.wizardstower.rc_guild.plugin

import content.global.skill.runecrafting.item.Talisman
import core.api.*
import core.game.dialogue.FaceAnim
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.InterfaceListener
import core.game.interaction.QueueStrength
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import core.game.world.GameWorld
import core.game.world.map.Location
import core.game.world.map.zone.ZoneBorders
import core.game.world.map.zone.ZoneRestriction
import shared.consts.*

/*
 * TODO CHECKLIST
 * [ ] - Entering the portal in the Dagon'Hai caves now works with an omni-talisman. 26 November 2008
 * [ ] - Unlike the Omni-tiara, the Omni-talisman cannot grant access to the free to play altars, such as air, mind, water, earth, fire, and body, while in a free players' world.
 *          1. https://youtu.be/EAhQXrs4TOo?si=mDf3NpWxcE3svq6w&t=448
 * [ ] - The Omni-talisman counts for a Soul talisman, even if the player obtained their omni-talisman prior to the soul talisman's release.
 * [ ] - Access to all Elriss dialogues requires Ring of Charos (a) and a set of Runecrafter robes (any colour) equipped.
 */
class RunecraftingGuildPlugin : InteractionListener, InterfaceListener, MapArea {

    companion object {
        private const val GUILD_REGION = 6741
        private const val TOWER_REGION = 12337
        private const val MIN_RC_LEVEL = 50
        private const val ALTARS_TEXT_COMPONENT = 33

        private val GUILD_DESTINATION = Location.create(1696, 5461, 2)
        private val TOWER_DESTINATION = Location.create(3106, 3160, 1)

        private val SCENERY_ANIMATIONS = mapOf(
            Scenery.CONTAINMENT_UNIT_38327 to 10193,
            Scenery.GLASS_SPHERES_38331 to 10128,
            Scenery.GYROSCOPE_38330 to 10127,
            Scenery.RUNESTONE_ACCELERATOR_38329 to 10196,
        )

        private val MAP_SCENERY = intArrayOf(Scenery.MAP_TABLE_38315, Scenery.MAP_38422, Scenery.MAP_38421)

        private val WIZARD_NPCs = intArrayOf(
            NPCs.WIZARD_8033, NPCs.WIZARD_8034, NPCs.WIZARD_8035, NPCs.WIZARD_8036,
            NPCs.WIZARD_8037, NPCs.WIZARD_8038, NPCs.WIZARD_8039, NPCs.WIZARD_8040,
        )

        // Item IDs of all talismans. In-game command [::talismankit] adds all talisman items to inventory.
        val talismanIDs = Talisman.values().map { it.item }.toIntArray()

        // Talisman item ID -> altar icon component on the map table interface.
        // (Components 45 and 46 are for: Elemental talisman [5516] and Soul talisman [1460]).
        private val talismanToComponentMap = mapOf(
            Items.AIR_TALISMAN_1438    to 35,
            Items.BODY_TALISMAN_1446   to 36,
            Items.MIND_TALISMAN_1448   to 37,
            Items.EARTH_TALISMAN_1440  to 38,
            Items.WATER_TALISMAN_1444  to 39,
            Items.FIRE_TALISMAN_1442   to 40,
            Items.CHAOS_TALISMAN_1452  to 41,
            Items.LAW_TALISMAN_1458    to 42,
            Items.BLOOD_TALISMAN_1450  to 43,
            Items.NATURE_TALISMAN_1462 to 44,
            Items.DEATH_TALISMAN_1456  to 47,
            Items.COSMIC_TALISMAN_1454 to 48,
        )

        private val altarComponents = talismanToComponentMap.values.toIntArray()

        /*
         * Two-way toggle: hat with goggles <-> hat without goggles.
         */

        val hatToggleMap = mapOf(
            Items.RUNECRAFTER_HAT_13626 to Items.RUNECRAFTER_HAT_13625,
            Items.RUNECRAFTER_HAT_13621 to Items.RUNECRAFTER_HAT_13620,
            Items.RUNECRAFTER_HAT_13616 to Items.RUNECRAFTER_HAT_13615,
        ).let { it + it.entries.associate { (k, v) -> v to k } }

        private val RC_HAT = hatToggleMap.keys.toIntArray()
    }

    override fun defineAreaBorders(): Array<ZoneBorders> = arrayOf(ZoneBorders.forRegion(GUILD_REGION))

    override fun getRestrictions(): Array<ZoneRestriction> = arrayOf(
        ZoneRestriction.CANNON,
        ZoneRestriction.RANDOM_EVENTS,
        ZoneRestriction.GRAVES,
        ZoneRestriction.FIRES,
    )

    private fun isInTower(player: core.game.node.entity.player.Player) =
        player.viewport.region!!.regionId == TOWER_REGION

    /**
     * Reveals the given altar icons on the map interface.
     */
    private fun revealAltars(player: core.game.node.entity.player.Player, components: IntArray) {
        components.forEach { setComponentVisibility(player, Components.RCGUILD_MAP_780, it, false) }
    }

    override fun defineListeners() {

        /*
         * Handles animated guild scenery.
         */

        on(SCENERY_ANIMATIONS.keys.toIntArray(), IntType.SCENERY, "activate") { _, node ->
            if(node.location != Location(1701, 5474, 2))
            animateScenery(node.asScenery(), SCENERY_ANIMATIONS.getValue(node.id))
            return@on true
        }

        /*
         * Handles map table and wall maps open the study interface.
         */

        on(MAP_SCENERY, IntType.SCENERY, "Study") { player, _ ->
            openInterface(player, Components.RCGUILD_MAP_780)
            return@on true
        }

        /*
         * Handles using a talisman on the map table to reveals its altar.
         */

        onUseWith(IntType.SCENERY, talismanIDs, Scenery.MAP_TABLE_38315) { player, used, _ ->
            openInterface(player, Components.RCGUILD_MAP_780)
            talismanToComponentMap[used.id]?.let { revealAltars(player, intArrayOf(it)) }
            return@onUseWith true
        }

        /*
         * Handles using the omni talisman on the map table to reveals all altars.
         */

        onUseWith(IntType.SCENERY, Items.OMNI_TALISMAN_13649, Scenery.MAP_TABLE_38315) { player, _, _ ->
            openInterface(player, Components.RCGUILD_MAP_780)
            revealAllAltars(player)
            return@onUseWith true
        }

        /*
         * Handles teleport to guild.
         */

        on(Scenery.PORTAL_38279, IntType.SCENERY, "Enter") { player, _ ->
            if (getStatLevel(player, Skills.RUNECRAFTING) < MIN_RC_LEVEL) {
                sendDialogue(player, "You require $MIN_RC_LEVEL Runecrafting to enter the Runecrafters' Guild.")
                return@on true
            }
            if (!isQuestComplete(player, Quests.RUNE_MYSTERIES)) {
                sendDialogue(player, "You need to complete Rune Mysteries to enter the Runecrafting guild.")
                return@on true
            }

            val destination = if (isInTower(player)) GUILD_DESTINATION else TOWER_DESTINATION

            player.lock(4)
            visualize(player, Animations.RC_TP_A_10180, Graphics.RC_GUILD_TP)
            queueScript(player, 3, QueueStrength.SOFT) {
                teleport(player, destination)
                visualize(player, Animations.RC_TP_B_10182, Graphics.RC_GUILD_TP)
                face(player, destination)
                return@queueScript stopExecuting(player)
            }
            return@on true
        }

        /*
         * Handles toggle goggles on the RC hat.
         */

        on(RC_HAT, IntType.ITEM, "Goggles") { player, node ->
            val newHatId = hatToggleMap[node.id] ?: return@on false
            replaceSlot(player, node.asItem().slot, Item(newHatId))
            return@on true
        }

        /*
         * Handles the rewards interface.
         */

        on(NPCs.WIZARD_ELRISS_8032, IntType.NPC, "Exchange") { player, _ ->
            openInterface(player, Components.RCGUILD_REWARDS_779)
            return@on true
        }

        /*
         * Handles dialogue interaction with Wizards.
         */

        on(WIZARD_NPCs, IntType.NPC, "talk-to") { player, _ ->
            sendOptions(player, "Select an option", "I want to join the orb project!", "Never mind.")
            addDialogueAction(player) { _, _ -> closeDialogue(player) }
            return@on true
        }

        on(NPCs.WIZARD_GRAYZAG_707, IntType.NPC, "talk-to") { player, node ->
            sendNPCDialogueLines(
                player, node.id, FaceAnim.SILENT, false,
                "Not now, I'm trying to concentrate on a",
                "very difficult spell!"
            )
            return@on true
        }

        /*
         * Handles dialogue interaction with Wizard Vief.
         */

        on(NPCs.WIZARD_VIEF_8030, IntType.NPC, "talk-to") { player, node ->
            sendNPCDialogue(player, node.id, "Ah! You'll help me, won't you?", FaceAnim.HAPPY)
            return@on true
        }
    }

    override fun defineDestinationOverrides() {
        setDest(IntType.SCENERY, intArrayOf(Scenery.PORTAL_38279), "enter") { p, node ->
            if (isInTower(p.asPlayer())) node.asScenery().location else GUILD_DESTINATION
        }
    }

    override fun defineInterfaceListeners() {
        // Omni talisman staff / omni tiara equipped -> all altars are shown.
        onOpen(Components.RCGUILD_MAP_780) { player, _ ->
            if (inEquipment(player, Items.OMNI_TALISMAN_STAFF_13642) || inEquipment(player, Items.OMNI_TIARA_13655)) {
                revealAllAltars(player)
            }
            return@onOpen true
        }
    }

    private fun revealAllAltars(player: core.game.node.entity.player.Player) {
        revealAltars(player, altarComponents)
        sendString(player, "All the altars of ${GameWorld.settings!!.name}.", Components.RCGUILD_MAP_780, ALTARS_TEXT_COMPONENT)
    }
}