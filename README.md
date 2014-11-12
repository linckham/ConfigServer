ConfigServer
============
The description of Config Server
    1.The config server is a publish-subscribe service.When the publisher is disconnected from the server,the server can delete all the configuration
that published by this publisher. Meanwhile,the server will notify this change event to the subscriber immediately.
    2.The config server use TCP protocol to implements the pub-sub. It use the netty 4.0 as the network communication framework.
        a) the core code of the remote module is reference from alibaba/RocketMQ.
    3.All of the configurations that published by publisher are storage in MySQL database.
    4.It can use as a registry for distribute service framework. For example,DUBBO,HSF,SAF and so on.

The usage of Config Server
step 1 download the config server source code from the github.
  cd "your directory"
  git clone https://github.com/linckham/ConfigServer.git

step 2 install the MySQL server and initialize the config server sql script
  a) how to install MySQL server,see more details from Internet.
  b) connect to the MySQL server
  c) source the config server sql script
     source "your direcoty/ConfigServer/doc/sql/config-server.sql"

step 3 compile the config server project and start the config server
   a) cd "your directory"
   b) mvn clean package -Dmaven.test.skip=true
   c) cd "your directory/ConfigServer/configserver-core/deploy"
   d) cd bin
   e) sh startup.sh

step 4 subscribe the specified path
   a) find the ClientSubscribe class from configserver-client project
   b) run it as an common JAVA main program

step 5 publish the specified configuration to the config server
    a) find the ClientPublish class from configserver-client project
    b) run it as an common JAVA main program