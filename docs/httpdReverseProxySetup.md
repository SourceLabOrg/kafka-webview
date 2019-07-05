# Kafka WebView Documentation

## Setting up Kafka WebView behind httpd (apache) reverse proxy.

The following steps have been verified against Apache Httpd version **2.4.5** and higher.

This guide assumes you've already successfully installed apache httpd.  If not, we recommend you review [Compiling and Installing](http://httpd.apache.org/docs/2.4/install.html) documentation.

### Loading Modules

You must configure HTTPD to load the following modules:
- proxy_module
- proxy_http_module
- proxy_wstunnel_module

Typically in your `httpd.conf` file you ensure the following lines exist:

```
LoadModule proxy_module libexec/apache2/mod_proxy.so
LoadModule proxy_http_module libexec/apache2/mod_proxy_http.so
LoadModule proxy_wstunnel_module libexec/apache2/mod_proxy_wstunnel.so
```

### Reverse Proxy without URL Prefix

In the following example lets assume:
- **reverseproxy.example.com** is the hostname your reverse proxy is serving requests on.
- **internal-hostname:8080** is the hostname Kafka WebView is listening for requests on.
   
Add a new VirtualHost entry to HTTPD. It should be configured to look like: 

```
<VirtualHost *:80>
    ServerName reverseproxy.example.com
    
    ProxyPass         /websocket/info http://internal-hostname:8080/websocket/info
    ProxyPassReverse  /websocket/info http://internal-hostname:8080/websocket/info
    ProxyPass         /websocket/ ws://internal-hostname:8080/websocket/
    ProxyPassReverse  /websocket/ ws://internal-hostname:8080/websocket/
    ProxyPass         / http://internal-hostname:8080/ nocanon
    ProxyPassReverse  / http://internal-hostname:8080/
    ProxyRequests off
</VirtualHost>
```

You should be able to now access Kafka Webview by accessing **http://reverseproxy.example.com/**.

### Reverse Proxy with Prefixed URL


In the following example lets assume:
- **reverseproxy.example.com** is the hostname your reverse proxy is serving requests on.
- **internal-hostname:8080** is the hostname Kafka WebView is listening for requests on.
- **/kafka-webview-prefix/** is the URL prefix that you want routed to Kafka Webview.

In this example, the URL you would be making requests on: **http://reverseproxy.example.com/kafka-webview-prefix/**  

Add a new VirtualHost entry to HTTPD. It should be configured to look like: 

```
<VirtualHost *:80>
    ServerName reverseproxy.example.com
    
    ProxyPass         /kafka-webview-prefix/websocket/info http://internal-hostname:8080/kafka-webview-prefix/websocket/info
    ProxyPassReverse  /kafka-webview-prefix/websocket/info http://internal-hostname:8080/kafka-webview-prefix/websocket/info
    ProxyPass         /kafka-webview-prefix/websocket/ ws://internal-hostname:8080/kafka-webview-prefix/websocket/
    ProxyPassReverse  /kafka-webview-prefix/websocket/ ws://internal-hostname:8080/kafka-webview-prefix/websocket/
    ProxyPass         /kafka-webview-prefix/ http://internal-hostname:8080/kafka-webview-prefix/ nocanon
    ProxyPassReverse  /kafka-webview-prefix/ http://internal-hostname:8080/kafka-webview-prefix/
    ProxyRequests off
</VirtualHost>
```

Update your Kafka WebView `config.yml` file to include the following:

```yml
server:
  servlet:
    context-path: /kafka-webview-prefix
```

You should be able to now access Kafka Webview by accessing **http://reverseproxy.example.com/kafka-webview-prefix/**.
