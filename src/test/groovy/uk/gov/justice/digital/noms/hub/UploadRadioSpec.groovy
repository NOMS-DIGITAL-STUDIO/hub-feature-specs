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
class UploadRadioSpec extends GebSpec {

    private static final String MP3_FILENAME = 'hub-feature-specs-test-audio_0.7mb.mp3'
    private static final String TITLE = 'hub-feature-specs:Upload Radio'
    private static final String CHANNEL = 'Takeover'

    private File file
    private MetadataStore metadataStore = new MetadataStore()
    private MediaStore mediaStore = new MediaStore()
    private Hub theHub = new Hub()
    private String contentStoreUri

    def setup() {
        metadataStore.connect()
        mediaStore.connect()

        file = new File(this.getClass().getResource("/${MP3_FILENAME}").toURI())

        contentStoreUri = "${mediaStore.getMediaStorePublicUrlBase()}/${AZURE_CONTAINER_NAME}/"
    }

    def 'Upload radio'() {
        given: 'that I am on the Upload Radio page'
        go theHub.adminUiUri + 'radio'
        verifyThatTheCurrentPageTitleIs('Upload - Radio')

        and: 'have provided a title'
        $('form').title = TITLE

        and: 'picked a channel'
        $('form').channel = CHANNEL

        and: 'and chosen a radio show'
        $('form').main = file.absolutePath

        when: 'I click the Save button'
        $('#upload').click()
        await().until{ $('#uploadSuccess').text() == 'Saved successfully' }

        then: 'the radio show is published'
        await().until{ metadataStore.documentIsPresentWithFilename(MP3_FILENAME) }
        Document document = metadataStore.database.contentItem.find(filename: MP3_FILENAME).first()

        document != null
        document.metadata.title == TITLE
        document.metadata.channel == CHANNEL

        document.files.main == "${contentStoreUri}${MP3_FILENAME}"

        mediaStore.getContainer().getBlockBlobReference(MP3_FILENAME).exists()
    }

    def 'Navigate back to the All Content list page'() {
        given: 'that I am on the Upload Radio page'
        go theHub.adminUiUri + 'radio'
        verifyThatTheCurrentPageTitleIs('Upload - Radio')

        when: 'I click the All Content link'
        $('#all-content').click()

        then: 'I am taken to the All Content list page'
        verifyThatTheCurrentPageTitleIs('All Content')
    }

    private void verifyThatTheCurrentPageTitleIs(String aTitle) {
        assert title == aTitle
    }

    def cleanup() {
        metadataStore.removeDocumentsWithFilenames MP3_FILENAME
        mediaStore.removeContentWithFilenames MP3_FILENAME
    }



}
