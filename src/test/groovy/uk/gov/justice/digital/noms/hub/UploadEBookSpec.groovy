package uk.gov.justice.digital.noms.hub

import geb.spock.GebSpec
import groovy.util.logging.Slf4j
import org.bson.Document
import uk.gov.justice.digital.noms.hub.util.Hub
import uk.gov.justice.digital.noms.hub.util.MediaStore
import uk.gov.justice.digital.noms.hub.util.MetadataStore

import static MediaStore.AZURE_CONTAINER_NAME
import static org.awaitility.Awaitility.await

@Slf4j
class UploadEBookSpec extends GebSpec {
    private static final String PDF_FILENAME = 'hub-feature-specs-test-ebook.pdf'
    private static final String TITLE = 'hub-feature-specs:Upload eBook'
    private static final String DESCRIPTION = 'A description'

    private File file
    private MetadataStore metadataStore = new MetadataStore()
    private MediaStore mediaStore = new MediaStore()
    private Hub theHub = new Hub()

    def setup() {
        metadataStore.connect()
        mediaStore.connect()
        file = new File(this.getClass().getResource("/${PDF_FILENAME}").toURI())
    }

    def 'Upload eBook'() {
        given: 'that I am on the Upload eBook page'
        go theHub.adminUiUri + 'book'
        verifyThatTheCurrentPageTitleIs('Upload - eBook')

        and: 'have provided a title'
        $('form').title = TITLE

        and: 'have provided a description'
        $('form').description = DESCRIPTION

        and: 'and chosen an eBook'
        $('form').mainFile = file.absolutePath

        when: 'I click the Save button'
        $('#upload').click()
        await().until{ $('#uploadSuccess').text() == 'Saved successfully' }

        then: 'the prospectus is published'
        await().until{ metadataStore.documentIsPresentWithFilename(PDF_FILENAME) }
        Document document = metadataStore.database.contentItem.find(filename: PDF_FILENAME).first()
        document != null
        document.metadata.title == TITLE
        document.metadata.description == DESCRIPTION
        document.uri == "${mediaStore.getMediaStorePublicUrlBase()}/${AZURE_CONTAINER_NAME}/${PDF_FILENAME}"

        mediaStore.getContainer().getBlockBlobReference(PDF_FILENAME).exists()
    }

    private void verifyThatTheCurrentPageTitleIs(String aTitle) {
        assert title == aTitle
    }

    def cleanup() {
        metadataStore.removeDocumentsWithFilenames PDF_FILENAME
        mediaStore.removeContentWithFilenames PDF_FILENAME
    }

}
