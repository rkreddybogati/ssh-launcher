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

Supported Platforms
===================

To support a platform, we need:

  + Supported OS
  + Supported terminal emulator
  + Supported SSH client

We support:

  + Mac OS + Terminal.app + OpenSSH
  + Windows + PuTTY (both terminal emulator and SSH client)
  + Windows + cmd.exe + OpenSSH

Note that a recent version of OpenSSH for Windows is [hard to come by][0],
and that older versions (the widely distributed sourceforge one) are unable to
properly parse permissions (and will therefore refuse to launch).


TODOs
=====

  + On systems where we support multiple SSH platforms, we should try each of
    one of them until one succeeds. Additionally, we should make the prefered
    platform configurable.

  [0]: http://miked.ict.rave.ac.uk/display/sshwindows/OpenSSH+for+Windows
