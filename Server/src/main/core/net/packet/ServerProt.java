package core.net.packet;

/** Revision 578 gameplay wire IDs and frame sizes. Client symbols are recorded next to
 * descriptive names; these names are not claims about original Jagex identifiers.
 * Login, JS5 and management-server protocols are separate namespaces.
 */
public final class ServerProt {
    private ServerProt() {}
    public static final int COUNT = 106;
    public static final int GLOBAL_SPOT_ANIMATION = 0; // Client: PACKET_0
    public static final int SOUND_AREA = 1; // Client: SOUND_AREA
    public static final int CHAT_FILTER_SETTINGS = 2; // Client: CHAT_FILTER_SETTINGS
    public static final int OBJ_DEL = 3; // Client: OBJ_DEL
    public static final int UPDATE_RUNENERGY = 4; // Client: UPDATE_RUNENERGY
    public static final int CAM_LOOK_AT = 5; // Client: PACKET_5
    public static final int UPDATE_IGNORELIST = 6; // Client: PACKET_6
    public static final int UPDATE_RUNWEIGHT = 7; // Client: UPDATE_RUNWEIGHT
    public static final int UPDATE_REBOOT_TIMER = 8; // Client: UPDATE_REBOOT_TIMER
    public static final int IF_SETANIM = 9; // Client: PACKET_9
    public static final int IF_COMPONENT_24BIT_VALUES = 10; // Client: PACKET_10
    public static final int SET_WALK_OPTION = 11; // Client: PACKET_11
    public static final int ENTITY_RENDER_PRIORITY = 12; // Client: PACKET_12
    public static final int OBJ_ADD = 13; // Client: OBJ_ADD
    public static final int CAM_FORCE_ANGLE = 14; // Client: PACKET_14
    public static final int IF_DELAYED_STATE_13 = 15; // Client: PACKET_15
    public static final int LOC_ANIM_ABSOLUTE = 16; // Client: PACKET_16
    public static final int MINIMAP_FLAG = 17; // Client: PACKET_17
    public static final int REFLECTION_CHEAT_CHECK = 18; // Client: REFLECTION_CHEAT_CHECK
    public static final int FRIENDLIST_LOADING = 19; // Client: PACKET_19
    public static final int UPDATE_STAT = 20; // Client: UPDATE_STAT
    public static final int DELETE_CLIENT_CONTAINER = 21; // Client: PACKET_21
    public static final int VARC_LARGE = 22; // Client: PACKET_22
    public static final int CAM_SHAKE = 23; // Client: PACKET_23
    public static final int IF_SET_PLAYER_BODY_MODEL = 24; // Client: PACKET_24
    public static final int CAM_MOVE_TO = 25; // Client: CAM_LOOKAT
    public static final int CLIENT_RANDOM_DATA = 26; // Client: PACKET_26
    public static final int IF_SET_FONT = 27; // Client: PACKET_27
    public static final int IF_CLOSESUB = 28; // Client: PACKET_28
    public static final int VARP_SMALL = 29; // Client: PACKET_29
    public static final int CAM_SMOOTH_RESET = 30; // Client: PACKET_30
    public static final int IF_SET_SCROLL_Y = 31; // Client: PACKET_31
    public static final int NPC_INFO_SMALL = 32; // Client: PACKET_32
    public static final int MAP_PROJANIM_HALFSQ = 33; // Client: MAP_PROJANIM_HALFSQ
    public static final int MAP_PROJANIM = 34; // Client: MAP_PROJANIM
    public static final int UPDATE_CLAN_CHAT = 35; // Client: PACKET_35
    public static final int IF_SETSPRITE = 36; // Client: PACKET_36
    public static final int NPC_INFO_LARGE = 37; // Client: PACKET_37
    public static final int SET_INTERFACE_SETTINGS = 38; // Client: SET_INTERFACE_SETTINGS
    public static final int MAP_PROJANIM_SPECIFIC = 39; // Client: MAP_PROJANIM_SPECIFIC
    public static final int MINIMAP_STATE = 40; // Client: PACKET_40
    public static final int UPDATE_ZONE_PARTIAL_FOLLOWS = 41; // Client: PACKET_41
    public static final int UPDATE_ZONE_FULL_FOLLOWS = 42; // Client: PACKET_42
    public static final int MESSAGE_QUICKCHAT_PRIVATE_ECHO = 43; // Client: PACKET_43
    public static final int IF_SETCOLOUR = 44; // Client: PACKET_44
    public static final int PRIVATE_CHAT_FILTER = 45; // Client: PACKET_45
    public static final int SET_INTERACTION = 46; // Client: SET_INTERACTION
    public static final int VARBIT_LARGE = 47; // Client: PACKET_47
    public static final int VARC_SMALL = 48; // Client: PACKET_48
    public static final int CLIENT_SETTINGS_COOKIE = 49; // Client: PACKET_49
    public static final int IGNORE_LIST_INCREMENTAL = 50; // Client: PACKET_50
    public static final int MESSAGE_QUICKCHAT_CLAN = 51; // Client: PACKET_51
    public static final int IF_OPENSUB = 52; // Client: IF_OPENSUB
    public static final int VARCSTR_SMALL = 53; // Client: PACKET_53
    public static final int IF_SETOBJ = 54; // Client: PACKET_54
    public static final int MESSAGE_PRIVATE = 55; // Client: PACKET_55
    public static final int OPEN_URL = 56; // Client: PACKET_56
    public static final int IF_SETANGLE = 57; // Client: PACKET_57
    public static final int LOGOUT = 58; // Client: LOGOUT
    public static final int UPDATE_ZONE_PARTIAL_ENCLOSED = 59; // Client: UPDATE_ZONE_PARTIAL_ENCLOSED
    public static final int IF_SETTEXT = 60; // Client: PACKET_60
    public static final int MIDI_SONG = 61; // Client: MIDI_SONG
    public static final int MIDI_JINGLE = 62; // Client: MIDI_JINGLE
    public static final int TEXT_COORD = 63; // Client: TEXT_COORD
    public static final int VARBIT_SMALL = 64; // Client: PACKET_64
    public static final int NPC_ANIMATION_DIRECT = 65; // Client: PACKET_65
    public static final int IF_MOVESUB = 66; // Client: PACKET_66
    public static final int CLAN_MEMBER_INCREMENTAL = 67; // Client: PACKET_67
    public static final int LAST_LOGIN_INFO = 68; // Client: LAST_LOGIN_INFO
    public static final int UPDATE_INV_FULL = 69; // Client: PACKET_69
    public static final int UPDATE_FRIENDLIST = 70; // Client: UPDATE_FRIENDLIST
    public static final int GRAND_EXCHANGE_OFFER = 71; // Client: PACKET_71
    public static final int PLAYER_INFO = 72; // Client: PLAYER_INFO
    public static final int VARP_LARGE = 73; // Client: PACKET_73
    public static final int LOC_ANIM = 74; // Client: LOC_ANIM
    public static final int RESET_CLIENT_VARPS = 75; // Client: PACKET_75
    public static final int MESSAGE_PRIVATE_ECHO = 76; // Client: PACKET_76
    public static final int WIDGETSTRUCT_SETTING = 77; // Client: WIDGETSTRUCT_SETTING
    public static final int IF_SET_SELF_BODY_MODEL = 78; // Client: PACKET_78
    public static final int OBJ_REVEAL = 79; // Client: OBJ_REVEAL
    public static final int LOC_DEL = 80; // Client: LOC_DEL
    public static final int IF_SETMODEL = 81; // Client: PACKET_81
    public static final int RUN_DIALOG_ABORT_HOOKS = 82; // Client: PACKET_82
    public static final int OBJ_COUNT = 83; // Client: OBJ_COUNT
    public static final int MESSAGE_CLAN = 84; // Client: PACKET_84
    public static final int VARCSTR_LARGE = 85; // Client: PACKET_85
    public static final int IF_SETPOSITION = 86; // Client: PACKET_86
    public static final int RUNCLIENTSCRIPT = 87; // Client: PACKET_87
    public static final int REBUILD_REGION = 88; // Client: REBUILD_REGION
    public static final int IF_SETPLAYER_MODEL_TYPE3 = 89; // Client: PACKET_89
    public static final int IF_SET_IDENTITY_KIT_MODEL = 90; // Client: PACKET_90
    public static final int CAM_RESET = 91; // Client: PACKET_91
    public static final int SOUND_SYNTH = 92; // Client: PACKET_92
    public static final int IF_OPENTOP = 93; // Client: IF_OPENTOP
    public static final int LOC_PREFETCH = 94; // Client: LOC_PREFETCH
    public static final int IF_SETNPC_MODEL_TYPE2 = 95; // Client: PACKET_95
    public static final int MAP_ANIM = 96; // Client: MAP_ANIM
    public static final int IF_SETHIDE = 97; // Client: PACKET_97
    public static final int REBUILD_NORMAL = 98; // Client: REBUILD_NORMAL
    public static final int MESSAGE_GAME = 99; // Client: MESSAGE_GAME
    public static final int UPDATE_INV_PARTIAL = 100; // Client: PACKET_100
    public static final int LOC_MERGE = 101; // Client: LOC_MERGE
    public static final int HINT_ICON = 102; // Client: PACKET_102
    public static final int RESET_ENTITY_ANIMATIONS = 103; // Client: PACKET_103
    public static final int LOC_ADD_CHANGE = 104; // Client: LOC_ADD_CHANGE
    public static final int MESSAGE_QUICKCHAT_PRIVATE = 105; // Client: PACKET_105

