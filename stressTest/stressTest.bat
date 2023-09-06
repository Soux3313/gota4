set token=hackeraccesstoken

set address0=localhost
set address1=localhost
set address2=localhost

set port0=8000
set port1=8001
set port2=8002
set port3=8003
set port4=8004
set port5=8005
set port6=8006
set port7=8007
set port8=8008
set port9=8009
set port10=8010
set port11=8011
set port12=8012
set port13=8013
set port14=8014
set port15=8015
set port16=8016
set port17=8017
set port18=8018
set port19=8019
set port20=8020



set id0=42
set id1=420
set id2=69

START /WAIT "Gradle" cmd /c "cd ../ & gradle build"

copy /b/v/y "..\Turnierverwaltung\build\libs\Turnierverwaltung.jar"
copy /b/v/y "..\KISpieler\build\libs\KISpieler.jar"
copy /b/v/y "..\Server\build\libs\Server.jar"
copy /b/v/y "..\Beobachter\build\libs\Beobachter.jar"

START "Turnierverwaltung" cmd /c "java -jar Turnierverwaltung.jar -address https://localhost:8000 -token %token% -players spieler.txt -backupdir ./ & pause"

START "SERVER" cmd /c "java -jar Server.jar -hostname %address0% -port %port0% -token %token% -debug -traffic"
START "KI1" cmd /c "java -jar KISpieler.jar -hostname %address1% -port %port1%"
START "KI2" cmd /c "java -jar KISpieler.jar -hostname %address2% -port %port2%"
START "KI3" cmd /c "java -jar KISpieler.jar -hostname %address2% -port %port3%"
START "KI4" cmd /c "java -jar KISpieler.jar -hostname %address2% -port %port4%"
START "KI5" cmd /c "java -jar KISpieler.jar -hostname %address2% -port %port5%"
START "KI6" cmd /c "java -jar KISpieler.jar -hostname %address2% -port %port6%"
START "KI7" cmd /c "java -jar KISpieler.jar -hostname %address2% -port %port7%"
START "KI8" cmd /c "java -jar KISpieler.jar -hostname %address2% -port %port8%"
START "KI9" cmd /c "java -jar KISpieler.jar -hostname %address2% -port %port9%"
START "KI10" cmd /c "java -jar KISpieler.jar -hostname %address2% -port %port10%"
START "KI11" cmd /c "java -jar KISpieler.jar -hostname %address2% -port %port11%"
START "KI12" cmd /c "java -jar KISpieler.jar -hostname %address2% -port %port12%"
START "KI13" cmd /c "java -jar KISpieler.jar -hostname %address2% -port %port13%"
START "KI14" cmd /c "java -jar KISpieler.jar -hostname %address2% -port %port14%"
START "KI15" cmd /c "java -jar KISpieler.jar -hostname %address2% -port %port15%"
START "KI16" cmd /c "java -jar KISpieler.jar -hostname %address2% -port %port16%"
START "KI17" cmd /c "java -jar KISpieler.jar -hostname %address2% -port %port17%"
START "KI18" cmd /c "java -jar KISpieler.jar -hostname %address2% -port %port18%"
START "KI19" cmd /c "java -jar KISpieler.jar -hostname %address2% -port %port19%"
START "KI20" cmd /c "java -jar KISpieler.jar -hostname %address2% -port %port20%"

START "Beobachter" cmd /c "java -jar Beobachter.jar"