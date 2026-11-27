package core.net.packet.out;

import core.net.packet.IoBuffer;
import core.net.packet.PacketHeader;

public enum ZoneProt {
    LOC_ADD_CHANGE(0, 104),
    MAP_PROJANIM(1, 34),
    OBJ_ADD(2, 13),
    TEXT_COORD(3, 63),
    LOC_PREFETCH(4, 94),
    LOC_DEL(5, 80),
    MAP_PROJANIM_HALFSQ(6, 33),
    LOC_ANIM(7, 74),
    MAP_ANIM(8, 96),
    MAP_PROJANIM_SPECIFIC(9, 39),
    SOUND_AREA(10, 1),
    OBJ_DEL(11, 3),
    OBJ_COUNT(12, 83),
    OBJ_REVEAL(13, 79),
    LOC_MERGE(14, 101);

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
        return new IoBuffer(59, PacketHeader.SHORT).putS(x).put(plane).putC(z);
    }

    public static IoBuffer reveal(IoBuffer enclosed, int item, int count, int tile, int receiver) {
        return OBJ_REVEAL.start(enclosed).putLEShort(count).putLEShortA(receiver).putS(tile).putLEShortA(item);
    }
}
