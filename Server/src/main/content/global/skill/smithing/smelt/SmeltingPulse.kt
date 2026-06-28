package content.global.skill.smithing.smelt

import content.data.GameAttributes
import content.global.skill.smithing.bar.BarItem
import core.api.*
import core.game.container.impl.EquipmentContainer
import core.game.event.ResourceProducedEvent
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.diary.DiaryType
import core.game.node.entity.skill.SkillPulse
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import core.game.world.map.Location
import core.game.world.update.flag.context.Graphics
import core.tools.Log
import core.tools.RandomFunction
import core.tools.StringUtils
import shared.consts.Animations
import shared.consts.Items
import shared.consts.Quests
import shared.consts.Sounds

class SmeltingPulse : SkillPulse<Item?> {
    private val barItem: BarItem?
    private val superHeat: Boolean
    private var ticks = 0
    private var amount: Int

    constructor(player: Player?, node: Item?, barItem: BarItem?, amount: Int) : super(player, node) {
        this.barItem = barItem
        this.amount = amount
        this.superHeat = false
    }

    constructor(player: Player?, node: Item?, barItem: BarItem?, amount: Int, heat: Boolean) : super(player, node) {
        this.barItem = barItem
        this.amount = amount
        this.superHeat = heat
        this.resetAnimation = false
    }

    override fun checkRequirements(): Boolean {
        closeChatBox(player)
        if (barItem == null || player == null) {
            return false
        }
        if (getStatLevel(player, Skills.SMITHING) < barItem.level) {
            sendMessage(
                player,
                "You need a Smithing level of at least ${barItem.level} to smelt ${
                    barItem.product.name.lowercase().replace(" bar", ".")
                }"
            )
            return false
        }

        if (barItem == BarItem.BLURITE && !isQuestComplete(player, Quests.THE_KNIGHTS_SWORD)) {
            sendDialogue(player, "You need complete the Knights' Sword to smelt this bar.")
            return false
        }

        for (item in barItem.ores) {
            val itemName = getItemName(item.id).lowercase()
            if (!player.inventory.contains(item.id, item.amount)) {
                sendMessage(player, "You have run out of $itemName to smelt.")
                return false
            }
        }
        return true
    }


    override fun animate() {
        if (ticks == 0 || ticks % 5 == 0) {
            if (superHeat) {
                visualize(
                    player,
                    Animations.HUMAN_CAST_SUPERHEAT_SPELL_725,
                    Graphics(shared.consts.Graphics.SUPERHEAT_ITEM_148, 96),
                )
            } else {
                animate(player, Animations.HUMAN_FURNACE_SMELT_3243)
                playAudio(player, Sounds.FURNACE_2725)
            }
        }
    }

    override fun reward(): Boolean {
        if (!superHeat && ++ticks % 5 != 0) {
            return false
        }
        if (!superHeat) {
            sendMessage(
                player,
                "You place a lump of " + StringUtils.formatDisplayName(barItem.toString()).lowercase() + " in the furnace.",
            )
        }
        for (i in barItem!!.ores) {
            if (!removeItem(player, i)) {
                return true
            }
        }
        if (success(player)) {
            var amt =
                if (((freeSlots(player) != 0 && !superHeat && withinDistance(
                        player,
                        Location(3107, 3500, 0)
                    ) && player.inventory.containsItems(*barItem.ores)) &&
                            player.achievementDiaryManager.getDiary(DiaryType.VARROCK)!!.level != -1 &&
                            player.achievementDiaryManager.checkSmithReward(barItem) &&
                            RandomFunction.random(100) <= 10)
                ) {
                    2
                } else {
                    1
                }
            if (amt != 1) {
                if (!removeItem(player, barItem.ores)) {
                    amt = 1
                } else {
                    sendMessage(player, "The magic of the Varrock armour enables you to smelt 2 bars at the same time.")
                }
            }
            addItem(player, barItem.product.id, amt)
            player.dispatch(ResourceProducedEvent(barItem.product.id, 1, player, -1))
            var xp = barItem.experience * amt

            if (((player.equipment[EquipmentContainer.SLOT_HANDS] != null && player.equipment[EquipmentContainer.SLOT_HANDS].id == Items.GOLDSMITH_GAUNTLETS_776)) && barItem.product.id == Items.GOLD_BAR_2357) {
                xp = 56.2 * amt
            }
            rewardXP(player, Skills.SMITHING, xp)
            if (!superHeat) {
                sendMessage(
                    player,
                    "You retrieve a bar of " +
                            barItem.product.name
                                .lowercase()
                                .replace(" bar", " from the furnace.")
                )
            }
        } else {
            sendMessage(player, "The ore is too impure and you fail to refine it.")
        }
        amount--
        return amount < 1
    }

    private fun hasForgingRing(player: Player): Boolean = inEquipment(player, RING_OF_FORGING,1)

    fun success(player: Player): Boolean {
        if (barItem === BarItem.IRON && !superHeat) {
            if (hasForgingRing(player)) {
                var charges = player.getAttribute(GameAttributes.ROF_CHARGES,140) - 1
                if (charges <= 0) {
                    if (removeItem(player, Items.RING_OF_FORGING_2568, Container.EQUIPMENT)) {
                        charges = 140
                        sendMessage(
                            player,
                            "Your Ring of forging uses up its last charge and disintegrates."
                        )
                        stop()
                    } else {
                        log(this.javaClass, Log.ERR, "Failed to delete empty ring of forging for player " + player.name)
                        return false
                    }
                }
                setAttribute(player,"/save:${GameAttributes.ROF_CHARGES}",charges)
                return true
            } else {
                return RandomFunction.nextBool()
            }
        }
        return true
    }

    /*
    fun success(player: Player): Boolean {
        if (barItem == BarItem.IRON && !superHeat) {
            return if (hasForgingRing(player)) {
                val ring = getItemFromEquipment(player, EquipmentSlot.RING)
                if (ring != null) {
                    if (getCharge(ring) == 1000) setCharge(ring, 140)
                    adjustCharge(ring, -1)
                    if (getCharge(ring) == 0) {
                        removeItem(player, ring)
                        sendMessage(player, "Your ring of forging uses up its last charge and disintegrates.")
                        stop()
                    }
                }
                true
            } else {
                RandomFunction.getRandom(100) <= (if (getStatLevel(player, Skills.SMITHING) >= 45) 80 else 50)
            }
        }
        return true
    }
    */

    companion object {
        private const val RING_OF_FORGING = Items.RING_OF_FORGING_2568
    }
}
