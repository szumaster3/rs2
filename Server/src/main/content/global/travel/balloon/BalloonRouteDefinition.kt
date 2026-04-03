package content.global.travel.balloon

import core.api.sendAngleOnInterface
import core.api.sendAnimationOnInterface
import core.api.sendModelOnInterface
import core.game.node.entity.player.Player

/**
 * Builder responsible for render balloon route screens.
 */
class BalloonRouteBuilder(private val p: Player, private val c: Int){

    /**
     * Sends a single model to a given interface slot.
     *
     * @param slot interface component id
     * @param id model id
     */
    fun model(slot: Int, id: Int)
    {
        sendModelOnInterface(p, c, slot, id)
    }

    /**
     * Sends multiple models to interface slots.
     *
     * @param data list of (slot, modelId) pairs
     */
    fun models(vararg data: Pair<Int, Int>)
    {
        data.forEach { (slot, id) ->
            sendModelOnInterface(p, c, slot, id)
        }
    }

    /**
     * Sends an animated model to interface.
     *
     * @param slot interface component slot
     * @param model model id
     * @param anim animation id
     * @param angle rotation angle (default 2100)
     * @param x horizontal rotation offset
     * @param y vertical rotation offset
     */
    fun animated(
        slot: Int,
        model: Int,
        anim: Int,
        angle: Int = 2100,
        x: Int = 200,
        y: Int = 300
    ) {
        sendModelOnInterface(p, c, slot, model)
        sendAngleOnInterface(p, c, slot, angle, x, y)
        sendAnimationOnInterface(p, anim, c, slot)
    }

    /**
     * Sends an animated model to interface.
     *
     * @param slot interface component slot
     * @param model model id
     * @param anim animation id
     * @param angle rotation angle (default 2100)
     * @param x horizontal rotation offset
     * @param y vertical rotation offset
     */
    fun eagles(
        vararg slots: Int,
        model: Int = 19780,
        anim: Int = 341,
        angle: Int = 2100,
        x: Int = 200,
        y: Int = 300
    ) {
        slots.forEach { slot ->
            animated(slot, model, anim, angle, x, y)
        }
    }
}

/**
 * Entry point for balloon route.
 *
 * Creates a scoped builder used to declaratively define interface layout.
 *
 * Example:
 * ```
 * balloonScreen(player, component) {
 *     model(40, 19558)
 *     animated(175, 19781, 373)
 * }
 * ```
 *
 * @param p player instance
 * @param c interface component id
 * @param block configuration block
 */
fun balloonScreen(
    p: Player,
    c: Int,
    block: BalloonRouteBuilder.() -> Unit
) {
    BalloonRouteBuilder(p, c).block()
}

/**
 * Registry holding all balloon routes models for each stage.
 */
object BalloonRouteDefinition {

    /*
     * Taverley route stages.
     */

    fun taverley_0(p: Player, c: Int) = balloonScreen(p, c) {
        // Floor
        models(40 to 19558, 45 to 19559, 50 to 19560, 55 to 19561)
        // Trees
        models(123 to 19580, 124 to 19578, 125 to 19582, 103 to 19579, 104 to 19576, 105 to 19581, 83 to 19575, 84 to 19574, 85 to 19577)
        // Small trees
        models(102 to 19521, 82 to 19519, 107 to 19521, 87 to 19519, 106 to 19521, 86 to 19519)
        // Smallest
        models(91 to 19522, 92 to 19522)
        // Evergreens
        models(127 to 19570, 107 to 19569, 87 to 19568, 128 to 19570, 108 to 19569, 88 to 19568)
        // Eagles
        eagles(145, 148)
        eagles(177, angle = 2100, x = 200, y = 300)
        eagles(209, angle = 2576, x = 200, y = 300)
        // Clouds
        models(155 to 19525, 156 to 19526, 210 to 19525, 211 to 19524, 212 to 19524, 213 to 19524, 214 to 19526, 227 to 19525, 228 to 19524, 229 to 19526, 172 to 19525, 173 to 19524, 174 to 19526)
        // Stars
        animated(175, 19781, 373, 2100, 0, 1500)
        animated(230, 19781, 373, 2100, 0, 1500)
        // Base
        model(78, 19572)
    }

