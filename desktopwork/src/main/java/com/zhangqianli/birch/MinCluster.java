//MinCluster.java  
  
package com.zhangqianli.birch;
    
import java.util.ArrayList;  
    
//最小簇  
public class MinCluster {  
    
    private CF cf;  
    private ArrayList<String> inst_marks;  //这个是什么东西？数据标记？还用链表存储。标记该簇中包含哪些类别的数据。
        
    public MinCluster(){  
        cf=new CF();  
        inst_marks=new ArrayList<String>();  
    }  
    
    public CF getCf() {  
        return cf;  
    }  
    
    public void setCf(CF cf) {  
        this.cf = cf;  
    }  
    
    public ArrayList<String> getInst_marks() {  
        return inst_marks;  
    }  
    
    public void setInst_marks(ArrayList<String> inst_marks) {  
        this.inst_marks = inst_marks;  
    }  
        
    //计算簇的直径  
    public static double getDiameter(CF cf){  
        double diameter=0.0;  
        int n=cf.getN();  
        for(int i=0;i<cf.getLS().length;i++){  
            double ls=cf.getLS()[i];  
            double ss=cf.getSS()[i];  
            diameter=diameter+(2*n*ss-2*ls*ls); //这个程序的D2和D，在微博上应该是写反了，程序里是正确的。 
        }  
        diameter=diameter/(n*n-n);  
        return Math.sqrt(diameter);  
    }  
        
    //计算和另外一个簇合并后的直径  
    public static double getDiameter(MinCluster cluster1,MinCluster cluster2){  
        CF cf=new CF(cluster1.getCf());  
        cf.addCF(cluster2.getCf(), true);  
        return getDiameter(cf);  
    }  
        
    public void mergeCluster(MinCluster cluster){  
        this.getCf().addCF(cluster.getCf(), true);  
        for(int i=0;i<cluster.getInst_marks().size();i++){  
            this.getInst_marks().add(cluster.getInst_marks().get(i));  
        }  
    }  
}  