Build instructions
==================

Install Java and Gradle.

Create a `gradle.properties` file in `$HOME/.gradle`. Add the following
properties:

    signingKeyStore=Keystore to use for signing
    signingKeyPass=Keystore password for signing
    signingKeyAlias=Key alias to use for signing

Then, run:

    ./gradlew fullBuild

Finally, customize `sample.html`, make a few changes to change the host you
are connecting to, launch a webserver, and access `sample.html`.
