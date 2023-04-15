# Kafka end-to-end pipelines demo 

This project demonstrates usage of Kafka, Kafka Connect, KafkaStreams and ksqlDB

## Demo app
Demo application simulates book purchases and book ratings then processes the data to create reports:
* Number of purchases by book title and time period
* Average rating for book titles

Book info is stored in Postgres database, while purchases and ratings are simulated by calling endpoints defined in BookController class.  
Book data is ingested from PostgreSQL to Kafka by using Kafka Connect and JDBC Source connector.  
For every book purchase a record is written to Kafka topic called "book_purchases" and for every book rating, a record is sent to topic called "book_ratings"

Kafka Streams instance created in BookPurchaseProcessor class is using data from "books" and "book_purchases" topics, counting purchases by book isbn and time windows of 5 minutes. 
It sends results to "book_purchases_count" topic. The results are written as records with value of type BookPurchaseCountRecord, while the key of the record is isbn of the book combined with TimeWindow info  

For calculating average rating of the books, we use ksqlDB and queries defined in {project_root}/ksqldb/queries.txt
Final results are written to "book_ratings_avg_enriched" topic.

Once we have results in topics, we use Elasticsearch sink connector to ingest data from Kafka to Elastic. 
Then we can easily create visualizations in Kibana 

## Docker Compose
{project_root}/docker/docker-compose.yml file contains configuration for docker containers used for this demo:

* zookeeper
* kafka broker
* schema-registry
* kafka connect
* ksqldb server
* ksql CLI
* kafka proxy
* postgres db
* elasticsearch
* kibana


## Steps for running the demo

### 1. Generate Java classes from avro schemas 
Avro schemas are defined in {project_root}/application/src/main/resources/avro folder.  
In terminal, navigate to project root directory and run gradle task : "./gradlew generateAvroJava"

### 2. Start kafka, kafka, connect, postgresql and other components using docker
In terminal, navigate to {project_root}/docker/ and run command "docker-compose up -d"  
Wait for all docker containers to start. Give some time for all components to initialize.

### 3. Start the application 
In terminal, navigate to project root directory and run gradle task : "./gradlew bootRun"  
Application will create database table in PostgreSQL, insert sample books, and create topics in kafka.  
It provides endpoints for making book purchases and ratings, and also runs Kafka Streams app for counting purchases by the book title in specified time frames.  

### 4. Deploy Kafka JDBC Source Connector
The connector is downloaded from https://www.confluent.io/hub/confluentinc/kafka-connect-jdbc and unpacked to {project_root}/docker/jdbc-connector

Deploy connector instance using following configuration:
```yaml
{
    "name": "{{ _['pg-connector'] }}",
    "config": {
        "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
        "connection.url": "jdbc:postgresql://postgres:5432/javacro22",
        "connection.user": "admin",
        "connection.password": "admin123",
        "topic.prefix": "",
        "poll.interval.ms" : 3000,
        "table.whitelist" : "books",
        "mode" : "incrementing",
        "incrementing.column.name" : "id",
        "transforms":"createKey,extractString,setValueSchema",
        "transforms.createKey.type":"org.apache.kafka.connect.transforms.ValueToKey",
        "transforms.createKey.fields":"isbn",
        "transforms.extractString.type":"org.apache.kafka.connect.transforms.ExtractField$Key",
        "transforms.extractString.field":"isbn",
        "transforms.setValueSchema.type" : "org.apache.kafka.connect.transforms.SetSchemaMetadata$Value",
        "transforms.setValueSchema.schema.name" : "digital.porsche.ib.javacro2022.kafka_pipelines.domain.model.book.BookRecord"
    }
}
```
Send post request with this configuration as request body to http://localhost:8083/connectors  
You can use curl, Postman, Insomnia or other tool for this purpose

### 5. Start purchase simulators  
In terminal, navigate to project root directory and run gradle task : "./gradlew run"  
Simulation is making random book purchase and rating every second. Leave it running.

### 6. Execute ksqlDB queries using ksqlDC CLI
Open new terminal instance and run following command:  
docker exec -it ksqldb-cli ksql http://ksqldb-server:8088  
After that, in same terminal paste queries from {project_root}/ksqldb/queries.txt

### 7. Deploy Elasticsearch Sink Connector
The connector is downloaded from: https://www.confluent.io/hub/confluentinc/kafka-connect-elasticsearch and unpacked to {project_root}/docker/elasticsearch-connector  
Deploy the connector instance using following configuration:

```yaml
{
    "name": "{{ _['elascit-connector'] }}",
        "config": {
        "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
        "tasks.max": "1",
        "topics": "book_purchases_count,BOOK_RATINGS_AVG_ENRICHED",
        "key.ignore": "false",
        "connection.url": "http://elastic:9200",
        "type.name": "_doc"
        }
}
```
Send post request with this configuration as request body to http://localhost:8083/connectors  
You can use curl, Postman, Insomnia or other tool for this purpose

### 8. Create dashboards in Kibana
In your browser go to http://localhost:5601/app/management/kibana/indexPatterns and create two index patterns:   
* book_purchases_count
* book_ratings_avg_enriched

Navigate to http://localhost:5601/app/dashboards and create new dashboard.  

Click "Create visualization" button.  
Select "book_purchases_count" index pattern in dropdown on the left side.  
Drag "book.title.keyword" field to the "Horizontal axis" field on the right side of the page  
Drag "count" field to the "Vertical axis" field on the right side of the page  
Click "Save and return" button in the top left corner of the page.  

Click "Create visualization" button again.  
Select "book_ratings_avg_enriched" index pattern in dropdown on the left side.  
Drag "TITLE.keyword" field to the "Horizontal axis" field on the right side of the page  
Drag "AVG_RATING" field to the "Vertical axis" field on the right side of the page  
Click "Save and return" button in the top left corner of the page.  

Save dashboard by clicking "Save" button in the top left corner of the page.

If ReaderSimulator is running, graphs in the dashboard should pick up the changes every time you refresh it.  