    private static final int[] LENGTHS = {
        10, 6, 2, 3, 1, 8, -2, 2, 2, 8,
        -1, -1, 3, 5, 6, 10, 7, 2, -2, 0,
        6, 3, 8, 8, 12, 8, 28, 8, 6, 3,
        2, 8, -2, 15, 15, -2, 8, -2, 14, 17,
        1, 3, 3, -1, 8, 1, -1, 6, 5, -1,
        -1, -1, 9, -1, 12, -1, -1, 12, 0, -2,
        -2, 4, 6, -1, 3, 5, 10, -1, 4, -2,
        -2, 20, -2, 6, 4, 0, -1, 12, 6, 7,
        2, 8, 0, 7, -1, -2, 10, -2, -2, 6,
        12, 2, 6, 5, 3, 8, 6, 7, -2, -1,
        -2, 14, 12, 0, 4, -1
    };
    private static final String[] NAMES = {
        "GLOBAL_SPOT_ANIMATION", "SOUND_AREA", "CHAT_FILTER_SETTINGS", "OBJ_DEL",
        "UPDATE_RUNENERGY", "CAM_LOOK_AT", "UPDATE_IGNORELIST", "UPDATE_RUNWEIGHT",
        "UPDATE_REBOOT_TIMER", "IF_SETANIM", "IF_COMPONENT_24BIT_VALUES", "SET_WALK_OPTION",
        "ENTITY_RENDER_PRIORITY", "OBJ_ADD", "CAM_FORCE_ANGLE", "IF_DELAYED_STATE_13",
        "LOC_ANIM_ABSOLUTE", "MINIMAP_FLAG", "REFLECTION_CHEAT_CHECK", "FRIENDLIST_LOADING",
        "UPDATE_STAT", "DELETE_CLIENT_CONTAINER", "VARC_LARGE", "CAM_SHAKE",
        "IF_SET_PLAYER_BODY_MODEL", "CAM_MOVE_TO", "CLIENT_RANDOM_DATA", "IF_SET_FONT",
        "IF_CLOSESUB", "VARP_SMALL", "CAM_SMOOTH_RESET", "IF_SET_SCROLL_Y",
        "NPC_INFO_SMALL", "MAP_PROJANIM_HALFSQ", "MAP_PROJANIM", "UPDATE_CLAN_CHAT",
        "IF_SETSPRITE", "NPC_INFO_LARGE", "SET_INTERFACE_SETTINGS", "MAP_PROJANIM_SPECIFIC",
        "MINIMAP_STATE", "UPDATE_ZONE_PARTIAL_FOLLOWS", "UPDATE_ZONE_FULL_FOLLOWS", "MESSAGE_QUICKCHAT_PRIVATE_ECHO",
        "IF_SETCOLOUR", "PRIVATE_CHAT_FILTER", "SET_INTERACTION", "VARBIT_LARGE",
        "VARC_SMALL", "CLIENT_SETTINGS_COOKIE", "IGNORE_LIST_INCREMENTAL", "MESSAGE_QUICKCHAT_CLAN",
        "IF_OPENSUB", "VARCSTR_SMALL", "IF_SETOBJ", "MESSAGE_PRIVATE",
        "OPEN_URL", "IF_SETANGLE", "LOGOUT", "UPDATE_ZONE_PARTIAL_ENCLOSED",
        "IF_SETTEXT", "MIDI_SONG", "MIDI_JINGLE", "TEXT_COORD",
        "VARBIT_SMALL", "NPC_ANIMATION_DIRECT", "IF_MOVESUB", "CLAN_MEMBER_INCREMENTAL",
        "LAST_LOGIN_INFO", "UPDATE_INV_FULL", "UPDATE_FRIENDLIST", "GRAND_EXCHANGE_OFFER",
        "PLAYER_INFO", "VARP_LARGE", "LOC_ANIM", "RESET_CLIENT_VARPS",
        "MESSAGE_PRIVATE_ECHO", "WIDGETSTRUCT_SETTING", "IF_SET_SELF_BODY_MODEL", "OBJ_REVEAL",
        "LOC_DEL", "IF_SETMODEL", "RUN_DIALOG_ABORT_HOOKS", "OBJ_COUNT",
        "MESSAGE_CLAN", "VARCSTR_LARGE", "IF_SETPOSITION", "RUNCLIENTSCRIPT",
        "REBUILD_REGION", "IF_SETPLAYER_MODEL_TYPE3", "IF_SET_IDENTITY_KIT_MODEL", "CAM_RESET",
        "SOUND_SYNTH", "IF_OPENTOP", "LOC_PREFETCH", "IF_SETNPC_MODEL_TYPE2",
        "MAP_ANIM", "IF_SETHIDE", "REBUILD_NORMAL", "MESSAGE_GAME",
        "UPDATE_INV_PARTIAL", "LOC_MERGE", "HINT_ICON", "RESET_ENTITY_ANIMATIONS",
        "LOC_ADD_CHANGE", "MESSAGE_QUICKCHAT_PRIVATE"
    };

    public static String name(int opcode) {
        return opcode >= 0 && opcode < COUNT ? NAMES[opcode] : "UNKNOWN(" + opcode + ")";
    }

    // Protocol.inboundBuffer: PacketBit(5000), payload read at offset zero.
    public static final int MAX_PAYLOAD_SIZE = 5000;

    public static boolean validate(int opcode, PacketHeader header, int payload) {
        if (opcode < 0 || opcode >= LENGTHS.length || payload < 0 || payload > MAX_PAYLOAD_SIZE) return false;
        int expected = LENGTHS[opcode];
        if (expected == -1) return header == PacketHeader.BYTE && payload <= 255;
        if (expected == -2) return header == PacketHeader.SHORT && payload <= 65535;
        return header == PacketHeader.NORMAL && payload == expected;
    }

    public static String expected(int opcode) {
        if (opcode < 0 || opcode >= LENGTHS.length) return "unknown opcode";
        int size = LENGTHS[opcode];
        return name(opcode) + " " + (size == -1 ? "BYTE" : size == -2 ? "SHORT" : "NORMAL/" + size);
    }
}
