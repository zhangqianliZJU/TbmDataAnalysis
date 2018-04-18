package com.zhangqianli.jdbc;
import java.io.File;

public class FetchFileList {

    /**
     * 
     * @author 
     */
    public static void main(String[] args) {
        String directory = "J:\\tbm_text_import_sample";
    	System.out.println("所有txt文件总数为：" + getFileName(directory).length );
    }

    public static File[] getFileName(String directory) {
        //J:\\TBM_TXT
    	//String path = "C:\\Users\\zhangqianli\\Desktop\\sample"; // 路径%C:\\Users\\zhangqianli\\Desktop\\sample
        File f = new File(directory);
        if (!f.exists()) {
            System.out.println(directory + " not exists");
            return null;
        }

        File[] fa = f.listFiles();
        for (int i = 0; i < fa.length; i++) {
            File fs = fa[i];
            if (fs.isDirectory()) {
                System.out.println(fs.getName() + " [目录]");
            } else {
                System.out.println(fs.getName());
            }
        }
        return fa;
    }
}

