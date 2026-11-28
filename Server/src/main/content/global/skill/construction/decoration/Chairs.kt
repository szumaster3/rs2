package content.global.skill.construction.decoration

import content.global.skill.construction.Decoration
import shared.consts.Animations

/**
 * Enum representing all chairs, benches, and thrones in the player-owned house (POH).
 *
 * Entry:
 *  - [objectId]: the id of the object.
 *  - [anim]: the animation id when the player starts sitting down.
 *  - [sitAnim]: the animation id for the final seated position.
 */
enum class Chairs(
    val objectId: Int,
    val anim: Int,
    val sitAnim: Int
) {
    CRUDE_CHAIR(Decoration.CRUDE_CHAIR.objectId, Animations.CHAIR_SIT_READY_CHAIR1_4073, Animations.CHAIR_SIT_4103),
    WOODEN_CHAIR(Decoration.WOODEN_CHAIR.objectId, Animations.CHAIR_SIT_READY_CHAIR2_4075, Animations.CHAIR_SIT_4103),
    ROCKING_CHAIR(Decoration.ROCKING_CHAIR.objectId, Animations.CHAIR_SIT_READY_ROCKING_CHAIR3_4079, Animations.CHAIR_SIT_4103),
    OAK_CHAIR(Decoration.OAK_CHAIR.objectId, Animations.CHAIR_SIT_READY_CHAIR4_4081, Animations.CHAIR_SIT_4103),
    OAK_ARMCHAIR(Decoration.OAK_ARMCHAIR.objectId, Animations.CHAIR_SIT_READY_CHAIR5_4083, Animations.CHAIR_SIT_4103),
    TEAK_ARMCHAIR(Decoration.TEAK_ARMCHAIR.objectId, Animations.CHAIR_SIT_READY_CHAIR6_4085, Animations.CHAIR_SIT_4103),
    MAHOGANY_ARMCHAIR(Decoration.MAHOGANY_ARMCHAIR.objectId, Animations.CHAIR_SIT_READY_CHAIR7_4087, Animations.CHAIR_SIT_4103),

    BENCH_WOODEN(Decoration.BENCH_WOODEN.objectId, Animations.CHAIR_SIT_READY_DININGCHAIRS_1_4089, Animations.CHAIR_SIT_DIAGONAL_RIGHT_4104),
    BENCH_OAK(Decoration.BENCH_OAK.objectId, Animations.CHAIR_SIT_READY_DININGCHAIRS_2_4091, Animations.CHAIR_SIT_DIAGONAL_RIGHT_4104),
    BENCH_CARVED_OAK(Decoration.BENCH_CARVED_OAK.objectId, Animations.CHAIR_SIT_READY_DININGCHAIRS_3_4093, Animations.CHAIR_SIT_DIAGONAL_RIGHT_4104),
    BENCH_TEAK(Decoration.BENCH_TEAK.objectId, Animations.CHAIR_SIT_READY_DININGCHAIRS_4_4095, Animations.CHAIR_SIT_DIAGONAL_RIGHT_4104),
    BENCH_CARVED_TEAK(Decoration.BENCH_CARVED_TEAK.objectId, Animations.CHAIR_SIT_READY_DININGCHAIRS_5_4097, Animations.CHAIR_SIT_DIAGONAL_RIGHT_4104),
    BENCH_MAHOGANY(Decoration.BENCH_MAHOGANY.objectId, Animations.CHAIR_SIT_READY_DININGCHAIRS_6_4099, Animations.CHAIR_SIT_DIAGONAL_RIGHT_4104),
    BENCH_GILDED(Decoration.BENCH_GILDED.objectId, Animations.CHAIR_SIT_READY_DININGCHAIRS_7_4101, Animations.CHAIR_SIT_DIAGONAL_RIGHT_4104),

    CARVED_TEAK_BENCH(Decoration.CARVED_TEAK_BENCH.objectId, Animations.CHAIR_SIT_READY_DININGCHAIRS_5_4097, Animations.CHAIR_SIT_DIAGONAL_RIGHT_4104),
    MAHOGANY_BENCH(Decoration.MAHOGANY_BENCH.objectId, Animations.CHAIR_SIT_READY_DININGCHAIRS_6_4099, Animations.CHAIR_SIT_DIAGONAL_RIGHT_4104),
    GILDED_BENCH(Decoration.GILDED_BENCH.objectId, Animations.CHAIR_SIT_READY_DININGCHAIRS_7_4101, Animations.CHAIR_SIT_DIAGONAL_RIGHT_4104),

    OAK_THRONE(Decoration.OAK_THRONE.objectId, Animations.CHAIR_SIT_READY_THRONE_1_4111, Animations.CHAIR_SIT_4103),
    TEAK_THRONE(Decoration.TEAK_THRONE.objectId, Animations.CHAIR_SIT_READY_THRONE_2_4112, Animations.CHAIR_SIT_4103),
    MAHOGANY_THRONE(Decoration.MAHOGANY_THRONE.objectId, Animations.CHAIR_SIT_READY_THRONE_3_4113, Animations.CHAIR_SIT_4103),
    GILDED_THRONE(Decoration.GILDED_THRONE.objectId, Animations.CHAIR_SIT_READY_THRONE_4_4114, Animations.CHAIR_SIT_4103),
    SKELETON_THRONE(Decoration.SKELETON_THRONE.objectId, Animations.CHAIR_SIT_READY_THRONE_5_4115, Animations.CHAIR_SIT_4103),
    CRYSTAL_THRONE(Decoration.CRYSTAL_THRONE.objectId, Animations.CHAIR_SIT_READY_THRONE_6_4116, Animations.CHAIR_SIT_4103),
    DEMONIC_THRONE(Decoration.DEMONIC_THRONE.objectId, Animations.CHAIR_SIT_READY_THRONE_7_4117, Animations.CHAIR_SIT_4103);

    companion object {
        private val map = values().associateBy { it.objectId }

        /**
         * Gets the [Chairs] entry for the given object id.
         */
        fun fromId(id: Int) = map[id]
    }
}