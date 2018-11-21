package com.bocloud.paas.web.utils;

import java.io.File;

import com.bocloud.common.utils.FileSeparator;

public class FileOperation {

    public static boolean deleteFile(String filepath) {
        File file = new File(filepath);
        if (!file.exists() || !file.isFile()) {
            return false;
        }
        if (file.delete()) {
            return true;
        }
        return false;
    }

    public static boolean deleteDirectory(String filepath) {
        String[] allPath = filepath.split(FileSeparator.separator);
        String dirPath = "";
        for (int i = 0; i < allPath.length - 1; i++) {
            dirPath += FileSeparator.separator + allPath[i];
        }
        if (!dirPath.endsWith(FileSeparator.separator)) {
            dirPath += FileSeparator.separator;
        }
        File dirFile = new File(dirPath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        if (dirFile.delete()) {
            return true;
        }
        return false;
    }

    public static boolean moveFile(String source, String target) {
        File file = new File(source);
        if (!file.exists()) {
            return false;
        }
        String[] p = file.getAbsolutePath().split(FileSeparator.separator);
        String filename = p[p.length - 1];
        File afile = new File(target);
        if (!afile.exists()) {
            afile.mkdirs();
        }
        String newPath = afile.getAbsolutePath() + File.separatorChar + filename;
        if (file.renameTo(new File(newPath))) {
            return true;
        }
        return false;
    }

    public static boolean makeDirs(String folderName) {
        if (folderName == null || folderName.isEmpty()) {
            return false;
        }
        File folder = new File(folderName);
        return (folder.exists() && folder.isDirectory()) ? true : folder.mkdirs();
    }


}
