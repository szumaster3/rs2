package content.global.skill.farming

import content.global.skill.summoning.familiar.npc.GiantEntNPC
import core.api.*
import core.cache.def.impl.LocType
import core.game.interaction.OptionHandler
import core.game.node.Node
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.diary.DiaryType
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import core.game.system.task.Pulse
import core.game.world.update.flag.context.Animation
import core.plugin.Initializable
import core.plugin.Plugin
import shared.consts.Animations
import shared.consts.Sounds
import java.util.concurrent.TimeUnit

@Initializable
class FruitAndBerryPicker : OptionHandler() {
    override fun newInstance(arg: Any?): Plugin<Any> {
        LocType.setOptionHandler("pick-coconut", this)
        LocType.setOptionHandler("pick-banana", this)
        LocType.setOptionHandler("pick-apple", this)
        LocType.setOptionHandler("pick-orange", this)
        LocType.setOptionHandler("pick-pineapple", this)
        LocType.setOptionHandler("pick-papaya", this)
        LocType.setOptionHandler("pick-leaf", this)
        LocType.setOptionHandler("pick-from", this)
        LocType.setOptionHandler("pick-fruit", this)
        LocType.setOptionHandler("pick-spine", this)
        return this
    }

    override fun handle(player: Player?, node: Node?, option: String?): Boolean {
        player ?: return false
        node ?: return false

        val fPatch = FarmingPatch.forObject(node.asScenery())
        fPatch ?: return false

        val patch = fPatch.getPatchFor(player)

        val plantable = patch.plantable
        plantable ?: return false

        val animation = Animation(Animations.PICKING_MID_2281)

        if (patch.getFruitOrBerryCount() <= 0) {
            sendMessage(player, "This shouldn't be happening. Please report this.")
            return true
        }

        if (!hasSpaceFor(player, Item(plantable.harvestItem))) {
            sendMessage(player, "You don't have enough inventory space to do that.")
            return true
        }

        if (System.currentTimeMillis() - patch.nextGrowth > TimeUnit.MINUTES.toMillis(45)) {
            patch.nextGrowth = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(45)
        }

        submitIndividualPulse(
            player,
            object : Pulse(animation.duration) {
                override fun pulse(): Boolean {
                    val reward = Item(plantable.harvestItem, 1)

                    if (!hasSpaceFor(player, reward)) {
                        sendMessage(player, "You have run out of inventory space.")
                        return true
                    }

                    val familiar = player.familiarManager.familiar
                    if (familiar != null && familiar is GiantEntNPC) {
                        familiar.modifyFarmingReward(fPatch, reward)
                    }

                    animate(player, animation)
                    playAudio(player, Sounds.FARMING_PICK_2437)
                    addItemOrDrop(player, reward.id, reward.amount)
                    rewardXP(player, Skills.FARMING, plantable.harvestXP)
                    patch.setCurrentState(patch.getCurrentState() - 1)

                    if (patch.patch.type == PatchType.CACTUS_PATCH) {
                        sendMessage(player, "You carefully pick a spine from the cactus.")
                    } else {
                        val determiner = if (patch.patch.type == PatchType.BUSH_PATCH) "some" else "a"
                        sendMessage(player, "You pick $determiner ${reward.name.lowercase()}.")
                    }

                    if (plantable == Plantable.POISON_IVY_SEED && patch.patch == FarmingPatch.CHAMPIONS_GUILD_BUSH) {
                        player.achievementDiaryManager.finishTask(player, DiaryType.VARROCK, 2, 0)
                    }
                    if (fPatch == FarmingPatch.TREE_GNOME_VILLAGE_FRUIT_TREE &&
                        (plantable == Plantable.PAPAYA_SAPLING || plantable == Plantable.PALM_SAPLING)) {
                        finishDiaryTask(player, DiaryType.ARDOUGNE, 2, 14)
                    }
                    return patch.getFruitOrBerryCount() == 0
                }
            },
        )

        return true
    }
}
