package content.region.misthalin.lumbridge.quest.cook

import content.data.GameAttributes
import core.api.*
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.quest.Quest
import core.game.node.entity.skill.Skills
import core.plugin.Initializable
import shared.consts.Components
import shared.consts.Items
import shared.consts.Quests
import shared.consts.Vars

@Initializable
class CooksAssistant : Quest(
    Quests.COOKS_ASSISTANT,
    15, 14, 1,
    Vars.VARP_QUEST_COOKS_ASSISTANT_PROGRESS_29,
    0, 1, 2
) {

    override fun drawJournal(player: Player, stage: Int) {
        super.drawJournal(player, stage)
        var line = 12

        when {
            stage < 10 -> {
                line(player, "I can start this quest by speaking to the !!Cook?? in the", line++)
                line(player, "!!Kitchen?? on the ground floor of !!Lumbridge Castle??.", line)
            }

            stage in 10..99 -> {
                line(player, "It's the !!Duke of Lumbridge's?? birthday and I have to help", line++)
                line(player, "his !!Cook?? make him a !!birthday cake??. To do this I need to", line++)
                line(player, "bring him the following ingredients:", line++)

                when {
                    hasSubmitted(player, "milk") || hasSubmitted(player, "all") ->
                        line(player, "---I have given the cook a bucket of milk./--", line++)
                    inInventory(player, BUCKET_OF_MILK, 1) ->
                        line(player, "I have found a !!bucket of milk?? to give to the cook.", line++)
                    else -> {
                        line(player, "I need to find a !!bucket of milk??. There's a cattle field east", line++)
                        line(player, "of Lumbridge, I should make sure I take an empty bucket", line++)
                        line(player, "with me.", line++)
                    }
                }

                when {
                    hasSubmitted(player, "flour") || hasSubmitted(player, "all") ->
                        line(player, "---I have given the cook a pot of flour./--", line++)
                    inInventory(player, POT_OF_FLOUR, 1) ->
                        line(player, "I have found a !!pot of flour?? to give to the cook.", line++)
                    else -> {
                        line(player, "I need to find a !!pot of flour??. There's a mill found north-", line++)
                        line(player, "west of Lumbridge, I should take an empty pot with me.", line++)
                    }
                }

                when {
                    hasSubmitted(player, "egg") || hasSubmitted(player, "all") ->
                        line(player, "---I have given the cook an egg./--", line++)
                    inInventory(player, EGG, 1) ->
                        line(player, "I have found an !!egg?? to give to the cook.", line++)
                    else -> {
                        line(player, "I need to find an !!egg??. The cook normally gets his eggs from", line++)
                        line(player, "the Groats' farm, found just to the west of the cattle", line++)
                        line(player, "field.", line++)
                    }
                }

                if (hasAllIngredients(player)) {
                    line(player, "I should return to the !!Cook??.", line)
                }
            }

            stage == 100 -> {
                line(player, "---It was the Duke of Lumbridge's birthday, but his cook had/--", line++)
                line(player, "---forgotten to buy the ingredients he needed to make him a/--", line++)
                line(player, "---cake. I brought the cook an egg, some flour and some milk/--", line++)
                line(player, "---and then cook made a delicious looking cake with them./--", line++)
                line++
                line(player, "---As a reward he now lets me use his high quality range/--", line++)
                line(player, "---which lets me burn things less whenever I wish to cook/--", line++)
                line(player, "---there./--", line++)
                line++
                line(player, "<col=FF0000>QUEST COMPLETE!</col>", line)
            }
        }
    }

    override fun finish(player: Player) {
        super.finish(player)

        var line = 10
        drawReward(player, Items.CAKE_1891)
        sendItemZoomOnInterface(player, Components.QUEST_COMPLETE_SCROLL_277, 5, Items.CAKE_1891, 240)

        drawReward(player, "1 Quest Point", line++)
        drawReward(player, "300 Cooking XP", line)

        rewardXP(player, Skills.COOKING, 300.0)
        removeAttributes(
            player,
            GameAttributes.MILK_SUBMITTED,
            GameAttributes.FLOUR_SUBMITTED,
            GameAttributes.EGG_SUBMITTED,
            GameAttributes.ALL_SUBMITTED,
            GameAttributes.PART_SUBMITTED,
        )
    }

    override fun newInstance(obj: Any?): Quest = this

    companion object {
        private const val BUCKET_OF_MILK = Items.BUCKET_OF_MILK_1927
        private const val POT_OF_FLOUR = Items.POT_OF_FLOUR_1933
        private const val EGG = Items.EGG_1944
    }

    private fun hasSubmitted(player: Player, type: String): Boolean =
        player.getAttribute("cooks_assistant:${type}_submitted", false)

    private fun hasAllIngredients(player: Player): Boolean =
        hasSubmitted(player, "all") ||
                (hasSubmitted(player, "milk") && hasSubmitted(player, "flour") && hasSubmitted(player, "egg"))
}
