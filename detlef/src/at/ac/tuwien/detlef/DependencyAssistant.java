
package at.ac.tuwien.detlef;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;
import at.ac.tuwien.detlef.db.EpisodeDBAssistant;
import at.ac.tuwien.detlef.db.EpisodeDBAssistantImpl;
import at.ac.tuwien.detlef.db.PodcastDBAssistant;
import at.ac.tuwien.detlef.db.PodcastDBAssistantImpl;
import at.ac.tuwien.detlef.download.DetlefDownloadManager;
import at.ac.tuwien.detlef.gpodder.GPodderSync;
import at.ac.tuwien.detlef.playlist.PlaylistAssistant;
import at.ac.tuwien.detlef.settings.ConnectionTester;
import at.ac.tuwien.detlef.settings.ConnectionTesterGpodderNet;
import at.ac.tuwien.detlef.settings.GpodderSettings;
import at.ac.tuwien.detlef.settings.GpodderSettingsDAO;
import at.ac.tuwien.detlef.settings.GpodderSettingsDAOAndroid;
import at.ac.tuwien.detlef.util.GUIUtils;

/**
 * This class acts as a central point for setting and retrieving service
 * classes.
 * 
 * @author johannes
 */
public class DependencyAssistant {

    private static DependencyAssistant dependencyAssistant =
            new DependencyAssistant();

    private static final PlaylistAssistant PLAYLIST = new PlaylistAssistant();

    private static final EpisodeDBAssistant EPISODE_DB_ASSISTANT =
            new EpisodeDBAssistantImpl();

    private static final PodcastDBAssistant PODCAST_DB_ASSISTANT =
            new PodcastDBAssistantImpl();

    private static DetlefDownloadManager downloadManager = null;

    private static final GUIUtils GUI_UTILS = new GUIUtils();

    public GUIUtils getGuiUtils() {
        return GUI_UTILS;
    }

    public PlaylistAssistant getPlaylist() {
        return PLAYLIST;
    }

    /**
     * @return Gets the quasi-singleton GPodderSync instance for this program.
     */
    public GPodderSync getGPodderSync() {
        return null;
    }

    /**
     * @return Gets the quasi-singleton PodcastDBAssistant instance for this
     *         program.
     */
    public PodcastDBAssistant getPodcastDBAssistant() {
        return PODCAST_DB_ASSISTANT;
    }

    /**
     * @return Gets the quasi-singleton EpisodeDBAssistant instance for this
     *         program.
     */
    public EpisodeDBAssistant getEpisodeDBAssistant() {
        return EPISODE_DB_ASSISTANT;
    }

    /**
     * Returns the download manager instance.
     */
    public synchronized DetlefDownloadManager getDownloadManager(Context context) {
        if (downloadManager == null) {
            downloadManager = new DetlefDownloadManager(context);
        }
        return downloadManager;
    }

    /**
     * @param context The {@link Context}. This is needed in order to be able to
     *            access Android's system settings. Usually this will be the
     *            current {@link Activity}.
     * @return Gets the {@link GpodderSettings gpodder.net settings instance}
     *         that provides the user name, password and device name settings.
     */
    public GpodderSettings getGpodderSettings(Context context) {

        HashMap<String, Object> dependencies = new HashMap<String, Object>();
        dependencies.put("sharedPreferences",
                PreferenceManager.getDefaultSharedPreferences(context));

        GpodderSettingsDAO gpodderSettingsDAO = new GpodderSettingsDAOAndroid();
        gpodderSettingsDAO.setDependencies(dependencies);
        return gpodderSettingsDAO.getSettings();

    }

    /**
     * @return The {@link ConnectionTester} that verifies a set of of
     *         {@link GpodderSettings}.
     */
    public ConnectionTester getConnectionTester() {
        return new ConnectionTesterGpodderNet().setContext(Detlef
                .getAppContext());
    }

    public static DependencyAssistant getDependencyAssistant() {
        return dependencyAssistant;
    }

    /**
     * Overwrites the default {@link DependencyAssistant} with a custom one. By
     * using this method you can easily replace parts of the Application with
     * Mocks which is useful for testing.
     * 
     * @param pDependencyAssistant
     */
    public static void setDependencyAssistant(
            DependencyAssistant pDependencyAssistant) {
        dependencyAssistant = pDependencyAssistant;
    }

}
