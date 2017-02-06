package uk.gov.justice.digital.noms.hub

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import groovy.util.logging.Slf4j

import static org.apache.http.HttpStatus.SC_CREATED
import static org.awaitility.Awaitility.await

@Slf4j
class BrowserProspectusesSpec extends BaseSpec {
    private static final String PDF_FILENAME_1 = 'hub-feature-specs-test-prospectus1.pdf'
    private static final String PDF_FILENAME_2 = 'hub-feature-specs-test-prospectus2.pdf'
    private static final String TITLE_1 = 'hub-feature-specs:Upload Course Prospectus One'
    private static final String TITLE_2 = 'hub-feature-specs:Upload Course Prospectus Two'
    private static final String CATEGORY = 'Science'

    private File file1
    private File file2

    def setup() {
        file1 = new File(this.getClass().getResource("/${PDF_FILENAME_1}").toURI())
        file2 = new File(this.getClass().getResource("/${PDF_FILENAME_2}").toURI())
    }

    def 'Browse Prospectuses'() {
        given: 'At least one prospectus already exists'
        uploadProspectus(file1, TITLE_1)
        await().until{ documentIsPresentInMongoDbWithFilename(PDF_FILENAME_1) }

        and: 'I upload a second one'
        uploadProspectus file2, TITLE_2
        await().until{ documentIsPresentInMongoDbWithFilename(PDF_FILENAME_2) }

        when: 'I view the hub admin ui'
        go adminUiUrl
        assert title == 'Upload - Prospectus'

        then: 'I see the second prospectus at the top of the list'
        assert $("#contentItem0-title").text() == TITLE_2
        assert $("#contentItem0-subject").text() == CATEGORY
        assert $("#contentItem0-filename").text() == PDF_FILENAME_2
    }

    def uploadProspectus(File file, String title) {
        HttpResponse<String> response = Unirest.post("http://hub-admin.herokuapp.com/hub-admin/content-items")
                .header('accept', 'application/json')
                .field('title', title)
                .field('file', file)
                .field('category', CATEGORY)
                .asString()
        assert response.getStatus() == SC_CREATED
    }

    def cleanup() {
        removeDocumentFromMongoDbWithFilename PDF_FILENAME_1, PDF_FILENAME_2
        removeFileFromMediaStoreWithFilename PDF_FILENAME_1, PDF_FILENAME_2
    }

}
