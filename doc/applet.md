Preparation
===========

Acquire the Scalr code signing certificate and private key in keystore format.

Create a `gradle.properties` file in `$HOME/.gradle`. Add the following
properties:

    signingKeyStore=Keystore to use for signing
    signingKeyPass=Keystore password for signing
    signingKeyAlias=Key alias to use for signing

Build Instructions
==================

Run:

    ./gradlew fullBuild

