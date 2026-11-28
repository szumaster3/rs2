package content.global.dialogue

import core.api.openNpcShop
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.dialogue.Topic
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

/**
 * Represents the Shopkeeper dialogue.
 * @author Vexia
 */
@Initializable
class ShopkeeperDialogue(player: Player? = null) : Dialogue(player) {

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> npc(FaceAnim.HALF_ASKING, "Can I help you at all?").also { stage++ }
            1 -> showTopics(
                Topic(FaceAnim.HALF_ASKING,"Yes please, what are you selling?", 3),
                Topic("How should I use your shop?", 2),
                Topic("No, thanks.", END_DIALOGUE)
            )
            2 -> npc(FaceAnim.HAPPY, "I'm glad you ask! You can buy as many of the items", "stocked as you wish. You can also sell most items to", "the shop.").also { stage = 1 }
            3 -> {
                end()
                openNpcShop(player, npc.id)
            }
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = ShopkeeperDialogue(player)

    override fun getIds(): IntArray = intArrayOf(
        NPCs.GENERALSHOPKEEPER1_520, NPCs.GENERALASSISTANT1_521, // Lumbridge
        NPCs.GENERALSHOPKEEPER2_522, NPCs.GENERALASSISTANT2_523, // Varrock
        NPCs.GENERALSHOPKEEPER3_524, NPCs.GENERALASSISTANT3_525, // Al Kharid
        NPCs.GENERALSHOPKEEPER4_526, NPCs.GENERALASSISTANT4_527, // Falador
        NPCs.GENERALSHOPKEEPER5_528, NPCs.GENERALASSISTANT5_529, // Edgeville
        NPCs.GENERALSHOPKEEPER6_530, NPCs.GENERALASSISTANT6_531, // Rimmington
        NPCs.SHOPKEEPER_532, NPCs.SHOP_ASSISTANT_533, // Karamja
        NPCs.KHAZARD_SHOPKEEPER_555 // Port Khazard
    )
}
