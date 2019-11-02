package com.fullerspectrum;

import java.io.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.*;
import java.nio.file.*;

public class Main {

    public static void main(String[] args) {
        for(int i=0; i<args.length; i++){
            Path given = new File(args[i]).toPath();

            if(Files.isDirectory(given)){
                try(Stream<Path> paths = Files.walk(Paths.get(args[i]))){
                    paths.map(path -> path.toString()).filter(f -> f.endsWith(".epub"))
                            .forEach(fileName -> {
                                modifyTextFileInZip(fileName);
                            });
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
            else{
                if(given.toString().endsWith(".epub"))
                    modifyTextFileInZip(given.toString());
                else{}
            }
        }
    }

    // A ton of this came from here: https://stackoverflow.com/a/43836969/5574157

    /**
     * opens zip (epub here) as filesystem
     * @param zipPath
     */
    static void modifyTextFileInZip(String zipPath) {
        Path zipFilePath = Paths.get(zipPath);
        try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, null)) {
            Collection<Path> files = zipFilter(fs);
            Path source = zipFilePath;
            for(Iterator<Path> j = files.iterator(); j.hasNext();){
                source = j.next();
            }
            Path temp = fs.getPath("/content.opf.tmp");
            Files.move(source, temp);
            streamCopy(temp, source);
            Files.delete(temp);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * finds content.opf in filesystem
     * @param fs
     * @return
     * @throws IOException
     */
    private static Collection<Path> zipFilter(FileSystem fs) throws IOException{
        Stream<Path> files = Files.walk(fs.getPath("/"));
        return files
                .filter(f -> f.getFileName() != null)
                .filter(f -> f.getFileName().toString().equals("content.opf"))
                .collect(Collectors.toList());

    }

    /**
     * finds spine element and adds attribute
     * @param src
     * @param dst
     * @throws IOException
     */
    static void streamCopy(Path src, Path dst) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(Files.newInputStream(src)));
             BufferedWriter bw = new BufferedWriter(
                     new OutputStreamWriter(Files.newOutputStream(dst)))) {

            String line;
            while ((line = br.readLine()) != null) {
                if(!line.contains("page-progression-direction=\"rtl\""))
                    line = line.replace("<spine", "<spine page-progression-direction=\"rtl\" ");
                bw.write(line);
                bw.newLine();
            }
        }
    }
}
