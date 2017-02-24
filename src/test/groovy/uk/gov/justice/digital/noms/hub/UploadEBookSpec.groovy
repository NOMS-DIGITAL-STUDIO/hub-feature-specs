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
    private static final String JPG_FILENAME = 'hub-feature-specs-test-image.jpg'
    private static final String TITLE = 'hub-feature-specs:Upload eBook'
    private static final String DESCRIPTION = 'A description'

    private File file
    private File file2
    private MetadataStore metadataStore = new MetadataStore()
    private MediaStore mediaStore = new MediaStore()
    private Hub theHub = new Hub()
    private String contentStoreUri

    def setup() {
        metadataStore.connect()
        mediaStore.connect()

        file = new File(this.getClass().getResource("/${PDF_FILENAME}").toURI())
        file2 = new File(this.getClass().getResource("/${JPG_FILENAME}").toURI())

        contentStoreUri = "${mediaStore.getMediaStorePublicUrlBase()}/${AZURE_CONTAINER_NAME}/"
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
        $('form').main = file.absolutePath

        and: 'and chosen a thumbnail'
        $('form').thumbnail = file2.absolutePath

        when: 'I click the Save button'
        $('#upload').click()
        await().until { $('#uploadSuccess').text() == 'Saved successfully' }

        then: 'the ebook and thumbnail are published'
        await().until { metadataStore.documentIsPresentWithFilename(PDF_FILENAME) }
        Document document = metadataStore.database.contentItem.find(filename: PDF_FILENAME).first()

        document != null
        document.metadata.title == TITLE
        document.metadata.description == DESCRIPTION

        document.files.main == "${contentStoreUri}${PDF_FILENAME}"
        document.files.thumbnail == "${contentStoreUri}${JPG_FILENAME}"

        [PDF_FILENAME, JPG_FILENAME].each {
            mediaStore.getContainer().getBlockBlobReference(it).exists()
        }
    }

    def 'Navigate back to the All Content list page'() {
        given: 'that I am on the Upload eBook page'
        go theHub.adminUiUri + 'book'
        verifyThatTheCurrentPageTitleIs('Upload - eBook')

        when: 'I click the All Content link'
        $('#all-content').click()

        then: 'I am taken to the All Content list page'
        verifyThatTheCurrentPageTitleIs('All Content')
    }

    private void verifyThatTheCurrentPageTitleIs(String aTitle) {
        assert title == aTitle
    }

    def cleanup() {
        metadataStore.removeDocumentsWithFilenames PDF_FILENAME

        [PDF_FILENAME, JPG_FILENAME].each {
            mediaStore.removeContentWithFilenames it
        }
    }
}