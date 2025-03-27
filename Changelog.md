# Changelog

## Version 3.x

### 3.2 *2025-03-27*

 - enrure image/ mimetype for images
 - restructure RODOS checks, also check during document updates 
 - allow replacing files and images 


### 3.1 *2025-01-27*

 - everything from 2.2.0 -> 2.3.0 (testing, REST-API,  minimum Dokpool Release is now 2.0.0)
 - use version ranges up to the next major version for non-plugin dependencies
 - remove dependency on commons-logging
 - remove dependency on SpringBoot
 - added dependency-check to pom.xml
 - deployment tested with Java 11 and Java 21 (build version remains 11)

### 3.0 *2023-10-24* (changes vs. 2.2.0)

 - start of upgrade to Java 11, Java build version: 11
 - dependency upgrades SpringBoot and jackson-annotations

## Version 2.x

### 2.3.0 *2024-12-16*

 - switch from Plone's XML-RPC to Plone's REST-API
 - new dependencies: org.apache.httpcomponents.client5 and com.fasterxml.jackson.core
 - minimum Dokpool Release is now 2.0.0
 - add proper testing via JUnit and remove old main method used for testing
 - remove dependency on Spring Boot and update others
 - remove exception subpackage; proper usage of exception requires changes in IRIXbroker

### 2.2.0 *2019-10-08*

 - add DPEvent support

### 2.0.0 *2018-12-17*

 - added query for activeScenarios
 - refactoring of package structure
 - many pom.xml updates
 - Java build version: 8

## Version 1.x

### 1.1.0 *2017-08-30*

 - new method getFolderPath() on Folder class
 - added support for generic properties in Documents (deprecated+not working since 2.3/3.1)
 - added Scenario class and query

### 1.0.0 *2016-10-05*

 - inital release seperate from dokpool repo
 - package name chnage from dokpool to elan
 - Java build version: 7
