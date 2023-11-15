/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository.patch.operation.resourcePolicy;

import java.util.Date;

import org.dspace.app.rest.exception.DSpaceBadRequestException;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.repository.patch.operation.PatchOperation;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.core.Context;
import org.dspace.util.MultiFormatDateParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implementation for ResourcePolicy startDate ADD patch.
 *
 * Example:
 * <code>
 * curl -X PATCH http://${dspace.server.url}/api/authz/resourcepolicies/<:id-resourcepolicy> -H "
 * Content-Type: application/json" -d '[{ "op": "add", "path": "
 * /startDate", "value": "YYYY-MM-DD"]'
 * </code>
 *
 * @author Maria Verdonck (Atmire) on 14/02/2020
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
@Component
public class ResourcePolicyStartDateAddOperation<R> extends PatchOperation<R> {

    @Autowired
    ResourcePolicyUtils resourcePolicyUtils;

    @Override
    public R perform(Context context, R resource, Operation operation) {
        checkOperationValue(operation.getValue());
        if (this.supports(resource, operation)) {
            ResourcePolicy resourcePolicy = (ResourcePolicy) resource;
            this.checkResourcePolicyForNotExistingStartDateValue(resourcePolicy);
            resourcePolicyUtils.checkResourcePolicyForConsistentStartDateValue(resourcePolicy, operation);
            this.add(resourcePolicy, operation);
            return resource;
        } else {
            throw new DSpaceBadRequestException(this.getClass() + " does not support this operation");
        }
    }

    /**
     * Performs the actual add startDate of resourcePolicy operation
     *
     * @param resourcePolicy resourcePolicy being patched
     * @param operation      patch operation
     */
    private void add(ResourcePolicy resourcePolicy, Operation operation) {
        String dateS = (String) operation.getValue();
        Date date = MultiFormatDateParser.parse(dateS);
        if (date == null) {
            throw new DSpaceBadRequestException("Invalid startDate value " + dateS);
        }
        resourcePolicy.setStartDate(date);
    }

    @Override
    public boolean supports(Object objectToMatch, Operation operation) {
        return (objectToMatch instanceof ResourcePolicy && operation.getOp().trim().equalsIgnoreCase(OPERATION_ADD)
            && operation.getPath().trim().equalsIgnoreCase(resourcePolicyUtils.OPERATION_PATH_STARTDATE));
    }

    /**
     * Throws PatchBadRequestException if a value is already set in the /startDate path.
     *
     * @param resource the resource to update
     */
    void checkResourcePolicyForNotExistingStartDateValue(ResourcePolicy resource) {
        if (resource.getStartDate() != null) {
            throw new DSpaceBadRequestException("Attempting to add a value to an already existing path.");
        }
    }
}
