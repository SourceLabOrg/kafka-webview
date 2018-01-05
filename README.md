# Kafka WebView

This project aims to be a full-featured web-based [Apache Kafka](https://kafka.apache.org/) consumer.  **Kafka WebView** presents an easy-to-use web based interface for reading data out of kafka topics and providing basic filtering and searching capabilities.

### Features

- Connect to multiple remote Kafka Clusters
- Connect to SSL authenticated clusters
- Supports standard key and value deserializers
- Supports uploading custom key and value deserializers
- Supports both customizable and enforced filtering over topics
- Basic user management
- Web Based Consumer Supports
  - Seeking to offsets
  - Seeking to timestamps
  - Filtering by partition
  - Configurable server-side filtering logic
- "Live" web socket based streaming consumer
  
### Screen Shots

#### Web Consumer
![Web Consumer Screenshot](images/webconsumer.png)

#### Streaming Consumer
![Streaming Consumer Screenshot](images/streamingConsumer.png)

#### Configuration
![Configuration Screenshot](images/configuration.png)

## Installation ##

TODO

## Setup ##

### 1. Setup users
                    
You first need to configure who has access to Kafka WebView.  Kafka WebView provides two roles for users: 
<strong>Admin</strong> and <strong>User</strong>.  

- <strong>Admin</strong> - has the ability to Manage and Configure all aspects of WebView, including defining Kafka Clusters, adding/removing users, defining Views etc.  
- <strong>User</strong> - has the ability to view Cluster information and consume previously defined Views.

If you've logged in with the Default Admin account, you'll want to create your own Administrator user account
and remove the default one.

### 2. Connect Kafka clusters

You'll need to let WebView know about what Kafka clusters you want to connect to.

WebView supports connecting to Clusters using SSL.  You'll need to follow the [standard Kafka consumer client directions](https://kafka.apache.org/documentation.html#security_ssl) to
create a Java Key Store (JKS) for your Trusted CA (TrustStore), and a JKS for your Consumer Key (KeyStore).

### 3. Configure custom Message Formats (Optional)

Kafka allows you to store data within the Cluster in any data-format and provides an Interface for
understanding how to Deserialize your data.  Out of the box Kafka WebView supports the following Deserializers that can be
used for both Keys and Values:

- ByteArray
- Bytes
- Double
- Float
- Integer
- Long
- Short
- String

Often times data is stored using a custom format such as [Avro](https://avro.apache.org/) or [ProtocolBuffers](https://developers.google.com/protocol-buffers/).
Admin users can upload a JAR containing custom Deserializer implementations to extend support to WebView to be able to properly deserialize your data format.

### 4. Configure Filters (Optional)

Filters are a construct unique to WebView.  Filters allow you to implement an Interface that can be used on the <strong>server side</strong> to filter messages coming from Kafka.  There are several benefits to doing
filtering on the server side in this way.  These can be used as a simple search-like filter and avoid passing large amounts of data
to the client web browser when you're looking for a small subset of messages.  Filters could also be used to enforce a restricted view of data from a Topic.

### 5. Define Views

Views are the last step where you put everything together.  Views let you configure what Topic you want to consume from, configure which Message Formats the Topic uses, and apply any Filters.<br/>

## Writing Custom Deserializers

The [Deserializer Interface](https://kafka.apache.org/0110/javadoc/org/apache/kafka/common/serialization/Deserializer.html)
is provided by Kafka, WebView requires nothing special or additional above implementing this interface.  If you already 
have a Deserializer implementation for consuming from Kafka then you simply can just use it as is.

If you don't already have an implementation, you can view the [interface here](https://github.com/apache/kafka/blob/0.11.0/clients/src/main/java/org/apache/kafka/common/serialization/Deserializer.java).

## Writing Custom Filters

The [RecordFilter Interface](https://github.com/Crim/kafka-webview/blob/master/kafka-webview-plugin/src/main/java/org/sourcelab/kafkaview/plugin/filter/RecordFilter.java)
is provided by Kafka WebView and is NOT part of the standard Kafka library.

## Example Deserializer and Filters Project

To get up and going quickly, the [Kafka-WebView-Example](#) project on GitHub can be cloned and used as a template.
This Maven based example project is configured with all of the correct dependencies and has a few example 
implementations.

### Packaging a Jar

If you're using the [Kafka-WebView-Example](#) project, it should be as simple as issuing the command `mvn package` and 
retrieving the compiled Jar from the target/ directory.
               
If you're building from your own project, you'll need to package a Jar that contains your implementation along with
any of it's required dependencies.

# Changelog

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

[View Changelog](CHANGELOG.md)
