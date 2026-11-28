package content.global.skill.summoning.familiar.npc

import content.global.skill.summoning.familiar.BurdenBeast
import content.global.skill.summoning.familiar.Familiar
import content.global.skill.summoning.familiar.FamiliarSpecial
import core.game.node.entity.combat.equipment.WeaponInterface
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.world.update.flag.context.Animation
import core.game.world.update.flag.context.Graphics
import core.plugin.Initializable
import shared.consts.Animations
import shared.consts.Items
import shared.consts.NPCs

@Initializable
class WarTortoiseNPC @JvmOverloads constructor(owner: Player? = null, id: Int = NPCs.LORE_WAR_TURTLE_6815) :
    BurdenBeast(owner, id, 4300, Items.WAR_TORTOISE_POUCH_12031, 20, 18, WeaponInterface.STYLE_DEFENSIVE) {
    override fun construct(owner: Player, id: Int): Familiar {
        return WarTortoiseNPC(owner, id)
    }

    override fun specialMove(special: FamiliarSpecial): Boolean {
        owner.getSkills().updateLevel(Skills.DEFENCE, 9, owner.getSkills().getStaticLevel(Skills.DEFENCE) + 9)
        visualize(Animation.create(8288), Graphics.create(shared.consts.Graphics.LORE_TURTLE_ADAMAN_SHELL_SPOT_1414))
        return true
    }

    override fun visualizeSpecialMove() {
        owner.visualize(
            Animation.create(Animations.LORE_HUMAN_ANIMAL_SPECIAL_7660),
            Graphics.create(shared.consts.Graphics.LORE_BOOST_RECEIVE_16_1310)
        )
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.LORE_WAR_TURTLE_6815, NPCs.LORE_WAR_TURTLE_COMBAT_6816)
    }
}
