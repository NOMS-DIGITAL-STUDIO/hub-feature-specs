package uk.gov.justice.digital.noms.hub

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import groovy.util.logging.Slf4j
import spock.lang.Ignore

import java.util.concurrent.Callable

import static org.apache.http.HttpStatus.SC_CREATED
import static org.awaitility.Awaitility.await
import static org.hamcrest.CoreMatchers.containsString

@Slf4j
class ViewSamaritansPoster extends BaseTest {
    private static final String IMAGE_FILE_NAME = 'Listener caller awareness digi screens ENGLISH vB slide6.jpg'
    private static final String TITLE_STR = 'hub-function-test:Upload Samaritan Posters:-Automated Test - 1'

    private File file

    def setup() {
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
        await().until(uriIdField(), containsString('Listener caller awareness digi screens ENGLISH vB slide6'))

        and: 'its most recent title'
        $('#titleId').text() == TITLE_STR
    }

    private Callable<String> uriIdField() {
        return new Callable<String>() {
            String call() throws Exception {
                return $('#uriId').text()
            }
        }
    }

    def uploadSamaritanImage() {
        HttpResponse<String> response = Unirest.post("http://hub-admin.herokuapp.com/hub-admin/content-items")
                .header('accept', 'application/json')
                .field('title', TITLE_STR)
                .field('file', file)
                .field('category', 'education')
                .asString()
        assert response.getStatus() == SC_CREATED
    }

    def cleanup() {
        removeDocumentFromMongoDbWithFilename IMAGE_FILE_NAME
        removeFileFromMediaStoreWithFilename IMAGE_FILE_NAME
    }
}
