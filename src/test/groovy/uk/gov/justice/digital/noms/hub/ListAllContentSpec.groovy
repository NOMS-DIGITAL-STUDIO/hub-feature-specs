package uk.gov.justice.digital.noms.hub

import geb.spock.GebSpec
import groovy.util.logging.Slf4j
import spock.lang.Shared
import uk.gov.justice.digital.noms.hub.util.Hub
import uk.gov.justice.digital.noms.hub.util.MediaStore
import uk.gov.justice.digital.noms.hub.util.MetadataStore

@Slf4j
class ListAllContentSpec extends GebSpec {
    private static final String PDF_FILENAME = 'hub-feature-specs-test-prospectus.pdf'
    private static final String VIDEO_FILENAME = 'hub-feature-specs-test-video.mp4'
    private static final String PROSPECTUS_TITLE = 'hub-feature-specs:Upload Prospectus'
    private static final String VIDEO_TITLE = 'hub-feature-specs:Upload Video'
    private static final String CATEGORY = 'Science'
    private static final String VIDEO_CONTENT_TYPE = 'video'
    private static final String PROSPECTUS_CONTENT_TYPE = 'prospectus'

    @Shared private MetadataStore metadataStore = new MetadataStore()
    @Shared private MediaStore mediaStore = new MediaStore()
    @Shared private Hub theHub = new Hub()

    def setup() {
        metadataStore.connect()
        mediaStore.connect()
    }

    def 'List all content'() {
        given: 'a prospectus is already uploaded'
        insertMetadata 1, PDF_FILENAME, PROSPECTUS_TITLE, PROSPECTUS_CONTENT_TYPE

        and: 'a video is uploaded'
        insertMetadata 2, VIDEO_FILENAME, VIDEO_TITLE, VIDEO_CONTENT_TYPE

        when: 'I am on the list all content page'
        go theHub.adminUiUri
        assert title == 'All Content'

        then: 'I see the second file uploaded at the top of the list'
        assert $("#contentItem0-title").text() == VIDEO_TITLE
        assert $("#contentItem0-content-type").text() == VIDEO_CONTENT_TYPE.capitalize()
        assert $("#contentItem0-timestamp").text() == '10:00 on 02/01/2117'

        and: 'I see the first file uploaded second in the list'
        assert $("#contentItem1-title").text() == PROSPECTUS_TITLE
        assert $("#contentItem1-content-type").text() == PROSPECTUS_CONTENT_TYPE.capitalize()
        assert $("#contentItem1-timestamp").text() == '10:00 on 01/01/2117'

    }

    def insertMetadata(int offset, String filename, String title, String contentType) {
        def metadata = [
                title:"${title}",
                contentType:"${contentType}",
                category:"${CATEGORY}",
                mediaType:"application/anything"]
        metadataStore.insertItem offset, filename, metadata
    }

    def cleanup() {
        metadataStore.removeDocumentsWithFilenames PDF_FILENAME, VIDEO_FILENAME
        mediaStore.removeContentWithFilenames PDF_FILENAME, VIDEO_FILENAME
    }

}
