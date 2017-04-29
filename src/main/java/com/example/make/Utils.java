package com.example.make;

import com.example.exec.Commands;

import java.io.File;
import java.nio.file.Path;

/**
 * Created by think on 17-4-29.
 */
public class Utils {
    public static File getDir(File baseDir,String dirnameRegex) throws Exception {
        File[] files = baseDir.listFiles();
        for (File file : files) {
            if(file.isDirectory()&&file.getName().matches(dirnameRegex)){
                return file;
            }
        }
        throw new Exception("dir not exsit: "+dirnameRegex);
    }
    public static File getFile(File baseDir,String filenameRegex) throws Exception {
        File[] files = baseDir.listFiles();
        for (File file : files) {
            if(!file.isDirectory()&&file.getName().matches(filenameRegex)){
                return file;
            }
        }
        throw new Exception("dir not exsit: "+filenameRegex);
    }
    public static void untar(File workDir,File tarFile) throws Exception {
        untar(workDir,tarFile,workDir);
    }
    public static void untar(File workDir,File tarFile,File dest) throws Exception {

        if(!dest.exists()){
            dest.mkdirs();
        }
        Commands commands = new Commands(workDir);
        commands.excute("tar","-zxvf",tarFile.getCanonicalPath(),"-C",dest.getCanonicalPath());
    }

    public static void tar(File workDir,String tarFileName,File include) throws Exception {

        Path relativize = workDir.toPath().relativize(include.toPath());
        Commands commands = new Commands(workDir);
        commands.excute("tar","-zcvf",tarFileName,relativize.toString());
    }

}
