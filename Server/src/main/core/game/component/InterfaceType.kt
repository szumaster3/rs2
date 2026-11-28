package core.game.component

import shared.consts.Components

/**
 * Represents interface types for different components.
 */
enum class InterfaceType(val fixedPaneId: Int, val resizablePaneId: Int, val fixedChildId: Int, val resizableChildId: Int) {
    // The resizable modal is sized by CS2 script722 (746:8).
    DEFAULT(Components.TOPLEVEL_548, Components.TOPLEVEL_FULLSCREEN_746, 16, 8),
    OVERLAY(Components.TOPLEVEL_548, Components.TOPLEVEL_FULLSCREEN_746, 6, 5),
    TAB(Components.TOPLEVEL_548, Components.TOPLEVEL_FULLSCREEN_746, 152, 33),
    SINGLE_TAB(Components.TOPLEVEL_548, Components.TOPLEVEL_FULLSCREEN_746, 147, 30),
    DIALOGUE(Components.CHATTOP_752, Components.CHATTOP_752, 12, 12),
    WINDOW_PANE(Components.TOPLEVEL_548, Components.TOPLEVEL_FULLSCREEN_746, 0, 0),
    CS_CHATBOX(Components.CHATTOP_752, Components.CHATTOP_752, 6, 6),
    CHATBOX(Components.CHATTOP_752, Components.CHATTOP_752, 9, 9),
    OVERLAY_B(Components.TOPLEVEL_548, Components.TOPLEVEL_FULLSCREEN_746, 16, 8),
    FULL_CHATBOX(Components.CHATTOP_752, Components.CHATTOP_752, 13, 13),
    VIEWPORT(Components.TOPLEVEL_548, Components.TOPLEVEL_FULLSCREEN_746, 5, 6),
}
