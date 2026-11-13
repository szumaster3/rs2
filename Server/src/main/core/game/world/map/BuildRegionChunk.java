package core.game.world.map;

import core.game.node.entity.player.Player;
import core.game.node.item.GroundItem;
import core.game.node.scenery.Constructed;
import core.game.node.scenery.Scenery;
import core.game.world.map.build.LandscapeParser;
import core.net.packet.IoBuffer;
import core.net.packet.out.ClearScenery;
import core.net.packet.out.ConstructGroundItem;
import core.net.packet.out.ConstructScenery;

import java.util.ArrayList;

/**
 * A region chunk, used for easily modifying objects.
 *
 * @author Emperor
 */
public class BuildRegionChunk extends RegionChunk {

    /**
     * The maximum amount of objects to be stored on one tile in the chunk.
     */
    public static final int ARRAY_SIZE = 10;

    /**
     * The list of changes made.
     */
    private final Scenery[][][] objects;

    /**
     * Constructs a new {@code BuildRegionChunk} {@code Object}
     *
     * @param base     The base location.
     * @param rotation The rotation.
     * @param plane    The region plane.
     */
    public BuildRegionChunk(Location base, int rotation, RegionPlane plane) {
        super(base, rotation, plane);
        this.objects = new Scenery[ARRAY_SIZE][8][8];
    }

