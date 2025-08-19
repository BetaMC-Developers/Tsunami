package org.bukkit.block;

import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.metadata.Metadatable;

/**
 * Represents a note.
 */
public interface NoteBlock extends BlockState, Metadatable { // Tsunami - extends Metadatable

    /**
     * Gets the note.
     *
     * @return
     */
    public Note getNote();

    /**
     * Gets the note.
     *
     * @return
     */
    public byte getRawNote();

    /**
     * Set the note.
     *
     * @param note
     */
    public void setNote(Note note);

    /**
     * Set the note.
     *
     * @param note
     */
    public void setRawNote(byte note);

    /**
     * Attempts to play the note at block<br />
     * <br />
     * If the block is no longer a note block, this will return false
     *
     * @return true if successful, otherwise false
     */
    public boolean play();

    /**
     * Plays an arbitrary note with an arbitrary instrument
     *
     * @return true if successful, otherwise false
     */
    public boolean play(byte instrument, byte note);

    /**
     * Plays an arbitrary note with an arbitrary instrument
     *
     * @return true if successful, otherwise false
     */
    public boolean play(Instrument instrument, Note note);
}
