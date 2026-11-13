package content.global.skill.construction;


import core.game.node.entity.player.Player;
import core.game.global.action.DoorActionHandler;
import core.game.node.scenery.Scenery;
import core.game.node.scenery.SceneryBuilder;
import core.game.world.map.*;
import java.awt.Point;

/**
 * Represents a room.
 *
 * @author Emperor
 */
public final class Room {

    /**
     * The default room type.
     */
    public static final int CHAMBER = 0x0;

    /**
     * The rooftop room type.
     */
    public static final int ROOF = 0x1;

    /**
     * The dungeon room type.
     */
    public static final int DUNGEON = 0x2;

    /**
     * The dungeon room type.
     */
    public static final int LAND = 0x4;

    /**
     * The room properties.
     */
    private RoomProperties properties;

    /**
     * The region chunk.
     */
    private RegionChunk chunk;

    /**
     * The hotspots.
     */
    private Hotspot[] hotspots;

    /**
     * The current rotation of the room.
     */
    private Direction rotation = Direction.NORTH;

    /**
     * Constructs a new {@code Room} {@code Object}.
     *
     * @param properties The room properties.
     */
    public Room(RoomProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates a new room.
     *
     * @param player     The player.
     * @param properties The room properties.
     * @return The room.
     */
    public static Room create(Player player, RoomProperties properties) {
        Room room = new Room(properties);
        room.configure(player.getHouseManager().getStyle());
        return room;
    }

    /**
     * Configures the room.
     */
    public void configure(HousingStyle style) {
        this.hotspots = new Hotspot[properties.getHotspots().length];
        for (int i = 0; i < hotspots.length; i++) {
            hotspots[i] = properties.getHotspots()[i].copy();
        }
        decorate(style);
    }

    /**
     * Redecorates the room.
     *
     * @param style The house style.
     */
    public void decorate(HousingStyle style) {
        Region region = RegionManager.forId(style.getRegionId());
        Region.load(region, true);
        chunk = region.getPlanes()[style.getPlane()].getRegionChunk(properties.getChunkX(), properties.getChunkY());
    }

    /**
     * Gets the hotspot object for the given hotspot type.
     *
     * @param hotspot The hotspot type.
     * @return The hotspot.
     */
    public Hotspot getHotspot(BuildHotspot hotspot) {
        for (Hotspot h : hotspots) {
            if (h.getHotspot() == hotspot) {
                return h;
            }
        }
        return null;
    }

    /**
     * Checks if the building hotspot has been built.
     *
     * @param hotspot The building hotspot.
     * @return {@code True} if so.
     */
    public boolean isBuilt(BuildHotspot hotspot) {
        Hotspot h = getHotspot(hotspot);
        return h != null && h.getDecorationIndex() > -1;
    }

    /**
     * Loads all the decorations.
     *
     * @param housePlane The plane.
     * @param chunk      The chunk used in the dynamic region.
     */
    public void loadDecorations(int housePlane, BuildRegionChunk chunk, HouseManager house) {
        HousingStyle style = house.getStyle();
        for (Hotspot spot : hotspots) {
            BuildHotspot hotspot = spot.getHotspot();
            Scenery template = findTemplateObject(this.chunk, spot, style);
            if (template == null) {
                spot.setCurrentX(-1);
                spot.setCurrentY(-1);
                continue;
            }
            int[] pos = RegionChunk.getRotatedPosition(template.getLocation().getChunkOffsetX(),
                    template.getLocation().getChunkOffsetY(), template.getDefinition().sizeX,
                    template.getDefinition().sizeY, template.getRotation(), chunk.getRotation());
            spot.setCurrentX(pos[0]);
            spot.setCurrentY(pos[1]);
            spot.setCurrentObjectId(template.getId());
            Scenery object = chunk.find(pos[0], pos[1], template.getId());
            if (object == null) {
                continue;
            }
            Decoration decoration = spot.getDecoration();
            if (decoration != null) {
                SceneryBuilder.replace(object, object.transform(decoration.getObjectId(house, hotspot)));
            } else if (hotspot == BuildHotspot.WINDOW || (!house.isBuildingMode() && hotspot == BuildHotspot.CHAPEL_WINDOW)) {
                SceneryBuilder.replace(object, object.transform(style.getWindow().getObjectId(style)));
            }
        }
        if (style == HousingStyle.TROPICAL_WOOD || style == HousingStyle.FANCY_STONE) {
            loadMissingStyleObjects(chunk, house);
        }
        if (!house.isBuildingMode()) {
            finishRoom(housePlane, house, chunk);
        }
    }

    private void loadMissingStyleObjects(BuildRegionChunk target, HouseManager house) {
        HousingStyle style = house.getStyle();
        Region reference = RegionManager.forId(HousingStyle.BASIC_WOOD.getRegionId());
        Region.load(reference, true);
        RegionChunk source = reference.getPlanes()[0].getRegionChunk(properties.getChunkX(), properties.getChunkY());
        for (Hotspot spot : hotspots) {
            if (spot.getCurrentX() >= 0) continue;
            BuildHotspot hotspot = spot.getHotspot();
            if (hotspot != BuildHotspot.WINDOW && hotspot != BuildHotspot.CHAIRS_1 && hotspot != BuildHotspot.CHAIRS_2
                    && hotspot != BuildHotspot.CHAIRS_3 && hotspot != BuildHotspot.DUNGEON_DOOR_LEFT
                    && hotspot != BuildHotspot.DUNGEON_DOOR_RIGHT && hotspot != BuildHotspot.DUNGEON_DOOR_LEFT2
                    && hotspot != BuildHotspot.DUNGEON_DOOR_RIGHT2) continue;
            Scenery template = findTemplateObject(source, spot, HousingStyle.BASIC_WOOD);
            if (template == null) continue;
            int[] pos = RegionChunk.getRotatedPosition(template.getLocation().getChunkOffsetX(),
                    template.getLocation().getChunkOffsetY(), template.getDefinition().sizeX,
                    template.getDefinition().sizeY, template.getRotation(), target.getRotation());
            spot.setCurrentX(pos[0]);
            spot.setCurrentY(pos[1]);
            spot.setCurrentObjectId(hotspot.getObjectId(style));
            Decoration decoration = spot.getDecoration();
            if (hotspot == BuildHotspot.WINDOW) {
                decoration = style.getWindow();
            }
            if (decoration != null || house.isBuildingMode()) {
                int id = decoration == null ? spot.getCurrentObjectId() : decoration.getObjectId(house, hotspot);
                SceneryBuilder.add(template.transform(id, (template.getRotation() + target.getRotation()) & 3,
                        target.getCurrentBase().transform(pos[0], pos[1], 0)));
            }
        }
    }

    private Scenery findTemplateObject(RegionChunk source, Hotspot spot, HousingStyle style) {
        BuildHotspot hotspot = spot.getHotspot();
        int id = hotspot.getObjectId(style);
        boolean lighting = hotspot == BuildHotspot.DUNGEON_LIGHT || hotspot == BuildHotspot.DUNGEON_LIGHT2;
        int alternateLight = hotspot == BuildHotspot.DUNGEON_LIGHT
                ? BuildHotspot.DUNGEON_LIGHT2.getObjectId() : BuildHotspot.DUNGEON_LIGHT.getObjectId();
        for (int x = spot.getChunkX(); x <= Math.max(spot.getChunkX(), spot.getChunkX2()); x++) {
            for (int y = spot.getChunkY(); y <= Math.max(spot.getChunkY(), spot.getChunkY2()); y++) {
                for (Scenery object : source.getObjects(x, y)) {
                    if (object != null && (object.getId() == id || lighting && object.getId() == alternateLight)) {
                        return object;
                    }
                }
            }
        }
        return null;
    }

    private void finishRoom(int housePlane, HouseManager house, BuildRegionChunk chunk) {
        if (properties.isRoof()) return;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                for (Scenery object : chunk.getObjects(x, y)) {
                    if (object == null || !object.isRenderable() || !object.isPermanent()) continue;
                    if (BuildingUtils.isDoorHotspot(object)) {
                        int id = getDoorReplacement(housePlane, house, object);
                        if (id == -1) {
                            SceneryBuilder.remove(object);
                        } else {
                            SceneryBuilder.replace(object, object.transform(id));
                        }
                    } else if (object.getId() == BuildHotspot.WINDOW.getObjectId(house.getStyle())
                            || object.getId() == BuildHotspot.CHAPEL_WINDOW.getObjectId(house.getStyle())) {
                        SceneryBuilder.replace(object, object.transform(house.getStyle().getWindow().getObjectId(house.getStyle())));
                    } else if (object.getDefinition().hasAction("build")) {
                        SceneryBuilder.remove(object);
                    }
                }
            }
        }
    }

    private int getDoorReplacement(int housePlane, HouseManager house, Scenery object) {
        Point offset = DoorActionHandler.getRotationPoint(object.getRotation());
        Room[][] rooms = house.getRooms()[housePlane];
        int x = object.getLocation().getChunkX() + offset.x;
        int y = object.getLocation().getChunkY() + offset.y;
        boolean edge = x < 0 || x >= rooms.length || y < 0 || y >= rooms[x].length;
        Room other = edge ? null : rooms[x][y];
        if (properties.isLand()) {
            return -1;
        }
        boolean otherChamber = other != null && other.properties.isChamber();
        if (otherChamber && other.getExits()[object.getRotation()]) {
            return -1;
        }
        if (!edge && !otherChamber && housePlane == 0) {
            return object.getId() % 2 != 0 ? house.getStyle().getDoorId() : house.getStyle().getSecondDoorId();
        }
        return properties.isDungeon() ? 13065 : house.getStyle().getWallId();
    }

    /**
     * Sets the decoration index for a group of object ids
     *
     * @param index The index.
     * @param hs    The building hotspot.
     */
    public void setAllDecorationIndex(int index, BuildHotspot hs) {
        for (int i = 0; i < hotspots.length; i++) {
            Hotspot h = hotspots[i];
            if (h.getHotspot() == hs) {
                h.setDecorationIndex(index);
            }
        }
    }

    /**
     * Gets the stairs hotspot for this room (or null if no stairs are available).
     *
     * @return The stairs.
     */
    public Hotspot getStairs() {
        for (Hotspot h : hotspots) {
            if (h.getHotspot().getType() == BuildHotspotType.STAIRCASE
                    || h.getHotspot() == BuildHotspot.LADDER || h.getHotspot() == BuildHotspot.TRAPDOOR
                    || (h.getHotspot() == BuildHotspot.CENTREPIECE_1 && h.getDecorationIndex() == 4)
                    || (h.getHotspot() == BuildHotspot.CENTREPIECE_2 && h.getDecorationIndex() == 2)) {
                return h;
            }
        }
        return null;
    }

    /**
     * Gets the exit directions
     *
     * @return The exits information.
     */
    public boolean[] getExits() {
        return getExits(rotation);
    }

    /**
     * Gets the exit directions.
     *
     * @return The directions at which you can exit the room (0=east, 1=south, 2=west, 3=north).
     */
    public boolean[] getExits(Direction rotation) {
        boolean[] exits = properties.getExits(chunk);
        boolean[] rotated = new boolean[exits.length];
        for (int i = 0; i < exits.length; i++) {
            rotated[(i + rotation.toInteger() - chunk.getRotation()) & 3] = exits[i];
        }
        return rotated;
    }

    /**
     * Gets the hotspot for the given coordinates.
     *
     * @param build The build hotspot.
     * @param x     The x-coordinate.
     * @param y     The y-coordinate.
     * @return The hotspot.
     */
    public Hotspot getHotspot(BuildHotspot build, int x, int y) {
        for (int i = 0; i < getHotspots().length; i++) {
            Hotspot h = getHotspots()[i];
            if (h.getCurrentX() == x && h.getCurrentY() == y && h.getHotspot() == build) {
                return h;
            }
        }
        return null;
    }

    /**
     * Sets the properties.
     *
     * @param properties The properties.
     */
    public void updateProperties(Player player, RoomProperties properties) {
        this.properties = properties;
        decorate(player.getHouseManager().getStyle());
        if (hotspots.length != properties.getHotspots().length) {
            return;
        }
        for (int i = 0; i < hotspots.length; i++) {
            Hotspot h = hotspots[i];
            Hotspot hs = hotspots[i] = properties.getHotspots()[i].copy();
            hs.setCurrentX(h.getCurrentX());
            hs.setCurrentY(h.getCurrentY());
            hs.setDecorationIndex(h.getDecorationIndex());
        }
    }

    /**
     * Gets the chunk.
     *
     * @return The chunk.
     */
    public RegionChunk getChunk() {
        return chunk;
    }

    /**
     * Sets the chunk.
     *
     * @param chunk The chunk to set.
     */
    public void setChunk(RegionChunk chunk) {
        this.chunk = chunk;
    }

    /**
     * Gets the hotspots.
     *
     * @return The hotspots.
     */
    public Hotspot[] getHotspots() {
        return hotspots;
    }

    /**
     * Sets the hotspots.
     *
     * @param hotspots The hotspots to set.
     */
    public void setHotspots(Hotspot[] hotspots) {
        this.hotspots = hotspots;
    }

    /**
     * Gets the properties.
     *
     * @return The properties.
     */
    public RoomProperties getProperties() {
        return properties;
    }

    /**
     * Sets the room rotation.
     *
     * @param rotation The rotation.
     */
    public void setRotation(Direction rotation) {
        this.rotation = rotation;
    }

    /**
     * Gets the rotation.
     *
     * @return The rotation.
     */
    public Direction getRotation() {
        return rotation;
    }

}
