package uk.gov.justice.digital.noms.hub.util

import com.gmongo.GMongo
import com.gmongo.GMongoClient
import com.mongodb.DB
import com.mongodb.MongoClientURI
import org.bson.types.ObjectId

class MetadataStore {
    public DB database
    private GMongo mongo
    private Date aDate

    def connect() {
        MongoClientURI mongoUri = new MongoClientURI(System.getenv('MONGODB_CONNECTION_URI') ?: 'mongodb://localhost:27017')
        mongo = new GMongoClient(mongoUri)
        database = mongo.getDB("hub_metadata")
        aDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", '2117-01-01T10:00:00Z')
    }

    def documentIsPresentWithFilename = {
        String filename ->
            database.contentItem.find(filename: filename) != null
    }

    def removeDocumentsWithFilenames(String... filenames) {
        filenames.each {
            database.contentItem.remove(filename: it)
        }
    }

    String insertItem(int offset, String filename, Map metadata) {
        ObjectId id = ObjectId.get()
        database.contentItem.insert(
                _id: id,
                uri: "uri${offset}",
                filename: "${filename}",
                timestamp: timestamp(offset),
                metadata: metadata
        )
        return id
    }

    String timestamp(int offsetDays) {
        return (aDate + (offsetDays - 1)).format("yyyy-MM-dd'T'HH:mm:ss'Z'")
    }

}
