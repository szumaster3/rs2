package content.global.skill.summoning.familiar.npc

import content.global.skill.summoning.familiar.Familiar
import content.global.skill.summoning.familiar.FamiliarSpecial
import core.game.node.entity.combat.equipment.WeaponInterface
import core.game.node.entity.player.Player
import core.game.node.entity.skill.SkillBonus
import core.game.node.entity.skill.Skills
import core.game.world.update.flag.context.Animation
import core.game.world.update.flag.context.Graphics
import core.plugin.Initializable
import shared.consts.Animations
import shared.consts.Items
import shared.consts.NPCs

@Initializable
class WolpertingerNPC @JvmOverloads constructor(owner: Player? = null, id: Int = NPCs.LORE_WOLPERTINGER_6869) :
    Familiar(owner, id, 6200, Items.WOLPERTINGER_POUCH_12089, 1, WeaponInterface.STYLE_CAST) {

    init {
        boosts.add(SkillBonus(Skills.HUNTER, 5.0))
    }

    override fun construct(owner: Player, id: Int): Familiar {
        return WolpertingerNPC(owner, id)
    }

    override fun specialMove(special: FamiliarSpecial): Boolean {
        owner.getSkills().updateLevel(Skills.MAGIC, 7, (owner.getSkills().getStaticLevel(Skills.MAGIC) + 7))
        visualize(Animation.create(8267), Graphics.create(1464))
        return true
    }

    override fun visualizeSpecialMove() {
        owner.visualize(Animation.create(Animations.LORE_HUMAN_ANIMAL_SPECIAL_7660), Graphics.create(shared.consts.Graphics.LORE_BOOST_RECEIVE_12_1306))
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.LORE_WOLPERTINGER_6869, NPCs.LORE_WOLPERTINGER_COMBAT_6870)
    }
}
