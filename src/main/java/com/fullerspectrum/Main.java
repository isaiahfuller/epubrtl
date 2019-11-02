package com.fullerspectrum;

import org.springframework.util.FileSystemUtils;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Scanner;
import java.util.stream.*;
import java.nio.file.*;

public class Main {

    public static void main(String[] args) {
        for(int i=0; i<args.length; i++){
            System.out.println(args[i]);
            Path currentDirectory = Paths.get("");
            String dPath = ".tempfolder_"+i;
            String zPath = args[i];
            System.out.println(dPath + "\n" + zPath);
            String content = "";
            Collection<Path> contentList = shortUnzip(zPath,dPath);
            for(Iterator<Path> j = contentList.iterator(); j.hasNext();){
                content = j.next().toString();
            }
            readFile(content);
            zip(dPath, zPath.substring(0,zPath.length()-5) + "_rtl" + ".epub");

            FileSystemUtils.deleteRecursively(new File(dPath));
        }
    }

    private static void readFile(String dPath){
        try{
            File file = new File(dPath);
            Scanner sc = new Scanner(file);
            String content = "";
            while(sc.hasNextLine()){
                String temp = sc.nextLine();
                if(temp.contains("<spine")){
                    if(temp.contains("page-progression-direction=\"rtl\"")){
                        System.out.println("Already set rtl");
                    }else{
                        temp = temp.substring(0,temp.length() - 1) + " page-progression-direction=\"rtl\">";
                    }
                }
                content += temp + "\n";
            }
            sc.close();
            FileWriter fw = new FileWriter(file);
            PrintWriter pw = new PrintWriter(fw);
            pw.write(content);
            pw.close();
        } catch(FileNotFoundException e){
            readFile(dPath);
        } catch(IOException e){
            System.err.println(e);
        }
    }
    private static Collection<Path> shortUnzip(String zipFilePath, String destDir){
        new File(destDir).mkdirs();
        Path path = FileSystems.getDefault().getPath(destDir);
        ArrayList<Path> r = new ArrayList<Path>();
        r.add(path);
        try{
            Files.setAttribute(path, "dos:hidden",true);
        } catch(Exception e){
            System.err.println(e);
        }
        ZipUtil.unpack(new File(zipFilePath), new File(destDir));
        try (Stream<Path> files = Files.walk(Paths.get(destDir))){
            return files
                    .filter(f -> f.getFileName().toString().equals("content.opf"))
                    .collect(Collectors.toList());
        }catch(Exception e){
            System.err.println(e);
        }
        return r;
    }
    private static void zip(String sourceDirPath, String zipFilePath){
        ZipUtil.pack(new File(sourceDirPath), new File(zipFilePath));
    }
}
