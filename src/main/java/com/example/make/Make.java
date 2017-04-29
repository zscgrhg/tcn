package com.example.make;


import com.example.exec.Commands;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Objects;

import static com.example.make.Utils.*;

/**
 * Created by think on 17-4-28.
 */
public class Make {


    final String TOMCAT_MATCHER = "apache-tomcat-\\d.*";
    final String APR_MATCHER = "apr-\\d.*";
    final String APR_UTIL_MATCHER = "apr-util-\\d.*";
    final String TCNATIVE_MATCHER = "tomcat-native.*";


    static final String java_home = System.getProperty("java.home");


    final File sourceDir;

    public Make(File sourceDir) {
        this.sourceDir = sourceDir;
    }

    public void prepare() throws Exception {

        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new Exception("dir not exist : ./source");
        }
        File apr = Utils.getFile(sourceDir, APR_MATCHER);
        File apr_util = Utils.getFile(sourceDir, APR_UTIL_MATCHER);
        File tomcat = Utils.getFile(sourceDir, TOMCAT_MATCHER);

        Objects.requireNonNull(apr);
        Objects.requireNonNull(apr_util);
        Objects.requireNonNull(tomcat);
        untar(sourceDir, apr);
        untar(sourceDir, apr_util);
        untar(sourceDir, tomcat);
        File tomcat_home = getDir(sourceDir, TOMCAT_MATCHER);
        File bin = getDir(tomcat_home, "bin");
        File tcnative = getFile(bin, TCNATIVE_MATCHER);
        untar(bin, tcnative, sourceDir);

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
        InputStream env = Make.class.getClassLoader().getResourceAsStream("setenv.sh");
        Files.copy(env, setenv, StandardCopyOption.REPLACE_EXISTING);

        Path setserver = tomcat_home.toPath().resolve("conf/server.xml");
        InputStream server = Make.class.getClassLoader().getResourceAsStream("server.xml");
        Files.copy(server, setserver, StandardCopyOption.REPLACE_EXISTING);
    }

    public void pkg() throws Exception {
        prepare();
        File apr_base = Utils.getDir(sourceDir, APR_MATCHER);
        File tomcat_home = getDir(sourceDir, TOMCAT_MATCHER);

        installApr(apr_base);

        File apr_util_base = Utils.getDir(sourceDir, APR_UTIL_MATCHER);
        installAprUtil(apr_util_base);

        File tcnative_src_base = Utils.getDir(sourceDir, TCNATIVE_MATCHER);
        File tcnative_base = Utils.getDir(tcnative_src_base, "native");
        File tcnative = tomcat_home.toPath().resolve("tcnative").toFile();
        tcnative.mkdir();
        installTCNative(tcnative_base, tcnative);
        setenv(tomcat_home);
        tar(sourceDir, "apr-"+tomcat_home.getName() + ".tar.gz", tomcat_home);
    }

    public static void main(String[] args) throws Exception {
        if(args==null||args.length==0){
            args=new String[]{"source"};
        }
        File baseDir = new File(".");
        String path = args[0];
        File src = baseDir.toPath().resolve(path).toFile();
        if(src.exists()&&src.isDirectory()){
            new Make(src).pkg();
        }else {
            new Make(baseDir).pkg();
        }

    }
}
