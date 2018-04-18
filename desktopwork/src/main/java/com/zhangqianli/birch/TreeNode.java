//TreeNode.java  
  
package com.zhangqianli.birch;
//TreeNode继承自CF，它的子类有NonLeafNode和LeafNode   
public abstract class TreeNode extends CF {  
    
    private TreeNode parent;  
    
    public TreeNode() {  
            
    }  
        
    public TreeNode(double[] data) {  
        super(data);  
    }  
    
    public TreeNode getParent() {  
        return parent;  
    }  
    
    public void setParent(TreeNode parent) {  
        this.parent = parent;  
    }  
    /**
     * 这个方法也很关键，从minCluster开始一直更新到根节点
     * @param cf
     */
    public void addCFUpToRoot(CF cf){  
        TreeNode node=this;  //一旦在MinCluster中插入一个CF，则需要从MinCluster到Root进行一次彻底的更新。
        while(node!=null){  
            node.addCF(cf, true);  //父类方法
            node=node.getParent();  
        }  
    }  
    public void deleteCFUpToRoot(CF cf)
    {
    	//异常点检测时删除异常点需要同时更新父节点的CF值，此处为减去。
    	TreeNode node = this;
    	while(node != null)
    	{
    		node.addCF(cf, false);
    		node = node.getParent();
    	}
    }
    abstract void split();  //抽象方法。这个方法写不好，很有可能造成内存泄漏
        
    abstract void absorbSubCluster(MinCluster cluster);  
}  