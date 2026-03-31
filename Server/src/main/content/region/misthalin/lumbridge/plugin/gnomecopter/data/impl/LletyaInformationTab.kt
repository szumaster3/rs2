package content.region.misthalin.lumbridge.plugin.gnomecopter.data.impl

import content.region.misthalin.lumbridge.plugin.gnomecopter.data.PagedInformation
import core.tools.DARK_BLUE
import core.tools.DARK_RED

enum class LletyaInformationTab(
    vararg info: String,
) {
    FIRST_PAGE("");

    val lines = info.toList()

    companion object : PagedInformation {
        override val pages: List<List<String>> =
            BurghDeRottInformationTab.values().map { it.lines }
    }
}