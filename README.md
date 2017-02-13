hub-feature-specs
====

Feature specifications for the Hub.
 
Ministry of Justice.
National Offender Management Service.

Machine Setup
----

Ensure phantomjs has been installed.

Environment variable required by the tests
----
```
azureBlobStoreConnUri - The Azure blob store connection string. 
e.g. DefaultEndpointsProtocol=http;AccountName=<account name>;AccountKey=<key>
```

```
azureBlobStorePublicUrlBase - The base URL for items in the blob store.   
e.g. https://<account name>.blob.core.windows.net
```

```
mongoDbUrl - The mongo database url for the application. Defaults to mongodb://localhost:27017
e.g. mongodb:<user>:<key>==@bar.documents.azure.com:10250/?ssl=true
``` 

```
adminAppUrl - The deployed Admin UI App Url
https://somewebsite/hub-admin-ui/
``` 

```
adminRestUrl - The deployed Admin RestUrl
https://somewebsite/hub-admin/
``` 

```
contentFeedAppUrl - The deployed Content Feed App Url
https://somewebsite/hub-content-feed-ui/
``` 
