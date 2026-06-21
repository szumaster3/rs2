package content.global.plugins.item.books_and_scrolls.impl

import content.global.plugins.item.books_and_scrolls.BookInterface
import content.global.plugins.item.books_and_scrolls.BookLine
import content.global.plugins.item.books_and_scrolls.Page
import content.global.plugins.item.books_and_scrolls.PageSet
import core.api.getQuestStage
import core.api.setQuestStage
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.player.Player
import core.tools.colorize
import shared.consts.Items
import shared.consts.Quests

class BaxtorianBook : InteractionListener {

    // Can be found in the Tourist Information Centre
    // just south of the Baxtorian Falls.

    companion object {
        private const val TITLE = "Book on Baxtorian"
        private val CONTENTS =
            arrayOf(
                //https://youtu.be/-4tWcoRGRjY?si=CVYqSGVhru--139B&t=155
                PageSet(
                    Page(
                        BookLine(colorize("%DRThe missing relics"), 55),
                        BookLine("Many artefacts of elven", 57),
                        BookLine("history were lost after the", 58),
                        BookLine("fourth age, following the", 59),
                        BookLine("departure of the elven", 60),
                        BookLine("colonies from these lands.", 61),
                        BookLine("The greatest loss to our", 62),
                        BookLine("collections of elf history", 63),
                        BookLine("were the hidden treasures", 64),
                        BookLine("of Baxtorian, specifically", 65),
                    ),
                    Page(
                        BookLine("the rumoured Chalice of", 66),
                        BookLine("Eternity. Some believe", 67),
                        BookLine("these treasures are still", 68),
                        BookLine("unclaimed, but it is more", 69),
                        BookLine("commonly believed that", 70),
                        BookLine("dwarf miners recovered", 71),
                        BookLine("the treasure early in the", 72),
                        BookLine("5th Age.", 73),
                        BookLine("Another great loss was", 75),
                        BookLine("Glarial's pebble, a key", 76),
                    ),
                ),
                //https://youtu.be/-4tWcoRGRjY?si=HmNPYUEHqTktXy4K&t=156
                PageSet(
                    Page(
                        BookLine("which allowed her family", 55),
                        BookLine("to visit her tomb.", 56),
                        BookLine("The pebble was taken by", 57),
                        BookLine("a gnome many years", 58),
                        BookLine("ago. It is hoped that", 59),
                        BookLine("descendents of that", 60),
                        BookLine("gnome may still have the", 61),
                        BookLine("pebble hidden in their", 62),
                        BookLine("cave under the Tree", 63),
                        BookLine("Gnome Village.", 64),
                    ),
                    Page(
                        BookLine("Unfortunately the maze", 66),
                        BookLine("around that village makes", 67),
                        BookLine("it difficult to contact the", 68),
                        BookLine("gnomes to investigate this", 69),
                        BookLine("matter.", 70),
                    ),
                ),
                //https://youtu.be/-4tWcoRGRjY?si=rJkwIXeMeMMXUadW&t=157
                PageSet(
                    Page(
                        BookLine(colorize("%DRThe fall of Baxtorian"), 55),
                        BookLine("The love between", 57),
                        BookLine("Baxtorian and Glarial was", 58),
                        BookLine("said to have lasted over a", 59),
                        BookLine("century. They lived a", 60),
                        BookLine("peaceful life learning and", 61),
                        BookLine("teaching the laws of", 62),
                        BookLine("nature.", 63),
                        BookLine("When their homeland in", 65),
                    ),

                    Page(
                        BookLine("the far west was plunged", 66),
                        BookLine("into chaos by dark forces,", 67),
                        BookLine("Baxtorian left on a", 68),
                        BookLine("dangerous campaign that", 69),
                        BookLine("lasted for five years. He", 70),
                        BookLine("survived to return to this", 71),
                        BookLine("land, but found his people", 72),
                        BookLine("slaughtered and his wife", 73),
                        BookLine("taken by the enemy.", 74),
                        BookLine("After years of searching", 76),
                    ),
                ),
                PageSet(
                    Page(
                        BookLine("for his love he finally", 55),
                        BookLine("gave up and returned to", 56),
                        BookLine("the home he made for", 57),
                        BookLine("Glarial under the", 58),
                        BookLine("Baxtorian Waterfall.", 59),
                        BookLine("Once he entered he", 60),
                        BookLine("never returned.", 61),
                        BookLine("Only he and Glarial had", 63),
                        BookLine("the power to enter the", 64),
                        BookLine("waterfall. Since Baxtorian", 65),
                    ),
                    Page(
                        BookLine("entered, no-one else can", 66),
                        BookLine("follow him in, it's as if the", 67),
                        BookLine("powers of nature still", 68),
                        BookLine("work to protect his peace.", 69),
                    ),
                ),
                PageSet(
                    Page(
                        BookLine(colorize("%DRThe power of nature"), 55),
                        BookLine("Glarial and Baxtorian", 57),
                        BookLine("were masters of nature.", 58),
                        BookLine("Trees and flowers would", 59),
                        BookLine("grow, hills form and", 60),
                        BookLine("rivers flood on their", 61),
                        BookLine("command.", 62),
                        BookLine("Baxtorian in particular", 64),
                        BookLine("had perfected rune lore.", 65),
                    ),
                    Page(
                        BookLine("It was said he could", 66),
                        BookLine("use the stones to control", 67),
                        BookLine("water, earth and air.", 68),
                    ),
                ),
                //https://youtu.be/-4tWcoRGRjY?si=6ia5vUlEuyBPkkLw&t=158
                PageSet(
                    Page(
                        BookLine(colorize("%DROde to Eternity"), 55),
                        BookLine("(A short piece written by", 57),
                        BookLine("Baxtorian himself.)", 58),
                        BookLine("What care I for this", 60),
                        BookLine("mortal coil,", 61),
                        BookLine("where treasures are yet", 62),
                        BookLine("so frail,", 63),
                        BookLine("for it is you that is my", 64),
                        BookLine("life blood,", 65),
                    ),
                    Page(
                        BookLine("the wine to my holy grail", 66),
                        BookLine("and if I see the", 67),
                        BookLine("judgement day,", 68),
                        BookLine("when the gods fill the air", 69),
                        BookLine("with dust,", 70),
                        BookLine("I'll happily choke on your", 71),
                        BookLine("memory,", 72),
                        BookLine("as my kingdom turns to", 73),
                        BookLine("rust.", 74),
                    ),
                ),
            )

        @Suppress("UNUSED_PARAMETER")
        private fun display(
            player: Player,
            pageNum: Int,
            buttonID: Int,
        ): Boolean {
            BookInterface.pageSetup(player, BookInterface.FANCY_BOOK_3_49, TITLE, CONTENTS)
            if (getQuestStage(player, Quests.WATERFALL_QUEST) == 20) {
                setQuestStage(player, Quests.WATERFALL_QUEST, 30)
            }
            return true
        }
    }

    override fun defineListeners() {
        on(Items.BOOK_ON_BAXTORIAN_292, IntType.ITEM, "read") { player, _ ->
            BookInterface.openBook(player, BookInterface.FANCY_BOOK_3_49, Companion::display)
            return@on true
        }
    }
}
