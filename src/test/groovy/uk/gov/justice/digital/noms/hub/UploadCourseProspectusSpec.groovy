package uk.gov.justice.digital.noms.hub

import com.mongodb.BasicDBObject
import groovy.util.logging.Slf4j
import org.bson.Document

import static org.awaitility.Awaitility.await

@Slf4j
class UploadCourseProspectusSpec extends BaseSpec {
    private static final String PDF_FILENAME = 'hub-feature-specs-test-prospectus1.pdf'
    private static final String TITLE = 'hub-feature-specs:Upload Course Prospectus'
    private static final String CATEGORY = 'Science'

    private File file
    private MongoUtils mongoUtils = new MongoUtils()

    def setup() {
        mongoUtils.connectToDb()
        file = new File(this.getClass().getResource("/${PDF_FILENAME}").toURI())
    }

    def 'Upload Course Prospectus'() {
        given: 'That I am on the Upload Prospectus page'
        go adminUiUrl
        verifyThatTheCurrentPageTitleIs('Upload - Prospectus')

        and: 'have provided a title'
        $('form').prospectusTitle = TITLE

        and: 'picked a subject'
        $('form').prospectusSubject = CATEGORY

        and: 'and chosen a prospectus'
        $('form').prospectusFile = file.absolutePath

        when: 'I click Save'
        $('#upload').click()
        await().until{ $('#uploadSuccess').text() == 'Saved successfully' }

        then: 'the prospectus is published'
        Document document = mongoUtils.mongoDatabase.getCollection(mongoUtils.CONTENT_ITEM_COLLECTION)
                                .find(new BasicDBObject(filename: PDF_FILENAME)).first()
        document != null
        document.title == TITLE
        document.category == CATEGORY
        document.uri == "${azureBlobStorePublicUrlBase}/${AZURE_CONTAINER_NAME}/${PDF_FILENAME}"

        container.getBlockBlobReference(PDF_FILENAME).exists()
    }

    private void verifyThatTheCurrentPageTitleIs(String aTitle) {
        assert title == aTitle
    }

    def cleanup() {
        mongoUtils.documentIsPresentWithFilename PDF_FILENAME
        removeFileFromMediaStoreWithFilename PDF_FILENAME
    }

}
