Environment
===========

Use a Mac.


Preparation
===========

Acquire the Scalr code signing certificate and private key in PKCS 12 format.

Import the certificate and private into your Keychain.

Create a `gradle.properties` file in `$HOME/.gradle`. Add the following
properties:

    signingKeyIdentity=SHA-1 hash for the cert


Build Instructions
==================

Run:

    ./gradlew codeSign

