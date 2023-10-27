/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.sherpa.v2;

/**
 * Enum that list the supported format by Sherpa API.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4Science)
 *
 */
public enum SHERPAFormat {

    JSON("Json"),
    IDS("Ids");

    private final String value;

    private SHERPAFormat(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
