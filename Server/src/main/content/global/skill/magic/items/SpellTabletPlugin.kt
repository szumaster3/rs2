package content.global.skill.magic.items

import content.global.skill.magic.SpellListener
import content.global.skill.magic.SpellListeners
import content.global.skill.magic.spells.ModernSpells
import core.api.playAudio
import core.api.setAttribute
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.game.world.update.flag.context.Animation
import shared.consts.Animations
import shared.consts.Items
import shared.consts.Sounds

class SpellTabletPlugin : InteractionListener {
    override fun defineListeners() {
        on(B2B_TABLET, IntType.ITEM, "break") { player, node ->
            breakTablet(player)
            SpellListeners.run(ModernSpells.BONES_TO_BANANAS, SpellListener.NONE, "modern", player)
            player.inventory.remove(Item(node.id))
            return@on true
        }

        on(B2P_TABLET, IntType.ITEM, "break") { player, node ->
            breakTablet(player)
            SpellListeners.run(ModernSpells.BONES_TO_PEACHES, SpellListener.NONE, "modern", player)
            player.inventory.remove(Item(node.id))
            return@on true
        }
    }

    private fun breakTablet(player: Player) {
        playAudio(player, Sounds.POH_TABLET_BREAK_979)
        player.animator.forceAnimation(Animation(Animations.POH_SMASH_MAGIC_TABLET_4069))
        player.lock(5)
        setAttribute(player, "tablet-spell", true)
    }

    companion object{
        private const val B2B_TABLET = Items.BONES_TO_BANANAS_8014
        private const val B2P_TABLET = Items.BONES_TO_PEACHES_8015
        private const val TGS_TABLET = Items.TELEKINETIC_GRAB_8022
    }
}
