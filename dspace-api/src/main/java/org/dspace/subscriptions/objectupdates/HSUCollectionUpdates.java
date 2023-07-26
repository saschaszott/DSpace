package org.dspace.subscriptions.objectupdates;

import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.discovery.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class HSUCollectionUpdates extends CollectionUpdates {

    @Autowired
    private SearchService searchService;

    /**
     * Suche nach DS-Items, die neu zum Repository hinzugefügt (d.h. freigegeben) wurden.
     *
     * @param context current DSpace session.
     * @param dSpaceObject
     * @param frequency
     * @return
     * @throws SearchServiceException
     */
    @Override
    public List<IndexableObject> findUpdates(Context context, DSpaceObject dSpaceObject, String frequency)
            throws SearchServiceException {
        DiscoverQuery discoverQuery = new DiscoverQuery();
        getDefaultFilterQueries().stream().forEach(fq -> discoverQuery.addFilterQueries(fq));
        discoverQuery.addFilterQueries("location.coll:(" + dSpaceObject.getID() + ")");

        // see discussion in Github issue https://github.com/DSpace/DSpace/issues/6583
        discoverQuery.addFilterQueries("dc.date.accessioned_dt:" + findLastFrequency(frequency));

        DiscoverResult discoverResult = searchService.search(context, discoverQuery);
        return discoverResult.getIndexableObjects();
    }
}
