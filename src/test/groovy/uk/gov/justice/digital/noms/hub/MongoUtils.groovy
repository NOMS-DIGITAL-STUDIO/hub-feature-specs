package uk.gov.justice.digital.noms.hub

import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoDatabase

class MongoUtils {
    public static final String CONTENT_ITEM_COLLECTION = 'contentItem'

    private String mongoDbUrl
    private MongoDatabase mongoDatabase

    def connectToDb() {
        mongoDbUrl = System.getenv('mongoDbUrl')
        if (!mongoDbUrl) {
            mongoDbUrl = 'mongodb://localhost:27017'
            log.info('mongoDbUrl: local')
        }
        MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoDbUrl))
        mongoDatabase = mongoClient.getDatabase('hub_metadata')
    }

    def documentIsPresentWithFilename = {
        String filename ->
            mongoDatabase.getCollection(CONTENT_ITEM_COLLECTION).find(new BasicDBObject(filename: filename)).first() != null
    }

    def removeDocumentWithFilename(String... filenames) {
        filenames.each {
            mongoDatabase.getCollection(CONTENT_ITEM_COLLECTION).deleteMany(new BasicDBObject(filename: it))
        }
    }

}
