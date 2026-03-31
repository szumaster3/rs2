package content.region.misthalin.lumbridge.plugin.gnomecopter.data.impl

import content.region.misthalin.lumbridge.plugin.gnomecopter.data.PagedInformation

enum class LletyaInformationTab(
    vararg info: String,
) {
    UNKNOWN_PAGE("");

    val lines = info.toList()

    companion object : PagedInformation {
        override val pages: List<List<String>> =
            values().map { it.lines }
    }
}