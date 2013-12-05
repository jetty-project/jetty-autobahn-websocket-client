Running the Autobahn Client tests
=================================

Jetty Native WebSocket Client tests for [Autobahn TestSuite](http://autobahn.ws/testsuite/)

Install the Autobahn wstest command line tool
---------------------------------------------

The full installation instructions can be found at [http://autobahn.ws/testsuite/installation]

    $ sudo apt-get install python python-dev python-twisted
    $ sudo apt-get install python-setuptools
    $ sudo easy_install autobahntestsuite

Run the Autobahn Fuzzing Server
-------------------------------

    $ wstest --mode=fuzzingserver

Let this run in a terminal window of its own.

Run the jetty-websocket client tests against Autobahn
-----------------------------------------------------

Switch to another terminal window and run.

    $ cd jetty-autobahn-websocket-client
    $ mvn clean install
    $ mvn exec:exec

