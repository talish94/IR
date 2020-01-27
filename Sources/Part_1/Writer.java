package Part_1;

import Controller.Controller;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import Part_1.Indexer;
import sun.awt.Mutex;

public class Writer {

    private final String path ;
    private StringBuilder allTermsInPostingFile;
    public static int counterTest =0;
    public static Mutex[] mArray = new Mutex[7];

    public Writer(String path) {

        allTermsInPostingFile = new StringBuilder();
        this.path = path;

        File adFile = new File(path + "\\a-d.txt");
        File eiFile = new File(path + "\\e-i.txt");
        File jmFile = new File(path + "\\j-m.txt");
        File nqFile = new File(path + "\\n-q.txt");
        File rvFile = new File(path + "\\r-v.txt");
        File wzFile = new File(path + "\\w-z.txt");
        File nonLetterFile = new File(path + "\\nonLetter.txt");
        File docsFile = new File(path + "\\docs.txt");
        //File entityFile = new File(path + "\\entity.txt");
        mArray[0] = new Mutex();
        mArray[1] = new Mutex();
        mArray[2] = new Mutex();
        mArray[3] = new Mutex();
        mArray[4] = new Mutex();
        mArray[5] = new Mutex();
        mArray[6] = new Mutex();

        try {

            if (!adFile.exists()) {
                adFile.createNewFile();
            }

            if (!eiFile.exists()) {
                eiFile.createNewFile();
            }

            if (!jmFile.exists()) {
                jmFile.createNewFile();
            }

            if (!nqFile.exists()) {
                nqFile.createNewFile();
            }

            if (!rvFile.exists()) {
                rvFile.createNewFile();
            }
            if (!wzFile.exists()) {
                wzFile.createNewFile();
            }
            if (!nonLetterFile.exists()) {
                nonLetterFile.createNewFile();
            }
            if (!docsFile.exists()) {
                docsFile.createNewFile();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void createPostingFile( HashMap<String, String> ChunkTermDicDocs) {

        String ad = "" ;
        String ei= ""  ;
        String jm = "" ;
        String nq= "" ;
        String rv= "" ;
        String wz= "" ;
        String nonLetter = "" ;


        for(Map.Entry<String,String> entry : ChunkTermDicDocs.entrySet()){
            String value = entry.getValue();
            String key = entry.getKey();
            //System.out.println(key + "|" + value);
            char charOfKey = key.charAt(0);


            if ((charOfKey>='a' && charOfKey<='d') || (charOfKey>='A' && charOfKey<='D'))
                ad = ad+key + "|" + value + "\n";
            else if ((charOfKey>='e' && charOfKey<='i') ||( charOfKey>='E' && charOfKey<='I'))
                ei = ei+key + "|" + value + "\n";
            else if ((charOfKey>='j' && charOfKey<='m') || (charOfKey>='J' && charOfKey<='M'))
                jm = jm+key + "|" + value + "\n";
            else if ((charOfKey>='n' && charOfKey<='q') || (charOfKey>='N' && charOfKey<='Q'))
                nq = nq+key + "|" + value + "\n";
            else if ((charOfKey>='r' && charOfKey<='v') || (charOfKey>='R' && charOfKey<='V'))
                rv = rv+key + "|" + value + "\n";
            else if ((charOfKey>='w' && charOfKey<='z') || (charOfKey>='W' && charOfKey<='Z'))
                wz = wz+key + "|" + value + "\n";
            else if(!(key.substring(0,1)).matches(".[a-zA-Z].")) {
                nonLetter = nonLetter + key + "|" + value + "\n";
            }
        }
        ChunkTermDicDocs.clear();

        try {
            PrintWriter out ;

            if (!ad.equals("")) {
                mArray[0].lock();
                //FileWriter fw = new FileWriter(path + "\\a-d.txt", true);

                PrintWriter out_a = new PrintWriter(new FileWriter(path + "\\a-d.txt", true));
                out_a.append(ad);
                out_a.close();
                mArray[0].unlock();
            }
            if (!ei.equals("")) {
                mArray[1].lock();
                PrintWriter out_e = new PrintWriter(new FileWriter(path + "\\e-i.txt", true));
                out_e.append(ei);
                out_e.close();
                mArray[1].unlock();
            }
            if (!jm.equals("")) {
                mArray[2].lock();
                PrintWriter out_j = new PrintWriter(new FileWriter(path + "\\j-m.txt", true));
                out_j.append(jm);
                out_j.close();
                mArray[2].unlock();
            }
            if (!nq.equals("")) {
                mArray[3].lock();
                PrintWriter out_n = new PrintWriter(new FileWriter(path + "\\n-q.txt", true));
                out_n.append(nq);
                out_n.close();
                mArray[3].unlock();
            }
            if (!rv.equals("")) {
                mArray[4].lock();
                PrintWriter out_r = new PrintWriter(new FileWriter(path + "\\r-v.txt", true));
                out_r.append(rv);
                out_r.close();
                mArray[4].unlock();
            }
            if (!wz.equals("")) {
               mArray[5].lock();
                PrintWriter out_w = new PrintWriter(new FileWriter(path + "\\w-z.txt", true));
                out_w.append(wz);
                out_w.close();
                mArray[5].unlock();
            }
            if (!nonLetter.equals("")) {
                mArray[6].lock();
                PrintWriter out_non = new PrintWriter(new FileWriter(path + "\\nonLetter.txt", true));
                out_non.append(nonLetter);
                out_non.close();
                mArray[6].unlock();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("Wrote this cuhnk to disk." + counterTest);
        counterTest++;

    }



    public void writeDocuments(HashMap<String, String> docs){

        try {
            PrintWriter out = new PrintWriter(new FileWriter(path + "\\docs.txt", true));
            for(Map.Entry<String,String> entry : docs.entrySet()) {
                String docId = entry.getKey();
                out.append(docId + "," + entry.getValue() + "\n");
            }
            out.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }
    public void sortPostingFiles(){

        File dir = new File(path);
        File[] dirFiles = dir.listFiles(); //max - all 7 posting files.
        String name="";
        if (dirFiles != null) {
            for (File fileInDir : dirFiles) {
                name = fileInDir.getName();
                HashMap<String , String>  terms = new HashMap<>();
                if (fileInDir != null) {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileInDir)));
                        for (String currTermInfo; (currTermInfo = br.readLine()) != null; ) {

                            try {
                                currTermInfo.substring(0, currTermInfo.indexOf("|"));
                            }
                            catch (Exception e){
                                continue;
                            }
                            String key = currTermInfo.substring(0, currTermInfo.indexOf("|"));
                            if(terms.containsKey(key)){
                                terms.replace(key ,terms.get(key)+"|"+currTermInfo.substring(currTermInfo.indexOf("|")+1));
                            }else if(terms.containsKey(key.toUpperCase())){
                                String value = terms.remove(key.toUpperCase());
                                terms.put(key ,currTermInfo+"|"+value.substring(value.indexOf("|")+1));
                            }else if(terms.containsKey(key.toLowerCase())){
                                terms.replace(key.toLowerCase() ,terms.get(key.toLowerCase())+"|"+currTermInfo.substring(currTermInfo.indexOf("|")+1));
                            }else{
                                terms.put(key , currTermInfo);
                            }
                        }

                        File newPostFile = new File(path + "\\" + name );
                        PrintWriter out = new PrintWriter(newPostFile);
                        for(String line : terms.values()) //write all to a new file.
                            out.append(line +"\n");
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } //finish gets the whole file.
            }
        }
    }
}