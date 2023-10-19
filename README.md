# Introduction
This app as a simple calculator, where you can store a couple of frequently used formulae (e.g. `V=4/3 R^3 Pi`) with arbitrary variables.
The formulae can then be evaluated by assigning values to the variables. 

# External objects
The following things are included from 3rd parties:
- resources: now in "res.tar.gz": The files in there have to be extracted to the right place ! Some of these are from different authors 
             (it has to be checked which ones)
- "exp4j-0.3.11.jar" : also the Apache-License text has to be included (already in resources ?)

All files are in [this directory](https://gitlab.com/emtap/formeval/tree/master/app/src/main/Files2Integrate)

Furthermore, the Android-Support Libraries "android.support.v4" and "android.support.v7" are used.

# Notes
Pipeline [disabled](https://gitlab.com/emtap/formeval/edit#js-shared-permissions) on 09.08.2019


# Here begins the original  /default README
# Introduction

This is a template for doing Android development using GitLab and [fastlane](https://fastlane.tools/).
It is based on the tutorial for Android apps in general that can be found [here](https://developer.android.com/training/basics/firstapp/). 
If you're learning Android at the same time, you can also follow along that
tutorial and learn how to do everything all at once.

# Reference links

- [GitLab CI Documentation](https://docs.gitlab.com/ee/ci/)
- [Blog post: Android publishing with GitLab and fastlane](https://about.gitlab.com/2019/01/28/android-publishing-with-gitlab-and-fastlane/)

You'll definitely want to read through the blog post since that walks you in detail
through a working production configuration using this model.

# Getting started

First thing is to follow the [Android tutorial](https://developer.android.com/training/basics/firstapp/) and
get Android Studio installed on your machine, so you can do development using
the Android IDE. Other IDE options are possible, but not directly described or
supported here. If you're using your own IDE, it should be fairly straightforward
to convert these instructions to use with your preferred toolchain.

## What's contained in this project

### Android code

The state of this project is as if you followed the first few steps in the linked
[Android tutorial](https://developer.android.com/training/basics/firstapp/) and
have created your project. You're definitely going to want to open up the
project and change the settings to match what you plan to build. In particular,
you're at least going to want to change the following:

- Application Name: "My First App"
- Company Domain: "example.com"

### Fastlane files

It also has fastlane setup per our [blog post](https://about.gitlab.com/2019/01/28/android-publishing-with-gitlab-and-fastlane/) on
getting GitLab CI set up with fastlane. Note that you may want to update your
fastlane bundle to the latest version; if a newer version is available, the pipeline
job output will tell you.

### Dockerfile build environment

In the root there is a Dockerfile which defines a build environment which will be
used to ensure consistent and reliable builds of your Android application using
the correct Android SDK and other details you expect. Feel free to add any
build-time tools or whatever else you need here.

We generate this environment as needed because installing the Android SDK
for every pipeline run would be very slow.

### Gradle configuration

The gradle configuration is exactly as output by Android Studio except for the
version name being updated to 

Instead of:

`versionName "1.0"`

It is now set to:

`versionName "1.0-${System.env.VERSION_SHA}"`

You'll want to update this for whatever versioning scheme you prefer.

### Build configuration (`.gitlab-ci.yml`)

The sample project also contains a basic `.gitlab-ci.yml` which will successfully 
build the Android application.

Note that for publishing to the test channels or production, you'll need to set
up your secret API key. The stub code is here for that, but please see our
[blog post](https://about.gitlab.com/2019/01/28/android-publishing-with-gitlab-and-fastlane/) for
details on how to set this up completely. In the meantime, publishing steps will fail.

The build script also handles automatic versioning by relying on the CI pipeline
ID to generate a unique, ever increasing number. If you have a different versioning
scheme you may want to change this.

```yaml
    - "export VERSION_CODE=$(($CI_PIPELINE_IID)) && echo $VERSION_CODE"
    - "export VERSION_SHA=`echo ${CI_COMMIT_SHA:0:8}` && echo $VERSION_SHA"
```