    fun taverley_1(p: Player, c: Int) = balloonScreen(p, c) {
        models(40 to 19562, 45 to 19563, 50 to 19564, 55 to 19565)
        models(158 to 19526)
        models(83 to 19551, 84 to 19553, 103 to 19552, 123 to 19527)
        model(90, 19616)
        models(111 to 19530, 91 to 19523, 112 to 19530, 92 to 19523)
        models(133 to 19570, 113 to 19569, 93 to 19568)
        models(154 to 19534, 134 to 19533, 114 to 19532, 94 to 19531, 155 to 19544, 135 to 19543, 115 to 19542, 95 to 19541, 156 to 19545, 136 to 19546, 116 to 19540, 96 to 19539, 157 to 19550, 137 to 19549, 117 to 19548, 97 to 19547)
        eagles(161, 179, 185, 204, 219)
        models(180 to 19525, 181 to 19526, 201 to 19525, 202 to 19524, 203 to 19526, 186 to 19525, 187 to 19524, 188 to 19526)
    }

    fun taverley_2(p: Player, c: Int) = balloonScreen(p, c) {
        models(40 to 19566, 45 to 19554, 50 to 19555, 55 to 19556)
        models(138 to 19538, 118 to 19537, 99 to 19536, 78 to 19535)
        models(119 to 19580, 120 to 19578, 121 to 19582, 98 to 19579, 100 to 19576, 101 to 19581, 79 to 19575, 80 to 19574, 81 to 19577)
        models(102 to 19521, 82 to 19519, 103 to 19521, 83 to 19519, 84 to 19522, 85 to 19522, 126 to 19570, 106 to 19569, 86 to 19568, 92 to 19522, 113 to 19521, 93 to 19519)
        model(97, 19567)
        eagles(146, 151, 192, 207, 208, 215)
        models(167 to 19525, 168 to 19524, 169 to 19524, 170 to 19524, 171 to 19526, 174 to 19525, 175 to 19524, 176 to 19526, 212 to 19525, 213 to 19524, 214 to 19526)
        animated(191, 19781, 373, 2100, 0, 1500)
    }

    /*
     * Crafting guild route stages.
     */

    fun crafting_guild_0(p: Player, c: Int) = balloonScreen(p, c) {
        // Floor
        models(40 to 19558, 45 to 19559, 50 to 19604, 55 to 19605)
        // Landing base
        model(138, 19572)
        // Mountain peak
        models(118 to 19607, 119 to 19611)
        // Mountain lower layer
        models(99 to 19608, 98 to 19608, 100 to 19612)
        // Mountain lowest layer
        models(78 to 19609, 79 to 19609, 80 to 19609, 81 to 19613)
        // Trees
        models(122 to 19570, 102 to 19569, 82 to 19568, 103 to 19521, 83 to 19519, 124 to 19570, 104 to 19569, 84 to 19568)
        // Houses
        models(128 to 19618, 108 to 19633, 88 to 19630, 129 to 19619, 109 to 19603, 89 to 19602, 130 to 19620, 110 to 19629, 90 to 19628, 131 to 19621, 111 to 19632, 91 to 19631)
        models(133 to 19570, 113 to 19569, 93 to 19568, 116 to 19521, 96 to 19519, 137 to 19570, 117 to 19569, 97 to 19568)
        // Eagles
        eagles(220, 221, 206, 175, 151, 171, 148)
        // Star
        animated(183, 19781, 373, 2100, 0, 1500)
        // Clouds
        models(162 to 19525, 163 to 19524, 164 to 19526, 192 to 19525, 193 to 19524, 194 to 19526, 233 to 19525, 234 to 19524, 235 to 19526)
    }

