package Part_2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import Part_1.Document;
import Part_1.Indexer;

public class Ranker {


    String path ;
    int avgLengthOfDoc ;
    int numberOfDoc;
    Set<String> setOfDoc ;



    public Ranker(String path){
        this.path = path;
        avgLengthOfDoc= 210;
        numberOfDoc =getNumberOfDocs();
        setOfDoc = new HashSet<>();
    }

    public HashMap<String,List<String> > rankAllQueries (HashMap<String, String[]> allQueries){
        HashMap<String,List<String> > listOfQueries = new LinkedHashMap<>();
        //LinkedList<LinkedList<String>> listOfDocs = new LinkedList<>();
        for(Map.Entry<String, String[]> entry : allQueries.entrySet() ){
            String key = entry.getKey();
            long startTime = System.nanoTime();
            List<String> value = rank(entry.getValue());
            System.out.println("Id:"+key+",Time:"+(System.nanoTime() - startTime) * Math.pow(10, -9));
            listOfQueries.put(key,value);
            setOfDoc.clear();

        }
        return listOfQueries;
    }

    public List<String> rank(String[] terms){


        this.avgLengthOfDoc = 209;
        HashMap<String,Double> allDocRank =new HashMap<>();
        HashMap<String,HashMap<String,int[]>> docsData = new HashMap<>();
        String term = null ;
        for(int indexQuery = 0 ;  indexQuery < terms.length ; indexQuery++) {
             term = checkTerm(terms[indexQuery]);
            if(term == null) {
                terms[indexQuery] ="";
                continue;
            }
            docsData.put(terms[indexQuery],getDataForTerm(term));
        }

        LinkedList<int[]> valuesList = new LinkedList<>();
       for(String docId : setOfDoc ){
           for(int indexQuery = 0 ;  indexQuery < terms.length ; indexQuery++){

               if(terms[indexQuery].equals(""))
                   continue;
               HashMap<String,int[]> dataForQuery =  docsData.get(terms[indexQuery]);
               if(dataForQuery.containsKey(docId))
                   valuesList.push(dataForQuery.get(docId));

           }
           Double rankedDoc =rankQueryDoc(valuesList);
           valuesList.clear();
           allDocRank.put(docId,rankedDoc);
       }


        //allDocRank.entrySet().stream().sorted((k2,k1) -> -k2.getValue().compareTo(k1.getValue()));



       int index =0;
       List<String> topValues = new LinkedList<>();
       for(Map.Entry<String,Double> entry : sortByValue(allDocRank).entrySet()){
           //System.out.println(entry.getValue());
           if(index == 50)
               break;
           else
               topValues.add(entry.getKey());
           index++;

       }


        return topValues;

    }

    public static Map<String, Double> sortByValue(final Map<String, Double> wordCounts) {
        return wordCounts.entrySet()
                .stream()
                .sorted((Map.Entry.<String, Double>comparingByValue().reversed()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    private String checkTerm(String term){


        if (Indexer.termDic.containsKey(term))
           return term;
        else if (term.equals(term.toUpperCase())){
            return term.toLowerCase();

        } else if (Indexer.termDic.containsKey(term.toUpperCase())){
                    return term.toUpperCase();
        }
        return null;
    }

    public String getFilePath(String term) {


        char charOfKey = term.charAt(0);


        if ((charOfKey >= 'a' && charOfKey <= 'd') || (charOfKey >= 'A' && charOfKey <= 'D'))
            return path + "\\a-d.txt";
        else if ((charOfKey >= 'e' && charOfKey <= 'i') || (charOfKey >= 'E' && charOfKey <= 'I'))
            return path + "\\e-i.txt";
        else if ((charOfKey >= 'j' && charOfKey <= 'm') || (charOfKey >= 'J' && charOfKey <= 'M'))
            return path + "\\j-m.txt";
        else if ((charOfKey >= 'n' && charOfKey <= 'q') || (charOfKey >= 'N' && charOfKey <= 'Q'))
            return path + "\\n-q.txt";
        else if ((charOfKey >= 'r' && charOfKey <= 'v') || (charOfKey >= 'R' && charOfKey <= 'V'))
            return path + "\\r-v.txt";
        else if ((charOfKey >= 'w' && charOfKey <= 'z') || (charOfKey >= 'W' && charOfKey <= 'Z'))
            return path + "\\w-z.txt";
        else if (!(term.substring(0, 1)).matches(".[a-zA-Z]."))
            return path + "\\nonLetter.txt";
        else
            return null;

    }


    public HashMap<String,int[]> getDataForTerm(String term ){


        File file = new File(getFilePath(term));
        HashMap<String,int[]> docs = new LinkedHashMap<>();
        try{
            BufferedReader myBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            for (String currLine; (currLine = myBufferedReader.readLine()) != null; ){
                int indexTermFromLine = currLine.indexOf("|");
                String lineTerm = currLine.substring(0,indexTermFromLine);
                boolean equalTermsUpper = lineTerm.equals(term.toUpperCase());
                boolean equalTermsLower = lineTerm.equals(term.toLowerCase());

                if(equalTermsUpper||equalTermsLower) {
                    int[] dataTermDoc ;
                    String docId;
                    String withOutTerm = currLine.substring(currLine.indexOf("|") + 1);
                    String[] splitLine = withOutTerm.split("\\|");
                    for(String docsLine : splitLine){
                        String[] docSplit = docsLine.split("[:;]+");
                        docId = docSplit[0]; /// doc name
                        setOfDoc.add(docId);
                        dataTermDoc = new int[5];
                        int[] termData;
                        termData=Indexer.termDic.get(term);
                        if(termData == null)
                            System.out.println(term+": null termData Rank!!!!");
                        dataTermDoc[0] = termData[0]; // # df
                        dataTermDoc[1] = Integer.parseInt(docSplit[1]); //tf
                        dataTermDoc[2] = geLengthOfDoc(docId); //docLength
                        if(dataTermDoc[2] == 0)
                            continue;
                        dataTermDoc[3] = Integer.parseInt(docSplit[2]); //location
                        dataTermDoc[4] =Integer.parseInt(docSplit[3]); //title
                        docs.put(docId,dataTermDoc);

                    }


                }


            }


        }
        catch (Exception e){
            e.printStackTrace();
        }
        return docs;
    }


   private int getNumberOfDocs(){


        if(Indexer.allDocuments == null)
            return 0;

        return Indexer.allDocuments.size();


   }

   private int geLengthOfDoc(String docId){


        String value = Indexer.allDocuments.get(docId);

        String[] split = value.split("[,\\|]+");
        int length = 0;
        try{
            length = Integer.parseInt(split[2]);

        }catch (Exception e){
            e.printStackTrace();
        }

        return length;

   }



    /**
     *
     *       need to change the avgLengthOfDoc to the current doc
     *                     0  1  2
     * @param data order: df,tf,docLength
     * @return
     */
    private double rankTermDoc(int[] data){

        double b = 0.75;
        double k = 0.5;
        double denominator = data[1] +k*((1-b)+((b*data[2])/avgLengthOfDoc));
        double numerator = (k+1)*data[1];
        double log = Math.log10((numberOfDoc-data[0]+0.5)/(data[0]+0.5));
        return (numerator/denominator)*log;

    }

    private double rankQueryDoc(LinkedList<int[]> dataQuery){

        double rankDoc = 0.0;
        for(int[] data : dataQuery ){
            double rankPerTerm = rankTermDoc(data);
            rankDoc+= rankPerTerm;

            double addToRank = rankPerTerm*0.1;


        }



        return rankDoc;
    }






}
