# Change Log
The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## 1.0.4 (unreleased)
- Update NPM dependencies for [CVE-2017-18214](https://nvd.nist.gov/vuln/detail/CVE-2017-18214).
- Update [Kafka-JUnit](https://github.com/salesforce/kafka-junit) dependency to [Kafka-JUnit4](https://github.com/salesforce/kafka-junit/tree/master/kafka-junit4).

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