    fun crafting_guild_1(p: Player, c: Int) = balloonScreen(p, c) {
        models(40 to 19606, 45 to 19561, 50 to 19562, 55 to 19562)
        models(99 to 19521, 78 to 19519, 119 to 19570, 98 to 19569, 79 to 19568)
        models(103 to 19625, 83 to 19623, 124 to 19626, 104 to 19627, 84 to 19624)
        models(106 to 19617, 86 to 19615, 108 to 19617, 88 to 19615, 111 to 19617, 91 to 19615, 96 to 19616)
        model(97, 19717)
        eagles(161, 185, 216, 217, 169)
        animated(126, 19781, 373, 2100, 0, 1500)
        animated(131, 19781, 373, 2100, 0, 1500)
        animated(186, 19781, 373, 2100, 0, 1500)
        models(166 to 19525, 167 to 19524, 168 to 19526, 200 to 19525, 201 to 19524, 202 to 19526, 205 to 19525, 206 to 19524, 207 to 19526)
    }

    fun crafting_guild_2(p: Player, c: Int) = balloonScreen(p, c) {
        models(40 to 19595, 45 to 19596, 50 to 19597, 55 to 19598)
        model(97, 19567)
        model(78, 19717)
        models(79 to 19616, 100 to 19617, 80 to 19615)
        models(123 to 19601, 103 to 19600, 83 to 19599, 84 to 19616, 105 to 19521, 85 to 19519, 86 to 19522, 107 to 19521, 87 to 19519, 108 to 19521, 88 to 19519, 89 to 19622, 130 to 19601, 110 to 19600, 90 to 19599)
        model(198, 19526)
        eagles(199, 120, 163, 209)
        animated(143, 19781, 373, 2100, 0, 1500)
        animated(104, 19781, 373, 2100, 0, 1500)
        models(222 to 19525, 223 to 19524, 224 to 19526, 166 to 19525, 167 to 19524, 168 to 19526, 173 to 19525, 174 to 19524, 175 to 19526, 228 to 19525, 229 to 19524, 230 to 19526, 210 to 19525, 211 to 19524, 212 to 19526)
    }

    /*
     * Varrock route stages.
     */

    fun varrock_0(p: Player, c: Int) = balloonScreen(p, c) {
        // Floor
        models(40 to 19731, 45 to 19735, 50 to 19736, 55 to 19737)
        model(99, 19572)
        // Mountain
        models(78 to 19607, 79 to 19611)
        // Evergreen
        models(120 to 19570, 100 to 19569, 80 to 19568, 83 to 19616, 86 to 19616)
        // Dead trees
        models(127 to 19529, 107 to 19528, 87 to 19523, 108 to 19530, 88 to 19523, 129 to 19529, 109 to 19528, 89 to 19523, 110 to 19530, 90 to 19523, 131 to 19529, 111 to 19528, 91 to 19523)
        // Small trees
        models(113 to 19521, 93 to 19519, 114 to 19521, 94 to 19519)
        // Evergreens
        models(135 to 19570, 115 to 19569, 95 to 19568, 137 to 19570, 117 to 19569, 97 to 19568)
        eagles(122, 161, 163, 182, 170, 174, 157, 216, 233)
        // Clouds
        models(198 to 19525, 199 to 19524, 200 to 19526, 144 to 19525, 145 to 19524, 146 to 19526, 205 to 19525, 206 to 19524, 207 to 19526, 187 to 19525, 188 to 19524, 189 to 19526, 192 to 19525, 193 to 19524, 194 to 19526)
    }

    fun varrock_1(p: Player, c: Int) = balloonScreen(p, c) {
        models(40 to 19738, 45 to 19739, 50 to 19740, 55 to 19741)
    }

    fun varrock_2(p: Player, c: Int) = balloonScreen(p, c) {
        models(40 to 19742, 45 to 19732, 50 to 19733, 55 to 19734)
    }

    /*
     * Grand tree route stages.
     */

    fun grand_tree_0(p: Player, c: Int) = balloonScreen(p, c) {

    }

    fun grand_tree_1(p: Player, c: Int) = balloonScreen(p, c) {

    }

    fun grand_tree_2(p: Player, c: Int) = balloonScreen(p, c) {

    }

    /*
     * Castle wars route stages.
     */

    fun castle_wars_0(p: Player, c: Int) = balloonScreen(p, c) {

    }

    fun castle_wars_1(p: Player, c: Int) = balloonScreen(p, c) {

    }

    fun castle_wars_2(p: Player, c: Int) = balloonScreen(p, c) {

    }
}