package content.global.plugins.item.books_and_scrolls.impl.journal

import content.global.plugins.item.books_and_scrolls.BookInterface
import content.global.plugins.item.books_and_scrolls.BookLine
import content.global.plugins.item.books_and_scrolls.Page
import content.global.plugins.item.books_and_scrolls.PageSet
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.player.Player
import core.game.world.GameWorld.settings
import shared.consts.Items

class GloughsJournal : InteractionListener {
    /*
     * Glough's journal is found during The Grand Tree quest and looks
     * a little deeper into the mind and motives of Glough the gnome.
     * It's used as a key piece of evidence as to what he's planning.
     *
     * Sources: https://youtu.be/-jfueZpRhzU?si=Knp0BnmPR3wz9Y2Q&t=492
     */

    companion object {
        private const val TITLE = "Journal"
        private val CONTENTS =
            arrayOf(
                PageSet(
                    Page(
                        BookLine("<col=FF2D00>The migration failed!</col>", 55),
                        BookLine("After spending half a", 56),
                        BookLine("century hiding", 57),
                        BookLine("underground you would", 58),
                        BookLine("think that the great", 59),
                        BookLine("migration would have", 60),
                        BookLine("improved life on", 61),
                        BookLine("" + settings!!.name + "for tree", 62),
                        BookLine("gnomes. However, rather", 63),
                        BookLine("than the great liberation", 64),
                    ),
                    Page(
                        BookLine("promised to us by King", 66),
                        BookLine("Healthorg at the end of", 67),
                        BookLine("the last age, we have been", 68),
                        BookLine("forced to live in hiding,", 69),
                        BookLine("up trees or in the gnome", 70),
                        BookLine("maze, laughed at and", 71),
                        BookLine("mocked by man. Living in", 72),
                        BookLine("constant fear of human", 73),
                        BookLine("aggression, we are in a", 74),
                        BookLine("no better situation now", 75),
                        BookLine("than when we lived in the", 76),

                        ),
                ),
                PageSet(
                    Page(
                        BookLine("caves! Change must come", 55),
                        BookLine("soon!", 56),
                        BookLine("", 57),
                        BookLine("<col=FF2D00>They must be stopped!</col>", 58),
                        BookLine("", 59),
                        BookLine("Today I heard of three", 60),
                        BookLine("more gnomes slain by", 61),
                        BookLine("Khazard's human troops", 62),
                        BookLine("for fun, I can't control", 63),
                        BookLine("my anger! Humanity", 64),
                        BookLine("seems to have acquired a", 65),
                    ),
                    Page(
                        BookLine("level of arrogance", 66),
                        BookLine("comparable to that of", 67),
                        BookLine("Zamorak, killing and", 68),
                        BookLine("pillaging at will! We are", 69),
                        BookLine("small and at heart not", 70),
                        BookLine("warriors but something", 71),
                        BookLine("must be done! We will", 72),
                        BookLine("pick up arms and go", 73),
                        BookLine("forth into the human", 74),
                        BookLine("world! We will defend", 75),
                        BookLine("ourselves and we will", 76)
                    ),
                ),
                PageSet(
                    Page(
                        BookLine("pursue justice for all", 55),
                        BookLine("gnomes who fell at the", 56),
                        BookLine("hands of humans!", 57),
                        BookLine("", 58),
                        BookLine("<col=FF2D00>Gaining support.</col>", 59),
                        BookLine("", 60),
                        BookLine("Some of the local gnomes", 61),
                        BookLine("seem strangely deluded", 62),
                        BookLine("about humans, many", 63),
                        BookLine("actually believe that", 64),
                        BookLine("humans are not all", 65),
                    ),
                    Page(
                        BookLine("naturally evil but instead", 66),
                        BookLine("vary from person to", 67),
                        BookLine("person. This sort of talk", 68),
                        BookLine("could be the end for the", 69),
                        BookLine("tree gnomes and I must", 70),
                        BookLine("continue to convince my", 71),
                        BookLine("fellow gnome folk the cold", 72),
                        BookLine("truth about these human", 73),
                        BookLine("creatures! How they will", 74),
                        BookLine("not stop until all gnome", 75),
                        BookLine("life is destroyed! Unless", 76),
                    ),
                ),
                PageSet(
                    Page(
                        BookLine("we can destroy them", 55),
                        BookLine("first!", 56),
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
            return true
        }
    }

    override fun defineListeners() {
        on(Items.GLOUGHS_JOURNAL_785, IntType.ITEM, "read") { player, _ ->
            BookInterface.openBook(player, BookInterface.FANCY_BOOK_3_49, Companion::display)
            return@on true
        }
    }
}
