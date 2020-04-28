import com.sun.javafx.collections.ImmutableObservableList;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.parser.internal.SnapshotFactory;
import org.eclipse.mat.snapshot.IPathsFromGCRootsComputer;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.NamedReference;
import org.eclipse.mat.util.ConsoleProgressListener;
import org.eclipse.mat.util.IProgressListener;

import java.io.*;
import java.util.*;

public class Main {
    private final Set<String> failed_hprofs = new HashSet<String>(){{
        add("com.www.zouzoubai.hprof");
    }};
    class Report{
        String pkgName;
        Set<IObject> leakedObjects = new HashSet<>();
        void save(File logFile){
            try (FileWriter writer = new FileWriter(logFile);){
                writer.write("package name : " + pkgName + '\n');
                writer.write("leaked objects count : " + leakedObjects.size() + '\n');
                for (IObject obj: leakedObjects){
                    writer.write(obj.toString() + '\n');
                }
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private IProgressListener listener = new ConsoleProgressListener(System.err);
    private HashSet<IClass> allServiceClass = new HashSet<IClass>();
    private HashSet<String> threadclses = new HashSet<String>();
    private HashSet<String> handlerclass = new HashSet<String>();
    private IClass clazz_weakref, clazz_softref, clazz_finalref, clazz_phanref, class_ActivityThread;
    private HashMap<IClass, Set<String>> pthfilter = new HashMap<IClass, Set<String>>();
    private ArrayList<Integer> allActiveServices;
    private HashSet<Integer> allFinalizedObjs;

    public ArrayList<Integer> getAllRef(IClass clazz_ref) {
        ArrayList<Integer> res = new ArrayList<Integer>();
        List<IClass> allSubclasses = clazz_ref.getAllSubclasses();
        allSubclasses.add(clazz_ref);
        for (IClass subclz : allSubclasses) {
            String name = subclz.getName();
            try {
                int[] objectIds = subclz.getObjectIds();
                for (int obj : objectIds) {
                    res.add(obj);
                }
            } catch (SnapshotException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return res;
    }
    public HashSet<Integer> getAllFinalizedObjs(ISnapshot st, ArrayList<Integer> finalrefs) {
        HashSet<Integer> objs = new HashSet<Integer>();
        for (int i : finalrefs) {
            IObject fref;
            try {
                fref = st.getObject(i);
                IObject value = (IObject) fref.resolveValue("referent");
                if (value != null)
                    objs.add(value.getObjectId());
            } catch (SnapshotException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return objs;
    }
    private ArrayList<Integer> getAllActiveServices() {
        ArrayList<Integer> activityThreads = new ArrayList<Integer>();
        ArrayList<Integer> res = new ArrayList<Integer>();
        if (class_ActivityThread != null) {
            int[] ids;
            try {
                ids = class_ActivityThread.getObjectIds();
                for (int id : ids) {
                    activityThreads.add(id);
                }
            } catch (SnapshotException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            List<IClass> subclasses = class_ActivityThread.getAllSubclasses();
            for (IClass clz : subclasses) {
                try {
                    ids = clz.getObjectIds();
                    for (int id : ids) {
                        activityThreads.add(id);
                    }
                } catch (SnapshotException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        for (int actthread : activityThreads) {
            try {
                IObject athd = class_ActivityThread.getSnapshot().getObject(actthread);
                IObject mServices = (IObject) athd.resolveValue("mServices");
                if (mServices != null) {
                    IObject mArray = (IObject) mServices.resolveValue("mArray");
                    if (mArray != null) {
                        List<NamedReference> references = mArray.getOutboundReferences();
                        for (NamedReference ref : references) {
                            String name = ref.getName();
                            if (name.matches("\\[[0-9]*\\]")) {
                                System.out.println(ref.getName() + " " + ref.getObjectId());
                                res.add(ref.getObjectId());
                            }
                        }
                    }
                }
            } catch (SnapshotException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return res;
    }
    public PathPattern getPathPattern(int[] path, ISnapshot ss) throws SnapshotException {
        PathPattern pattern = new PathPattern();
        IObject pre = null;
        String preRefName = "";
        boolean isSameWithPre = false;
        for (int i : path) {
            boolean preIsSameWithPre = isSameWithPre;
            IObject pthobj = ss.getObject(i);
            String rname = "";
            if (pre != null) {
                List<NamedReference> references = pthobj.getOutboundReferences();
                int id1 = -1;
                int id2 = 1;
                for (NamedReference ref : references) {
                    //System.out.println(ref);
                    try{
                        id1 = ref.getObjectId();
                        id2 = pre.getObjectId();
                    }catch (Exception e){

                    }
                    if (id1 == id2) {
                        String refname = ref.getName();
                        if (refname.matches("\\[[0-9]+\\]")) {
                            refname = "[ ]";
                        }
                        if (refname.equals(preRefName) && pre.getClazz().equals(pthobj.getClazz())) {
                            isSameWithPre = true;
                        } else {
                            isSameWithPre = false;
                        }
                        if (rname.length() > 0) {
                            rname = rname + ",";
                        }
                        rname = rname + refname;
                    }
                }
            } else {
                pattern.setStartClass(pthobj.getClazz().getName());
            }
            if (!isSameWithPre) {
                if (preIsSameWithPre) {
                    ArrayList<String> his = pattern.getPattern();
                    String string = his.get(his.size() - 1);
                    string = "[" + string;
                    his.set(his.size() - 1, string);
                } else {
                    pattern.addPathNode(rname);
                    String name = pthobj.getClazz().getName();
                    if (name.equals("java.lang.Class")) {
                        String technicalName = pthobj.getTechnicalName();
                        String trim = technicalName.substring(0, technicalName.lastIndexOf('@')).trim();
                        name = name + "[" + trim + "]";
                    } else if (name.equals("java.lang.Thread")) {
                        Collection<IClass> clses_finalDaemon = pthobj.getSnapshot()
                                .getClassesByName("java.lang.Daemons$FinalizerDaemon", false);
                        IClass cls_finalDaemon = clses_finalDaemon.iterator().next();
                        IObject singleton = (IObject) cls_finalDaemon.resolveValue("INSTANCE");
                        Object fd_thread = singleton.resolveValue("thread");
                        IObject targetThread = (IObject) pthobj.resolveValue("target");
                        if (targetThread != null) {
                            Object value2 = targetThread.resolveValue("thread");
                            if (fd_thread == value2) {
                                name = name + "(FinalizerDaemon)";
                            }
                        }
                    }
                    pattern.addPathNode(name);
                }
            } else {
                // do nothing
            }
            // if (pthobj.getClazz().equals(clazz_actRcd)
            // || (clazz_lclActRcd != null &&
            // pthobj.getClazz().equals(clazz_lclActRcd))) {
            // pattern = new PathPattern();
            // break;
            // }
            pre = pthobj;
            preRefName = rname;
        }
        return pattern;
    }
    Report analyse(File hprof) throws SnapshotException {
        String packageName = hprof.getName();
        packageName = packageName.substring(0,packageName.length() - 6);
        String appname = packageName;
        Report report = new Report();
        report.pkgName = appname;
        System.out.println("Processing " + packageName);
        SnapshotFactory sf = new SnapshotFactory();
        ISnapshot snapshot = sf.openSnapshot(hprof,new HashMap<String,String>(),listener);
        Collection<IClass> classes_service = snapshot.getClassesByName("android.app.Service", false);

        Collection<IClass> classes_FinalRef = snapshot.getClassesByName("java.lang.ref.FinalizerReference", false);
        Collection<IClass> classes_SoftRef = snapshot.getClassesByName("java.lang.ref.SoftReference", false);
        Collection<IClass> classes_WeakRef = snapshot.getClassesByName("java.lang.ref.WeakReference", false);
        Collection<IClass> classes_PhanRef = snapshot.getClassesByName("java.lang.ref.PhantomReference", false);

        Collection<IClass> classes_ActivityThread = snapshot.getClassesByName("android.app.ActivityThread", false);
        Collection<IClass> classes_Thread = snapshot.getClassesByName("java.lang.Thread", false);
        Collection<IClass> classes_Runnable = snapshot.getClassesByName("java.lang.Runnable", false);
        Collection<IClass> classes_Handler = snapshot.getClassesByName("android.os.Handler", false);

        if (classes_service == null || classes_service.isEmpty()) {
            System.err.println(String.format("Cannot find class %s in heap dump", "android.app.Service"));
            return null;
        }
        IClass clazz_Thread = classes_Thread.iterator().next();
        List<IClass> allThreadClazz = clazz_Thread.getAllSubclasses();
        IClass clazz_Runnable = classes_Runnable.iterator().next();
        List<IClass> allRunnableClazz = clazz_Runnable.getAllSubclasses();
        for (IClass cls : allThreadClazz) {
            this.threadclses.add(cls.getName());
        }
        this.threadclses.add(clazz_Thread.getName());
        for (IClass cls : allRunnableClazz) {
            this.threadclses.add(cls.getName());
        }
        this.threadclses.add(clazz_Runnable.getName());
        IClass clazz_Handler = classes_Handler.iterator().next();
        List<IClass> allHandlerClazz = clazz_Handler.getAllSubclasses();
        for (IClass cls : allHandlerClazz) {
            this.handlerclass.add(cls.getName());
        }
        this.handlerclass.add(clazz_Handler.getName());
        class_ActivityThread = classes_ActivityThread.iterator().next();
        this.pthfilter.clear();
        clazz_finalref = classes_FinalRef.iterator().next();
        clazz_softref = classes_SoftRef.iterator().next();
        clazz_weakref = classes_WeakRef.iterator().next();
        clazz_phanref = classes_PhanRef.iterator().next();

        this.pthfilter.put(clazz_finalref, null);
        this.pthfilter.put(clazz_softref, null);
        this.pthfilter.put(clazz_weakref, null);
        this.pthfilter.put(clazz_phanref, null);
        ArrayList<Integer> finalrefs = getAllRef(clazz_finalref);
        allActiveServices = this.getAllActiveServices();
        allFinalizedObjs = this.getAllFinalizedObjs(snapshot, finalrefs);
        // service
        {
            IClass clazz = classes_service.iterator().next();
            List<IClass> subclasses = clazz.getAllSubclasses();
            this.allServiceClass.addAll(subclasses);
            for (IClass subclz : subclasses) {
                String name = subclz.getName();
                int[] objIds = subclz.getObjectIds();
                for (int objId : objIds) {
                    boolean leaked = handleObj(snapshot, objId);
                    if (leaked){
                        report.leakedObjects.add(snapshot.getObject(objId));
                    }
                }
            }
        }
        snapshot.dispose();
        snapshot = null;
        return report;
    }

    private boolean handleObj(ISnapshot snapshot, int objId) throws SnapshotException {
        IPathsFromGCRootsComputer path = snapshot.getPathsFromGCRoots(objId, this.pthfilter);
        int[] p;
        while ((p = path.getNextShortestPath()) != null) {
            PathPattern pattern = getPathPattern(p, snapshot);
            IObject root = snapshot.getObject(p[p.length - 1]);// root;
            /*  exclude finalized */
            if (this.allFinalizedObjs.contains(root.getObjectId())) {
                continue;
            }
            if (!this.allActiveServices.contains(objId)) {
                return true;
            }
        }
        return false;
    }
    void work(String rootPath) throws SnapshotException {
        int totalApp = 0;
        int leakedApp = 0;
        int failedApp = 0;
        File rootDir = new File(rootPath);
        System.out.println(rootDir .getAbsolutePath());
        File[] hprofs =rootDir.listFiles();
        for (File app : hprofs){
            File result = new File(app.getAbsolutePath() + "/result.txt");
            if (result.exists()){
                System.out.println("result already exists skipped.");
                continue;
            }
            for (File hprof : app.listFiles()){
                String packageName = hprof.getName();
                if (packageName.length()< 6 || !packageName.substring(packageName.length()-6).equals(".hprof"))continue;
                totalApp ++;
                System.out.println(packageName);
               // if (failed_hprofs.contains(packageName)){
               //     failedApp ++;
               //     continue;
               // }
                try{
                    Report report = analyse(hprof);
                    if (!report.leakedObjects.isEmpty()){
                        leakedApp ++;
                    }
                    report.save(new File(app.getAbsolutePath() + "/result.txt"));
                }catch (Exception e){
                    failedApp ++;
                }
            }
        }

        System.out.println(String.format("\n%d app analysed, %d of which leaks, %d of which fails",totalApp,leakedApp,failedApp));

        int N = 0,T = 0,L = 0,F = 0;
        for (File app: rootDir.listFiles()){
            File result = new File(app.getAbsolutePath() + "/result.txt");
            T ++;
            if (result.exists()){
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(result));
                    String line = reader.readLine();
                    line = reader.readLine();
                    if (line.equals("leaked objects count : 0")){
                        N ++;
                    }else{
                        L ++;
                    }
                }catch(Exception e){
                    F ++;
                }
            }else{
                F ++;
            }
        }
        System.out.printf("Total = %d, Normal = %d, Leak = %d, Fail = %d.",T,N,L,F);
    }
    public static void main(String[] args) throws SnapshotException {
        new Main().work(args[0]);
    }
}
