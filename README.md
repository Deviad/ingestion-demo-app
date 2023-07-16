# Document Ingestion Service

## Ingestion

On application startup, an event triggers the ingestion activity which makes use of Apache Lucene.
The files are scanned, and they are processed in chunks.
Via the ApplicationDocumentServiceProperties you can control the batch size.

Essentially if there are 1000 articles and the batch size is 500, you will have two
batches.

# Additional information

You can find information on the endpoints, the developer manual, the test plan, etc. in the docs folder.

