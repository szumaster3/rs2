package core.net.lobby;

import core.net.IoSession;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.CRC32;

public final class WorldList {

    private static final Charset CP1252 =
            Charset.forName("windows-1252");

    // World list protocol.
    public static final int SUCCESS = 0;
    public static final int REJECT = 1;

    private static final int PROTOCOL_VERSION = 1;

    // Continent names.
    public static final String CONTINENT_OCEANIA = "Oceania";
    public static final String CONTINENT_EUROPE = "Europe";
    public static final String CONTINENT_AMERICAS = "Americas";

    // Country names.
    public static final String COUNTRY_USA = "United States";
    public static final String COUNTRY_AUSTRIA = "Austria";
    public static final String COUNTRY_AUSTRALIA = "Australia";
    public static final String COUNTRY_GERMANY = "Germany";
    public static final String COUNTRY_BRAZIL = "Brazil";
    public static final String COUNTRY_CANADA = "Canada";
    public static final String COUNTRY_SWITZERLAND = "Switzerland";
    public static final String COUNTRY_CHINA = "China";
    public static final String COUNTRY_DENMARK = "Denmark";
    public static final String COUNTRY_FINLAND = "Finland";
    public static final String COUNTRY_FRANCE = "France";
    public static final String COUNTRY_UK = "United Kingdom";
    public static final String COUNTRY_IRELAND = "Ireland";
    public static final String COUNTRY_INDIA = "India";
    public static final String COUNTRY_MEXICO = "Mexico";
    public static final String COUNTRY_NETHERLANDS = "Netherlands";
    public static final String COUNTRY_NORWAY = "Norway";
    public static final String COUNTRY_NEW_ZEALAND = "New Zealand";
    public static final String COUNTRY_PORTUGAL = "Portugal";
    public static final String COUNTRY_SWEDEN = "Sweden";
    public static final String COUNTRY_BELGIUM = "Belgium";

    // Country flags.
    public static final int COUNTRY_FLAG_USA = 0;
    public static final int COUNTRY_FLAG_AUSTRIA = 15;
    public static final int COUNTRY_FLAG_AUSTRALIA = 16;
    public static final int COUNTRY_FLAG_GERMANY = 22;
    public static final int COUNTRY_FLAG_BRAZIL = 31;
    public static final int COUNTRY_FLAG_CANADA = 38;
    public static final int COUNTRY_FLAG_SWITZERLAND = 43;
    public static final int COUNTRY_FLAG_CHINA = 48;
    public static final int COUNTRY_FLAG_DENMARK = 58;
    public static final int COUNTRY_FLAG_FINLAND = 69;
    public static final int COUNTRY_FLAG_FRANCE = 74;
    public static final int COUNTRY_FLAG_UK = 77;
    public static final int COUNTRY_FLAG_IRELAND = 101;
    public static final int COUNTRY_FLAG_INDIA = 103;
    public static final int COUNTRY_FLAG_MEXICO = 152;
    public static final int COUNTRY_FLAG_NETHERLANDS = 161;
    public static final int COUNTRY_FLAG_NORWAY = 162;
    public static final int COUNTRY_FLAG_NEW_ZEALAND = 166;
    public static final int COUNTRY_FLAG_PORTUGAL = 179;
    public static final int COUNTRY_FLAG_SWEDEN = 191;

    // Unknown flag ID; use the fallback.
    public static final int COUNTRY_FLAG_BELGIUM = 0;

    // World flags.
    public static final int FLAG_NON_MEMBERS = 0;
    public static final int FLAG_MEMBERS = 0x1;
    public static final int FLAG_QUICK_CHAT = 0x2;
    public static final int FLAG_PVP = 0x4;
    public static final int FLAG_LOOTSHARE = 0x8;
    public static final int FLAG_HIGHLIGHT = 0x10;

    private static final List<WorldDefinition> WORLD_LIST =
            new ArrayList<>();

