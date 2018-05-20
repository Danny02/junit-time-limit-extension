# JUnit Time Limits [![Build Status](https://travis-ci.com/Danny02/junit-timelimit-extension.svg?branch=master)](https://travis-ci.com/Danny02/junit-timelimit-extension) [![Test Coverage](https://api.codeclimate.com/v1/badges/39d3af77356397728dfd/test_coverage)](https://codeclimate.com/github/Danny02/junit-timelimit-extension/test_coverage)

It is common to categorize unit test by how quickly they execute. This for example 
enables enables one to execute only test which run in a very short amount of time.

This could be used for i.e. :
- a background process which test on each file save
- a git pre commit hook
- as a first testing stage of the CI pipeline

The problem one faces is, that these categories need to be curated by hand. This is 
especially error prone to changes over time.

This Extension gives you [annotations](src/main/java/com/github/danny02/annotation) to categorize your tests in how quick they run
and also checks the execution time of each categorized test.

The current categories are:

|Name|Time Limit|
|-|-:|
|[@Short](src/main/java/com/github/danny02/annotation/Short.java)|0 - 100ms|
|[@Medium](src/main/java/com/github/danny02/annotation/Medium.java)|80 - 500ms|
|[@Long](src/main/java/com/github/danny02/annotation/Long.java)|400 - 1500ms|
|[@Eternal](src/main/java/com/github/danny02/annotation/Eternal.java)|over 1500ms|
|[@TimeLimit](src/main/java/com/github/danny02/annotation/TimeLimit.java)|configurable|
 
Besides using the [annotations](src/main/java/com/github/danny02/annotation), 
you need to enable the [Extension](src/main/java/com/github/danny02/extension/TimeLimitExtension.java).
Take a look at the [JUnit documentation](https://junit.org/junit5/docs/current/user-guide/#extensions-registration)
on how to do it, the prefered way is to register it [automatically](https://junit.org/junit5/docs/current/user-guide/#extensions-registration-automatic) .

## Configuration

You can configure own time limit categories or overwrite defaults by using 
[JUnit Parameters](https://junit.org/junit5/docs/current/user-guide/#running-tests-config-params).

Just set the following properties:
````properties
com.github.danny02.timeout.categoryname.lower=300
com.github.danny02.timeout.categoryname.upper=750
````


## Credits
Inspired by [Bazel Test Timeouts](https://docs.bazel.build/versions/master/test-encyclopedia.html)

Implementation Idea from [Sam Brannen](https://github.com/sbrannen) see 
[Stackoverflow post](https://stackoverflow.com/questions/50229133/how-to-enable-a-global-timeout-for-junit-testcase-runs/50233807#50233807)