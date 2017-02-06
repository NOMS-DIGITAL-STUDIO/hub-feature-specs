package uk.gov.justice.digital.noms.hub

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import geb.spock.GebSpec
import groovy.util.logging.Slf4j
import spock.lang.Ignore
import uk.gov.justice.digital.noms.hub.util.MediaStore
import uk.gov.justice.digital.noms.hub.util.MetadataStore

import static org.apache.http.HttpStatus.SC_CREATED
import static org.awaitility.Awaitility.await

@Slf4j
class ViewSamaritansPosterSpec extends GebSpec {
    private static final String IMAGE_FILE_NAME = 'hub-feature-specs-test-image.jpg'
    private static final String TITLE = 'hub-feature-specs:Upload Samaritan Posters:-Automated Test - 1'

    private File file
    private MetadataStore metadataStore = new MetadataStore()
    private MediaStore mediaStore = new MediaStore()

    def setup() {
        metadataStore.connect()
        mediaStore.connect()
        file = new File(this.getClass().getResource("/${IMAGE_FILE_NAME}").toURI())
    }

    @Ignore('A future feature that was supported in the walking skeleton')
    def 'View Samaritans Poster'() {
        given: 'that is content uploaded'
        uploadSamaritanImage()

        when: 'I view the hub'
        go 'https://noms-digital-studio.github.io/hub-content-feed-ui/'

        then: 'I see the last image uploaded'
        $('h1').text() == 'Hub Content Feed'
        await().until{ $('#uriId').text() == 'Listener caller awareness digi screens ENGLISH vB slide6' }

        and: 'its most recent title'
        $('#titleId').text() == TITLE
    }

    def uploadSamaritanImage() {
        HttpResponse<String> response = Unirest.post("http://hub-admin.herokuapp.com/hub-admin/content-items")
                .header('accept', 'application/json')
                .field('title', TITLE)
                .field('file', file)
                .field('category', 'education')
                .asString()
        assert response.getStatus() == SC_CREATED
    }

    def cleanup() {
        metadataStore.removeDocumentsWithFilenames IMAGE_FILE_NAME
        mediaStore.removeContentWithFilenames IMAGE_FILE_NAME
    }
}
