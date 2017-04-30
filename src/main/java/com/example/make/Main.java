package com.example.make;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Objects;

import static com.example.make.Utils.*;

/**
 * Created by think on 17-4-28.
 */
public class Main {

    static final String ARCH=System.getProperty("os.arch");

    final String TOMCAT_MATCHER = "apache-tomcat-\\d.*";
    final String APR_MATCHER = "apr-\\d.*";
    final String APR_UTIL_MATCHER = "apr-util-\\d.*";
    final String TCNATIVE_MATCHER = "tomcat-native.*";


    static final String java_home = System.getProperty("java.home");


    final File sourceDir;
    final File outputDir;
    public Main(File sourceDir, File outputDir) {
        this.sourceDir = sourceDir;
        this.outputDir = outputDir;
    }


    public void deleteDir(File target) throws IOException {
        if(target.isDirectory()){
            File[] files = target.listFiles();
            for (File file : files) {
                deleteDir(file);
            }
        }
        target.delete();
    }
    public void prepare() throws Exception {

        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new Exception("dir not exist : ./source");
        }
        deleteDir(outputDir);
        Thread.sleep(1000L);
        File apr = Utils.getFile(sourceDir, APR_MATCHER);
        File apr_util = Utils.getFile(sourceDir, APR_UTIL_MATCHER);
        File tomcat = Utils.getFile(sourceDir, TOMCAT_MATCHER);

        Objects.requireNonNull(apr);
        Objects.requireNonNull(apr_util);
        Objects.requireNonNull(tomcat);
        untar(outputDir, apr);
        untar(outputDir, apr_util);
        untar(outputDir, tomcat);
        File tomcat_home = getDir(outputDir, TOMCAT_MATCHER);
        File bin = getDir(tomcat_home, "bin");
        File tcnative = getFile(bin, TCNATIVE_MATCHER);
        untar(bin, tcnative, outputDir);

    }


    public void installApr(File aprConfigureDir) throws Exception {

        String[] params = {
                "--prefix=/usr/local/apr"
        };
        MakeInstall makeInstall = new MakeInstall(aprConfigureDir, params);
        makeInstall.install();
    }

    public void installAprUtil(File aprUtilConfigureDir) throws Exception {
        String[] params = {
                "--with-apr=/usr/local/apr"
        };
        MakeInstall makeInstall = new MakeInstall(aprUtilConfigureDir, params);
        makeInstall.install();
    }

    public void installTCNative(File tcnativeConfigureDir, File dest) throws Exception {
        String[] params = {
                "--with-apr=/usr/local/apr",
                "--with-java-home=" + Paths.get(java_home).getParent().toFile().getCanonicalPath(),
                "--prefix=" + dest.getCanonicalPath()
        };
        MakeInstall makeInstall = new MakeInstall(tcnativeConfigureDir, params);
        makeInstall.install();
    }

    public void setenv(File tomcat_home) throws Exception {
        Path setenv = tomcat_home.toPath().resolve("bin/setenv.sh");

        Files.copy(sourceDir.toPath().resolve("setenv.sh"), setenv, StandardCopyOption.REPLACE_EXISTING);

        Path setserver = tomcat_home.toPath().resolve("conf/server.xml");

        Files.copy(sourceDir.toPath().resolve("server.xml"), setserver, StandardCopyOption.REPLACE_EXISTING);
    }

    public void setRedisson(File tomcat_home) throws Exception {

        copy(tomcat_home,"lib","redisson-all-2.8.1.jar");
        copy(tomcat_home,"lib","redisson-tomcat-7-2.8.1.jar");
        copy(tomcat_home,"conf","context.xml");
        copy(tomcat_home,".","redisson.json");
    }

    public void copy(File tomcat_home, String subDir, String src) throws Exception {
        Path redissonJar = tomcat_home.toPath().resolve(subDir).resolve(src);
        InputStream redisson = new FileInputStream(sourceDir.toPath().resolve(src).toFile());
        Files.copy(redisson, redissonJar, StandardCopyOption.REPLACE_EXISTING);
    }



    public void pkg() throws Exception {
        prepare();
        File apr_base = Utils.getDir(outputDir, APR_MATCHER);
        installApr(apr_base);
        File apr_util_base = Utils.getDir(outputDir, APR_UTIL_MATCHER);
        installAprUtil(apr_util_base);

        File tomcat_home = getDir(outputDir, TOMCAT_MATCHER);
        File tcnative = tomcat_home.toPath().resolve("tcnative").toFile();
        tcnative.mkdir();
        File tcnative_src_base = Utils.getDir(outputDir, TCNATIVE_MATCHER);
        File tcnative_base = Utils.getDir(tcnative_src_base, "native");
        installTCNative(tcnative_base, tcnative);

        setenv(tomcat_home);
        tar(outputDir, "apr-"+tomcat_home.getName()+"-"+lscpu().get("Architecture") + ".tar.gz", tomcat_home);

        setRedisson(tomcat_home);

        tar(outputDir, "redisson-"+tomcat_home.getName()+"-"+lscpu().get("Architecture") + ".tar.gz", tomcat_home);
    }

    public static void main(String[] args) throws Exception {
        if(args==null||args.length==0){
            args=new String[]{"source"};
        }
        File baseDir = new File(".");
        String path = args[0];
        File src = baseDir.toPath().resolve(path).toFile();
        if(src.exists()&&src.isDirectory()){
            new Main(src, src.toPath().resolve("target").toFile()).pkg();
        }else {
            new Main(baseDir, baseDir.toPath().resolve("target").toFile()).pkg();
        }
    }
}
