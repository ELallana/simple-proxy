#!/bin/bash
echo "Simple-Proxy-Master launch"

#JARS=./lib/*.jar
CLASSPATH=./target/simple-proxy-1.0-SNAPSHOT.jar
#for i in ${JARS}
#do    
#if [ "$i" != "${JARS}" ] ; 
#then        
# CLASSPATH=$CLASSPATH:"$i"    
#fi
#done

echo $CLASSPATH

#LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/lib:/usr/lib:./native-libs/solaris/x86
#export LD_LIBRARY_PATH
#echo LD_LIBRARY_PATH=$LD_LIBRARY_PATH

#java -cp $CLASSPATH com.carlosprados.lab.simpleproxy.Proxy 172.19.17.109 1521 172.19.18.189 1521 60000
#java -cp $CLASSPATH com.carlosprados.lab.simpleproxy.Proxy 172.19.17.109 1522 172.19.17.39 1521 60000
java -cp $CLASSPATH com.carlosprados.lab.simpleproxy.Proxy 172.19.17.109 10115 192.168.1.40 8282 60000

echo "bye"
