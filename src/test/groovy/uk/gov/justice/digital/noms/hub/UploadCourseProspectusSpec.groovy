package uk.gov.justice.digital.noms.hub

import com.mongodb.BasicDBObject
import geb.spock.GebSpec
import groovy.util.logging.Slf4j
import org.bson.Document
import uk.gov.justice.digital.noms.hub.util.MediaStore
import uk.gov.justice.digital.noms.hub.util.MetadataStore

import static org.awaitility.Awaitility.await
import static MediaStore.AZURE_CONTAINER_NAME
import static MetadataStore.CONTENT_ITEM_COLLECTION

@Slf4j
class UploadCourseProspectusSpec extends GebSpec {
    private static final String PDF_FILENAME = 'hub-feature-specs-test-prospectus1.pdf'
    private static final String TITLE = 'hub-feature-specs:Upload Course Prospectus'
    private static final String CATEGORY = 'Science'

    private File file
    private MetadataStore metadataStore = new MetadataStore()
    private MediaStore mediaStore = new MediaStore()
    private String adminUiUrl

    def setup() {
        metadataStore.connect()
        mediaStore.connect()
        adminUiUrl = System.getenv('adminUiUrl') ?: 'http://localhost:3000/'
        file = new File(this.getClass().getResource("/${PDF_FILENAME}").toURI())
    }

    def 'Upload Course Prospectus'() {
        given: 'that I am on the Upload Prospectus page'
        go adminUiUrl
        verifyThatTheCurrentPageTitleIs('Upload - Prospectus')

        and: 'have provided a title'
        $('form').prospectusTitle = TITLE

        and: 'picked a subject'
        $('form').prospectusSubject = CATEGORY

        and: 'and chosen a prospectus'
        $('form').prospectusFile = file.absolutePath

        when: 'I click the Save button'
        $('#upload').click()
        await().until{ $('#uploadSuccess').text() == 'Saved successfully' }

        then: 'the prospectus is published'
        Document document = metadataStore.getDatabase().getCollection(CONTENT_ITEM_COLLECTION)
                                .find(new BasicDBObject(filename: PDF_FILENAME)).first()
        document != null
        document.title == TITLE
        document.category == CATEGORY
        document.uri == "${mediaStore.getMediaStorePublicUrlBase()}/${AZURE_CONTAINER_NAME}/${PDF_FILENAME}"

        mediaStore.getContainer().getBlockBlobReference(PDF_FILENAME).exists()
    }

    private void verifyThatTheCurrentPageTitleIs(String aTitle) {
        assert title == aTitle
    }

    def cleanup() {
        metadataStore.documentIsPresentWithFilename PDF_FILENAME
        mediaStore.removeContentWithFilenames PDF_FILENAME
    }

}
