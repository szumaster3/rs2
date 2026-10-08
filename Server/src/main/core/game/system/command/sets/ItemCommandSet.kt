package core.game.system.command.sets

import content.global.skill.cooking.brewing.BrewGrowth
import core.api.addItem
import core.api.addItemOrDrop
import core.api.getOrStartTimer
import core.api.sendMessage
import core.game.node.entity.combat.spell.Runes
import core.game.node.item.Item
import core.game.system.command.Privilege
import core.plugin.Initializable
import shared.consts.Items

@Initializable
class ItemCommandSet : CommandSet(Privilege.ADMIN) {
    private val farmKit = arrayListOf(Items.RAKE_5341, Items.SPADE_952, Items.SEED_DIBBER_5343, Items.WATERING_CAN8_5340, Items.SECATEURS_5329, Items.GARDENING_TROWEL_5325)
    private val runeKit = Runes.values()
        .filter { "STAFF" !in it.name }
        .map { it.id }
        .toIntArray()

    private val talismanKit = intArrayOf(
        Items.AIR_TALISMAN_1438,
        Items.MIND_TALISMAN_1448,
        Items.WATER_TALISMAN_1444,
        Items.EARTH_TALISMAN_1440,
        Items.FIRE_TALISMAN_1442,
        Items.BODY_TALISMAN_1446,
        Items.COSMIC_TALISMAN_1454,
        Items.CHAOS_TALISMAN_1452,
        Items.NATURE_TALISMAN_1462,
        Items.LAW_TALISMAN_1458,
        Items.DEATH_TALISMAN_1456,
        Items.BLOOD_TALISMAN_1450,
    )

    override fun defineCommands() {

        /*
         * Command for giving the player a rope.
         */

        define(
            name = "rope",
            privilege = Privilege.ADMIN,
            usage = "::rope",
            description = "Giving the player a rope"
        ) { p, _ ->
            addItem(p, Items.ROPE_954)
        }

        /*
         * Command for giving the player a spade.
         */

        define(
            name = "spade",
            privilege = Privilege.ADMIN,
            usage = "::spade",
            description = "Giving the player a spade"
        ) { p, _ ->
            addItem(p, Items.SPADE_952)
        }

        /*
         * Command for giving the player a knife.
         */

        define(
            name = "knife",
            privilege = Privilege.ADMIN,
            usage = "::knife",
            description = "Giving the player a knife"
        ) { p, _ ->
            addItem(p, Items.KNIFE_946)
        }

        /*
         * Command for giving the player a chisel.
         */

        define(
            name = "chisel",
            privilege = Privilege.ADMIN,
            usage = "::chisel",
            description = "Giving the player a chisel"
        ) { p, _ ->
            addItem(p, Items.CHISEL_1755)
        }

        /*
         * Command for providing a kit of various farming tools.
         */

        define(
            name = "farmkit",
            privilege = Privilege.ADMIN,
            usage = "::farmkit",
            description = "Provides a kit of various farming equipment.",
        ) { player, _ ->
            for (item in farmKit) {
                player.inventory.add(Item(item))
            }
            return@define
        }

        /*
         * Command for giving 1000 of each rune type.
         */

        define(
            name = "runekit",
            privilege = Privilege.ADMIN,
            usage = "::runekit",
            description = "Gives 1k of each Rune type.",
        ) { player, _ ->
            for (item in runeKit) {
                addItem(player, item, 1000)
            }
            return@define
        }

        /*
         * Command for giving talismans.
         */

        define(
            name = "talismankit",
            privilege = Privilege.ADMIN,
            usage = "::talismankit",
            description = "Provides a kit of all talismans."
        ) { player, _ ->
            for (item in talismanKit) {
                addItemOrDrop(player, item)
            }
        }

        /*
         * Command for giving brewing items.
         * Author: Bishop
         */

        define("brewkit", Privilege.ADMIN, "brewkit <lt>0-10<gt>",
            "Give yourself everything you need to brew ales."){ player, args ->
            if (args.size == 1) {
                sendMessage(player, "Kit numbers: 0 - Dwarven Stout, 1 - Asgarnian Ale, 2 - Greenman's Ale,")
                sendMessage(player, "Kit numbers: 3 - Wizard's Mind Bomb, 4 - Dragon Bitter, 5 - Moonlight Mead,")
                sendMessage(player, "Kit numbers: 6 - Axeman's Folly, 7 - Chef's Delight, 8 - Slayer's Respite,")
                sendMessage(player, "Kit numbers: 9 - Cider, 10 - Kelda Stout")
                return@define
            }
            val item =
                when (args[1].toInt()) {
                    0 -> Items.HAMMERSTONE_HOPS_5994
                    1 -> Items.ASGARNIAN_HOPS_5996
                    2 -> Items.CLEAN_HARRALANDER_255
                    3 -> Items.YANILLIAN_HOPS_5998
                    4 -> Items.KRANDORIAN_HOPS_6000
                    5 -> Items.MUSHROOM_6004
                    6 -> Items.OAK_ROOTS_6043
                    7 -> Items.CHOCOLATE_DUST_1975
                    8 -> Items.WILDBLOOD_HOPS_6002
                    9 -> Items.APPLE_MUSH_5992
                    10 -> Items.KELDA_HOPS_6113
                    else -> {
                        reject(player, "Invalid brew kit specified.")
                        return@define
                    }
                }
            addItemOrDrop(player, Items.BUCKET_OF_WATER_1929, if(args[1].toInt() == 9) 0 else 2)
            addItemOrDrop(player, Items.BARLEY_MALT_6008, if(args[1].toInt() == 9) 0 else 2)
            addItemOrDrop(player, item, if(args[1].toInt() == 6 || args[1].toInt() == 10) 1 else 4)
            addItemOrDrop(player, Items.ALE_YEAST_5767)
            return@define
        }

        define("brew", Privilege.ADMIN, "brew <lt>mode<gt>",
            "0: force natural step, 1: force good step without maturity, 2: force good step with maturity, 3: force bad step") { player, args ->
            var forceStep = false
            var forceGood = false
            var forceMature = false
            var forceBad = false
            if (args.size == 1) {
                forceStep = true
            }
            if (args.size != 1) {
                when (args[1].toIntOrNull()) {
                    0 -> forceStep = true
                    1 -> forceGood = true
                    2 -> forceMature = true
                    3 -> forceBad = true
                    else -> reject(player, "${args[1]} is not a valid option.")
                }
            }

            val vats = getOrStartTimer<BrewGrowth>(player).getVats()
            for (vat in vats) {
                vat.brew(forceStep, forceGood, forceMature, forceBad)
            }
        }
    }
}
