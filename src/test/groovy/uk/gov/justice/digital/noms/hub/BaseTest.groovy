package uk.gov.justice.digital.noms.hub

import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.StorageException
import com.microsoft.azure.storage.blob.BlobContainerPermissions
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType
import com.microsoft.azure.storage.blob.CloudBlobContainer
import com.microsoft.azure.storage.blob.CloudBlockBlob
import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoDatabase
import geb.spock.GebSpec
import groovy.util.logging.Slf4j
import org.bson.Document

import java.security.InvalidKeyException
import java.util.concurrent.Callable

@Slf4j
class BaseTest extends GebSpec {
    protected static final String AZURE_CONTAINER_NAME = 'content-items'
    protected static final String CONTENT_ITEM_COLLECTION = 'contentItem'

    protected String mongoDbUrl
    protected String azureBlobStorePublicUrlBase
    protected String adminUiUrl
    protected MongoDatabase mongoDatabase
    protected CloudBlobContainer container


    def setup() {
        setAdminUrl()
        setupMongoDB()
        setupAzureBlobStore()
        go adminUiUrl
    }

    def setupMongoDB() {
        mongoDbUrl = System.getenv('mongoDbUrl')
        if (!mongoDbUrl) {
            mongoDbUrl = 'mongodb://localhost:27017'
            log.info('mongoDbUrl: local')
        }
        MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoDbUrl))
        mongoDatabase = mongoClient.getDatabase('hub_metadata')
    }

    def setAdminUrl() {
        adminUiUrl = System.getenv('adminUiUrl')
        if (!adminUiUrl) {
            adminUiUrl = 'http://hub-admin-ui.herokuapp.com/'
        }
    }

    def setupAzureBlobStore() throws URISyntaxException, InvalidKeyException, StorageException {
        setupAzurePublicUrlBase()
        container = setupAzureCloudStorageAccount().createCloudBlobClient().getContainerReference(AZURE_CONTAINER_NAME)
        container.createIfNotExists()

        BlobContainerPermissions containerPermissions = new BlobContainerPermissions()
        containerPermissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER)
        container.uploadPermissions(containerPermissions)
    }

    def setupAzurePublicUrlBase() {
        azureBlobStorePublicUrlBase = System.getenv('azureBlobStorePublicUrlBase')
        if (!azureBlobStorePublicUrlBase) {
            azureBlobStorePublicUrlBase = 'http://digitalhub2.blob.core.windows.net'
        }
    }

    def setupAzureCloudStorageAccount() {
        String azureConnectionUri = System.getenv('azureBlobStoreConnUri')
        if (!azureConnectionUri) {
            throw new RuntimeException('azureBlobStoreConnUri environment variable was not set')
        }
        CloudStorageAccount.parse(azureConnectionUri)
    }

    Callable<Boolean> documentIsPresentInMongoDbWithFilename(String filename) {
        return new Callable<Boolean>() {
            Boolean call() throws Exception {
                Document document = mongoDatabase.getCollection(CONTENT_ITEM_COLLECTION)
                        .find(new BasicDBObject(filename: filename)).first()
                return document != null
            }
        }
    }

    protected removeFileFromMediaStoreWithFilename(String filename) throws URISyntaxException, StorageException {
        CloudBlockBlob blob = container.getBlockBlobReference(filename)
        blob.deleteIfExists()
    }

    protected removeDocumentFromMongoDbWithFilename(String filename) {
        mongoDatabase.getCollection(CONTENT_ITEM_COLLECTION).deleteMany(new BasicDBObject(filename: filename))
    }

}
