# final-design
calabash_boy's final-design

用于批量检测apk中是否有内存泄漏的Service。

1、创建一个apk文件夹，存放所有的.apk

2、创建一个dump_files文件夹，用来存放堆镜像文件

3、启动LocalServer中的python socker server

4、运行proc.bat测试**一个**apk，格式为：./proc.bat [apk file]，由于同一个模拟器上的测试必须串行，这一过程需要手动，因为我电脑只能带一个模拟器。。。

5、.hprof文件存在于/data/local/tmp/dump.gc 使用adb命令pull整个文件夹

6、使用HprofAnalyser自动分析所有的堆镜像，每个hprof会生成一个result.txt文件说明是否有内存泄漏以及所有的泄露的object。
