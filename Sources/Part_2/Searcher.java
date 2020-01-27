package Part_2;

import Part_1.Document;
import Part_1.Parse;
import Part_1.Indexer;
import com.medallia.word2vec.Word2VecModel;
import javafx.scene.control.Alert;
import sun.awt.Mutex;


import java.io.*;
import java.net.URL;
import java.util.*;

public class Searcher {

    private String queryText;
    private Query query;
    private String stopWordsPath;
    private String queryFilePath;
    private boolean semantics;
    private Queue<String> relevantDocs;
    public Queue<Document> querySet;
    public LinkedList<Document> queryAfterParse = new LinkedList<>();
    int indexQuery;
    private Parse parse;
    private StringBuilder allLinesInQueries;
    private Mutex lockAddToAfterParse = new Mutex();
    public HashMap<String, String[]> queriesToSearchAndRank;
    public HashMap<String,List<String>> relevantDocsForAll;
    private String postingPathSaved ;

    //private Indexer indexer;

    public Searcher(String query, String  postingPathSaved, String queryFilePath, boolean semantics,boolean stemm) {
        this.parse = new Parse(stemm, postingPathSaved, true);
        this.queryText = query;
        this.semantics = semantics;
        this.queryFilePath = queryFilePath;
        indexQuery = 1;

        queriesToSearchAndRank = new LinkedHashMap<>();
        relevantDocsForAll = new HashMap<>();
        if (stemm)
            this.postingPathSaved = postingPathSaved+"\\stemming";
        else
            this.postingPathSaved = postingPathSaved+"\\nonStemming";
    }

    public void processQuery() {

        String[] queryTextSplit;
        Document doc;
        querySet = new LinkedList<>();

        if (queryText != null) { //only string. inputtt

            if(semantics)
                queryText = addSemantics(queryText);

            queryTextSplit = queryText.split(" ");
            StringBuilder queryToProcess = new StringBuilder();
            int i = 0;
            while (i < queryTextSplit.length) {
                //queryTextSplit[i] = removeDelimiters(queryTextSplit[i]);
                queryTextSplit[i] = queryTextSplit[i].toLowerCase();
                queryToProcess.append(" ").append(queryTextSplit[i]); //adds to SB
                i++;
            }
            queryText = queryToProcess.toString(); //after all removals
            queryText = queryText.substring(1); //remove first extra blank.
            query = new Query(queryText);
            //createQueryDoc();
            doc = new Document();
            doc.setId(String.valueOf(indexQuery));
            doc.setText(queryText);
            querySet.add(doc);
            LinkedList newList = new LinkedList(querySet);
            querySet.clear();
            queryAfterParse = parse.parseDocs(newList);

            doc = queryAfterParse.poll(); //only one document in this return list.
            String[] toRank = prepareToRank(doc); //prepare String[] of query words to rank.

            queriesToSearchAndRank.put(String.valueOf(indexQuery), toRank);
        }

        else { // queries text file !!
            query = new Query("");
            readQueryFile();
            Document currQuery = new Document();

            while (!queryAfterParse.isEmpty()) { //each query is a separate doc in list.
                // currQuery = new Document();
                currQuery = queryAfterParse.poll();
                String[] toRank = prepareToRank(currQuery); //prepare String[] of query words to rank.
                queriesToSearchAndRank.put(currQuery.getId(), toRank);
            }
        }

        Ranker ranker = new Ranker(postingPathSaved); ////////////////// ????????????????????

  /*      // puts all the query terms into an array for rank
        HashMap<String, int[]> queryDic = query.getQueryTermDic();
        String[] queryTerms = new String[queryDic.size()];
        int i = 0;
        for (String queryWord : queryDic.keySet()) {
            queryTerms[i] = queryWord;
            i++;
        }*/

        relevantDocsForAll = ranker.rankAllQueries(queriesToSearchAndRank);
    }

    private String[] prepareToRank(Document doc) {
        if (doc != null) {
            String[] allTermsInQuery;
            String allText = doc.getTermDicAsString();
            allText = allText.substring(1);
            allTermsInQuery = allText.split(" ");
            return allTermsInQuery;
        }
        return null;
    }