    public static final String[] THEMED_WORLDS = {
            "Trade - F2P",
            "Trade - Members",
            "Barbarian Assault",
            "Castle Wars",
            "Running - Air Runes",
            "Bounty Hunter",
            "Fist of Guthix",
            "House Parties",
            "Running - Nature Runes",
            "Role-Playing Server",
            "Burthorpe Games Room",
            "Vinesweeper",
            "High Lvl Duel - Tournaments",
            "Blast Furnace",
            "Great Orb Project",
            "Running - Law Runes",
            "Pest Control",
            "Runecrafting - ZMI Altar",
            "Shades of Mort'ton",
            "Tzhaar Fight Pits",
            "Duel - Tournaments",
            "Group Questing",
            "Falador Party Room",
            "Trouble Brewing",
            "Fishing Trawler",
            "Duel - Staked/Friendly",
            "Rat Pits"
    };

    static {
        addWorld(new WorldDefinition(
                1,
                0,
                FLAG_MEMBERS | FLAG_LOOTSHARE,
                "",
                "127.0.0.1",
                COUNTRY_SWEDEN,
                COUNTRY_FLAG_SWEDEN
        ));
    }

    private WorldList() {
    }

    /**
     * Adds a world to the world list.
     *
     * @param def The world definition.
     */
    public static synchronized void addWorld(WorldDefinition def) {
        Objects.requireNonNull(def, "World definition");

        int id = def.getWorldId();

        if (id < 1 || id > 32767) {
            throw new IllegalArgumentException(
                    "Invalid world ID: " + id
            );
        }

        for (WorldDefinition world : WORLD_LIST) {
            if (world.getWorldId() == id) {
                throw new IllegalArgumentException(
                        "Duplicate world ID: " + id
                );
            }
        }

        WORLD_LIST.add(def);
        WORLD_LIST.sort(
                Comparator.comparingInt(WorldDefinition::getWorldId)
        );
    }

    /**
     * Removes a world from the world list.
     *
     * @param worldId The world ID.
     * @return Whether the world was removed.
     */
    public static synchronized boolean removeWorld(int worldId) {
        return WORLD_LIST.removeIf(
                world -> world.getWorldId() == worldId
        );
    }

    /**
     * Sends the world list response to the client.
     *
     * @param session        The client session.
     * @param clientChecksum The client's world list checksum.
     */
    public static void sendUpdate(
            IoSession session,
            int clientChecksum
    ) {
        final List<WorldDefinition> worlds;

        synchronized (WorldList.class) {
            worlds = new ArrayList<>(WORLD_LIST);
        }

        int minId = worlds.isEmpty()
                ? 0
                : worlds.get(0).getWorldId();

        int maxId = worlds.isEmpty()
                ? 0
                : worlds.get(worlds.size() - 1).getWorldId();

        byte[] worldListData = encodeWorldList(
                worlds,
                minId,
                maxId
        );

        int checksum = calculateChecksum(worldListData);
        boolean updated = clientChecksum != checksum;

        ByteArrayOutputStream payload =
                new ByteArrayOutputStream();

        putByte(payload, PROTOCOL_VERSION);
        putByte(payload, updated ? 1 : 0);

        if (updated) {
            payload.write(
                    worldListData,
                    0,
                    worldListData.length
            );

            putInt(payload, checksum);
        }

        putPlayerInfo(payload, worlds, minId);

        byte[] data = payload.toByteArray();

        if (data.length > 0xFFFF) {
            throw new IllegalStateException(
                    "World list response exceeds maximum packet size."
            );
        }

        ByteBuffer response =
                ByteBuffer.allocate(data.length + 3);

        response.put((byte) SUCCESS);
        response.putShort((short) data.length);
        response.put(data);
        response.flip();

        session.queue(response);
    }

