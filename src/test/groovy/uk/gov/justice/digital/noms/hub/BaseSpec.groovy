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

import java.security.InvalidKeyException

@Slf4j
class BaseSpec extends GebSpec {
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
            adminUiUrl = 'http://localhost:3000/'
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

    def documentIsPresentInMongoDbWithFilename = {
        String filename ->
            mongoDatabase.getCollection(CONTENT_ITEM_COLLECTION).find(new BasicDBObject(filename: filename)).first() != null
    }

    protected removeFileFromMediaStoreWithFilename(String... filenames) throws URISyntaxException, StorageException {
        filenames.each {
            CloudBlockBlob blob = container.getBlockBlobReference(it)
            blob.deleteIfExists()
        }
    }

    protected removeDocumentFromMongoDbWithFilename(String... filenames) {
        filenames.each {
            mongoDatabase.getCollection(CONTENT_ITEM_COLLECTION).deleteMany(new BasicDBObject(filename: it))
        }
    }

}
