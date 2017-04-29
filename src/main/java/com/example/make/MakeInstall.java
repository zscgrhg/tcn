package com.example.make;

import com.example.exec.Commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by think on 17-4-29.
 */
public class MakeInstall {
    final File workDir;
    final List<String> cmds;

    public MakeInstall(File workDir, List<String> configParams) {
        if(workDir!=null&&workDir.exists()&&workDir.isDirectory()){
            this.workDir = workDir;
        }else {
            this.workDir=new File(".");
        }

        ArrayList<String> list = new ArrayList<>();
        list.addAll(configParams);
        list.add(0,"configure");
        list.add(0,"/bin/sh");
        this.cmds = Collections.unmodifiableList(list);
    }
    public MakeInstall(File workDir, String... configParams) {
        this(workDir,Arrays.asList(configParams));
    }

    private int configure() throws Exception {
        Commands commands = new Commands(workDir);
        commands.excute(cmds);
        return 0;
    }
    private int make() throws Exception {
        Commands commands = new Commands(workDir);
        Integer make = commands.excute("make");
        return make;
    }
    private int make_install() throws Exception {
        Commands commands = new Commands(workDir);
        Integer excute = commands.excute("make", "install");
        return excute;
    }
    public void install() throws Exception {
        configure();
        make();
        make_install();
    }
    public static void main(String[] args) {
        System.out.println(new File(".").toPath().toAbsolutePath());

    }
}
