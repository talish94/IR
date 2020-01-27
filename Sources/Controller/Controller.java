package Controller;

import Part_1.Document;
import Part_1.Indexer;
import Part_1.Parse;
import Part_1.ReadFile;
import Part_2.Searcher;
import Part_1.Indexer;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;


import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class Controller {

    public TextField documentPath ;
    public TextField postingPath;
    public Button start;
    public Button browse_Doc;
    public Button browse_posting;
    public Button reset;
    public Button displayInv;
    public Button loadInv;
    public CheckBox stemming;
    public String docPath= "" ;
    public String postingPathSaved = "" ;
    public boolean alreadyIndexedWithStemming = false;
    public boolean alreadyIndexedWithoutStemming = false;
    private boolean startsIndexing = false;
    public long startTime;
    public String loadDicPath;
    public boolean stemm = false;
    public Button saveResults;
    public TextField saveResultsPath;
    ReadFile rd;

    public TextField queryInput;
    public TextField QueryFilePath ;
    public String query;
    public CheckBox semantics;
    public boolean isQuery = false;
    public static HashMap<String, List<String>> allQueriesResults;



    public void onStart(){

        docPath = documentPath.getText();
        postingPathSaved = this.postingPath.getText();
        stemm = this.stemming.isSelected();
        String infoToDisplay = "";

        if(docPath.equals("") || postingPathSaved.equals("")){
            displayError("You have to fill the two paths");
        }else{

            if (stemm)
                deleteFiles(postingPathSaved+"\\stemming");
            else
                deleteFiles(postingPathSaved+"\\nonStemming");


            rd = new ReadFile(docPath ,stemm  ,postingPathSaved);
            try {
                ReadFile.stopParser = false;
                Parse.stopIndexer =false;

                startTime = System.nanoTime();
                startsIndexing = true;
                rd.start();

                if (stemm)
                    alreadyIndexedWithStemming = true;
                else
                    alreadyIndexedWithoutStemming = true;
            }
            catch(Exception e){
                e.printStackTrace();
            }finally {

                double totalTimeInSeconds = (System.nanoTime() - startTime) * Math.pow(10, -9);
                int numOfDocs = rd.getNumOfDoc();
                int numOfUniqueTerms = rd.getNumOFTerm();
                infoToDisplay = "Total documents indexed: " + numOfDocs + "\nNumber of unique " +
                        "terms in the corpus: " + numOfUniqueTerms + "\nTotal process' running time: "
                        + totalTimeInSeconds + " seconds";

                displayInfo(infoToDisplay);
                startsIndexing = false;

                System.out.println("Done!!!!!!!");
            }
        }
    }

    public void onBrowseDoc(){ Browse(documentPath); }

    public void onBrowsePosting(){ Browse(postingPath); }

    private void Browse(TextField text){
        JButton open = new JButton();
        JFileChooser jc = new JFileChooser();
        jc.setCurrentDirectory(new File("."));
        jc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if(jc.showOpenDialog(open) == JFileChooser.APPROVE_OPTION){
            text.setText(jc.getSelectedFile().getAbsolutePath());
        }
    }

    public void onReset(){

        postingPathSaved = this.postingPath.getText();
        if (!startsIndexing && ((alreadyIndexedWithStemming || alreadyIndexedWithoutStemming) || !postingPathSaved.equals(""))) {

            documentPath.setText("");
            postingPath.setText("");

            stemming.setSelected(false);

            File dir = new File(postingPathSaved);
            File[] dirFiles = dir.listFiles();
            if (dirFiles != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to reset?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
                alert.showAndWait();
                if (alert.getResult() == ButtonType.YES){
                    deleteFiles(postingPathSaved+"\\stemming");
                    deleteFiles(postingPathSaved+"\\nonStemming");
                }
            }

            alreadyIndexedWithStemming = false;
            alreadyIndexedWithoutStemming = false;
            rd = null;
            ReadFile.stopParser = false;
            Parse.stopIndexer = false;
            documentPath.clear();
            postingPath.clear();
            docPath = null;
            postingPath = new TextField();
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("No data to be reset!");
            alert.show();
        }
    }

    public void onDisplayInv(){

        ObservableList<Map.Entry<String, Integer>> invertedList = getObservableList();
        Stage stage = new Stage();
        stage.setTitle("Dictionary");

        TableColumn<Map.Entry<String, Integer>, String> tokenCol = new TableColumn<>("term");
        tokenCol.setMinWidth(200);
        tokenCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey()));

        TableColumn<Map.Entry<String, Integer> , Integer> numCol = new TableColumn<>("total shows");
        numCol.setMinWidth(100);
        numCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getValue()).asObject());

        TableView table = new TableView<>();
        table.setItems(invertedList);
        table.getColumns().addAll(tokenCol, numCol);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(table);

        Scene scene = new Scene(vbox);
        stage.setScene(scene);
        stage.show();
    }

    public void onLoadInv() throws IOException {

        postingPathSaved = this.postingPath.getText();
        stemm = this.stemming.isSelected();

        if (postingPathSaved.equals("")) {
            displayError("You have to fill the posting path to load a dictionary!");
        }

        else { //correct posting files  path.

            if (new File(postingPathSaved + "\\stemming").exists() && stemm) {
                loadDicPath = postingPathSaved + "\\stemming";
                //rd = new ReadFile(docPath, stemm, postingPathSaved);
            }
            else if (new File(postingPathSaved + "\\nonStemming").exists() && !stemm) {
                loadDicPath = postingPathSaved + "\\nonStemming";
                //rd = new ReadFile(docPath, stemm, postingPathSaved);
            }
            else {
                displayError("No files to load from the given path!");
                return;
            }

            File dir = new File(loadDicPath); //which dir to load from (stemm or not).
            File[] dirFiles = dir.listFiles();
            if (dirFiles != null) {
                for (File fileInDir : dirFiles) {
                    //each text file.
                    System.out.println("start load from file " + fileInDir.getName());
                    if (fileInDir != null)
                        load(fileInDir.getName());
                    System.out.println("loaded from file " + fileInDir.getName());
                }
                displayInfo("Dictionary was successfully loaded to memory.");
            }
        }
    }

    private ObservableList<Map.Entry<String, Integer>> getObservableList(){

        ObservableList<Map.Entry<String, Integer>> invertedList = FXCollections.observableArrayList();
        TreeMap<String,int[]> sortedList = new TreeMap<>(new Comparator<String>(){

            @Override
            public int compare(String s1, String s2) {
                int result = s1.compareToIgnoreCase(s2);
                if( result == 0 )
                    result = s1.compareTo(s2);
                return result;
            }
        });
        sortedList.putAll(Indexer.termDic);
        for(Map.Entry<String, int[]> entry : sortedList.entrySet()){
            Map.Entry<String, Integer> newEntry = new Map.Entry<String, Integer>() {
                @Override
                public String getKey() {
                    return entry.getKey();
                }

                @Override
                public Integer getValue() {
                    return entry.getValue()[1];
                }

                @Override
                public Integer setValue(Integer value) {
                    return null;
                }
            };
            invertedList.add(newEntry);
        }
        return invertedList;
    }

    private List<String> getInvAsList(){

        List<String> InvList = new ArrayList<>();
        TreeMap<String,int[]> sortedList = new TreeMap<>(new Comparator<String>(){

            @Override
            public int compare(String s1, String s2) {
                int result = s1.compareToIgnoreCase(s2);
                if( result == 0 )
                    result = s1.compareTo(s2);
                return result;
            }
        });
        sortedList.putAll(rd.indexer.termDic);
        for(Map.Entry<String, int[]> entry: sortedList.entrySet()) {
            int[] value = entry.getValue();
            InvList.add(entry.getKey() + "," + value[0] + "," + value[1]) ;
        }

        return InvList;
    }

    private void deleteFiles(String pathToDelete){


        File dir = new File(pathToDelete);
        if(dir.exists()) {
            File[] dirFiles = dir.listFiles();
            if (dirFiles != null) {
                for (File fileInDir : dirFiles) {
                    if (fileInDir != null)
                        fileInDir.delete();
                }
                dir.delete();
            }
        }
    }

    public void sortByValue() throws IOException {

        List<Map.Entry<String, int[]> > list =
                new LinkedList<Map.Entry<String, int[]> >(rd.indexer.termDic.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, int[]> >() {
            public int compare(Map.Entry<String, int[]> o1,
                               Map.Entry<String, int[]> o2)
            {
                return (o2.getValue()[1])-(o1.getValue()[1]);
            }
        });
        FileWriter pw = new FileWriter(postingPathSaved+"\\wordsAllWords.txt", false);
        Iterator it = list.iterator();
        int counter = 0;

        for(Map.Entry<String, int[]> entry  : list){

            if(counter < 10) {
                pw.write(entry.getKey() + "-" + entry.getValue()[1] + "\r\n");
                counter++;
            }else {
                break;
            }
        }
        pw.close();
        System.out.println("finish!!");

    }

    private void displayError(String error){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(error);
        alert.show();
    }

    private void displayInfo(String info){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(info);
        alert.show();
    }

    private void load (String fileNameToLoadFrom) {

        if(fileNameToLoadFrom.equals("docs.txt")){
            loadDoc(fileNameToLoadFrom);
            return;
        }
        String line1;
        String term;
        int totalShows = 0;
        int numOfDocs = 0;
        int[] termData = new int[2];
        String[] allDocsInfo = null;
        String currFilePath = loadDicPath + "\\" + fileNameToLoadFrom ;
        try {
            Scanner scanner = new Scanner((new File(currFilePath)));
            while (scanner.hasNextLine()) {

                String test = scanner.nextLine();
                line1 = test;
                if(line1.indexOf("|") == -1) {
                    System.out.println(line1);
                    continue;
                }
                term = line1.substring(0, line1.indexOf("|")); // only term itself, with no other data.
                line1 = line1.substring(line1.indexOf("|")+1); //without term itself.
                totalShows = 0;
                numOfDocs =0;
                String[] lineChar = line1.split("\\|");
                numOfDocs += lineChar.length;
                for(String s : lineChar) {
                    String[] dot = s.split("[:;]+");
                    try {
                        totalShows += Integer.parseInt(dot[1]);
                    }
                    catch (Exception e){
                        System.out.println(line1);
                    }
                }

                termData[0] = numOfDocs;
                termData[1] = totalShows;

                if(term.length() == 0)
                    continue;
                if(!Character.isLetter(term.charAt(0))){

                    if(Indexer.termDic.containsKey(term)) {
                        int[] savedTermData;
                        savedTermData = Indexer.termDic.get(term);
                        int[] updateTermInfo = new int[3];
                        updateTermInfo[0] = savedTermData[0] + termData[0]; // adds 1 to curr # of docs
                        updateTermInfo[1] = savedTermData[1] + termData[1]; //#shows total == adds num of appearences in specific doc. !!!
                        Indexer.termDic.replace(term, updateTermInfo);
                    }else{
                        Indexer.termDic.put(term,termData);
                    }
                }
                else if(Indexer.termDic.containsKey(term)){
                    int[] savedTermData;
                    savedTermData = Indexer.termDic.get(term);
                    int[] updateTermInfo = new int[3];
                    updateTermInfo[0] = savedTermData[0] + termData[0]; // adds 1 to curr # of docs
                    updateTermInfo[1] = savedTermData[1] + termData[1]; //#shows total == adds num of appearences in specific doc. !!!
                    Indexer.termDic.replace(term, updateTermInfo);

                }
                else if(Indexer.termDic.containsKey(term.toUpperCase()))
                {
                    int[] savedTermData = Indexer.termDic.get(term.toUpperCase());
                    int[] updateTermInfo = new int[3];
                    updateTermInfo[0] = savedTermData[0] + termData[0]; // adds 1 to curr # of docs
                    updateTermInfo[1] = savedTermData[1] + termData[1]; //#shows total == adds num of appearences in specific doc. !!!
                    Indexer.termDic.remove(term.toUpperCase());
                    Indexer.termDic.put(term, updateTermInfo);
                }else if( Indexer.termDic.containsKey(term.toLowerCase())){

                    int[] savedTermData = Indexer.termDic.get(term.toLowerCase());
                    int[] updateTermInfo = new int[3];
                    updateTermInfo[0] = savedTermData[0] + termData[0]; // adds 1 to curr # of docs
                    updateTermInfo[1] = savedTermData[1] + termData[1]; //#shows total == adds num of appearences in specific doc. !!!
                    Indexer.termDic.replace(term.toLowerCase(), updateTermInfo);

                }else{
                    Indexer.termDic.put(term,termData);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDoc(String fileNameToLoadFrom){

        try{
            String currFilePath = loadDicPath + "\\" + fileNameToLoadFrom ;
            Scanner scanner = new Scanner((new File(currFilePath)));
            while (scanner.hasNextLine()) {
                String test = scanner.nextLine();
                int line = test.indexOf(",");
                if(line != -1){
                    Indexer.allDocuments.put(test.substring(0,line),test.substring(line+1));
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean alreadyIndexedAll() {
        return alreadyIndexedWithStemming && alreadyIndexedWithoutStemming;
    }

    public void onRun() {
        //isQuery = true;
        postingPathSaved = this.postingPath.getText(); //posing directories and stop words locationnn!
        String query = queryInput.getText();
        String path = QueryFilePath.getText();
        Searcher searcher;
        //HashMap<String, List<String>> allQueriesResults;
        if(Indexer.termDic.isEmpty()|| Indexer.allDocuments.isEmpty()){
            displayError("You have to load the Dictionary first");
            return;
        }
        if((query == null || query.equals("")) && (path == null|| path.equals(""))) {
            displayError("You have to fill in the query file path or query ");
            return;
        }


        if (query != null && !query.equals("")) //an input query text.
            searcher = new Searcher(query, postingPathSaved, null, semantics.isSelected(), stemm);
        else
            searcher = new Searcher(null, postingPathSaved, path + "\\queries 03.txt", semantics.isSelected(), stemm);
        System.out.println("Start query");
        searcher.processQuery(); //updates the relevant docs for queryyyy
        allQueriesResults = searcher.relevantDocsForAll;
        System.out.println("End ranking!!!!");

        for (Map.Entry<String, List<String>> entry : allQueriesResults.entrySet()) {
            String queryID = entry.getKey();
            List<String> value = entry.getValue();
            for (String docID : value)
                System.out.println(queryID + " 0 " + docID +" 0 1 mt");
        }
        System.out.println(allQueriesResults.size());

        System.out.println("done queryyyy");
        displayResults();
    }


    public void onBrowseQuery() { Browse(QueryFilePath); }

    public void onSaveResults() {

        if (saveResultsPath.getText().equals(""))
            displayError("You have to fill a path to save the results!");
        else {
            try {
                PrintWriter out = new PrintWriter(new FileWriter(saveResultsPath.getText() + "\\results.txt", true));

                for (Map.Entry<String, List<String>> entry : allQueriesResults.entrySet()) {
                    String queryID = entry.getKey();
                    List<String> value = entry.getValue();
                    for (String docID : value)
                        out.append(queryID + " 0 " + docID + " 0 1 mt\n");

                }
                displayInfo("Your results were successfully saved to a text file!");
                out.close();

            } catch (IOException e) {
                displayError("IOException");
                e.printStackTrace();
            }
        }
    }

    public void onBrowseResults(ActionEvent actionEvent) {
            Browse(saveResultsPath);
    }

    public void displayResults(){
        ObservableList<Map.Entry<String, String>> invertedList = ObservableListResultst();
        Stage stage = new Stage();
        stage.setTitle("Results");
        TableView table = new TableView<>();

        TableColumn<Map.Entry<String, String>, String> tokenCol = new TableColumn<>("Query Id");
        tokenCol.setMinWidth(200);
        tokenCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey()));

        TableColumn<Map.Entry<String, String> , String> numCol = new TableColumn<>("Doc Id");
        numCol.setMinWidth(100);
        numCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue()));

        TableColumn button = new TableColumn<>("Doc Id");
        button.setMinWidth(50);
        button.setCellFactory(ActionButtonTableCell.forTableColumn("Entities",
                (Map.Entry<String, String> entry) -> {
                    getEntityForDoc(entry.getValue());
                    return entry;
                }));


        table.setItems(invertedList);
        //table.getColumns().addAll(tokenCol, numCol);
        table.getColumns().addAll(tokenCol, numCol,button);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(table);
        Scene scene = new Scene(vbox);
        stage.setScene(scene);
        stage.show();
    }

    private ObservableList<Map.Entry<String, String>> ObservableListResultst(){

        ObservableList<Map.Entry<String, String>> invertedList = FXCollections.observableArrayList();

        for(Map.Entry<String, List<String>> entry: this.allQueriesResults.entrySet()){

            for(String docId : entry.getValue()) {
                Map.Entry<String,String> newEntry =
                        new AbstractMap.SimpleEntry<>(entry.getKey(), docId);
                invertedList.add(newEntry);
            }
        }
        return invertedList;
    }

    private String getEntityForDoc(String id){

        String value = Indexer.allDocuments.get(id);
        HashMap<String,Integer> entitys = new HashMap<>();
        if(value == null)
            return "";
        String[] split = value.split("\\|");
        int index = 0;
        for(String entityData: split){
            if(index != 0){
                String[] secondSplit = entityData.split(",");
                entitys.put(secondSplit[0],Integer.parseInt(secondSplit[1]));
            }else{
                index++;
            }
        }

        index = 0;
        String ans = "";
        for(Map.Entry<String,Integer> entry : sortByValue(entitys).entrySet()){
            if(index == 5)
                break;
            else
                ans+= entry.getKey()+"\n";
            index++;

        }

        displayEntity(ans,id);
        return ans;
    }

    private void displayEntity(String info,String docId){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(docId);
        alert.setContentText(info);
        alert.show();
    }

    public static Map<String, Integer> sortByValue(final Map<String, Integer> wordCounts) {
        return wordCounts.entrySet()
                .stream()
                .sorted((Map.Entry.<String, Integer>comparingByValue().reversed()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
}