    public BuildRegionChunk(Location base, int rotation, RegionPlane plane, Scenery[][] objects) {
        this(base, rotation, plane);
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                Scenery object = objects[x][y];
                if (object != null && object.getId() != 0) {
                    this.objects[0][x][y] = copyObject(object, object.getRotation(), object.getLocation());
                }
            }
        }
    }

    @Override
    protected boolean appendUpdate(Player player, IoBuffer buffer) {
        boolean updated = false;
        for (Scenery[][] layer : objects) {
            for (Scenery[] column : layer) {
                for (Scenery object : column) {
                    if (object != null && !object.isRenderable()) {
                        ClearScenery.write(buffer, object);
                        updated = true;
                    }
                }
            }
        }
        for (Scenery[][] layer : objects) {
            for (Scenery[] column : layer) {
                for (Scenery object : column) {
                    if (object instanceof Constructed && object.isRenderable()) {
                        ConstructScenery.write(buffer, object);
                        updated = true;
                    }
                }
            }
        }
        ArrayList<GroundItem> totalItems = drawItems(items, player);
        for (GroundItem item : totalItems) {
            if (item != null && item.isActive() && item.getLocation() != null) {
                if (!item.isPrivate() || item.droppedBy(player)) {
                    ConstructGroundItem.write(buffer, item);
                    updated = true;
                }
            }
        }
        return updated;
    }

    @Override
    public void rotate(Direction direction) {
        int angle = direction.toInteger();
        if (angle > 3) {
            throw new IllegalArgumentException("Chunk rotation must be cardinal");
        }
        int turns = (angle - rotation) & 3;
        if (turns == 0) {
            return;
        }
        // Rotate a detached copy before installing it in the destination region.
        Scenery[][][] rotated = new Scenery[ARRAY_SIZE][SIZE][SIZE];
        for (Scenery[][] layer : objects) {
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    Scenery object = layer[x][y];
                    if (object == null) {
                        continue;
                    }
                    int[] pos = getRotatedPosition(x, y, object.getDefinition().sizeX,
                            object.getDefinition().sizeY, object.getRotation(), turns);
                    Scenery copy = copyObject(object, (object.getRotation() + turns) & 3,
                            currentBase.transform(pos[0], pos[1], 0));
                    store(rotated, pos[0], pos[1], copy);
                }
            }
        }
        System.arraycopy(rotated, 0, objects, 0, objects.length);
        rotation = angle;
    }

    @Override
    public BuildRegionChunk copy(RegionPlane plane) {
        BuildRegionChunk chunk = new BuildRegionChunk(base, rotation, plane);
        for (int i = 0; i < chunk.objects.length; i++) {
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    Scenery o = objects[i][x][y];
                    if (o != null) {
                        chunk.objects[i][x][y] = copyObject(o, o.getRotation(), o.getLocation());
                    }
                }
            }
        }
        return chunk;
    }

    private static Scenery copyObject(Scenery object, int rotation, Location location) {
        Scenery copy = object.transform(object.getId(), rotation, location);
        if (object instanceof Constructed) {
            copy = copy.asConstructed();
        }
        copy.setActive(object.isActive());
        copy.setRenderable(object.isRenderable());
        return copy;
    }

    @Override
    public void rebuildFlags(RegionPlane from) {
        super.copyTerrain(from);
        for (Scenery[][] layer : objects) {
            for (Scenery[] column : layer) {
                for (Scenery object : column) {
                    if (object != null && object.isRenderable()) {
                        Location loc = object.getLocation();
                        LandscapeParser.applyClippingFlagsFor(plane, loc.getLocalX(), loc.getLocalY(), object);
                    }
                }
            }
        }
    }

    @Override
    public void clear() {
        super.clear();
        for (int i = 0; i < objects.length; i++) {
            for (int x = 0; x < objects[i].length; x++) {
                for (int y = 0; y < objects[i][x].length; y++) {
                    objects[i][x][y] = null;
                }
            }
        }
    }

    /**
     * Removes the scenery.
     *
     * @param object The object to remove.
     */
    public void remove(Scenery object) {
        int chunkX = object.getLocation().getChunkOffsetX();
        int chunkY = object.getLocation().getChunkOffsetY();
        for (int i = 0; i < objects.length; i++) {
            if (objects[i][chunkX][chunkY] == object) {
                if (object instanceof Constructed) {
                    objects[i][chunkX][chunkY] = null;
                } else {
                    object.setRenderable(false);
                }
                object.setActive(false);
                return;
            }
        }
    }

    /**
     * Stores an object on the region chunk.
     *
     * @param object The object.
     */
    public void store(Scenery object) {
        int chunkX = object.getLocation().getChunkOffsetX();
        int chunkY = object.getLocation().getChunkOffsetY();
        store(objects, chunkX, chunkY, object);
        object.setActive(true);
        object.setRenderable(true);
    }

    private static void store(Scenery[][][] layers, int x, int y, Scenery object) {
        for (Scenery[][] layer : layers) {
            if (layer[x][y] == object) {
                return;
            }
        }
        for (Scenery[][] layer : layers) {
            if (layer[x][y] == null) {
                layer[x][y] = object;
                return;
            }
        }
        throw new IllegalStateException("Too many scenery objects at " + object.getLocation());
    }

    /**
     * Gets the objects index for the given object id.
     *
     * @param x        The x-coordinate on the region chunk.
     * @param y        The y-coordinate on the region chunk.
     * @param objectId The object id.
     */
    public int getIndex(int x, int y, int objectId) {
        int first = -1;
        for (int i = 0; i < objects.length; i++) {
            Scenery o = get(x, y, i);
            if (o == null || !o.isRenderable()) {
                continue;
            }
            if (objectId == o.getId() || (objectId == -1 && o.getDefinition().hasOptions(false))) {
                return i;
            }
            if (first == -1) {
                first = i;
            }
        }
        return objectId == -1 ? first : -1;
    }

    public Scenery find(int x, int y, int objectId) {
        int index = getIndex(x, y, objectId);
        return index == -1 ? null : get(x, y, index);
    }

    /**
     * Gets a scenery.
     *
     * @param x     The chunk x-coordinate.
     * @param y     The chunk y-coordinate.
     * @param index The index (0 = default).
     * @return The object.
     */
    public Scenery get(int x, int y, int index) {
        return objects[index][x][y];
    }

    @Override
    public Scenery[] getObjects(int chunkX, int chunkY) {
        Scenery[] objects = new Scenery[ARRAY_SIZE];
        for (int i = 0; i < ARRAY_SIZE; i++) {
            objects[i] = this.objects[i][chunkX][chunkY];
        }
        return objects;
    }

    /**
     * Gets the objects.
     *
     * @param index The index.
     * @return The objects array.
     */
    public Scenery[][] getObjects(int index) {
        return objects[index];
    }

    @Override
    public void setCurrentBase(Location currentBase) {
        for (int i = 0; i < objects.length; i++) {
            for (int x = 0; x < objects[i].length; x++) {
                for (int y = 0; y < objects[i][x].length; y++) {
                    if (objects[i][x][y] != null) {
                        Location newLoc = currentBase.transform(x, y, 0);
                        objects[i][x][y].setLocation(newLoc);
                    }
                }
            }
        }
        super.setCurrentBase(currentBase);
    }
}
