
import org.eclipse.mat.snapshot.ISnapshot;

public class Pair {
    private ISnapshot snapshot;
    private int[]path;
    public Pair(ISnapshot snapshot,int[]path) {
        // TODO Auto-generated constructor stub
        this.setSnapshot(snapshot);
        this.setPath(path);
    }
    public ISnapshot getSnapshot() {
        return snapshot;
    }
    public void setSnapshot(ISnapshot snapshot) {
        this.snapshot = snapshot;
    }
    public int[] getPath() {
        return path;
    }
    public void setPath(int[] path) {
        this.path = path;
    }
    @Override
    public boolean equals(Object obj){
        Pair p2=(Pair)obj;
        if(this.snapshot==p2.snapshot){
            if(path!=null&&p2.path!=null){
                if(path.length==p2.path.length){
                    for(int i=0;i<path.length;i++){
                        if(path[i]!=p2.path[i]){
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        int code=this.snapshot.hashCode();
        return code;
    }

}
