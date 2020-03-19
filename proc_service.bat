call apktool d -f %1 -o temp
call python get_exported_services.py ./temp/AndroidManifest.xml
call adb push data.txt /storage/self/primary/data.txt
call adb install %1
call adb shell am start -n com.example.myapplication/.MainActivity
call adb shell am stopservice --user 0 -n com.example.activeapp/.WorkService
call adb shell am startservice --user 0 -n com.example.activeapp/.WorkService
call rd/s/q temp
