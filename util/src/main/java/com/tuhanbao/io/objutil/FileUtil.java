package com.tuhanbao.io.objutil;

import java.io.File;
import java.io.IOException;

import com.tuhanbao.io.base.Constants;
import com.tuhanbao.io.txt.util.TxtUtil;

public final class FileUtil
{
    public static String appendPath(String ... paths)
    {
    	if (paths == null || paths.length == 0) return null;
    	
    	String path0 = paths[0];
        StringBuilder sb = new StringBuilder();
    	int length = paths.length;
    	String lastPath = null;
    	if (!StringUtil.isEmpty(path0)) {
    	    sb.append(path0);
    	    lastPath = path0;
    	}
    	
    	for (int i = 1; i < length; i++) {
    	    String path = paths[i];
    	    if (StringUtil.isEmpty(path)) {
    	        continue;
    	    }
    	    
    		if (lastPath != null && !lastPath.endsWith(Constants.FILE_SEP) && !lastPath.endsWith("\\")) {
    			sb.append(Constants.FILE_SEP);
    		}
    		sb.append(path);
    		lastPath = path;
    	}
    	return sb.toString();
    }
    
    public static String appendStr(String gap, String ... paths)
    {
    	if (paths == null || paths.length == 0) return null;
    	
    	StringBuilder sb = new StringBuilder(paths[0]);
    	int length = paths.length;
    	for (int i = 1; i < length; i++) {
    		if (!paths[i - 1].endsWith(gap)) {
    			sb.append(gap);
    		}
    		sb.append(paths[i]);
    	}
    	return sb.toString();
    }
    
    public static boolean isExists(String url) {
    	return new File(url).exists();
    }

    public static String[] getMenus(String filePath) {
        if (StringUtil.isEmpty(filePath)) return StringUtil.EMPTY_STRING_ARRAY;
        
        filePath = filePath.replace("/", Constants.FILE_SEP);
        filePath = filePath.replace("\\", Constants.FILE_SEP);
        return StringUtil.string2Array(filePath, Constants.FILE_SEP);
    }
    
    public static String getSuffix(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        int index = filename.lastIndexOf('.');
        return index < 0 ? null : filename.substring(index).toLowerCase();
    }

    public static File getChildFile(String name, File f) {
        if (f == null) return null;
        File child = null;
        for (File file : f.listFiles()) {
            if (file.isDirectory()) {
                child = getChildFile(name, file);
                if (child == null) continue;
            }
            else {
                if (file.getName().equals(name)) {
                    return file;
                }
            }
        }

        return child;
    }
    
    public static void writeFile(String url, String txt, OverwriteStrategy os) throws IOException {
        boolean isExist = FileUtil.isExists(url);
        if (isExist) {
            if (os == OverwriteStrategy.NEVER_COVER) {
                return;
            }
            else if (os == OverwriteStrategy.BAK) {
                url += ".bak";
            }
        }
        TxtUtil.write(url, txt);
    }

    public static void createFile(File f) throws IOException {
        File parentFile = f.getParentFile();
        if (parentFile == null || !parentFile.exists()) createDir(parentFile);
        f.createNewFile();
    }
    
    public static void createDir(File f) throws IOException 
    {
        File parentFile = f.getParentFile();
        if (!parentFile.exists()) parentFile.mkdirs();
        f.mkdirs();
    }   
    
    /**
     * 比如  abc.png， suffix为16*16
     * 返回abc16*16.png
     * @param url
     * @param suffix
     * @return
     */
    public static String addSuffix2File(String url, String suffix) {
        int lastIndexOf = url.lastIndexOf(".");
        if (lastIndexOf != -1) {
            return url.substring(0, lastIndexOf) + suffix + url.substring(lastIndexOf);
        }
        else {
            return url + suffix;
        }
    }
}
