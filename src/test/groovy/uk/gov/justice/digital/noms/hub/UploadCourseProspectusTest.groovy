package uk.gov.justice.digital.noms.hub

import com.mongodb.BasicDBObject
import groovy.util.logging.Slf4j
import org.bson.Document

import static org.awaitility.Awaitility.await

@Slf4j
class UploadCourseProspectusTest extends BaseTest {
    private static final String PDF_FILE_NAME = 'MEng Mathematics and Computer Science.pdf'
    private static final String TITLE_STR = 'hub-feature-tests:Upload Course Prospectus'
    private static final String CATEGORY_STR = 'Science'

    private File file

    def setup() {
        file = new File(this.getClass().getResource("/${PDF_FILE_NAME}").toURI())
    }

    def 'Upload Course Prospectus'() {
        given: 'That I am on the Upload Prospectus page'
        assert title == 'Upload - Prospectus'

        and: 'have provided a title'
        $('form').prospectusTitle = TITLE_STR

        and: 'picked a subject'
        $('form').prospectusSubject = CATEGORY_STR

        and: 'and chosen a prospectus'
        $('form').prospectusFile = file.absolutePath

        when: 'I click Save'
        $('input[type=submit]').click()

        then: 'the prospectus is published'
        await().until(documentIsPresentInMongoDbWithFilename(PDF_FILE_NAME))

        Document document = mongoDatabase.getCollection(CONTENT_ITEM_COLLECTION).find(new BasicDBObject(filename: PDF_FILE_NAME)).first()
        document != null
        document.title == TITLE_STR
        document.category == CATEGORY_STR
        document.uri == "${azureBlobStorePublicUrlBase}/${AZURE_CONTAINER_NAME}/${PDF_FILE_NAME}"

        container.getBlockBlobReference(PDF_FILE_NAME).exists()
    }

    def cleanup() {
        removeDocumentFromMongoDbWithFilename(PDF_FILE_NAME)
        removeFileFromMediaStoreWithFilename(PDF_FILE_NAME)
    }

}
