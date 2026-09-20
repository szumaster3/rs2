package content.region.wilderness.plugin

import shared.consts.Items

/**
 * Represents a Wilderness capes.
 */
enum class WildernessCape(
    val itemIds: IntArray,
) {
    BOX(intArrayOf(Items.TEAM_1_CAPE_4315, Items.TEAM_11_CAPE_4335, Items.TEAM_21_CAPE_4355, Items.TEAM_31_CAPE_4375, Items.TEAM_41_CAPE_4395)),
    X(intArrayOf(Items.TEAM_2_CAPE_4317, Items.TEAM_12_CAPE_4337, Items.TEAM_22_CAPE_4357, Items.TEAM_32_CAPE_4377, Items.TEAM_42_CAPE_4397)),
    CROSS(intArrayOf(Items.TEAM_3_CAPE_4319, Items.TEAM_13_CAPE_4339, Items.TEAM_23_CAPE_4359, Items.TEAM_33_CAPE_4379, Items.TEAM_43_CAPE_4399)),
    DASHED_LINES(intArrayOf(Items.TEAM_4_CAPE_4321, Items.TEAM_14_CAPE_4341, Items.TEAM_24_CAPE_4361, Items.TEAM_34_CAPE_4381, Items.TEAM_44_CAPE_4401)),
    HORIZONTAL_STRIPES(intArrayOf(Items.TEAM_5_CAPE_4323, Items.TEAM_15_CAPE_4343, Items.TEAM_25_CAPE_4363, Items.TEAM_35_CAPE_4383, Items.TEAM_45_CAPE_4403)),
    BOX_AT_TOP(intArrayOf(Items.TEAM_6_CAPE_4325, Items.TEAM_16_CAPE_4345, Items.TEAM_26_CAPE_4365, Items.TEAM_36_CAPE_4385, Items.TEAM_46_CAPE_4405)),
    SCARAB(intArrayOf(Items.TEAM_7_CAPE_4327, Items.TEAM_17_CAPE_4347, Items.TEAM_27_CAPE_4367, Items.TEAM_37_CAPE_4387, Items.TEAM_47_CAPE_4407)),
    CIRCLE(intArrayOf(Items.TEAM_8_CAPE_4329, Items.TEAM_18_CAPE_4349, Items.TEAM_28_CAPE_4369, Items.TEAM_38_CAPE_4389, Items.TEAM_48_CAPE_4409)),
    OUTLINE(intArrayOf(Items.TEAM_9_CAPE_4331, Items.TEAM_19_CAPE_4351, Items.TEAM_29_CAPE_4371, Items.TEAM_39_CAPE_4391, Items.TEAM_49_CAPE_4411)),
    CLAW(intArrayOf(Items.TEAM_10_CAPE_4333, Items.TEAM_20_CAPE_4353, Items.TEAM_30_CAPE_4373, Items.TEAM_40_CAPE_4393, Items.TEAM_50_CAPE_4413));

    companion object
    {
        /**
         * Represents every wilderness cape item id.
         */
        val allItemIds = values().flatMap { it.itemIds.toList() }.toIntArray()
    }
}