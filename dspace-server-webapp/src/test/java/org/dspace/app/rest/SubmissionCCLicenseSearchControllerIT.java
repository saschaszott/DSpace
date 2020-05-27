/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;


import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.junit.Test;

/**
 * Class to the methods from the SubmissionCCLicenseSearchController
 * Since the CC Licenses and the corresponding URIs are obtained from the CC License API, a mock service has been
 * implemented.
 * This mock service will return a fixed set of CC Licenses using a similar structure to the ones obtained from the
 * CC License API.
 * Refer to {@link org.dspace.license.MockCCLicenseConnectorServiceImpl} for more information
 */
public class SubmissionCCLicenseSearchControllerIT extends AbstractControllerIntegrationTest {


    @Test
    public void searchRightsByQuestionsTest() throws Exception {
        getClient().perform(get(
                "/api/config/submissioncclicenses/search/rightsByQuestions?license=license2&answer_license2-field0" +
                        "=license2-field0-enum1"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.url", is("mock-license-uri")))
                   .andExpect(jsonPath("$.type", is("submissioncclicenseUrl")))
                   .andExpect(jsonPath("$._links.self.href",
                                       is("http://localhost/api/config/submissioncclicenses/search/rightsByQuestions" +
                                                  "?license=license2" +
                                                  "&answer_license2-field0=license2-field0-enum1")));
    }

    @Test
    public void searchRightsByQuestionsTestLicenseWithoutFields() throws Exception {
        getClient().perform(get("/api/config/submissioncclicenses/search/rightsByQuestions?license=license3"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.url", is("mock-license-uri")))
                   .andExpect(jsonPath("$.type", is("submissioncclicenseUrl")))
                   .andExpect(jsonPath("$._links.self.href",
                                       is("http://localhost/api/config/submissioncclicenses/search/rightsByQuestions" +
                                                  "?license=license3")));
    }

    @Test
    public void searchRightsByQuestionsNonExistingLicense() throws Exception {
        getClient().perform(get(
                "/api/config/submissioncclicenses/search/rightsByQuestions?license=nonexisting-license" +
                        "&answer_license2-field0=license2-field0-enum1"))
                   .andExpect(status().isNotFound());
    }

    @Test
    public void searchRightsByQuestionsMissingRequiredAnswer() throws Exception {
        getClient().perform(get(
                "/api/config/submissioncclicenses/search/rightsByQuestions?license=license1&answer_license1field0" +
                        "=license1field0enum1"))
                   .andExpect(status().isBadRequest());
    }

    @Test
    public void searchRightsByQuestionsAdditionalNonExistingAnswer() throws Exception {
        getClient().perform(get(
                "/api/config/submissioncclicenses/search/rightsByQuestions?license=license2" +
                        "&answer_license2field0=license2field0enum1&answer_nonexisting=test"))
                   .andExpect(status().isBadRequest());
    }
}
