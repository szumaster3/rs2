package core.net

import content.global.plugins.iface.ge.GEInterface
import content.global.plugins.iface.ge.StockMarket
import core.game.interaction.InterfaceOperation
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class InterfaceRegressionTests {
    @Test
    fun collectionBoxUsesEnum1080InsteadOfArithmetic() {
        listOf(18, 23, 28, 36, 44, 52).forEachIndexed { index, child ->
            assertEquals(index, GEInterface.collectionOfferIndex(child))
        }
        assertNull(GEInterface.collectionOfferIndex(19))
    }

    @Test
    fun collectMenuMatchesNativeDynamicOptionOrder() {
        assertEquals(false, StockMarket.collectAsNote(true, 1, InterfaceOperation.OP1))
        assertEquals(true, StockMarket.collectAsNote(true, 1, InterfaceOperation.OP2))
        assertEquals(true, StockMarket.collectAsNote(true, 2, InterfaceOperation.OP1))
        assertEquals(false, StockMarket.collectAsNote(true, 2, InterfaceOperation.OP2))
        assertEquals(false, StockMarket.collectAsNote(false, 2, InterfaceOperation.OP1))
        assertNull(StockMarket.collectAsNote(false, 2, InterfaceOperation.OP2))
        assertNull(StockMarket.collectAsNote(true, 1, InterfaceOperation.OP3))
        assertNull(StockMarket.collectAsNote(true, 0, InterfaceOperation.OP1))
    }
}
