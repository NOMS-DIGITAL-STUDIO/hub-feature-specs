package uk.gov.justice.digital.noms.hub.util

import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.StorageException
import com.microsoft.azure.storage.blob.BlobContainerPermissions
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType
import com.microsoft.azure.storage.blob.CloudBlobContainer
import com.microsoft.azure.storage.blob.CloudBlockBlob

import java.security.InvalidKeyException

class MediaStore {
    public static final String AZURE_CONTAINER_NAME = 'content-items'

    String mediaStorePublicUrlBase

    CloudBlobContainer container

    def connect() throws URISyntaxException, InvalidKeyException, StorageException {
        setupAzurePublicUrlBase()
        container = setupAzureCloudStorageAccount().createCloudBlobClient().getContainerReference(AZURE_CONTAINER_NAME)
        container.createIfNotExists()

        BlobContainerPermissions containerPermissions = new BlobContainerPermissions()
        containerPermissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER)
        container.uploadPermissions(containerPermissions)
    }

    def setupAzurePublicUrlBase() {
        mediaStorePublicUrlBase = System.getenv('AZURE_BLOB_STORE_PUBLIC_URL_BASE') ?: 'http://127.0.0.1:10000'
    }

    static setupAzureCloudStorageAccount() {
        String azureConnectionUri = System.getenv('AZURE_BLOB_STORE_CONNECTION_URI')
        if (!azureConnectionUri) {
            azureConnectionUri = "AccountName=abc;AccountKey=YWJjCg==;BlobEndpoint=http://127.0.0.1:10000/devstoreaccount1;"
        }
        CloudStorageAccount.parse(azureConnectionUri)
    }

    def removeContentWithFilenames(String... filenames) throws URISyntaxException, StorageException {
        filenames.each {
            CloudBlockBlob blob = container.getBlockBlobReference(it)
            blob.deleteIfExists()
        }
    }

}
