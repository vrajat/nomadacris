# nomadacris
A Java slave to [locust.io|http://locust.io]. 

Locust supports a master and slave configuration. Slaves run the tests. 
Master and slaves communicate over a socket and messages are encoded using msgpack.
Slaves can be written in any language. The main software implements slaves in 
python. [Boomer|https://github.com/myzhan/boomer] implements a slave in Golang.

_Nomadacris_ implements a slave in Java. 

### Major differences
* ZeroMQ is not yet supported.
* TaskSet and Tasks protocol is not yet the same as the python implementation.
* Parallelism is implemented using Threadpools and not CoRoutines.

## Run Example

Start Locust Master

    # dummy.py is an artifact of the python implementation
    pip install locustio
    locust -f nomadacris/dummy.py --master --master-bind-host=127.0.0.1 --master-bind-port=5557


Run Example

    mvn clean package
    java -jar target/nomadacris-1.0-SNAPSHOT.jar -m 127.0.0.1 -p 5557 \
         --taskList io.vrajat.nomadacris.examples.Fibonacci
         
 ## Create your own test
 
 * Implement _TaskList_ and _Task_. 
 * Copy the jar to your classpath. 
 * Specify class name in _--taskList_ argument. 