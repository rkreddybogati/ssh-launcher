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

  + Mac OS  + Terminal.app   + OpenSSH
  + Windows + PuTTY (acts as both a terminal emulator and SSH client)
  + Windows + cmd.exe        + OpenSSH (Cygwin)
  + Linux   + Gnome Terminal + OpenSSH
  + Linux   + Xterm          + OpenSSH


Adding A Platform
=================

Providers
---------

To support a new OS / terminal emulator, you need to add a new provider.
The provider should then be registered with the provider manager, which
is tasked with returning appropriate providers based on the detected platform.

The provider's responsibility is to instantiate a new controller, perform its
initialization actions, and return a command line that can be executed by
the applet to launch the SSH client.

The provider is here to account for the fact that most Unix software does
not create its own window. If you directly call OpenSSH, it'll spawn as a
subprocess of the applet, and will not be visible to the user.
This is why providers must use "tricks" such as AppleScript or launching a new
terminal emulator process to "break out" of the applet's process.

For software that creates its own window, like PuTTY, the provider is a
pass-through wrapper.


Controllers
-----------

You probably do not need to create a new controller, unless you want to
support an SSH client that is not supported.

The controller is responsible for locating the SSH client, setting up the
environment for it (e.g. creating a key file), and returning a command line
suitable for execution in a terminal emulator to run the SSH client (e.g.
`/usr/bin/ssh user@host.com` could be the output of the controller).


Tests
=====

Java Tests
----------

A suite of Java tests is available. For obvious reasons, these do not
validate providers.


Integration Test
----------------

An integration test is present. It uses a special pass-through provider and
a CLI interface to the launcher, but does not test the other providers.


Further Work
------------

Integration tests using a dummy SSH client (the applet uses $PATH, so one
can simply shadow ssh with something else) need to be added to validate
providers.

Due to the nature of their purpose, providers are reasonably brittle. Testing
them would be ideal.
