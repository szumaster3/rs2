package content.region.kandarin.camelot.ctr

import shared.consts.NPCs

/**
 * Represents the Knight's Training npc.
 */
enum class WaveTier(val id: Int) {
    I(NPCs.SIR_BEDIVERE_6177),
    II(NPCs.SIR_PELLEAS_6176),
    III(NPCs.SIR_TRISTRAM_6175),
    IV(NPCs.SIR_PALOMEDES_1883),
    V(NPCs.SIR_LUCAN_6173),
    VI(NPCs.SIR_GAWAIN_6172),
    VII(NPCs.SIR_KAY_6171),
    VIII(NPCs.SIR_LANCELOT_6170),
    IX(-1);

    fun next(): WaveTier? =
        values().getOrNull(ordinal + 1)

    companion object {
        fun forId(id: Int): WaveTier? =
            values().find { it.id == id }
    }
}