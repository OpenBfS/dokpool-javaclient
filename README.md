# Dokpool Javaclient - Java Library providing access to Dokpool via REST-Calls

further information:

 - [Changelog](Changelog.md)
 - [License](LICENSE)

## Usage

Declare dependency in Maven:

```xml
<dependency>
    <groupId>de.bfs</groupId>
    <artifactId>dokpool-client</artifactId>
    <version>[3.1,)</version>
</dependency>
```

Creating a new document for your dokpool plone instance `my.inst.example/dokpool` within Dokpool `myPool` works as follow:

```java
import de.bfs.dokpool.client.base.*;
import de.bfs.dokpool.client.content.*;
//[...]
DocpoolBaseService docpoolBaseService = new DocpoolBaseService("https://my.inst.example/dokpool", "me", "myPassword");
DocumentPool myDocpool = docpoolBaseService.getPrimaryDocumentPool();
//use the first available group folder (ensure there is one):
Folder myGroupFolder = myDocpool.getGroupFolders().get(0);
Map<String, Object> docAttributes = new HashMap<String, Object>();
    docAttributes.put("title", "JavaDocpoolTestDocument");
    docAttributes.put("description", "Created by mvn test.");
    docAttributes.put("text", "This is just a Test and can be deleted.");
    //this document is for ELAN = Elektronische Lagedarstellung für den Notfallschutz
    docAttributes.put("local_behaviors", new String[] {"elan"});
Document myDoc = myGroupFolder.createDPDocument("my-new-doucment", docAttributes);
System.out.println(myDoc.getPathAfterPlonesite());
//Prints something like: /groupfolder/my-new-doucment
//You will find your new document at: https://my.inst.example/dokpool/groupfolder/my-new-doucment
```

See the [test class](src/test/java/de/bfs/dokpool/DokpoolTest.java) for more examples. Building Javadocs is explained below.

## Building

To create dokpool-client simply build it with:

    mvn package -DskipTests

and find from:

    target/dokpool-client.jar

To create the api docs within folder apidocs run:

    mvn javadoc:javadoc

Test with

    cp .testenv.example .testenv #adapt to dokpool instance
    bash -c 'set -o allexport && source .testenv && mvn test'

If you plan to use IDEA J then do

    cat .testenv |  tr '\n' ';'

and copy the output to the Environment variable entry in Run -> Edit Configurations (should open the JUnit config).

If you plan to use VSCode, add this to your `.vscode/settings.json`:

```json
{
    "java.test.config": {
        "name": "dokpoolTestConfig",
        "workingDirectory": "${workspaceFolder}",
        "envFile" : "${workspaceFolder}/.testenv"
    }
}
```

To go on with building irix-xxx install it into your local maven repo ~/.m2:

    mvn install -DskipTests

