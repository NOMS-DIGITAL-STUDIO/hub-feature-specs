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
class UploadVideoSpec extends GebSpec {

    private static final String MP4_FILENAME = 'hub-feature-specs-test-video_400KB.mp4'
    private static final String JPG_FILENAME = 'hub-feature-specs-test-image.jpg'
    private static final String TITLE = 'hub-feature-specs:Upload Video'
    private static final String CATEGORY = 'Science'

    private File file
    private File file2
    private MetadataStore metadataStore = new MetadataStore()
    private MediaStore mediaStore = new MediaStore()
    private Hub theHub = new Hub()
    private String contentStoreUri

    def setup() {
        metadataStore.connect()
        mediaStore.connect()

        file = new File(this.getClass().getResource("/${MP4_FILENAME}").toURI())
        file2 = new File(this.getClass().getResource("/${JPG_FILENAME}").toURI())

        contentStoreUri = "${mediaStore.getMediaStorePublicUrlBase()}/${AZURE_CONTAINER_NAME}/"
    }

    def 'Upload video'() {
        given: 'that I am on the Upload Video page'
        go theHub.adminUiUri + 'video'
        verifyThatTheCurrentPageTitleIs('Upload - Video')

        and: 'have provided a title'
        $('form').title = TITLE

        and: 'picked a subject'
        $('form').category = CATEGORY

        and: 'and chosen a video'
        $('form').main = file.absolutePath

        and: 'and chosen a thumbnail'
        $('form').thumbnail = file2.absolutePath

        when: 'I click the Save button'
        $('#upload').click()
        await().until{ $('#uploadSuccess').text() == 'Saved successfully' }

        then: 'the video and thumbnail are published'
        await().until{ metadataStore.documentIsPresentWithFilename(MP4_FILENAME) }
        Document document = metadataStore.database.contentItem.find(filename: MP4_FILENAME).first()

        document != null
        document.metadata.title == TITLE
        document.metadata.category == CATEGORY

        document.files.main == "${contentStoreUri}${MP4_FILENAME}"
        document.files.thumbnail == "${contentStoreUri}${JPG_FILENAME}"

        [MP4_FILENAME, JPG_FILENAME].each {
            mediaStore.getContainer().getBlockBlobReference(it).exists()
        }
    }

    def 'Navigate back to the All Content list page'() {
        given: 'that I am on the Upload Video page'
        go theHub.adminUiUri + 'video'
        verifyThatTheCurrentPageTitleIs('Upload - Video')

        when: 'I click the All Content link'
        $('#all-content').click()

        then: 'I am taken to the All Content list page'
        verifyThatTheCurrentPageTitleIs('All Content')
    }

    private void verifyThatTheCurrentPageTitleIs(String aTitle) {
        assert title == aTitle
    }

    def cleanup() {
        metadataStore.removeDocumentsWithFilenames MP4_FILENAME

        [MP4_FILENAME, JPG_FILENAME].each {
            mediaStore.removeContentWithFilenames it
        }
    }



}
