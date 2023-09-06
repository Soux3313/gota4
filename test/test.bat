set token=hackeraccesstoken

set address0=localhost
set address1=localhost
set address2=localhost

set port0=8000
set port1=8001
set port2=8002

set id0=42
set id1=420
set id2=69

START /WAIT "Gradle" cmd /c "cd ../ & gradle build"

copy /b/v/y "..\KISpieler\build\libs\KISpieler.jar"
copy /b/v/y "..\Server\build\libs\Server.jar"
copy /b/v/y "..\Beobachter\build\libs\Beobachter.jar"

START "SERVER" cmd /c "java -jar Server.jar -hostname %address0% -port %port0% -token %token% -debug -traffic"
START "KI1" cmd /c "java -jar KISpieler.jar -hostname %address1% -port %port1%"
START "KI2" cmd /c "java -jar KISpieler.jar -hostname %address2% -port %port2%"
START "Beobachter" cmd /c "java -jar Beobachter.jar"

timeout /T 5 /nobreak

:: curl --insecure -X POST -H "Content-Type: application/json" -d "{\"name\":\"KISpieler 1\",\"url\":\"%address1%:%port1% \"}" https://%address0%:%port0%/players/%id1%?token=%token%
:: curl --insecure -X POST -H "Content-Type: application/json" -d "{\"name\":\"KISpieler 2\",\"url\":\"%address2%:%port2% \"}" https://%address0%:%port0%/players/%id2%?token=%token%

curl --insecure -X POST -H "Content-Type: application/json" -d @addGame.json https://%address0%:%port0%/games/%id0%?token=%token%