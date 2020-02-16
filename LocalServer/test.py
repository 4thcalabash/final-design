#!/usr/bin/env python
# coding: utf-8

# In[2]:


import socket
import os
import subprocess


# In[3]:


HOST = '192.168.31.25'
PORT = 8890


# In[7]:


s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind((HOST,PORT))
s.listen(1)
while 1:
    conn, addr = s.accept()
    message = conn.recv(1024)
    packageName = message.decode('utf-8')
    print(packageName)
    if (packageName == 'exit'):
        break
    result = os.popen('adb root').readlines()
    print(result)
    cmd = 'adb shell am dumpheap %s /data/local/tmp/dump.gc/%s.hprof'%(packageName,packageName)
    result = os.popen(cmd).readlines()
    print(result)
    print('success create %s.hprof'%(packageName))
s.close()


# In[ ]:





# In[ ]:




