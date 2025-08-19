package org.bukkit.craftbukkit.block;

import com.google.common.base.Preconditions;
import net.minecraft.server.TileEntityNote;
import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.NoteBlock;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public class CraftNoteBlock extends CraftBlockState implements NoteBlock {
    private final CraftWorld world;
    private final TileEntityNote note;

    public CraftNoteBlock(final Block block) {
        super(block);

        world = (CraftWorld) block.getWorld();
        note = (TileEntityNote) world.getTileEntityAt(getX(), getY(), getZ());
    }

    public Note getNote() {
        return new Note(note.note);
    }

    public byte getRawNote() {
        return note.note;
    }

    public void setNote(Note n) {
        note.note = n.getId();
    }

    public void setRawNote(byte n) {
        note.note = n;
    }

    public boolean play() {
        Block block = getBlock();

        synchronized (block) {
            if (block.getType() == Material.NOTE_BLOCK) {
                note.play(world.getHandle(), getX(), getY(), getZ());
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean play(byte instrument, byte note) {
        Block block = getBlock();

        synchronized (block) {
            if (block.getType() == Material.NOTE_BLOCK) {
                world.getHandle().playNote(getX(), getY(), getZ(), instrument, note);
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean play(Instrument instrument, Note note) {
        Block block = getBlock();

        synchronized (block) {
            if (block.getType() == Material.NOTE_BLOCK) {
                world.getHandle().playNote(getX(), getY(), getZ(), instrument.getType(), note.getId());
                return true;
            } else {
                return false;
            }
        }
    }

    // Tsunami start
    public void setMetadata(Plugin owningPlugin, String key, MetadataValue value) {
        Preconditions.checkArgument(owningPlugin != null, "owningPlugin cannot be null");
        Preconditions.checkArgument(key != null, "key cannot be null");
        Preconditions.checkArgument(value != null, "value cannot be null");

        String fullKey = owningPlugin.getDescription().getName().toLowerCase() + "." + key;
        note.metadataStore.put(fullKey, value);
    }

    public void removeMetadata(Plugin owningPlugin, String key) {
        Preconditions.checkArgument(owningPlugin != null, "owningPlugin cannot be null");
        Preconditions.checkArgument(key != null, "key cannot be null");

        String fullKey = owningPlugin.getDescription().getName().toLowerCase() + "." + key;
        note.metadataStore.remove(fullKey);
    }

    public MetadataValue getMetadata(Plugin owningPlugin, String key) {
        Preconditions.checkArgument(owningPlugin != null, "owningPlugin cannot be null");
        Preconditions.checkArgument(key != null, "key cannot be null");

        String fullKey = owningPlugin.getDescription().getName().toLowerCase() + "." + key;
        return note.metadataStore.get(fullKey);
    }

    public boolean hasMetadata(Plugin owningPlugin, String key) {
        Preconditions.checkArgument(owningPlugin != null, "owningPlugin cannot be null");
        Preconditions.checkArgument(key != null, "key cannot be null");

        String fullKey = owningPlugin.getDescription().getName().toLowerCase() + "." + key;
        return note.metadataStore.containsKey(fullKey);
    }
    // Tsunami end

}
