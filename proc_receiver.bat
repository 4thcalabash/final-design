call apktool d -f %1 -o temp
call python get_static_receiver.py ./temp/AndroidManifest.xml
call adb push data.txt /storage/self/primary/data_receiver.txt
call adb install %1
call adb shell am start -n com.example.myapplication/.MainActivity
adb shell am broadcast -a Intent.ACTION_HEADSET_PLUG -n com.example.myapplication/.LeakReceiver
adb shell am broadcast -a Intent.ACTION_HEADSET_PLUG -n com.example.myapplication/.LeakReceiver
adb shell am broadcast -a Intent.ACTION_HEADSET_PLUG -n com.example.myapplication/.LeakReceiver
adb shell am dumpheap com.example.myapplication /data/local/tmp/dump.gc/receiver_test/com.example.myapplication.hprof
call rd/s/q temp
