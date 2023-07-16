# Metrics
The metrics can be accessed from http://localhost:8080/metrics


Documents (articles) and companies are loaded on application startup.

## Documents metrics

To check how long it takes to index the documents (articles), you can call this endpoint
http://localhost:8080/actuator/metrics/documents.index
and look for this object `{"statistic":"TOTAL_TIME","value":6.245297125}`.
In my case the value is with Java 17`7.24603075` and with Java 20 with the virtual threads I have `6.245297125`.

The number of documents which was processed successfully is available at:
http://localhost:8080/actuator/metrics/documents.processed.successfully


## Companies metrics
Companies metrics are available at:
http://localhost:8080/actuator/metrics/companies.index.execution.time



