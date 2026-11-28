package content.global.plugins.npc

import content.global.plugins.`object`.BankBoothPlugin
import core.api.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.Node
import core.game.node.entity.Entity
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.world.map.Direction
import core.game.world.map.Location
import core.plugin.Initializable
import shared.consts.NPCs

/**
 * Handles banker NPC behavior including interactions with banks and collection boxes.
 */
@Initializable
class BankerNPC : AbstractNPC, InteractionListener {

    companion object {
        /** Lunar Isle bank region ID */
        private const val LUNAR_ISLE_BANK_REGION = 8253

        /** IDs of banker NPCs handled by this plugin */
        val NPC_IDS = intArrayOf(
            NPCs.SIRSAL_BANKER_4519, NPCs.AIDE_TUTOR_BANKER_4907, NPCs.JADE_4296, NPCs.AFR_OGRESS_BANKER1_7049, NPCs.AFR_OGRESS_BANKER2_7050,
            NPCs.SWAN_ARNOLD_3824, NPCs.BANKER_6538, NPCs.ATVAR_COOKGUILD_BANKER01_44, NPCs.ATVAR_COOKGUILD_BANKER02_45, NPCs.BANKER1_494, NPCs.BANKER2_495,
            NPCs.KHARIDBANKER1_496, NPCs.KHARIDBANKER2_497, NPCs.FAIRY_BANKER_498, NPCs.SHILOBANKER_499, NPCs.WEREWOLFBANKER_1036, NPCs.MISC_BANKER_1360,
            NPCs.DWARF_CITY_BANKER1_2163, NPCs.DWARF_CITY_BANKER2_2164, NPCs.MOURNING_ELF_BANKERM_2354, NPCs.MOURNING_ELF_BANKERF_2355, NPCs.WOM_BANKER1_2568, NPCs.WOM_BANKER2_2569,
            NPCs.WOM_BANKER3_2570, NPCs.FEVER_BANKER_01_3198, NPCs.FEVER_BANKER_02_3199, NPCs.CONTACT_BANKER_MALE_5258, NPCs.BANKER_5259, NPCs.CONTACT_BANKER_FEMALE_5260,
            NPCs.STONEMASON_5261, NPCs.DORGESH_FEMALE_BANKER_5776, NPCs.DORGESH_MALE_BANKER_5777, NPCs.BANKER_5912, NPCs.BANKER_5913, NPCs.FALADOR_BANKER_6200,
            NPCs.BANKER_6532, NPCs.BANKER_6533, NPCs.BANKER_6534, NPCs.BANKER_6535, NPCs.KING_AWOWOGEI_7445, NPCs.KING_AWOWOGEI_7446,
            NPCs.HVH_BANKER_7605, NPCs.GUNDAI_902, NPCs.AHOY_GHOST_BANKER_1702, NPCs.GNOME_BANKER_166, NPCs.ELID_BANKER_3046,
            NPCs.MAGNUS_GRAM_5488, NPCs.TZHAAR_BANKER_2619, NPCs.CORNELIUS_3568
        )

        /**
         * Checks if the player is restricted from accessing Lunar Isle bank.
         *
         * @param player the player trying to access the bank
         * @param node the NPC node
         * @return true if restricted, false otherwise
         */
        fun checkLunarIsleRestriction(player: Player, node: Node): Boolean {
            if (node.location.regionId != LUNAR_ISLE_BANK_REGION) return false
            return !hasSealOfPassage(player)
        }

        /**
         * Attempts to open a bank for the player via the NPC.
         *
         * @param player the player interacting
         * @param node the banker NPC node
         * @return true if interaction was handled
         */
        fun attemptBank(player: Player, node: Node): Boolean {
            val npc = node as NPC

            if (checkLunarIsleRestriction(player, node)) {
                openDialogue(player, npc.id, npc)
                return true
            }

            npc.faceLocation(null)
            openBankAccount(player)
            return true
        }

        /**
         * Attempts to open Grand Exchange collection box via the NPC.
         *
         * @param player the player interacting
         * @param node the banker NPC node
         * @return true if interaction was handled
         */
        fun attemptCollect(player: Player, node: Node): Boolean {
            val npc = node as NPC

            if (checkLunarIsleRestriction(player, node)) {
                openDialogue(player, npc.id, npc)
                return true
            }

            npc.faceLocation(null)
            openGrandExchangeCollectionBox(player)
            return true
        }
    }

    /** Default constructor */
    constructor() : super(0, null)

    /** Private constructor used for NPC instantiation */
    private constructor(id: Int, location: Location) : super(id, location)

    /**
     * Constructs a new NPC instance with the given id and location.
     *
     * @param id the NPC ID
     * @param location the location to spawn at
     * @param objects optional extra parameters
     * @return the newly constructed NPC
     */
    override fun construct(id: Int, location: Location, vararg objects: Any?): AbstractNPC = BankerNPC(id, location)

    /**
     * Finds a bank booth adjacent to this NPC.
     */
    private fun findAdjacentBankBoothLocation(): Pair<Direction, Location>? {
        for (side in arrayOf(Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST)) {
            val boothLocation = location.transform(side)
            val sceneryObject = getScenery(boothLocation)

            if (sceneryObject != null && sceneryObject.id in BankBoothPlugin.BANK_BOOTHS) {
                return Pair(side, boothLocation.transform(side, 1))
            }
        }

        return null
    }

    /**
     * Provides a destination override for the NPC when walking towards it.
     *
     * @param entity the entity trying to walk to the NPC
     * @param node the NPC node
     * @return the location the entity should stop at
     */
    private fun provideDestinationOverride(entity: Entity, node: Node): Location {
        val npc = node as NPC

        return when (npc.id) {
            NPCs.MOURNING_ELF_BANKERM_2354,
            NPCs.MOURNING_ELF_BANKERF_2355 -> npc.location.transform(npc.direction, 2)
            NPCs.AFR_OGRESS_BANKER1_7049,
            NPCs.AFR_OGRESS_BANKER2_7050 -> npc.location.transform(3, 1, 0)
            NPCs.BANKER_6532,
            NPCs.BANKER_6533,
            NPCs.BANKER_6534,
            NPCs.BANKER_6535 -> npc.location.transform(npc.direction, 1)
            NPCs.MAGNUS_GRAM_5488 -> npc.location.transform(Direction.NORTH, 2)
            else -> {
                if (npc is BankerNPC) {
                    npc.findAdjacentBankBoothLocation()?.let {
                        return it.second
                    }
                }
                return npc.location
            }
        }
    }

    override fun defineListeners() {
        on(NPC_IDS, IntType.NPC, "bank", handler = Companion::attemptBank)
        on(NPC_IDS, IntType.NPC, "collect", handler = Companion::attemptCollect)
    }

    override fun defineDestinationOverrides() {
        setDest(IntType.NPC, NPC_IDS, "bank", "collect", "talk-to", handler = ::provideDestinationOverride)
    }

    override fun init() {
        super.init()
        findAdjacentBankBoothLocation()?.let {
            val (boothDirection, _) = it
            direction = boothDirection
            isWalks = false
            setAttribute("facing_booth", true)
        }
    }

    override fun getIds(): IntArray = NPC_IDS
}
