package core.net.packet.out;

import core.net.packet.ServerProt;

import core.net.packet.IoBuffer;
import core.net.packet.PacketHeader;

public enum ZoneProt {
    LOC_ADD_CHANGE(0, ServerProt.LOC_ADD_CHANGE),
    MAP_PROJANIM(1, ServerProt.MAP_PROJANIM),
    OBJ_ADD(2, ServerProt.OBJ_ADD),
    TEXT_COORD(3, ServerProt.TEXT_COORD),
    LOC_PREFETCH(4, ServerProt.LOC_PREFETCH),
    LOC_DEL(5, ServerProt.LOC_DEL),
    MAP_PROJANIM_HALFSQ(6, ServerProt.MAP_PROJANIM_HALFSQ),
    LOC_ANIM(7, ServerProt.LOC_ANIM),
    MAP_ANIM(8, ServerProt.MAP_ANIM),
    MAP_PROJANIM_SPECIFIC(9, ServerProt.MAP_PROJANIM_SPECIFIC),
    SOUND_AREA(10, ServerProt.SOUND_AREA),
    OBJ_DEL(11, ServerProt.OBJ_DEL),
    OBJ_COUNT(12, ServerProt.OBJ_COUNT),
    OBJ_REVEAL(13, ServerProt.OBJ_REVEAL),
    LOC_MERGE(14, ServerProt.LOC_MERGE);

    private final int enclosedId, opcode;

    ZoneProt(int enclosedId, int opcode) {
        this.enclosedId = enclosedId;
        this.opcode = opcode;
    }

    public IoBuffer start(IoBuffer enclosed) {
        return enclosed == null
                ? new IoBuffer(opcode, this == TEXT_COORD ? PacketHeader.BYTE : PacketHeader.NORMAL)
                : enclosed.put(enclosedId);
    }

    public static IoBuffer enclosed(int x, int z, int plane) {
        return new IoBuffer(ServerProt.UPDATE_ZONE_PARTIAL_ENCLOSED, PacketHeader.SHORT).putS(x).put(plane).putC(z);
    }

    public static IoBuffer reveal(IoBuffer enclosed, int item, int count, int tile, int receiver) {
        return OBJ_REVEAL.start(enclosed).putLEShort(count).putLEShortA(receiver).putS(tile).putLEShortA(item);
    }
}
