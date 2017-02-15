package uk.gov.justice.digital.noms.hub.util

import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoDatabase

class MetadataStore {
    public static final String CONTENT_ITEM_COLLECTION = 'contentItem'

    MongoDatabase database

    def connect() {
        MongoClient mongoClient =
                new MongoClient(new MongoClientURI(System.getenv('MONGODB_CONNECTION_URI') ?: 'mongodb://localhost:27017'))
        database = mongoClient.getDatabase('hub_metadata')
    }

    def documentIsPresentWithFilename = {
        String filename ->
            database.getCollection(CONTENT_ITEM_COLLECTION).find(new BasicDBObject(filename: filename)).first() != null
    }

    def removeDocumentsWithFilenames(String... filenames) {
        filenames.each {
            database.getCollection(CONTENT_ITEM_COLLECTION).deleteMany(new BasicDBObject(filename: it))
        }
    }

}
