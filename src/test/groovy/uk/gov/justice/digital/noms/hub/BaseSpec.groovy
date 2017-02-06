package uk.gov.justice.digital.noms.hub

import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.StorageException
import com.microsoft.azure.storage.blob.BlobContainerPermissions
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType
import com.microsoft.azure.storage.blob.CloudBlobContainer
import com.microsoft.azure.storage.blob.CloudBlockBlob
import geb.spock.GebSpec
import groovy.util.logging.Slf4j

import java.security.InvalidKeyException

@Slf4j
class BaseSpec extends GebSpec {
    protected static final String AZURE_CONTAINER_NAME = 'content-items'

    protected String azureBlobStorePublicUrlBase
    protected String adminUiUrl
    protected CloudBlobContainer container


    def setup() {
        setAdminUrl()
        setupAzureBlobStore()
        go adminUiUrl
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

    protected removeFileFromMediaStoreWithFilename(String... filenames) throws URISyntaxException, StorageException {
        filenames.each {
            CloudBlockBlob blob = container.getBlockBlobReference(it)
            blob.deleteIfExists()
        }
    }

}
