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
        angle: Int = 2567,
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
        model: Int = 19780,//-1white
        anim: Int = 341,
        angle: Int = 2567,
        x: Int = 200,
        y: Int = 300
    ) {
        slots.forEach { slot ->// zoom 2567
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
        models(102 to 19521, 82 to 19519, 107 to 19521, 87 to 19519, 106 to 19521, 86 to 19519, 109 to 19521, 89 to 19519)
        // Smallest
        models(91 to 19522, 92 to 19522)
        // Evergreens
        models(127 to 19570, 107 to 19569, 87 to 19568, 128 to 19570, 108 to 19569, 88 to 19568)
        // Eagles
        eagles(145, 148)
        eagles(177)
        eagles(209)
        // Clouds
        models(155 to 19525, 156 to 19526, 210 to 19525, 211 to 19524, 212 to 19524, 213 to 19526, 227 to 19525, 228 to 19524, 229 to 19526, 172 to 19525, 173 to 19524, 174 to 19526)
        // Stars
        animated(175, 19781, 373)
        animated(230, 19781, 373)
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
        models(168 to 19525, 169 to 19524, 170 to 19524, 171 to 19526, 174 to 19525, 175 to 19524, 176 to 19526, 212 to 19525, 213 to 19524, 214 to 19526)
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
        animated(183, 19781, 373)
        // Clouds
        models(162 to 19525, 163 to 19524, 164 to 19526, 192 to 19525, 193 to 19524, 194 to 19526, 233 to 19525, 234 to 19524, 235 to 19526)
    }

    fun crafting_guild_1(p: Player, c: Int) = balloonScreen(p, c) {
        models(40 to 19606, 45 to 19561, 50 to 19562, 55 to 19562)
        models(168 to 19525, 169 to 19524, 170 to 19526, 200 to 19525, 201 to 19524, 202 to 19526, 206 to 19524, 207 to 19526)
        models(99 to 19521, 78 to 19519, 119 to 19570, 98 to 19569, 79 to 19568)
        models(103 to 19625, 83 to 19623, 124 to 19626, 104 to 19627, 84 to 19624)
        models(107 to 19617, 87 to 19615, 109 to 19617, 89 to 19615, 112 to 19617, 92 to 19615, 97 to 19616)
        model(97, 19717)
        eagles(161, 187, 216, 217, 171)
        animated(127, 19781, 373)
        animated(132, 19781, 373)
        animated(188, 19781, 373)
        models(193 to 19525, 194 to 19524, 195 to 19526)
        models(96 to 19616)
        models(205 to 19525)
    }

    fun crafting_guild_2(p: Player, c: Int) = balloonScreen(p, c) {
        models(40 to 19595, 45 to 19596, 50 to 19597, 55 to 19598)
        model(97, 19567)
        model(78, 19717)
        models(79 to 19616, 100 to 19617, 80 to 19615)
        models(123 to 19601, 103 to 19600, 83 to 19599, 84 to 19616, 105 to 19521, 85 to 19519, 86 to 19522, 107 to 19521, 87 to 19519, 108 to 19521, 88 to 19519, 89 to 19622, 130 to 19601, 110 to 19600, 90 to 19599)
        model(198, 19526)
        eagles(199, 120, 163, 209)
        animated(143, 19781, 373)
        animated(104, 19781, 373)
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
        // Clouds
        models(198 to 19525, 199 to 19524, 200 to 19526, 144 to 19525, 145 to 19524, 146 to 19526, 206 to 19525, 207 to 19524, 208 to 19526, 188 to 19525, 189 to 19524, 190 to 19526, 193 to 19525, 194 to 19524, 195 to 19526)
        // Eagles
        eagles(122, 161, 163, 170, 174, 157, 216, 232)
    }

    fun varrock_1(p: Player, c: Int) = balloonScreen(p, c) {
        models(40 to 19738, 45 to 19739, 50 to 19740, 55 to 19741)
        eagles(139)
        models(119 to 19570, 98 to 19569, 79 to 19568)
        models(101 to 19521, 81 to 19519)
        models(122 to 19570, 102 to 19569, 82 to 19568)
        models(103 to 19521, 83 to 19519)
        models(200 to 19525, 201 to 19524, 202 to 19526)
        models(224 to 19525, 225 to 19524, 226 to 19526)

        // House
        models(85 to 19753, 105 to 19744, 125 to 19744, 145 to 19749)
        models(86 to 19760, 106 to 19763, 126 to 19745, 146 to 19750, 166 to 19755)
        models(87 to 19759, 107 to 19764, 127 to 19746, 147 to 19751, 167 to 19756)
        models(88 to 19760, 108 to 19765, 128 to 19745, 148 to 19752, 168 to 19757)
        models(89 to 19761, 109 to 19743, 129 to 19743, 149 to 19754)

        eagles(218,214,209,190,214,154)

        animated(187, 19781, 373, 2100, 0, 1500)
        animated(192, 19781, 373, 2100, 0, 1500)
        animated(236, 19781, 373, 2100, 0, 1500)

        models(171 to 19525, 172 to 19524, 173 to 19526)
        models(215 to 19525, 216 to 19524, 217 to 19526)

        models(113 to 19521, 93 to 19519)
        models(134 to 19570, 114 to 19569, 94 to 19568)
        models(135 to 19570, 115 to 19569, 95 to 19568)
        models(137 to 19570, 117 to 19569, 97 to 19568)
    }

    fun varrock_2(p: Player, c: Int) = balloonScreen(p, c) {
        models(40 to 19742, 45 to 19732, 50 to 19733, 55 to 19734)
        models(119 to 19570, 98 to 19569, 79 to 19568)
        models(120 to 19570, 100 to 19569, 80 to 19568)
        models(101 to 19521, 81 to 19519)
        eagles(141,199,225,230)
        models(198 to 19526)
        models(201 to 19525, 202 to 19526)
        // Tower
        models(83 to 19729, 84 to 19729, 85 to 19729, 86 to 19729, 87 to 19729, 88 to 19729, 89 to 19729, 90 to 19729, 91 to 19729, 92 to 19729, 93 to 19729, 94 to 19729)
        models(103 to 19729, 104 to 19729, 105 to 19729, 106 to 19729, 107 to 19729, 108 to 19729, 109 to 19729, 110 to 19729, 111 to 19729, 112 to 19729, 113 to 19729, 114 to 19729)
        models(123 to 19729, 124 to 19729, 125 to 19730, 126 to 19729, 127 to 19729, 128 to 19730, 129 to 19729, 130 to 19729, 131 to 19730, 132 to 19729, 133 to 19729, 134 to 19729)
        models(143 to 19724, 144 to 19724, 145 to 19724, 146 to 19724, 147 to 19724, 148 to 19724, 149 to 19724, 150 to 19724, 151 to 19724, 152 to 19724, 153 to 19724, 154 to 19724)
        models(163 to 19725, 164 to 19726, 167 to 19725, 168 to 19726, 171 to 19725, 172 to 19726)
        models(183 to 19727, 184 to 19728, 187 to 19727, 188 to 19728, 191 to 19727, 192 to 19728)
        model(97, 19567)
    }

    /*
     * Grand tree route stages.
     * Author: Edie @psyopcutie
     */

    fun grand_tree_0(p: Player, c: Int) = balloonScreen(p, c) {
        models(40 to 19560, 45 to 19561, 50 to 19562, 55 to 19562)
        model(138, 19572)
        models(118 to 19607, 119 to 19611)
        models(99 to 19608, 98 to 19608, 100 to 19612)
        models(78 to 19609, 79 to 19609, 80 to 19609, 81 to 19613)
        models(201 to 19525, 202 to 19524, 203 to 19526)
        models(186 to 19525, 187 to 19524, 188 to 19526)
        eagles(191, 229, 237, 144, 130)
        models(230 to 19525, 231 to 19524, 232 to 19526)
        models(192 to 19525, 193 to 19524, 194 to 19524, 195 to 19526)
        animated(214, 19781, 373, 2100, 0, 1500)
        animated(85, 19717, 373)
        animated(94, 19717, 373)
        models(97 to 19616, 93 to 19616)
        models(84 to 19615, 104 to 19617)
        models(89 to 19622)
        models(90 to 19615, 110 to 19617)
    }

    fun grand_tree_1(p: Player, c: Int) = balloonScreen(p, c) {
        models(40 to 19634, 45 to 19636, 50 to 19637, 55 to 19638)
        models(79 to 19522)
        models(80 to 19639, 100 to 19640, 120 to 19642, 140 to 19643)
        models(101 to 19521, 81 to 19519)
        models(82 to 19639, 102 to 19640, 122 to 19642, 142 to 19643)
        models(103 to 19521, 83 to 19519)
        models(84 to 19639, 104 to 19640, 124 to 19642, 144 to 19643)
        models(87 to 19639, 107 to 19640, 127 to 19641, 147 to 19642, 167 to 19643)
        models(128 to 19580, 129 to 19578, 130 to 19582, 108 to 19579, 109 to 19576, 110 to 19581, 88 to 19575, 89 to 19574, 90 to 19577)
        models(111 to 19521, 91 to 19519)
        models(92 to 19639, 112 to 19640, 132 to 19641, 152 to 19642, 172 to 19643)
        models(94 to 19599, 114 to 19600, 134 to 19601)
        models(95 to 19599, 115 to 19600, 135 to 19601)
        models(96 to 19523, 116 to 19528, 136 to 19529)
        models(97 to 19523, 117 to 19528, 137 to 19529)
        eagles(219,183,154,196,197,149,150)
        animated(181, 19781, 373, 2100, 0, 1500)
        animated(220, 19781, 373, 2100, 0, 1500)
        models(200 to 19525, 201 to 19526)
        models(203 to 19525, 204 to 19524, 205 to 19526)
        animated(224, 19781, 373, 2100, 0, 1500)
        models(208 to 19525, 209 to 19526)
        animated(228, 19781, 373, 2100, 0, 1500)
        models(191 to 19525, 192 to 19526)
        models(214 to 19525, 215 to 19524, 216 to 19524, 217 to 19526)
    }

    fun grand_tree_2(p: Player, c: Int) = balloonScreen(p, c) {
        // Ground
        models(
            40 to 19713,
            45 to 19714,
            50 to 19715,
            55 to 19716
        )

        // Landing base
        model(97, 19567)

        // Waterfall
        models(
            87 to 19644,
            88 to 19645,
            89 to 19646,
            90 to 19647,
            91 to 19648,
            92 to 19649,
            99 to 19650,
            98 to 19651,
            100 to 19652,
            101 to 19653,
            78 to 19654,
            102 to 19655,
            103 to 19656,
            104 to 19657,
            105 to 19658,
            106 to 19659,
            107 to 19660,
            108 to 19660,
            109 to 19661,
            110 to 19662,
            111 to 19663,
            112 to 19664,
            79 to 19665,
            120 to 19666,
            121 to 19667,
            122 to 19668,
            123 to 19669,
            124 to 19670,
            125 to 19671,
            126 to 19672,
            127 to 19673,
            128 to 19674,
            129 to 19675,
            80 to 19676,
            130 to 19677,
            131 to 19678,
            132 to 19679,
            140 to 19680,
            141 to 19681,
            142 to 19682,
            143 to 19683,
            144 to 19684,
            145 to 19685,
            146 to 19686,
            81 to 19687,
            147 to 19688,
            148 to 19689,
            149 to 19690,
            150 to 19691,
            151 to 19692,
            161 to 19693,
            162 to 19694,
            163 to 19695,
            164 to 19696,
            165 to 19697,
            82 to 19698,
            166 to 19699,
            167 to 19700,
            168 to 19701,
            169 to 19702,
            170 to 19703,
            182 to 19704,
            183 to 19705,
            187 to 19706,
            188 to 19707,
            189 to 19708,
            83 to 19709,
            84 to 19710,
            85 to 19711,
            86 to 19712
        )

        // Clouds
        models(
            174 to 19525,
            198 to 19525,
            212 to 19525,

            199 to 19524,
            213 to 19524,

            175 to 19526,
            200 to 19526,
            214 to 19526
        )
        eagles(178, 225)
    }

    /*
     * Castle wars route stages.
     * Author: Edie @psyopcutie
     */

    fun castle_wars_0(p: Player, c: Int) = balloonScreen(p, c) {
        // Ground
        models(
            40 to 19583,
            45 to 19588,
            50 to 19588,
            55 to 19589
        )

        // Launch base
        model(78, 19572)

        // Trees
        models(
            80 to 19568,
            100 to 19569,
            120 to 19570
        )

        // Rocks
        models(
            83 to 19616,
            84 to 19616,
            93 to 19616,
            85 to 19615,
            92 to 19615,
            105 to 19617,
            112 to 19617
        )

        // Shark
        model(97, 19717)

        // Clouds
        models(
            227 to 19525, 181 to 19525, 164 to 19525, 169 to 19525, 212 to 19525, 175 to 19525,
            228 to 19524, 182 to 19524, 165 to 19524, 213 to 19524, 176 to 19524,
            229 to 19526, 183 to 19526, 166 to 19526, 170 to 19526, 214 to 19526, 177 to 19526
        )

        eagles(140, 147, 207, 130, 193, 136)
    }

    fun castle_wars_1(p: Player, c: Int) = balloonScreen(p, c) {
        // Ground
        models(
            40 to 19590,
            45 to 19591,
            50 to 19592,
            55 to 19593
        )

        // Trees
        models(
            95 to 19568,
            115 to 19569,
            135 to 19570,

            87 to 19519,
            88 to 19519,

            107 to 19521,
            108 to 19521,

            89 to 19575,
            90 to 19574,
            91 to 19577,

            109 to 19579,
            110 to 19576,
            111 to 19581,

            129 to 19580,
            130 to 19578,
            131 to 19582,

            92 to 19599,
            96 to 19599,

            112 to 19600,
            116 to 19600,

            132 to 19601,
            136 to 19601,

            93 to 19639,
            113 to 19640,
            133 to 19641,
            153 to 19642,
            173 to 19643
        )

        // Rocks
        models(
            85 to 19616,
            86 to 19616
        )

        // Shark
        model(79, 19717)

        // Clouds
        models(
            140 to 19525,
            180 to 19525,
            189 to 19525,
            203 to 19525,
            215 to 19525,
            228 to 19525,

            141 to 19524,
            158 to 19524,
            190 to 19524,
            204 to 19524,
            216 to 19524,
            229 to 19524,
            230 to 19524,

            142 to 19526,
            159 to 19526,
            181 to 19526,
            191 to 19526,
            205 to 19526,
            217 to 19526,
            231 to 19526
        )
        animated(152, 19781, 373)
        animated(156, 19781, 373)

        // Birds
        eagles(118, 197)
    }

    fun castle_wars_2(p: Player, c: Int) = balloonScreen(p, c) {
        // Ground
        models(
            40 to 19594,
            45 to 19584,
            50 to 19585,
            55 to 19586
        )

        // Landing base
        model(97, 19567)

        // Trees
        models(
            78 to 19599,
            86 to 19599,
            87 to 19599,

            99 to 19600,
            106 to 19600,
            107 to 19600,

            118 to 19601,
            126 to 19601,
            127 to 19601,

            80 to 19568,
            93 to 19568,

            100 to 19569,
            113 to 19569,

            120 to 19570,
            133 to 19570,

            83 to 19575,
            84 to 19574,
            85 to 19577,

            103 to 19579,
            104 to 19576,
            105 to 19581,

            123 to 19580,
            124 to 19578,
            125 to 19582,

            92 to 19519,
            112 to 19521
        )

        // Clouds
        models(
            170 to 19525,
            183 to 19525,
            208 to 19525,
            215 to 19525,
            222 to 19525,

            171 to 19524,
            184 to 19524,
            185 to 19524,
            198 to 19524,
            209 to 19524,
            210 to 19524,
            216 to 19524,

            172 to 19526,
            186 to 19526,
            199 to 19526,
            211 to 19526,
            217 to 19526,
            223 to 19526
        )

        // Birds
        eagles(140, 153, 179)

        animated(138, 19781, 373)
        animated(147, 19781, 373)
     }
}