package uk.gov.justice.digital.noms.hub

import com.mongodb.BasicDBObject
import geb.spock.GebSpec
import groovy.util.logging.Slf4j
import org.bson.Document
import uk.gov.justice.digital.noms.hub.util.MediaStore
import uk.gov.justice.digital.noms.hub.util.MetadataStore
import uk.gov.justice.digital.noms.hub.util.Hub

import static org.awaitility.Awaitility.await
import static MediaStore.AZURE_CONTAINER_NAME
import static MetadataStore.CONTENT_ITEM_COLLECTION

@Slf4j
class UploadVideoSpec extends GebSpec {

    private static final String MP4_FILENAME = 'hub-feature-specs-test-video_400KB.mp4'
    private static final String TITLE = 'hub-feature-specs:Upload Video'
    private static final String CATEGORY = 'Science'

    private File file
    private MetadataStore metadataStore = new MetadataStore()
    private MediaStore mediaStore = new MediaStore()
    private Hub theHub = new Hub()
    private String videoUploadUrl

    def setup() {
        metadataStore.connect()
        mediaStore.connect()
        videoUploadUrl = theHub.adminUiUri + 'video'
        file = new File(this.getClass().getResource("/${MP4_FILENAME}").toURI())
    }

    def 'Upload video'() {
        given: 'that I am on the Upload Video page'
        go videoUploadUrl
        verifyThatTheCurrentPageTitleIs('Upload - Video')

        and: 'have provided a title'
        $('form').title = TITLE

        and: 'picked a subject'
        $('form').category = CATEGORY

        and: 'and chosen a video'
        $('form').mainFile = file.absolutePath

        when: 'I click the Save button'
        $('#upload').click()
        await().until{ $('#uploadSuccess').text() == 'Saved successfully' }

        then: 'the video is published'
        Document document = metadataStore.getDatabase().getCollection(CONTENT_ITEM_COLLECTION)
                .find(new BasicDBObject(filename: MP4_FILENAME)).first()
        document != null
        document.metadata.title == TITLE
        document.metadata.category == CATEGORY
        document.uri == "${mediaStore.getMediaStorePublicUrlBase()}/${AZURE_CONTAINER_NAME}/${MP4_FILENAME}"

        mediaStore.getContainer().getBlockBlobReference(MP4_FILENAME).exists()
    }

    private void verifyThatTheCurrentPageTitleIs(String aTitle) {
        assert title == aTitle
    }

    def cleanup() {
        metadataStore.documentIsPresentWithFilename MP4_FILENAME
        mediaStore.removeContentWithFilenames MP4_FILENAME
    }



}