    /**
     * Encodes the static world list configuration.
     */
    private static byte[] encodeWorldList(
            List<WorldDefinition> worlds,
            int minId,
            int maxId
    ) {
        ByteArrayOutputStream buffer =
                new ByteArrayOutputStream();

        Map<Country, Integer> countries =
                new LinkedHashMap<>();

        for (WorldDefinition world : worlds) {
            Country country = new Country(
                    world.getCountry(),
                    world.getRegion()
            );

            countries.computeIfAbsent(
                    country,
                    key -> countries.size()
            );
        }

        if (countries.size() > 256) {
            throw new IllegalStateException(
                    "Too many countries in the world list."
            );
        }

        // Country table.
        putSmart(buffer, countries.size());

        for (Country country : countries.keySet()) {
            putSmart(buffer, country.flag);
            putJagString(buffer, country.name);
        }

        // World ID range and world count.
        putSmart(buffer, minId);
        putSmart(buffer, maxId);
        putSmart(buffer, worlds.size());

        // World definitions.
        for (WorldDefinition world : worlds) {
            Country country = new Country(
                    world.getCountry(),
                    world.getRegion()
            );

            int countryIndex = countries.get(country);
            int flags = world.getFlag();

            String activity = world.getActivity() == null
                    ? ""
                    : world.getActivity();

            // Highlight requires an activity name.
            if (activity.isEmpty()) {
                flags &= ~FLAG_HIGHLIGHT;
            }

            putSmart(
                    buffer,
                    world.getWorldId() - minId
            );

            putByte(buffer, countryIndex);
            putInt(buffer, flags);
            putJagString(buffer, activity);
            putJagString(buffer, world.getIp());
        }

        return buffer.toByteArray();
    }

    /**
     * Encodes the current player count for each world.
     */
    private static void putPlayerInfo(
            ByteArrayOutputStream buffer,
            List<WorldDefinition> worlds,
            int minId
    ) {
        for (WorldDefinition world : worlds) {
            putSmart(
                    buffer,
                    world.getWorldId() - minId
            );

            int players = world.getPlayerCount();

            if (players == -1 || players == 0xFFFF) {
                putShort(buffer, 0xFFFF);
            } else {
                putShort(
                        buffer,
                        Math.max(0, Math.min(players, 2000))
                );
            }
        }
    }

    /**
     * Calculates the CRC32 checksum of the static world list.
     */
    private static int calculateChecksum(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return (int) crc.getValue();
    }

    /**
     * Retained for compatibility with existing callers.
     *
     * The checksum is calculated automatically from the
     * current world list configuration.
     */
    public static void flagUpdate() {
        // No explicit invalidation is required.
    }

    /**
     * Writes an unsigned byte.
     */
    private static void putByte(
            ByteArrayOutputStream buffer,
            int value
    ) {
        buffer.write(value & 0xFF);
    }

    /**
     * Writes a big-endian short.
     */
    private static void putShort(
            ByteArrayOutputStream buffer,
            int value
    ) {
        putByte(buffer, value >>> 8);
        putByte(buffer, value);
    }

    /**
     * Writes a big-endian integer.
     */
    private static void putInt(
            ByteArrayOutputStream buffer,
            int value
    ) {
        putByte(buffer, value >>> 24);
        putByte(buffer, value >>> 16);
        putByte(buffer, value >>> 8);
        putByte(buffer, value);
    }

    /**
     * Writes an unsigned smart value.
     */
    private static void putSmart(
            ByteArrayOutputStream buffer,
            int value
    ) {
        if (value < 0 || value > 32767) {
            throw new IllegalArgumentException(
                    "Invalid smart value: " + value
            );
        }

        if (value < 128) {
            putByte(buffer, value);
        } else {
            putShort(buffer, value + 32768);
        }
    }

    /**
     * Writes a CP1252 string surrounded by null bytes.
     * Equivalent to pjstr2.
     */
    private static void putJagString(
            ByteArrayOutputStream buffer,
            String value
    ) {
        if (value == null) {
            value = "";
        }

        if (value.indexOf('\0') != -1) {
            throw new IllegalArgumentException(
                    "String contains a null character."
            );
        }

        byte[] bytes = value.getBytes(CP1252);

        putByte(buffer, 0);
        buffer.write(bytes, 0, bytes.length);
        putByte(buffer, 0);
    }

    /**
     * Represents a unique country table entry.
     */
    private static final class Country {

        private final int flag;
        private final String name;

        private Country(int flag, String name) {
            this.flag = flag;
            this.name = Objects.requireNonNull(name);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof Country)) {
                return false;
            }

            Country other = (Country) obj;

            return flag == other.flag
                    && name.equals(other.name);
        }

        @Override
        public int hashCode() {
            return 31 * flag + name.hashCode();
        }
    }
}