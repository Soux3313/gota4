#!/bin/bash

token=hackeraccesstoken

address0=localhost
address1=localhost
address2=localhost

port0=8000
port1=8001
port2=8002

id0=42
id1=420
id2=69

kill_children() {
    # Get our process group id
    PGID=$(ps -o pgid= $$ | grep -o '[0-9]*')

    # Kill it in a new new process group
    setsid kill -- -"$PGID"
    exit 0
}

cd .. && gradle build
cd ./test

cp ../KISpieler/build/libs/KISpieler.jar .
cp ../Server/build/libs/Server.jar .
cp ../Beobachter/build/libs/Beobachter.jar .

trap kill_children EXIT

java -jar Server.jar -hostname $address0 -port $port0 -token $token -debug -traffic &
java -jar KISpieler.jar -hostname $address1 -port $port1 &
java -jar KISpieler.jar -hostname $address2 -port $port2 &
java -jar Beobachter.jar &

sleep 5

curl --insecure -X POST -H "Content-Type: application/json" -d "{\"name\":\"KISpieler 1\",\"url\":\"${address1}:${port1} \"}" https://${address0}:${port0}/players/${id1}?token=${token}
curl --insecure -X POST -H "Content-Type: application/json" -d "{\"name\":\"KISpieler 2\",\"url\":\"${address2}:${port2} \"}" https://${address0}:${port0}/players/${id2}?token=${token}

curl --insecure -X POST -H "Content-Type: application/json" -d @addGame.json https://${address0}:${port0}/games/${id0}?token=${token}

read -s -n 1 -p "Press any key to abort . . ."
kill_children

