package core.net.packet;

/** Revision 578 gameplay wire IDs and frame sizes. Client symbols are recorded next to
 * descriptive names; these names are not claims about original Jagex identifiers.
 * Login, JS5 and management-server protocols are separate namespaces.
 */
public final class ClientProt {
    private ClientProt() {}
    public static final int COUNT = 82;
    public static final int OPOBJ_EXAMINE = 0; // Client: PACKET_0
    public static final int MESSAGE_PRIVATE = 1; // Client: MESSAGE_PRIVATE
    public static final int IF_BUTTON3 = 2; // Client: PACKET_2
    public static final int OPLOC2 = 3; // Client: PACKET_3
    public static final int IF_BUTTON2 = 4; // Client: PACKET_4
    public static final int OPOBJ2 = 5; // Client: PACKET_5
    public static final int IF_ON_GROUNDITEM = 6; // Client: PACKET_6
    public static final int EVENT_CAMERA_POSITION = 7; // Client: EVENT_CAMERA_POSITION
    public static final int WINDOW_STATUS = 8; // Client: WINDOW_STATUS
    public static final int IF_ON_LOC = 9; // Client: PACKET_9
    public static final int IF_BUTTON7 = 10; // Client: PACKET_10
    public static final int IF_ON_IF = 11; // Client: PACKET_11
    public static final int IF_BUTTON1 = 12; // Client: IF_BUTTON
    public static final int IF_DRAG = 13; // Client: PACKET_13
    public static final int IF_BUTTON10 = 14; // Client: PACKET_14
    public static final int CLAN_KICK = 15; // Client: PACKET_15
    public static final int IGNORE_ADD = 16; // Client: PACKET_16
    public static final int IF_BUTTON8 = 17; // Client: PACKET_17
    public static final int FRIEND_ADD = 18; // Client: PACKET_18
    public static final int MESSAGE_QUICKCHAT_PUBLIC = 19; // Client: MESSAGE_QUICKCHAT_PUBLIC
    public static final int SOUND_SONGEND = 20; // Client: SOUND_SONGEND
    public static final int IF_BUTTON_CONTINUE = 21; // Client: PACKET_21
    public static final int OPOBJ1 = 22; // Client: PACKET_22
    public static final int EVENT_KEYBOARD = 23; // Client: EVENT_KEYBOARD
    public static final int IGNORE_REMOVE = 24; // Client: PACKET_24
    public static final int TRANSMITVAR_VERIFYID = 25; // Client: TRANSMITVAR_VERIFYID
    public static final int OPOBJ5 = 26; // Client: PACKET_26
    public static final int OPPLAYER7 = 27; // Client: PACKET_27
    public static final int OPNPC1 = 28; // Client: PACKET_28
    public static final int RESUME_P_NAMEDIALOG = 29; // Client: RESUME_P_NAMEDIALOG
    public static final int MESSAGE_QUICKCHAT_PRIVATE = 30; // Client: MESSAGE_QUICKCHAT_PRIVATE
    public static final int OPNPC4 = 31; // Client: PACKET_31
    public static final int OPPLAYER2 = 32; // Client: PACKET_32
    public static final int EVENT_FRAME_MAP_LOADED = 33; // Client: EVENT_FRAME_MAP_LOADED
    public static final int REFLECTION_CHECK = 34; // Client: REFLECTION_CHECK
    public static final int OPPLAYER8 = 35; // Client: PACKET_35
    public static final int OPPLAYER5 = 36; // Client: PACKET_36
    public static final int EVENT_MOUSE_CLICK = 37; // Client: EVENT_MOUSE_CLICK
    public static final int FACE_HERE = 38; // Client: PACKET_38
    public static final int OPPLAYER1 = 39; // Client: PACKET_39
    public static final int OPLOC3 = 40; // Client: PACKET_40
    public static final int OPPLAYER3 = 41; // Client: PACKET_41
    public static final int CLIENT_PREFERENCES = 42; // Client: PACKET_42
    public static final int IF_BUTTON4 = 43; // Client: PACKET_43
    public static final int EVENT_APPLET_FOCUS = 44; // Client: EVENT_APPLET_FOCUS
    public static final int OPPLAYER6 = 45; // Client: PACKET_45
    public static final int OPNPC_EXAMINE = 46; // Client: PACKET_46
    public static final int OPLOC5 = 47; // Client: PACKET_47
    public static final int IF_ON_PLAYER = 48; // Client: PACKET_48
    public static final int FRIEND_SETRANK = 49; // Client: PACKET_49
    public static final int OPNPC2 = 50; // Client: PACKET_50
    public static final int CLOSE_MODAL = 51; // Client: CLOSE_MODAL
    public static final int IF_ON_TILE = 52; // Client: PACKET_52
    public static final int FRIEND_REMOVE = 53; // Client: PACKET_53
    public static final int IF_BUTTON9 = 54; // Client: PACKET_54
    public static final int MAP_BUILD_COMPLETE = 55; // Client: MAP_BUILD_COMPLETE
    public static final int URL_REQUEST = 56; // Client: URL_REQUEST
    public static final int IF_BUTTON6 = 57; // Client: PACKET_57
    public static final int RESUME_P_OBJDIALOG = 58; // Client: RESUME_P_OBJDIALOG
    public static final int MOVE_MINIMAPCLICK = 59; // Client: MOVE_MINIMAPCLICK
    public static final int MESSAGE_PUBLIC = 60; // Client: MESSAGE_PUBLIC
    public static final int EVENT_MOUSE_MOVE = 61; // Client: EVENT_MOUSE_MOVE
    public static final int SET_CHATFILTERSETTINGS = 62; // Client: SET_CHATFILTERSETTINGS
    public static final int IF_BUTTON5 = 63; // Client: PACKET_63
    public static final int CLAN_JOIN_LEAVE = 64; // Client: PACKET_64
    public static final int WORLD_MAP_CLICK = 65; // Client: PACKET_65
    public static final int OPLOC_EXAMINE = 66; // Client: GET_EXAMINE
    public static final int RESUME_P_COUNTDIALOG = 67; // Client: RESUME_P_COUTNDIALOG
    public static final int OPNPC5 = 68; // Client: PACKET_68
    public static final int OPPLAYER4 = 69; // Client: PACKET_69
    public static final int OPNPC3 = 70; // Client: PACKET_70
    public static final int NO_TIMEOUT = 71; // Client: NO_TIMEOUT
    public static final int OPLOC4 = 72; // Client: PACKET_72
    public static final int OPLOC1 = 73; // Client: OPLOC1
    public static final int RESUME_P_STRINGDIALOG = 74; // Client: RESUME_P_STRINGDIALOG
    public static final int IF_ON_NPC = 75; // Client: PACKET_75
    public static final int CLIENT_CHEAT = 76; // Client: CLIENT_CHEAT
    public static final int REPORT_ABUSE = 77; // Client: BUG_REPORT
    public static final int MOVE_GAMECLICK = 78; // Client: MOVE_GAMECLICK
    public static final int OPOBJ4 = 79; // Client: PACKET_79
    public static final int OPOBJ3 = 80; // Client: PACKET_80
    public static final int EVENT_HOST_PING = 81; // Client: IDLE_TIMER

