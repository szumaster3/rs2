package content.region.kandarin.gnome_stronghold.quest.makinghistory.book

import content.global.plugins.item.books_and_scrolls.BookInterface
import content.global.plugins.item.books_and_scrolls.BookLine
import content.global.plugins.item.books_and_scrolls.Page
import content.global.plugins.item.books_and_scrolls.PageSet
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.player.Player
import shared.consts.Items

class DrozalJournal : InteractionListener {

    // Sources: https://youtu.be/aki67Pfh42A?si=hGgvfJy76POA4mTs&t=98

    companion object {
        private val TITLE = "Drozal's Journal"
        private val CONTENTS = arrayOf(
            PageSet(
                Page(
                    BookLine("----------------------", 55),
                    BookLine("---1st Bennath", 56),
                    BookLine("----------------------", 57),
                    BookLine("My life is full of changes", 58),
                    BookLine("at the moment. I've met", 59),
                    BookLine("a great and powerful", 60),
                    BookLine("follower of Zamorak,", 61),
                    BookLine("whom has offered me the", 62),
                    BookLine("chance to join him and", 63),
                    BookLine("others in the quest for", 64),
                    BookLine("ultimate power and", 65),
                ),
                Page(
                    BookLine("desolation of others. This", 66),
                    BookLine("is just the opportunity", 67),
                    BookLine("I've been looking for! I", 68),
                    BookLine("move into the old outpost", 69),
                    BookLine("(North of Ardougne) at", 70),
                    BookLine("the end of this week.", 71),
                    BookLine("I can hardly wait!", 73),
                ),
            ),
            PageSet(
                Page(
                    BookLine("----------------------", 55),
                    BookLine("---15th Bennath", 56),
                    BookLine("----------------------", 57),
                    BookLine("Been here a few days", 58),
                    BookLine("now. I'm overwhelmed on", 59),
                    BookLine("just how evil the other 8", 60),
                    BookLine("are. I suppose this is", 61),
                    BookLine("amplified by living in such", 62),
                    BookLine("a small building.", 63),
                ),
                Page(
                    BookLine("----------------------", 66),
                    BookLine("---20th Bennath", 67),
                    BookLine("----------------------", 68),
                    BookLine("Started causing havoc to", 69),
                    BookLine("the people of Ardougne", 70),
                    BookLine("today. We poisoned the", 71),
                    BookLine("water supply with a", 72),
                    BookLine("strange concoction which", 73),
                    BookLine("caused everyone to break", 74),
                    BookLine("out in boils! Very funny.", 75),
                    BookLine("The others have been", 76),
                ),
            ),
            PageSet(
                Page(
                    BookLine("stealthily setting fire to", 55),
                    BookLine("some people and laughing", 56),
                    BookLine("at their conclusion of", 57),
                    BookLine("spontaneous combustion.", 58),
                    BookLine("I think I shall have to", 60),
                    BookLine("come up with something", 60),
                    BookLine("more evil for tomorrow.", 61),
                ),
                Page(
                    BookLine("----------------------", 66),
                    BookLine("---32nd Bennath", 67),
                    BookLine("----------------------", 68),
                    BookLine("Following my genius plan", 69),
                    BookLine("to make all the children", 70),
                    BookLine("invisible, the city is now", 71),
                    BookLine("in total chaos as the kids", 72),
                    BookLine("test their freedom by", 73),
                    BookLine("putting adults into", 74),
                    BookLine("a panic.", 75),
                ),
            ),
            PageSet(
                Page(
                    BookLine("Honestly, some of the", 55),
                    BookLine("tricks the kids play are", 56),
                    BookLine("better than anything I", 57),
                    BookLine("could invent.", 58),
                ),
                Page(
                    BookLine("----------------------", 66),
                    BookLine("---20th Raktuber", 67),
                    BookLine("----------------------", 68),
                    BookLine("Sadly things have calmed", 69),
                    BookLine("down in the city, and", 70),
                    BookLine("they've started to realise", 71),
                    BookLine("that we're the cause of all", 72),
                    BookLine("the tragedy. Luckily, the", 73),
                    BookLine("people don't have enough", 74),
                    BookLine("power to get past our", 75),
                    BookLine("defences, and even if", 76),
                ),
            ),
            PageSet(
                Page(
                    BookLine("they did, they would wish", 55),
                    BookLine("they hadn't thought of", 56),
                    BookLine("such an attack.", 57),
                ), Page(
                    BookLine("----------------------", 66),
                    BookLine("---28th Raktuber", 67),
                    BookLine("----------------------", 68),
                    BookLine("We have been told to", 69),
                    BookLine("'beware for your days", 70),
                    BookLine("are numbered' by the", 71),
                    BookLine("people of Ardougne. It", 72),
                    BookLine("seems the city has asked", 73),
                    BookLine("for some external help,", 74),
                    BookLine("which will be upon us", 75),
                    BookLine("some time soon. I'm sure", 76),
                )
            ),
            PageSet(
                Page(
                    BookLine("we will eliminate such a", 55),
                    BookLine("threat though.", 56),
                ),
            )
        )
    }

    @Suppress("UNUSED_PARAMETER")
    private fun display(
        player: Player,
        pageNum: Int,
        buttonID: Int,
    ): Boolean {
        BookInterface.pageSetup(player, BookInterface.FANCY_BOOK_3_49, TITLE, CONTENTS)
        return true
    }

    override fun defineListeners() {
        on(Items.JOURNAL_6755, IntType.ITEM, "read") { player, _ ->
            BookInterface.openBook(player, BookInterface.FANCY_BOOK_3_49, ::display)
            return@on true
        }
    }
}
