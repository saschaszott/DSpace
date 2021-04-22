/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.profile.service;

import java.sql.SQLException;
import java.util.List;

import org.dspace.app.orcid.model.OrcidTokenResponseDTO;
import org.dspace.app.profile.OrcidEntitySyncPreference;
import org.dspace.app.profile.OrcidProfileSyncPreference;
import org.dspace.app.profile.OrcidSyncMode;
import org.dspace.app.profile.ResearcherProfile;
import org.dspace.content.Item;
import org.dspace.core.Context;

/**
 * Service that handle the the syncronization between a DSpace profile and the
 * relative ORCID profile, if any.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public interface ProfileOrcidSynchronizationService {

    /**
     * Configure the given profile with the data present in the given ORCID token.
     * This action is required to synchronize profile and related entities with
     * ORCID.
     *
     * @param  context      the relevant DSpace Context.
     * @param  profile      the profile to configure
     * @param  token        the ORCID token
     * @throws SQLException if a SQL error occurs during the profile update
     */
    public void linkProfile(Context context, ResearcherProfile profile, OrcidTokenResponseDTO token)
        throws SQLException;

    /**
     * Set the publications synchronization preference for the given profile.
     *
     * @param  context                  the relevant DSpace Context.
     * @param  profile                  the researcher profile to update
     * @param  value                    the new synchronization preference value
     * @throws SQLException             if a SQL error occurs during the profile
     *                                  update
     * @throws IllegalArgumentException if the given researcher profile is no linked
     *                                  with an ORCID account
     */
    public void setPublicationPreference(Context context, ResearcherProfile researcherProfile,
        OrcidEntitySyncPreference value) throws SQLException;

    /**
     * Set the projects synchronization preference for the given profile.
     *
     * @param  context                  the relevant DSpace Context.
     * @param  profile                  the researcher profile to update
     * @param  value                    the new synchronization preference value
     * @throws SQLException             if a SQL error occurs during the profile
     *                                  update
     * @throws IllegalArgumentException if the given researcher profile is no linked
     *                                  with an ORCID account
     */
    public void setProjectPreference(Context context, ResearcherProfile researcherProfile,
        OrcidEntitySyncPreference value) throws SQLException;

    /**
     * Update the profile's synchronization preference for the given profile.
     *
     * @param  context                  the relevant DSpace Context.
     * @param  profile                  the researcher profile to update
     * @param  value                    the new synchronization preference value
     * @throws SQLException             if a SQL error occurs during the profile
     *                                  update
     * @throws IllegalArgumentException if the given researcher profile is no linked
     *                                  with an ORCID account
     */
    public void setProfilePreference(Context context, ResearcherProfile researcherProfile,
        List<OrcidProfileSyncPreference> values) throws SQLException;

    /**
     * Set the ORCID synchronization mode for the given profile.
     *
     * @param  context      the relevant DSpace Context.
     * @param  profile      the researcher profile to update
     * @param  value        the new synchronization mode value
     * @throws SQLException if a SQL error occurs during the profile update
     */
    public void setSynchronizationMode(Context context, ResearcherProfile researcherProfile,
        OrcidSyncMode value) throws SQLException;

    /**
     * Check if the given researcher profile is configured to synchronize the given
     * item with ORCID.
     *
     * @param  profile the researcher profile
     * @param  item    the entity type to check
     * @return         true if the given entity type can be synchronize with ORCID,
     *                 false otherwise
     */
    public boolean isSynchronizationEnabled(ResearcherProfile profile, Item item);

    /**
     * Check if the given researcher profile item is configured to synchronize the
     * given item with ORCID.
     *
     * @param  profile the researcher profile item
     * @param  item    the entity type to check
     * @return         true if the given entity type can be synchronize with ORCID,
     *                 false otherwise
     */
    public boolean isSynchronizationEnabled(Item profile, Item item);
}
