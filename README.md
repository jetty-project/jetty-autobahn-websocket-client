Running the Autobahn Client tests
=================================

Grab yourself a copy of autobahn
--------------------------------

    $ git clone https://github.com/oberstet/Autobahn


Install the python dependencies
-------------------------------

    $ sudo apt-get install python python-dev python-twisted
    $ sudo apt-get install python-setuptools


Install Autobahn itself
-----------------------

    $ cd Autobahn
    $ git checkout v0.4.2
    $ cd lib/python
    $ sudo python setup.py install


Run the Autobahn Fuzzing Server
-------------------------------

    $ cd Autobahn/testsuite/websockets
    $ python fuxxing_server.py


Run the jetty-websocket client tests against Autobahn
-----------------------------------------------------

    $ cd jetty-autobahn-websocket-client
    $ mvn clean install
    $ mvn exec:exec

