# dokpool_javaclient - java library providing access to Dokpool via REST-Calls


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

    sed 's/export//' .testenv |  tr '\n' ';'

and copy the output to the Environment variable entry in Run -> Edit Configurations (should open the JUnit config).

To go on with building irix-xxx install it into your local maven repo ~/.m2:

    mvn install

