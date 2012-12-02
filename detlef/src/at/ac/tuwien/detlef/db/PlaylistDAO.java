
package at.ac.tuwien.detlef.db;

import java.util.ArrayList;

import at.ac.tuwien.detlef.domain.Episode;

/**
 * DAO for playlist access.
 */
public interface PlaylistDAO {

    /**
     * Interface for listeners interested in playlist status changes.
     */
    public interface OnPlaylistChangeListener {
        /**
         * Gets called when a playlist item was added.
         * 
         * @param position The position of the new playlist item.
         * @param episode The episode that was added as an item.
         */
        void onPlaylistEpisodeAdded(int position, Episode episode);

        /**
         * Gets called when the user rearranges a playlist item.
         * 
         * @param firstPosition The initial position of the playlist item.
         * @param secondPosition The late position of the playlist item.
         */
        void onPlaylistEpisodePositionChanged(int firstPosition, int secondPosition);

        /**
         * Gets called when some playlist item is removed (including when the
         * user deletes a podcast and its episodes).
         * 
         * @param position The position of the item to be removed.
         */
        void onPlaylistEpisodeRemoved(int position);
    }

    /**
     * Adds an episode to the end of the playlist.
     * 
     * @param podcast The episode to be added
     */
    boolean addEpisodeToEndOfPlaylist(Episode episode);

    /**
     * Adds an episode to the beginning of the playlist.
     * 
     * @param podcast The episode to be added
     * @return True if successful, false if not.
     */
    boolean addEpisodeToBeginningOfPlaylist(Episode episode);

    /**
     * @return Gets an ArrayList of the episodes in playlist order. Is an
     *         ArrayList in order to support ArrayAdapters. This will always
     *         return a new instance.
     */
    ArrayList<Episode> getNonCachedEpisodes();

    /**
     * Removes an episode from playlist.
     * 
     * @param episode The ordering number of the episode to be removed.
     * @return True if successful, false if not.
     */
    boolean removeEpisode(int position);

    /**
     * Moves an episode to a different position in the playlist.
     * 
     * @param firstPosition The position of the episode to be moved.
     * @param secondPosition The position where the episode should be moved
     *            into.
     * @return True if successful, false if not.
     */
    boolean moveEpisode(int firstPosition, int secondPosition);

    /**
     * Adds a listener that will get notified when the playlist changes.
     * 
     * @param listener The listener to be notified.
     */
    void addPlaylistChangedListener(OnPlaylistChangeListener listener);

    /**
     * Removes a listener. See also addPlaylistChangedListener.
     * 
     * @param listener The listener to be removed.
     */
    void removePlaylistChangeListener(OnPlaylistChangeListener listener);

    /**
     * Removes all episodes from the playlist.
     */
    void clearPlaylist();
}
