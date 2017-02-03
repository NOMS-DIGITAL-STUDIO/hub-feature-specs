package uk.gov.justice.digital.noms.hub

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import groovy.util.logging.Slf4j

import static org.apache.http.HttpStatus.SC_CREATED
import static org.awaitility.Awaitility.await

@Slf4j
class BrowserProspectusesTest extends BaseTest {
    private static final String PDF_FILE_NAME = 'MEng Mathematics and Computer Science.pdf'
    private static final String TITLE_STR = 'hub-feature-tests:Upload Course Prospectus'
    private static final String CATEGORY_STR = 'Science'

    private File file

    def setup() {
        file = new File(this.getClass().getResource("/${PDF_FILE_NAME}").toURI())
    }

    def 'Browse Prospectuses'() {
        given: 'that a prospectus is uploaded'
        uploadProspectus()
        await().until(documentIsPresentInMongoDbWithFilename(PDF_FILE_NAME))

        when: 'I view the hub admin ui'
        go adminUiUrl
        assert title == 'Upload - Prospectus'

        then: 'I see the prospectus at the top of the list'
        assert $("table tbody tr td")[0].text() == TITLE_STR
        assert $("table tbody tr td")[1].text() == CATEGORY_STR
        assert $("table tbody tr td")[2].text() == PDF_FILE_NAME
    }

    def uploadProspectus() {
        HttpResponse<String> response = Unirest.post("http://hub-admin.herokuapp.com/hub-admin/content-items")
                .header('accept', 'application/json')
                .field('title', TITLE_STR)
                .field('file', file)
                .field('category', CATEGORY_STR)
                .asString()
        assert response.getStatus() == SC_CREATED
    }

    def cleanup() {
        removeDocumentFromMongoDbWithFilename PDF_FILE_NAME
        removeFileFromMediaStoreWithFilename PDF_FILE_NAME
    }

}
