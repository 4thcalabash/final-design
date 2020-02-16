
import java.util.ArrayList;

public class PathPattern {
    private String startClass;
    private boolean endwithSelfDefRoot=false;
    private ArrayList<String>pattern=new ArrayList<String>();
    public PathPattern() {
        // TODO Auto-generated constructor stub
    }
    public void addPathNode(String node){
        this.getPattern().add(node);
    }
    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return this.toString().hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        return this.toString().equalsIgnoreCase(obj.toString());
    }
    public boolean contains(PathPattern p){
        if(this.toString().indexOf(p.toString())>=0){
            return true;
        }
        return false;
    }
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        StringBuffer sb=new StringBuffer();
        for(String s:this.pattern){
            sb.append(s+"\n");
        }
        return sb.toString();
    }
    public ArrayList<String> getPattern() {
        return pattern;
    }
    public void setPattern(ArrayList<String> pattern) {
        this.pattern = pattern;
    }
    public String getStartClass() {
        return startClass;
    }
    public void setStartClass(String startClass) {
        this.startClass = startClass;
    }
    public static class Entry<PathPattern,Integer>implements java.util.Map.Entry<PathPattern,Integer>{
        PathPattern key;
        Integer value;
        public Entry(PathPattern pt,Integer size){
            this.key=pt;
            this.value=size;
        }
        @Override
        public PathPattern getKey() {
            // TODO Auto-generated method stub
            return key;
        }

        @Override
        public Integer getValue() {
            // TODO Auto-generated method stub
            return value;
        }

        @Override
        public Integer setValue(Integer object) {
            // TODO Auto-generated method stub
            Integer temp=value;
            value=object;
            return temp;
        }

    }
    public boolean startsWith(PathPattern pt) {
        // TODO Auto-generated method stub
        return this.toString().startsWith(pt.toString());
    }
    public boolean endsWith(PathPattern pt) {
        // TODO Auto-generated method stub
        return this.toString().endsWith(pt.toString());
    }
    public boolean isEndwithSelfDefRoot() {
        return endwithSelfDefRoot;
    }
    public void setEndwithSelfDefRoot(boolean endwithSelfDefRoot) {
        this.endwithSelfDefRoot = endwithSelfDefRoot;
    }
}
