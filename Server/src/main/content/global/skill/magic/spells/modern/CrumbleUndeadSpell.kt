package content.global.skill.magic.spells.modern

import content.global.skill.magic.spells.ModernSpells
import content.global.skill.magic.spells.SpellProjectile
import content.region.kandarin.feldip.quest.zogre.npc.ZogreNPC
import core.game.node.Node
import core.game.node.entity.Entity
import core.game.node.entity.combat.BattleState
import core.game.node.entity.combat.spell.CombatSpell
import core.game.node.entity.combat.spell.Runes
import core.game.node.entity.combat.spell.SpellType
import core.game.node.entity.impl.Animator.Priority
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.SpellBookManager.SpellBook
import core.game.world.update.flag.context.Animation
import core.game.world.update.flag.context.Graphics
import core.plugin.Initializable
import core.plugin.Plugin
import shared.consts.Animations
import shared.consts.NPCs
import shared.consts.Sounds
import java.util.*


/**
 * The Crumble undead spell.
 */
@Initializable
class CrumbleUndeadSpell : CombatSpell(
    SpellType.CRUMBLE_UNDEAD,
    SpellBook.MODERN,
    39,
    24.5,
    Sounds.CRUMBLE_CAST_AND_FIRE_122,
    Sounds.CRUMBLE_HIT_124,
    Animation(Animations.HUMAN_CASTCRUMBLEUNDEAD_724, Priority.HIGH),
    Graphics(shared.consts.Graphics.CRUMBLEUNDEAD_CASTING_145, 92),
    SpellProjectile.create(shared.consts.Graphics.CRUMBLEUNDEAD_TRAVEL_146),
    Graphics(shared.consts.Graphics.CRUMBLEUNDEAD_IMPACT_147, 92),
    Runes.EARTH_RUNE.getItem(2),
    Runes.AIR_RUNE.getItem(2),
    Runes.CHAOS_RUNE.getItem(1)
) {

    override fun cast(entity: Entity, target: Node): Boolean {
        val npc = target as? NPC
        if (npc == null) {
            return false
        }

        val ogres = npc.id in ZogreNPC.IDS || npc.id == NPCs.ZOGRE_SLASH_BASH_2060

        if (ogres) {
            return super.cast(entity, target)
        }

        if (npc.task?.undead != true) {
            (entity as? Player)?.packetDispatch?.sendMessage("This spell only affects the undead.")
            return false
        }

        return super.cast(entity, target)
    }

    @Throws(Throwable::class)
    override fun newInstance(arg: SpellType?): Plugin<SpellType?> {
        SpellBook.MODERN.register(ModernSpells.CRUMBLE_UNDEAD, this)
        return this
    }

    override fun getMaximumImpact(entity: Entity, victim: Entity, state: BattleState): Int = type.getImpactAmount(entity, victim, 0)
}
