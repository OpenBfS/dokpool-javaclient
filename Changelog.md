# Changelog

## Version 3.x

### 3.4.0 *2026-04-17*

 - remove deprectaed classes DocpoolBaseService (with c instead of k) and Scenario
 - move to Java 25 and Jackson 3
 - change default docType to "mresult_other" if behaviors include doksys
 - tests can now be performed in a (sub...)subfolder of the group folder

### 3.3.4 *2026-02-16*

 - use Java's own HttpClient instead of Apache HttpClient
 - bump some maven plugin versions
 - fix some Javadoc errors and warnings

### 3.3.3 *2025-10-29*

 - bump dependencies (Apache HTTPClient5, Jackson)
 - bump dependency-check-maven version to 12.1.8
 - because of this, the minimal maven version is now 3.6.3

### 3.3.2 *2025-07-23*

 - more optional exceptions (when failing to get doc. pool)

### 3.3.1 *2025-06-23*

 - add option to throw some runtime exceptions
   Not throwing them remains the default.

### 3.3.0 *2025-04-28*

 - added Event.isActive(), Document.assignEventIdsUids and
   Document.assignAllActiveEvents
 - added DocumentPool.getSupportedApps
 - logging abstraction removed (better readability)
 - for token/value properties getStringAttribute
   now works and only returns the token
 - consistent code naming: Dokpool is the software and
   Doc(ument)Pool is a single pool of documents
   (camel case with lower c)
 - replaced Exceptions with DokpoolRuntimeException in most cases
   DokpoolRuntimeException will likely be thrown by some methods
   in the following releases
 - jars are now smaller without dependencies
 - code style changes (tabs -> 4 spaces) and use of checkstyle

### 3.2.0 *2025-03-27*

 - ensure image/ mimetype for images
 - restructure RODOS checks, also check during document updates 
 - allow replacing files and images 


### 3.1.0 *2025-01-27*

 - everything from 2.2.0 -> 2.3.0 (testing, REST-API,  minimum Dokpool Release is now 2.0.0)
 - use version ranges up to the next major version for non-plugin dependencies
 - remove dependency on commons-logging
 - remove dependency on SpringBoot
 - added dependency-check to pom.xml
 - deployment tested with Java 11 and Java 21 (build version remains 11)

### 3.0.0 *2023-10-24* (changes vs. 2.2.0)

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
