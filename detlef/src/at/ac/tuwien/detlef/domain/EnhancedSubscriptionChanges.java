/* *************************************************************************
 *  Copyright 2012-2014 The detlef developers                              *
 *                                                                         *
 *  This program is free software: you can redistribute it and/or modify   *
 *  it under the terms of the GNU General Public License as published by   *
 *  the Free Software Foundation, either version 2 of the License, or      *
 *  (at your option) any later version.                                    *
 *                                                                         *
 *  This program is distributed in the hope that it will be useful,        *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
 *  GNU General Public License for more details.                           *
 *                                                                         *
 *  You should have received a copy of the GNU General Public License      *
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.  *
 ************************************************************************* */



package at.ac.tuwien.detlef.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.os.Parcel;
import android.os.Parcelable;

import com.dragontek.mygpoclient.simple.IPodcast;

/**
 * Class representing changes in podcast subscriptions.
 */
public class EnhancedSubscriptionChanges implements Serializable, Parcelable {

    private static final long serialVersionUID = 1L;

    /** Stubs for added podcasts. */
    private final List<Podcast> add;

    /** Stubs for removed podcasts. */
    private final List<Podcast> remove;

    /** Timestamp of the changes. */
    private final long timestamp;

    /**
     * Create new EnhancedSubscriptionChanges.
     *
     * The Lists passed to the constuctor are copied into Lists of Podcast. The timestamp an all
     * Podcasts is set to the given timestamp.
     *
     * @param add List of IPodcasts (stubs are OK) to add.
     * @param remove List of IPodcasts (stubs are OK) to remove.
     * @param timestamp The timestamp of the changes.
     */
    public EnhancedSubscriptionChanges(List <? extends IPodcast > add,
                                       List <? extends IPodcast > remove, long timestamp) {
        this.add = new ArrayList<Podcast>(add.size());
        for (IPodcast ip : add) {
            Podcast p = new Podcast(ip);
            p.setLastUpdate(0);
            this.add.add(p);
        }

        this.remove = new ArrayList<Podcast>(remove.size());
        for (IPodcast ip : remove) {
            Podcast p = new Podcast(ip);
            p.setLastUpdate(timestamp);
            this.remove.add(p);
        }

        this.timestamp = timestamp;
    }

    public List<Podcast> getAdd() {
        return add;
    }

    public Set<String> getAddUrls() {
        return getUrlsFromPodcasts(getAdd());
    }

    public List<Podcast> getRemove() {
        return remove;
    }

    public Set<String> getRemoveUrls() {
        return getUrlsFromPodcasts(getRemove());
    }

    private static Set<String> getUrlsFromPodcasts(List<Podcast> from) {
        Set<String> to = new HashSet<String>(from.size());

        for (Podcast p : from) {
            to.add(p.getUrl());
        }

        return to;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(add);
        dest.writeList(remove);
        dest.writeLong(timestamp);
    }

    public static final Parcelable.Creator<EnhancedSubscriptionChanges> CREATOR = new
    Creator<EnhancedSubscriptionChanges>() {
        @Override
        public EnhancedSubscriptionChanges[] newArray(int size) {
            return new EnhancedSubscriptionChanges[size];
        }

        @Override
        @SuppressWarnings("unchecked")
        public EnhancedSubscriptionChanges createFromParcel(Parcel source) {
            List<IPodcast> a = source.readArrayList(IPodcast.class.getClassLoader());
            List<IPodcast> r = source.readArrayList(IPodcast.class.getClassLoader());
            long t = source.readLong();
            return new EnhancedSubscriptionChanges(a, r, t);
        }
    };
}
