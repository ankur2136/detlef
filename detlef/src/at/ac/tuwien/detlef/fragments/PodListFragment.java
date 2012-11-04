
package at.ac.tuwien.detlef.fragments;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.adapters.PodListAdapter;
import at.ac.tuwien.detlef.db.EpisodeDAO;
import at.ac.tuwien.detlef.db.EpisodeDAOImpl;
import at.ac.tuwien.detlef.db.PodcastDAO;
import at.ac.tuwien.detlef.db.PodcastDAOImpl;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.models.PodListModel;

public class PodListFragment extends ListFragment {

    private static final String TAG = PodListFragment.class.getName();

    private PodListAdapter adapter;
    private PodListModel<Podcast> model;

    int clickCounter = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<Podcast> podlist = new ArrayList<Podcast>();
        fillWithDummyContents(podlist);
        adapter = new PodListAdapter(getActivity(), R.layout.pod_list_layout, podlist);
        setListAdapter(adapter);

        model = new PodListModel<Podcast>(podlist);
        model.addPodListChangeListener(new PodListModel.PodListChangeListener() {
            public void onPodListChange() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void fillWithDummyContents(List<Podcast> podlist) {
        PodcastDAO dao = new PodcastDAOImpl(this.getActivity().getApplicationContext());
        EpisodeDAO edao = new EpisodeDAOImpl(this.getActivity().getApplicationContext());

        Podcast all = new Podcast();
        all.setTitle("All Podcasts");
        all.setDescription("description all");
        all.setLastUpdate(System.currentTimeMillis());
        all.setLogoUrl("logoUrl");
        all.setUrl("url");
        all.setId(dao.insertPodcast(all));

        Podcast p1 = new Podcast();
        p1.setTitle("My Podcast 1");
        p1.setDescription("description 1");
        p1.setLastUpdate(System.currentTimeMillis());
        p1.setLogoUrl("logoUrl");
        p1.setUrl("url");
        p1.setId(dao.insertPodcast(p1));

        Podcast p2 = new Podcast();
        p2.setTitle("My Podcast 2");
        p2.setDescription("description 2");
        p2.setLastUpdate(System.currentTimeMillis());
        p2.setLogoUrl("logoUrl");
        p2.setUrl("url");
        p2.setId(dao.insertPodcast(p2));

        Podcast p3 = new Podcast();
        p3.setTitle("My Podcast 3");
        p3.setDescription("description 3");
        p3.setLastUpdate(System.currentTimeMillis());
        p3.setLogoUrl("logoUrl");
        p3.setUrl("url");
        p3.setId(dao.insertPodcast(p3));

        Podcast p4 = new Podcast();
        p4.setTitle("My Podcast 4");
        p4.setDescription("description 4");
        p4.setLastUpdate(System.currentTimeMillis());
        p4.setLogoUrl("logoUrl");
        p4.setUrl("url");
        p4.setId(dao.insertPodcast(p4));

        Episode e1 = new Episode();
        e1.setAuthor("author");
        e1.setDescription("description");
        e1.setFileSize("filesize");
        e1.setGuid("guid");
        e1.setLink("link");
        e1.setMimetype("mimetype");
        e1.setPodcast(all);
        e1.setReleased(System.currentTimeMillis());
        e1.setTitle("title");
        e1.setUrl("url");
        e1.setId(edao.insertEpisode(e1));
        edao.insertEpisode(e1);

        Episode e2 = new Episode();
        e2.setAuthor("author");
        e2.setDescription("description");
        e2.setFileSize("filesize");
        e2.setGuid("guid");
        e2.setLink("link");
        e2.setMimetype("mimetype");
        e2.setPodcast(p1);
        e2.setReleased(System.currentTimeMillis());
        e2.setTitle("title");
        e2.setUrl("url");
        e2.setId(edao.insertEpisode(e2));

        dao.deletePodcast((dao.getAllPodcasts()).get(0));

        podlist.addAll(dao.getAllPodcasts());
        adapter = new PodListAdapter(getActivity(), R.layout.pod_list_layout, podlist);
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
        registerForContextMenu(getListView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.podcast_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete_feed:
                onDeleteFeedClicked(info.position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.pod_fragment_layout, container, false);
    }

    private void onDeleteFeedClicked(int pos) {
        Log.v(TAG, String.format("onDeleteFeedClicked %d", pos));
        model.removePodcast(model.get(pos));
    }
}