    private void readQueryFile() {

        File queriesFile = new File(queryFilePath);

        if (queriesFile != null) {
            allLinesInQueries = new StringBuilder();
            try {
                BufferedReader myBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(queriesFile)));
                for (String currLine; (currLine = myBufferedReader.readLine()) != null; )
                    allLinesInQueries.append(currLine + System.lineSeparator());

                //createDoc();
                String text;
                String idQuery;
                int idQueryyy;
                String restOfQueries = allLinesInQueries.toString();
                //String id = allLinesInQueries.substring(allLinesInQueries.indexOf("<num>") + 2, endIndexId).trim();
                int startInd = allLinesInQueries.indexOf("<title>");
                Document[] allQueries = new Document[30];
                int numOfQueries = 0;

                while (startInd != -1) {
                    numOfQueries++;
                    idQueryyy = restOfQueries.indexOf(":")+2; //index!!! of Query number ID
                    idQuery = restOfQueries.substring(idQueryyy, restOfQueries.indexOf("<title>")-2); //number itself.
                    int endInd = restOfQueries.indexOf("<desc>", startInd)-5; //searches for "<desc>" from starts index
                    int descStart = restOfQueries.indexOf("<desc>", startInd);
                    int descEnd = restOfQueries.indexOf("<narr>", startInd);
                    String queryDescription = restOfQueries.substring(descStart + 19, descEnd).trim();
                    String currQuery = restOfQueries.substring(startInd+8, endInd)+" "+queryDescription; //query itself.

                    int endQuery = restOfQueries.indexOf("</top>", endInd);
                    restOfQueries = restOfQueries.substring(endQuery);

                    //set Id Query <num>"
                    if (semantics)
                        currQuery = addSemantics(currQuery);

                    allQueries[numOfQueries] = new Document();
                    allQueries[numOfQueries].setId(idQuery);
                    allQueries[numOfQueries].setText(currQuery);
                    querySet.add(allQueries[numOfQueries]);

                    startInd = restOfQueries.indexOf("<title>"); //continues to the next doc in file
                }

                //allLinesInQueries = new StringBuilder(); //initialize
                myBufferedReader.close();

                LinkedList newList = new LinkedList(querySet);
                querySet.clear();

                try {
                    LinkedList<Document> AfterParseQueries = parse.parseDocs(newList);
                    lockAddToAfterParse.lock();      ///??????????????????????/
                    queryAfterParse.addAll(AfterParseQueries);
                    lockAddToAfterParse.unlock();      ///??????????????????????/
                    // parseIndexrList++;
                    //System.out.println("to Parser: " +parseIndexrList);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error in folder path");
            alert.show();
        }
        //stopReadFile = true;
    }

    private String removeDelimiters(String word) {
        if (!word.equals("")) {
            int length = word.length();

            // removes delimiter from the beginning of the word
            if (word.charAt(0) > 'z' || word.charAt(0) < 'A' || (word.charAt(0) > 'Z' && word.charAt(0) < 'a')) {
                return removeDelimiters(word.substring(1));
            }

            // removes delimiter from the end of the word
            if (word.charAt(length - 1) > 'z' || word.charAt(length - 1) < 'A' || (word.charAt(length - 1) > 'Z' && word.charAt(length - 1) < 'a'))
                return removeDelimiters(word.substring(0, length - 1));

            return word;
        }
        return word;
    }

    public Queue<String> getRelevantDocs() {
        return relevantDocs;
    }

    public String addSemantics(String queryTerms) { //returns the query itself + 3 synonymus for each word in query.

        String[] querySplitted = queryTerms.split(" ");
        StringBuilder semanticQuery = new StringBuilder();
        int i = 0;

        while (i < querySplitted.length) {
            querySplitted[i] = removeDelimiters(querySplitted[i]);
            String result = addSemanticWordsOffline(querySplitted[i]);

            if (result != null && !result.equals(""))
                semanticQuery.append(result); //2 more words.
            else{
                semanticQuery.append(querySplitted[i]);
            }
            i++;
        }
        return semanticQuery.toString();
    }

    public String addSemanticWordsOffline(String term) {

       // String path = "C:\\Users\\Tali\\IdeaProjects\\RI"; //// changeeee
        String results = "";

        try {
            Word2VecModel model = Word2VecModel.fromTextFile(new File(System.getProperty("user.dir") + "\\Resources\\word2vec.c.output.model.txt"));
            com.medallia.word2vec.Searcher semanticSearcher = model.forSearch();

            int numOfResultsInList = 4;

            List<com.medallia.word2vec.Searcher.Match> matches = semanticSearcher.getMatches(term, numOfResultsInList);

            for (com.medallia.word2vec.Searcher.Match match : matches) {
                results = results + " " + match.match(); //return the term;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (com.medallia.word2vec.Searcher.UnknownWordException e) {
            // model doesn't know the word.
        }
        return results;
    }

        /*public String addSemanticWords(String terms) {

        StringBuilder allWords = new StringBuilder();
        allWords.append(terms); //word itself.
        URL address;

        String[] termsSplit = terms.split(" "); //if it's more than 1 word.
        String toCheck = "";
        for (int i = 0; i < termsSplit.length; i++) {
            if (i != termsSplit.length - 1)
                toCheck = toCheck + termsSplit[i] + "+";
            else //last word.
                toCheck = toCheck + termsSplit[i];
        }
        try {
            address = new URL("https://api.datamuse.com/words?ml=" + toCheck);
            StringBuilder sb = new StringBuilder("{\"result\":");

            BufferedReader in = new BufferedReader(new InputStreamReader(address.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                sb.append(inputLine);
            in.close();

            String allSynonyms = sb.toString();
            allSynonyms = allSynonyms.replaceAll(":|\\,|\\[|\\]|\\{|\\}"," ");
            allSynonyms = allSynonyms.replaceAll("\"", "");
            allSynonyms = allSynonyms.replaceAll("  ", " ");

            String toReturn = "";
            String[] toChoose = allSynonyms.split(" ");

            int startInd = allSynonyms.indexOf("word")+5;
            int only3words = 0;

                for (int i = 0; i < toChoose.length; i++) {
                    if (toChoose[i].equals("word")) {
                        toReturn = toReturn + " " + toChoose[i + 1];
                        allSynonyms = allSynonyms.substring(allSynonyms.indexOf("word") + 4);
                        toChoose = allSynonyms.split(" "); //again
                        only3words++;
                        i=0;
                        if (only3words == 3)
                            break;
                    }
                }

            return toReturn;

        } catch (IOException e) {
            return null;
        }
    }*/
}