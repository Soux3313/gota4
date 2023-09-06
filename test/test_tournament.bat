set token=hackeraccesstoken

set address0=localhost
set address1=localhost
set address2=localhost
set address3=localhost
set address4=localhost
set address5=localhost

set port0=33100
set port1=8001
set port2=8002
set port3=8003
set port4=8004
set port5=8005

set id0=42
set id1=420
set id2=69

START /WAIT "Gradle" cmd /c "cd ../ & gradle build"

copy /b/v/y "..\KISpieler\build\libs\KISpieler.jar"
copy /b/v/y "..\Server\build\libs\Server.jar"
copy /b/v/y "..\Beobachter\build\libs\Beobachter.jar"
copy /b/v/y "..\Turnierverwaltung\build\libs\Turnierverwaltung.jar"

START "SERVER" cmd /c "java -jar Server.jar -hostname %address0% -port %port0% -token %token% -debug -traffic"
START "KI1" cmd /c "java -jar KISpieler.jar -hostname %address1% -port %port1%"
START "KI2" cmd /c "java -jar KISpieler.jar -hostname %address2% -port %port2%"
START "KI3" cmd /c "java -jar KISpieler.jar -hostname %address3% -port %port3%"
START "KI4" cmd /c "java -jar KISpieler.jar -hostname %address4% -port %port4%"
START "KI5" cmd /c "java -jar KISpieler.jar -hostname %address5% -port %port5%"
START "Beobachter" cmd /c "java -jar Beobachter.jar"

timeout /T 5 /nobreak

START "Turnierverwaltung" cmd /c "java -jar Turnierverwaltung.jar -token %token%"