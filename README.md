# CIMS Mobile 

The tablet application providing the primary user interface used by CIMS mobile
users. It is used by field workers to perform data entry. The application is
essentially a convenient interface for launching forms in CIMS Forms,
pre-filled with data based on a database of households and individuals. It 
communicates with CIMS Server for synchronizing data used for field worker 
authentication, hierarchy search, and for pre-filling forms.

For more information, please see the [CIMS background
documentation](https://github.com/cims-bioko/cims-bioko.github.io/wiki/Background).

## Usage

The cims-tablet application is a mobile application written for devices running
the Android operating system. To use it, it must be installed to an
Android-enabled device. 

## Building

Recently, the application build was migrated to a Gradle build to conform more
with modern Android tooling. As a result, as long as you have Java installed
you should be able to build a debug version of the app and install it by
issuing the following command from this directory:

```
./gradlew installDebug
```

Also, you can run instrumentation (integration-style) on a connected android
device by issuing the following:

```
./gradlew connectedCheck
```

You should see any test failures on the console and the test results should
be available as an HTML report at
build/reports/androidTests/connected/index.html.
