
package at.ac.tuwien.detlef.db;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.domain.Episode;

public final class PlaylistDAOImpl implements PlaylistDAO, EpisodeDAO.OnEpisodeChangeListener {

    private static final String TAG = PlaylistDAOImpl.class.getName();

    private static final PlaylistDAOImpl INSTANCE = new PlaylistDAOImpl(Detlef.getAppContext());

    private final DatabaseHelper dbHelper;
    private final Set<OnPlaylistChangeListener> listeners = new HashSet<OnPlaylistChangeListener>();
    private final EpisodeDAO edao;

    /**
     * Interface for listeners interested in playlist status changes.
     */
    public interface OnPlaylistChangeListener {
        void onPlaylistChanged(List<Episode> playlist);
    }

    /**
     * Returns (and lazily initializes) the PlaylistDAOImpl singleton instance.
     */
    public static PlaylistDAOImpl i() {
        return INSTANCE;
    }

    public PlaylistDAOImpl(Context context) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            edao = EpisodeDAOImpl.i();

            dbHelper = new DatabaseHelper(context);

            /* Take care of any pending database upgrades. */

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.close();
        }
    }

    public void addPlaylistChangedListener(OnPlaylistChangeListener listener) {
        listeners.add(listener);
    }

    public void removePlaylistChangeListener(OnPlaylistChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListenersChanged() {
        for (OnPlaylistChangeListener listener : listeners) {
            listener.onPlaylistChanged(getEpisodes());
        }
    }

    private int getNextFreePosition(SQLiteDatabase db) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            Cursor cursor = db.rawQuery("SELECT IFNULL(MAX("
                    + DatabaseHelper.COLUMN_PLAYLIST_POSITION + "), (-1)) FROM "
                    + DatabaseHelper.TABLE_PLAYLIST, null);
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) + 1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public boolean addEpisodeToEndOfPlaylist(Episode episode) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                int nextPosition = getNextFreePosition(db);
                Log.d(getClass().getName(), "Next free position: " + nextPosition);

                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_PLAYLIST_EPISODE, episode.getId());
                values.put(DatabaseHelper.COLUMN_PLAYLIST_POSITION, nextPosition);

                long id = db.insert(DatabaseHelper.TABLE_PLAYLIST, null, values);
                if (id == -1) {
                    throw new SQLiteException("Failed to insert playlist item");
                }

                notifyListenersChanged();
                return true;
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
                return false;
            } finally {
                if (db != null && db.isOpen()) {
                    db.close();
                }
            }
        }
    }

    @Override
    public boolean addEpisodeToBeginningOfPlaylist(Episode episode) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                shiftPositionsFromBy(0, 1, db);

                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_PLAYLIST_EPISODE, episode.getId());
                values.put(DatabaseHelper.COLUMN_PLAYLIST_POSITION, 0);

                long id = db.insert(DatabaseHelper.TABLE_PLAYLIST, null, values);
                Log.d(getClass().getName(), "New playlist item id: " + id);
                if (id == -1) {
                    throw new SQLiteException("Failed to insert playlist item");
                }

                notifyListenersChanged();
                return true;
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
                return false;
            } finally {
                if (db != null && db.isOpen()) {
                    db.close();
                }
            }
        }
    }

    private void shiftPositionsFromBy(int from, int by, SQLiteDatabase db) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            String[] selectionArgs = {
                    String.valueOf(from)
            };
            String operator;
            if (by >= 0) {
                operator = "+";
            } else {
                operator = "-";
                by *= (-1);
            }
            Log.d(getClass().getName(), "Shifting playlist from " + from + " by " + operator + by);
            db.execSQL("UPDATE " + DatabaseHelper.TABLE_PLAYLIST + " SET "
                    + DatabaseHelper.COLUMN_PLAYLIST_POSITION + " = "
                    + DatabaseHelper.COLUMN_PLAYLIST_POSITION + operator + by + " WHERE "
                    + DatabaseHelper.COLUMN_PLAYLIST_POSITION + " >= ?", selectionArgs);
        }
    }

    @Override
    public List<Episode> getEpisodes() {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            SQLiteDatabase db = null;
            try {
                List<Episode> allEpisodes = new ArrayList<Episode>();
                db = dbHelper.getReadableDatabase();
                Cursor c = db.rawQuery(
                        "SELECT "
                                + DatabaseHelper.COLUMN_PLAYLIST_EPISODE + " FROM "
                                + DatabaseHelper.TABLE_PLAYLIST + " ORDER BY "
                                + DatabaseHelper.COLUMN_PLAYLIST_POSITION + " ASC", null);
                if (c.moveToFirst()) {
                    do {
                        Log.d(getClass().getName(), "Getting episode " + c.getLong(0));
                        Episode e = edao.getEpisode(c.getLong(0));
                        allEpisodes.add(e);
                    } while (c.moveToNext());
                }
                c.close();
                return allEpisodes;
            } finally {
                if (db != null && db.isOpen()) {
                    db.close();
                }
            }
        }
    }

    @Override
    public boolean removeEpisode(int position) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            int ret = 0;
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                String selection = DatabaseHelper.COLUMN_PLAYLIST_POSITION + " = ?";
                String[] selectionArgs = {
                        String.valueOf(position)
                };

                Log.d(getClass().getName(), "Deleting position " + position);
                ret = db.delete(DatabaseHelper.TABLE_PLAYLIST, selection, selectionArgs);

                shiftPositionsFromBy(position, -1, db);
                db.close();

                notifyListenersChanged();
                return (ret == 1);
            } catch (Exception ex) {
                Log.e(getClass().getName(), ex.getMessage());
                return false;
            } finally {
                if (db != null && db.isOpen()) {
                    db.close();
                }
            }
        }
    }

    @Override
    public boolean moveEpisode(int firstPosition, int secondPosition) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            SQLiteDatabase db = null;
            int ret = 0;
            try {
                db = dbHelper.getWritableDatabase();
                long id = getIdAt(firstPosition, db);

                shiftPositionsFromBy(firstPosition, -1, db);
                shiftPositionsFromBy(secondPosition, 1, db);

                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_PLAYLIST_POSITION, secondPosition);

                ret = db.update(DatabaseHelper.TABLE_PLAYLIST, values,
                        DatabaseHelper.COLUMN_PLAYLIST_ID
                                + " = ?", new String[] {
                            String.valueOf(id)
                        });

                notifyListenersChanged();
                return ret == 1;
            } finally {
                if (db != null && db.isOpen()) {
                    db.close();
                }
            }
        }
    }

    private long getIdAt(int position, SQLiteDatabase db) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            String[] selectionArgs = {
                    String.valueOf(position)
            };
            Cursor c = db.rawQuery(
                    "SELECT "
                            + DatabaseHelper.COLUMN_PLAYLIST_ID + " FROM "
                            + DatabaseHelper.TABLE_PLAYLIST + " WHERE "
                            + DatabaseHelper.COLUMN_PLAYLIST_POSITION + " = ?", selectionArgs);
            long ret = -1;
            if (c.moveToFirst()) {
                ret = c.getLong(0);
            }
            c.close();
            return ret;
        }
    }

    @Override
    public void onEpisodeChanged(Episode episode) {
        // do nothing
    }

    @Override
    public void onEpisodeAdded(Episode episode) {
        // not of interest
    }

    @Override
    public void onEpisodeDeleted(Episode episode) {
        // TODO Auto-generated method stub
        
    }
}
