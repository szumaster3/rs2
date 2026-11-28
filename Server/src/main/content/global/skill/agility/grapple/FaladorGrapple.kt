package content.global.skill.agility.grapple

import content.global.skill.agility.AgilityHandler
import core.api.*
import core.cache.def.impl.LocType
import core.game.component.Component
import core.game.interaction.OptionHandler
import core.game.node.Node
import core.game.node.entity.impl.ForceMovement
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.diary.DiaryType
import core.game.node.entity.skill.Skills
import core.game.system.task.Pulse
import core.game.world.GameWorld.Pulser
import core.game.world.map.Location
import core.game.world.update.flag.context.Animation
import core.game.world.update.flag.context.Graphics
import core.plugin.Initializable
import core.plugin.Plugin
import shared.consts.Animations
import shared.consts.Components
import shared.consts.Items
import shared.consts.Sounds

@Initializable
class FaladorGrapple : OptionHandler() {
    companion object {
        private val REQUIREMENTS = HashMap<Int?, Int>()
        private var requirementsString: String? = null

        init {
            REQUIREMENTS.putIfAbsent(Skills.AGILITY, 11)
            REQUIREMENTS.putIfAbsent(Skills.RANGE, 19)
            REQUIREMENTS.putIfAbsent(Skills.STRENGTH, 37)
            requirementsString =
                "You need at least " + REQUIREMENTS[Skills.AGILITY] + " " + Skills.SKILL_NAME[Skills.AGILITY] + ", " +
                REQUIREMENTS[Skills.RANGE] +
                " " +
                Skills.SKILL_NAME[Skills.RANGE] +
                ", and " +
                REQUIREMENTS[Skills.STRENGTH] +
                " " +
                Skills.SKILL_NAME[Skills.STRENGTH] +
                " to use this shortcut."
        }

        private val crossbowIds =
            intArrayOf(
                Items.DORGESHUUN_CBOW_8880,
                Items.MITH_CROSSBOW_9181,
                Items.ADAMANT_CROSSBOW_9183,
                Items.RUNE_CROSSBOW_9185,
                Items.KARILS_CROSSBOW_4734,
                Items.HUNTERS_CROSSBOW_10156,
            )
        private const val MITHRIL_GRAPPLE = Items.MITH_GRAPPLE_9419
    }

    override fun newInstance(arg: Any?): Plugin<Any> {
        LocType.forId(17049).handlers["option:grapple"] = this
        LocType.forId(17050).handlers["option:grapple"] = this
        LocType.forId(17051).handlers["option:jump"] = this
        LocType.forId(17052).handlers["option:jump"] = this
        return this
    }

    override fun handle(
        player: Player,
        node: Node,
        option: String,
    ): Boolean {
        val destination: Location
        val current = player.location
        player.logoutListeners["karamja-grapple"] = { p: Player ->
            p.location = current
        }
        when (option) {
            "jump" ->
                ForceMovement.run(
                    player,
                    current,
                    if (node.asScenery().id ==
                        17051
                    ) {
                        Location.create(3033, 3390, 0)
                    } else {
                        Location.create(3032, 3388, 0)
                    },
                    Animation(Animations.HW07_GETTING_FLUNG_IN_THE_AIR_7268),
                    10,
                )

            "grapple" -> {
                destination =
                    if (node.asScenery().id == 17049) Location.create(3032, 3389, 1) else Location.create(3032, 3391, 1)
                for ((key, value) in REQUIREMENTS) {
                    if (player.getSkills().getLevel(key!!) < value) {
                        sendDialogue(player, requirementsString.toString())
                        return true
                    }
                }
                if (!anyInEquipment(player, *crossbowIds) || !inEquipment(player, MITHRIL_GRAPPLE)) {
                    sendMessage(player, "You need a mithril grapple tipped bolt with a rope to do that.")
                    return true
                }
                lock(player, 15)
                Pulser.submit(
                    object : Pulse(1, player) {
                        var counter = 1
                        var tab: Component? = null

                        override fun pulse(): Boolean {
                            when (counter++) {
                                1 -> {
                                    player.faceLocation(destination)
                                    playAudio(player, Sounds.CROSSBOW_GRAPPLING_2928)
                                    player.visualize(
                                        Animation(Animations.XBOWS_HUMAN_FIRE_AND_CLIMB_GRAPPLE_4455),
                                        Graphics(shared.consts.Graphics.XBOWS_FIRE_AND_CLIMBED_GRAPPLE_760),
                                    )
                                }

                                8 -> {
                                    tab = player.interfaceManager.singleTab
                                    openOverlay(player, Components.FADE_TO_BLACK_115)
                                    setMinimapState(player, 2)
                                    removeTabs(player, 0, 1, 2, 3, 4, 5, 6, 11, 12)
                                }

                                13 -> player.properties.teleportLocation = destination
                                14 -> {
                                    player.unlock()
                                    restoreTabs(player)
                                    if (tab != null) {
                                        player.interfaceManager.openTab(tab!!)
                                    }
                                    setMinimapState(player, 0)
                                    closeOverlay(player)
                                    closeInterface(player)
                                    finishDiaryTask(player, DiaryType.FALADOR, 1, 2)
                                    setVarbit(player, 5723, 1, true)
                                    AgilityHandler.checkGrappleBreak(player)
                                    player.logoutListeners.remove("falador-grapple")
                                    return true
                                }
                            }
                            return false
                        }
                    },
                )
            }
        }
        return true
    }

    override fun getDestination(
        moving: Node,
        destination: Node,
    ): Location? = if (destination.asScenery().id == 17050) Location.create(3032, 3388, 0) else null
}
