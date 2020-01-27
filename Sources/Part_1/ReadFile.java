package Part_1;

import Controller.Controller;
import javafx.scene.control.Alert;
import sun.awt.Mutex;

import javax.print.Doc;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;


/**
 * This class reads all the files in the directory's path
 */

public class ReadFile extends Thread {

    private String pathDir;
    private StringBuilder allLinesInDoc;
    public static boolean stopParser = false;
    public Queue<Document> documentsSet = new LinkedList<>();
    public LinkedList<Document> afterParse = new LinkedList<>();
    ExecutorService poolReadFile =  Executors.newFixedThreadPool(10);
    private Parse parse ;
    private int numOfDoc = 0;
    private Mutex lockAddToAfterParse = new Mutex();
    public Indexer indexer;
    boolean stopReadFile = false;
    private int parseIndexrList =1;







    /**
     * the constructor of the class
     *
     * @param pathDir - the path of the directory the corpus is found in
     */
    public ReadFile(String pathDir , boolean stemm , String postingPathSaved ) {

        this.parse = new Parse(stemm,pathDir, false);
        this.indexer = new Indexer(stemm , postingPathSaved);
        this.pathDir = pathDir;


    }

    // reads all the files inside the corpus directory
    public void readInsideAllFiles() {

        File rootDirectory = new File(pathDir + "\\corpus");
        File[] allDirectories = rootDirectory.listFiles();
        if (allDirectories != null) {
            allLinesInDoc = new StringBuilder();
            for (File file : allDirectories) {
                File[] current = file.listFiles(); // gets the file itself, inside the corpus directory
                if (null != current) {
                    for (File txtfile : current) {
                        try {
                            BufferedReader myBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(txtfile)));
                            for (String currLine; (currLine = myBufferedReader.readLine()) != null; )
                                allLinesInDoc.append(currLine + System.lineSeparator());
                            createDoc();

                            allLinesInDoc = new StringBuilder();
                            myBufferedReader.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }


        }
        else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error in folder path");
            alert.show();
        }

        stopReadFile =true;

    }

    private void createDoc(){

        String text;
        String id = "";
        int startInd = allLinesInDoc.indexOf("<DOC>");
        while (startInd != -1) {

            Document newDoc = new Document();
            int endInd = allLinesInDoc.indexOf("</DOC>", startInd); //searches for "</DOC>" from starts index
            String currDoc = allLinesInDoc.substring(startInd, endInd);

            //set Id <DOCNO>, </DOCNO>"
            int startIndexId = currDoc.indexOf("<DOCNO>");
            int endIndexId = currDoc.indexOf("</DOCNO>");
            if (startIndexId == -1 || endIndexId == -1)
                newDoc.setId("");
            else {
                id = currDoc.substring(startIndexId + 7, endIndexId).trim();
                newDoc.setId(id);
            }

            //set Title <TI>,</TI>
            int startIndexTitle = currDoc.indexOf("<TI>", startInd);
            int endIndexTitle = currDoc.indexOf("</TI>", startInd);
            if (startIndexTitle == -1 || endIndexTitle == -1)
                newDoc.setTitle("");
            else {
                String title = currDoc.substring(startIndexTitle + 7, endIndexTitle).trim();
                newDoc.setTitle(title);
            }


            // gets the document's <TEXT></TEXT> tags
            if (currDoc.contains("<TEXT>")) {
                int startOfText;
                int addStart = 6;
                if (currDoc.contains("<F P=106>") || currDoc.contains("<F P=105>")) {
                    startOfText = currDoc.indexOf("[Text]");
                    if (currDoc.contains("[Excerpt]")) {
                        startOfText = currDoc.indexOf("[Excerpt]");
                        addStart = 9;
                    } else if (currDoc.contains("[Excerpts]")) {
                        startOfText = currDoc.indexOf("[Excerpts]");
                        addStart = 10;
                    }

                } else
                    startOfText = currDoc.indexOf("<TEXT>");
                int endOfText = currDoc.indexOf("</TEXT>");
                String docText = currDoc.substring(startOfText + addStart, endOfText).trim();
                if (docText.length() > 0)
                    newDoc.setText(docText);
            }


            documentsSet.add(newDoc);
            numOfDoc++;
            if (numOfDoc % 50 == 0) {
                LinkedList newList = new LinkedList(documentsSet);
                documentsSet.clear();
                try {
                    poolReadFile.execute(() ->
                    {
                        LinkedList<Document> AfterParseFuncation = parse.parseDocs(newList);
                        lockAddToAfterParse.lock();
                        afterParse.addAll(AfterParseFuncation);
                        lockAddToAfterParse.unlock();
                        parseIndexrList++;
                        //System.out.println("to Parser: " +parseIndexrList);



                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(parseIndexrList%5 == 0) {

                lockAddToAfterParse.lock();
                LinkedList<Document> IndexrSend = new LinkedList<>(afterParse);
                afterParse.clear();
                lockAddToAfterParse.unlock();
                indexer.indexAll(IndexrSend);
            }

            startInd = allLinesInDoc.indexOf("<DOC>", endInd); //continues to the next doc in file
        }
    }

    public void start(){

        readInsideAllFiles();
        poolReadFile.shutdown();

        try {
            poolReadFile.awaitTermination(30, TimeUnit.MINUTES);
            LinkedList newList = new LinkedList(documentsSet);
            documentsSet.clear();
            LinkedList<Document> AfterParseFuncation = parse.parseDocs(newList);
            lockAddToAfterParse.lock();
            afterParse.addAll(AfterParseFuncation);
            lockAddToAfterParse.unlock();
            LinkedList<Document> tmp = new LinkedList<>(afterParse);
            afterParse.clear();
            indexer.indexAll(tmp);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        indexer.shutDown();

    }

    public int getNumOfDoc(){
        return numOfDoc;
    }

    public int getNumOFTerm(){
        return indexer.termDic.size();
    }

}