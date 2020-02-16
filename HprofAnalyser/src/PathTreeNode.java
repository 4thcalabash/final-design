
import java.util.HashSet;
import java.util.LinkedList;

import org.eclipse.mat.snapshot.model.IObject;

public class PathTreeNode {
    private IObject obj;
    private int objno;
    private HashSet<PathTreeNode>children=new HashSet<PathTreeNode>();
    public PathTreeNode(int objno) {
        // TODO Auto-generated constructor stub
        this.setObjno(objno);
    }
    public void addChild(PathTreeNode child){
        this.getChildren().add(child);
    }
    public HashSet<PathTreeNode> getChildren() {
        return children;
    }
    public int getObjno() {
        return objno;
    }
    public void setObjno(int objno) {
        this.objno = objno;
    }
    public String toString(){
        return ""+this.objno;
    }
    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return this.objno;
    }
    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        return this.objno==((PathTreeNode)obj).objno;
    }
    public IObject getObj() {
        return obj;
    }
    public void setObj(IObject obj) {
        this.obj = obj;
    }

}
