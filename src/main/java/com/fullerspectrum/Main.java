package com.fullerspectrum;

import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.util.Scanner;
import java.util.zip.*;
import java.nio.file.*;

public class Main {

    public static void main(String[] args) {
        for(int i=0; i<args.length; i++){
            System.out.println(args[i]);
        }
        Path currentDirectory = Paths.get("");
        String dPath = "tempfolder";
        //String zPath = "vol1.epub"; // TEMP, replace with arg
        String zPath = args[0];
        System.out.println(dPath + "\n" + zPath);

        //unzip(zPath, dPath);

        shortUnzip(zPath,dPath);
        readFile();
        zip(dPath, zPath.substring(0,zPath.length()-5) + "_rtl" + ".epub");
    }

    //This is now completely unnecessary
    private static void unzip(String zipFilePath, String destDir) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if(!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while(ze != null){
                String fileName = ze.getName();
                File destinationDir = new File("tempfolder");
                if(ze.isDirectory()){
                    Files.createDirectories(Paths.get(destDir + File.separator + ze.getName()));
                }else{
                    String canonicalDestinationDirPath = destinationDir.getCanonicalPath();
                    File newFile = new File(destDir + File.separator + fileName);
                    String canonicalDestinationFile = newFile.getCanonicalPath();
                    if(!canonicalDestinationFile .startsWith(canonicalDestinationDirPath + File.separator))
                        throw new IOException("Entry is outside of the target dir: " + ze.getName());
                    System.out.println("Unzipping to " + newFile.getAbsolutePath());
                    //create directories for sub directories in zip
                    new File(newFile.getParent()).mkdirs();
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private static void readFile(){
        try{
            File file = new File("tempfolder" + File.separator + "content.opf");
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
        } catch(Exception e){
            System.err.println(e);
        }
    }
    private static void shortUnzip(String zipFilePath, String destDir){
        ZipUtil.unpack(new File(zipFilePath), new File(destDir));
    }
    private static void zip(String sourceDirPath, String zipFilePath){
        ZipUtil.pack(new File(sourceDirPath), new File(zipFilePath));
    }
}
