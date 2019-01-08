# Change Log
The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## 2.1.1 (01/08/2019)
#### New Features
- Added ability to Copy previously created views.
- Better expose underlying exceptions/errors when things go wrong.

#### Bug fixes
- [ISSUE-116](https://github.com/SourceLabOrg/kafka-webview/issues/116) No longer require KeyStore and KeyStore password when configuring a SSL+SASL cluster.

## 2.1.0 (12/20/2018)
#### New Features
- Add support for SASL authentication to Kafka clusters.
- Add support for LDAP user authentication to application.
- Consumer details can now be viewed on Clusters.
- Explicitly expose user login session timeout configuration value in documentation.

#### Bug fixes
- Topics shown on brokers now include "internal" topics.
- Generated consumer client.id shortened.
- [#111](https://github.com/SourceLabOrg/kafka-webview/issues/111) Add ProtocolBuffer support to Jackson via third party module.

#### Internal Dependency Updates
- Upgrade from Spring Boot 2.0.5 to 2.0.7
- Updated Kafka Client library version to 2.0.0
- Updated NPM dependencies:
  - sockjs-client from 1.1.4 to 1.3.0
  - browser-sync from 2.16.0 to 2.26.3
  - gulp-sass from 3.1.0 to 3.2.1

## 2.0.1 (10/02/2018)
- [Issue#100](https://github.com/SourceLabOrg/kafka-webview/issues/100) Fix start.sh script
- Dependency JARs were accidentally being included twice in release packages.

## 2.0.0 (09/24/2018)

- Added new Stream consumer management page at /configuration/stream
- Added ability to disable user authentication.  This allows for anonymous user access.
- Added ability to create topics via the UI.
- Added ability to modify topic configuration via the UI.
- Various UI usability improvements.
- Updated SpringBoot framework from 1.5.x to 2.0.5.
- Updates Kafka Client from 0.11.0.2 to 1.1.1.

### Breaking Changes

Kafka-WebView's configuration file has changed slightly.  Most notably the `require-ssl` property has moved and been renamed requiredSsl.
Please review the default [config.yml](kafka-webview-ui/src/assembly/distribution/config.yml) for references to the new configuration options. 

## 1.0.5 (06/22/2018)
- [Issue#75](https://github.com/SourceLabOrg/kafka-webview/issues/75) Bugfix Add Jackson serializer that falls back to using toString() when consuming entries from Kafka.
- [Issue#72](https://github.com/SourceLabOrg/kafka-webview/issues/72) Bugfix User role input not displayed when creating new user.
- [Issue#81](https://github.com/SourceLabOrg/kafka-webview/issues/81) Bugfix Handle NoClassDefFoundError exceptions gracefully.
- [Issue#74](https://github.com/SourceLabOrg/kafka-webview/issues/74) Improvement UI Tweak to display large number of partitions in datatable views.
- [Issue#71](https://github.com/SourceLabOrg/kafka-webview/issues/71) Improvement Topics sorted in select boxes.
- [Issue#83](https://github.com/SourceLabOrg/kafka-webview/issues/83) Improvement Wrap key and message values in <pre></pre> tags. 

## 1.0.4 (06/11/2018)
- Update NPM dependencies for [CVE-2017-18214](https://nvd.nist.gov/vuln/detail/CVE-2017-18214).
- Update [Kafka-JUnit](https://github.com/salesforce/kafka-junit) dependency to [Kafka-JUnit4](https://github.com/salesforce/kafka-junit/tree/master/kafka-junit4).
- Update SpringBoot dependency from 1.5.10 to 1.5.13.
- Update start.sh script to allow setting JVM Heap options.

## 1.0.3 (02/11/2018)
- Fix 500 error unable to find templates when running under windows.
- Add start.bat script for running under windows.
- Update SpringBoot dependency from 1.5.6 to 1.5.10.
- Update logback-core dependency from 1.1.11 to 1.2.3 [details](https://github.com/spring-projects/spring-boot/issues/8635)
- [Issue#57](https://github.com/SourceLabOrg/kafka-webview/issues/57) Configure consumerId and consumerGroup using a configurable prefix.

## 1.0.2 (01/26/2018)
- Increase file upload limit from 15MB to 64MB.

## 1.0.1 (01/17/2018)
- Add Dockerfile / Dockerhub images.
- [Issue#49](https://github.com/SourceLabOrg/kafka-webview/issues/49) Fix bug in View create when a Filter has configurable options.

## 1.0.0 (01/06/2018)
- Initial release!
