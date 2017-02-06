package uk.gov.justice.digital.noms.hub

import com.mongodb.BasicDBObject
import groovy.util.logging.Slf4j
import org.bson.Document
import spock.lang.Ignore

import static org.awaitility.Awaitility.await

@Slf4j
class UploadSamaritansPosterSpec extends BaseSpec {
    private static final String IMAGE_FILE_NAME = 'hub-feature-specs-test-image.jpg'
    private static final String TITLE = 'hub-feature-specs:Upload Samaritan Posters:-Automated Test - 1'
    private static final String CATEGORY = 'education'

    private File file

    def setup() {
        file = new File(this.getClass().getResource("/${IMAGE_FILE_NAME}").toURI())
    }

    @Ignore('A future feature that was supported in the walking skeleton')
    def 'Upload Samaritan Posters'() {
        given: 'that I have selected an image'
        $('form').file = file.absolutePath
        assert $('form').file.contains(IMAGE_FILE_NAME)

        and: 'provided a title'
        $('form').title = TITLE
        assert $('form').title == TITLE

        and: 'provided a category'
        $('form').category = CATEGORY
        assert $('form').category == CATEGORY

        when: 'I click Save'
        $('input[type=submit]').click()

        then: 'the image and title are published'
        await().until{ documentIsPresentInMongoDbWithFilename IMAGE_FILE_NAME }

        Document document = mongoDatabase.getCollection(CONTENT_ITEM_COLLECTION).find(new BasicDBObject(title: TITLE)).first()
        document != null
        document.title == TITLE
        document.category == CATEGORY
        document.uri == "${azureBlobStorePublicUrlBase}/${AZURE_CONTAINER_NAME}/${IMAGE_FILE_NAME}"
        container.getBlockBlobReference(IMAGE_FILE_NAME).exists()
    }

    def cleanup() {
        removeDocumentFromMongoDbWithFilename IMAGE_FILE_NAME
        removeFileFromMediaStoreWithFilename IMAGE_FILE_NAME
    }

}
