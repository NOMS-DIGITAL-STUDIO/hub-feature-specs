package uk.gov.justice.digital.noms.hub

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import geb.spock.GebSpec
import groovy.util.logging.Slf4j
import spock.lang.Shared
import uk.gov.justice.digital.noms.hub.util.Hub
import uk.gov.justice.digital.noms.hub.util.MediaStore
import uk.gov.justice.digital.noms.hub.util.MetadataStore

import static org.apache.http.HttpStatus.SC_CREATED
import static org.awaitility.Awaitility.await

@Slf4j
class ListAllContentSpec extends GebSpec {
    private static final String PDF_FILENAME = 'hub-feature-specs-test-prospectus1.pdf'
    private static final String VIDEO_FILENAME = 'hub-feature-specs-test-prospectus2.pdf'
    private static final String PROSPECTUS_TITLE = 'hub-feature-specs:Upload Prospectus'
    private static final String VIDEO_TITLE = 'hub-feature-specs:Upload Video'
    private static final String CATEGORY = 'Science'
    private static final String VIDEO_CONTENT_TYPE = 'video'
    private static final String PROSPECTUS_CONTENT_TYPE = 'prospectus'

    @Shared private File prospectusFile
    @Shared private File videoFile
    @Shared private MetadataStore metadataStore = new MetadataStore()
    @Shared private MediaStore mediaStore = new MediaStore()
    @Shared private Hub theHub = new Hub()

    def setup() {
        metadataStore.connect()
        mediaStore.connect()
        prospectusFile = new File(this.getClass().getResource("/${PDF_FILENAME}").toURI())
        videoFile = new File(this.getClass().getResource("/${VIDEO_FILENAME}").toURI())
    }

    def 'List all content'() {
        given: 'a prospectus is already uploaded'
        uploadFile prospectusFile, PROSPECTUS_TITLE, PROSPECTUS_CONTENT_TYPE
        await().until{ metadataStore.documentIsPresentWithFilename(PDF_FILENAME) }

        and: 'a video is uploaded'
        uploadFile videoFile, VIDEO_TITLE, VIDEO_CONTENT_TYPE
        await().until{ metadataStore.documentIsPresentWithFilename(VIDEO_FILENAME) }

        when: 'I am on the list all content page'
        go theHub.adminUiUri
        assert title == 'All Content'

        then: 'I see the second file at the top of the list'
        assert $("#contentItem0-title").text() == VIDEO_TITLE
        assert $("#contentItem0-content-type").text() == VIDEO_CONTENT_TYPE.capitalize()
        assert !$("#contentItem0-timestamp").text().empty
    }

    def uploadFile(File file, String title, String contentType) {

        def metadata = """{"title":"${title}", "contentType":"${contentType}", "category":"${CATEGORY}", "mediaType":"application/anything"}"""

        HttpResponse<String> response = Unirest.post(theHub.adminUri + "/hub-admin/content-items")
                .header('accept', 'application/json')
                .field('file', file)
                .field('metadata', metadata)
                .basicAuth(theHub.username, theHub.password)
                .asString()
        assert response.getStatus() == SC_CREATED
    }

    def cleanup() {
        metadataStore.removeDocumentsWithFilenames PDF_FILENAME, VIDEO_FILENAME
        mediaStore.removeContentWithFilenames PDF_FILENAME, VIDEO_FILENAME
    }

}
