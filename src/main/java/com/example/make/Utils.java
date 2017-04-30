package com.example.make;

import com.example.exec.Commands;
import com.example.exec.Handler;
import com.example.exec.FineCommand;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Created by think on 17-4-29.
 */
public class Utils {
    static final String user_home = System.getProperty("user.home");

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

    public static Map<String,String> lscpu() throws Exception {
        FineCommand<Map<String,String>> command=new FineCommand<Map<String, String>>() {
            @Override
            protected Handler<Map<String, String>> fineHandler(Process process) {
                return new LscpuHandler();
            }
        };
        Map<String, String> lscpu = command.excute("lscpu");
        return lscpu;
    }

    public static void main(String[] args) {
        File f=new File("/home/think/IdeaProjects/tcn/source/target/apr-util-1.5.4/xml/expat/.libs");
        System.out.println(f.isDirectory());
        for (File file : f.listFiles()) {
            System.out.println(file);
            //boolean delete = file.delete();
            //System.out.println(delete);
        }

    }
}