    private static final int[] LENGTHS = {
        2, -1, 8, 7, 8, 7, 15, 4, 6, 15,
        8, 16, 8, 16, 8, -1, -1, 8, -1, -1,
        4, 6, 7, -1, -1, 2, 7, 3, 3, -1,
        -1, 3, 3, 4, -1, 3, 3, 6, 4, 3,
        7, 3, -1, 8, 1, 3, 2, 7, 11, -1,
        3, 0, 12, -1, 8, 0, -1, 8, 2, 18,
        -1, -1, 3, 8, -1, 4, 2, 4, 3, 3,
        3, 0, 7, 7, -1, 11, -1, -1, 5, 7,
        7, 2
    };
    private static final String[] NAMES = {
        "OPOBJ_EXAMINE", "MESSAGE_PRIVATE", "IF_BUTTON3", "OPLOC2",
        "IF_BUTTON2", "OPOBJ2", "IF_ON_GROUNDITEM", "EVENT_CAMERA_POSITION",
        "WINDOW_STATUS", "IF_ON_LOC", "IF_BUTTON7", "IF_ON_IF",
        "IF_BUTTON1", "IF_DRAG", "IF_BUTTON10", "CLAN_KICK",
        "IGNORE_ADD", "IF_BUTTON8", "FRIEND_ADD", "MESSAGE_QUICKCHAT_PUBLIC",
        "SOUND_SONGEND", "IF_BUTTON_CONTINUE", "OPOBJ1", "EVENT_KEYBOARD",
        "IGNORE_REMOVE", "TRANSMITVAR_VERIFYID", "OPOBJ5", "OPPLAYER7",
        "OPNPC1", "RESUME_P_NAMEDIALOG", "MESSAGE_QUICKCHAT_PRIVATE", "OPNPC4",
        "OPPLAYER2", "EVENT_FRAME_MAP_LOADED", "REFLECTION_CHECK", "OPPLAYER8",
        "OPPLAYER5", "EVENT_MOUSE_CLICK", "FACE_HERE", "OPPLAYER1",
        "OPLOC3", "OPPLAYER3", "CLIENT_PREFERENCES", "IF_BUTTON4",
        "EVENT_APPLET_FOCUS", "OPPLAYER6", "OPNPC_EXAMINE", "OPLOC5",
        "IF_ON_PLAYER", "FRIEND_SETRANK", "OPNPC2", "CLOSE_MODAL",
        "IF_ON_TILE", "FRIEND_REMOVE", "IF_BUTTON9", "MAP_BUILD_COMPLETE",
        "URL_REQUEST", "IF_BUTTON6", "RESUME_P_OBJDIALOG", "MOVE_MINIMAPCLICK",
        "MESSAGE_PUBLIC", "EVENT_MOUSE_MOVE", "SET_CHATFILTERSETTINGS", "IF_BUTTON5",
        "CLAN_JOIN_LEAVE", "WORLD_MAP_CLICK", "OPLOC_EXAMINE", "RESUME_P_COUNTDIALOG",
        "OPNPC5", "OPPLAYER4", "OPNPC3", "NO_TIMEOUT",
        "OPLOC4", "OPLOC1", "RESUME_P_STRINGDIALOG", "IF_ON_NPC",
        "CLIENT_CHEAT", "REPORT_ABUSE", "MOVE_GAMECLICK", "OPOBJ4",
        "OPOBJ3", "EVENT_HOST_PING"
    };

    public static String name(int opcode) {
        return opcode >= 0 && opcode < COUNT ? NAMES[opcode] : "UNKNOWN(" + opcode + ")";
    }

    /** Zero-based operation index for the ten native widget action opcodes. */
    public static int interfaceOperationIndex(int opcode) {
        switch (opcode) {
            case IF_BUTTON1: return 0;
            case IF_BUTTON2: return 1;
            case IF_BUTTON3: return 2;
            case IF_BUTTON4: return 3;
            case IF_BUTTON5: return 4;
            case IF_BUTTON6: return 5;
            case IF_BUTTON7: return 6;
            case IF_BUTTON8: return 7;
            case IF_BUTTON9: return 8;
            case IF_BUTTON10: return 9;
            default: return -1;
        }
    }

    public static int length(int opcode) {
        if (opcode < 0 || opcode >= LENGTHS.length) throw new IllegalArgumentException("Unknown client opcode " + opcode);
        return LENGTHS[opcode];
    }
}
