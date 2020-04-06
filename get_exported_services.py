#!/usr/bin/env python
# coding: utf-8

# In[41]:


from xml.dom.minidom import parse
import sys
import os


# In[59]:


def get_exported_services(xml_path):
    domTree = parse(xml_path)
    manifest = domTree.documentElement
    services = manifest.getElementsByTagName('service')
    exported_services = [];
    for service in services:
        if (service.getAttribute('android:exported') == 'true'):
            exported_services.append(service.getAttribute('android:name'))
    activities = manifest.getElementsByTagName('activity')
    main_activity = []
    for activity in activities:
        actions = activity.getElementsByTagName('action')
        count = 0
        for action in actions:
            if action.getAttribute('android:name') == 'android.intent.action.MAIN':
                count = count | 1
        categories = activity.getElementsByTagName('category')
        for category in categories:
            if (category.getAttribute('android:name') == 'android.intent.category.LAUNCHER'):
                count = count | 2
        if (count == 3):
            main_activity.append(activity.getAttribute('android:name'))
    package_name = manifest.getAttribute('package')
    return [package_name,exported_services,main_activity]


# In[70]:


def writeDataFile(pkgname,svclist,main_activity):
    f = open('data.txt','w')
    f.write(pkgname)
    f.write('\n')
    f.write(str(len(svclist)))
    f.write('\n')
    for svc in svclist:
        f.write(svc)
        f.write('\n')
    f.write(main_activity[0])
    f.write('\n')
    t = main_activity[0]
    list = t.split('.')
    length = len(list[len(list)-1])
    tt = t[0:len(t) - length - 1]
    tt = tt + '/.' + list[len(list)- 1]
    f.write(tt)
    f.write('\n')
    f.close()


# In[72]:


if __name__ == '__main__':
    print(sys.argv[1])
    package_name,list,main_activity = (get_exported_services(sys.argv[1]))
    print(list)
    print(package_name)
    print(main_activity)
    writeDataFile(package_name,list,main_activity)
    os.system('rd/s/q %s'%(sys.argv[1].split('.')[0]))
    


# In[ ]:




