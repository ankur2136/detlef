package at.ac.tuwien.detlef.filter;

import at.ac.tuwien.detlef.domain.Episode;

public class NullFilter implements EpisodeFilter {

    @Override
    public boolean filter(Episode episode) {
        return false;
    }

}
