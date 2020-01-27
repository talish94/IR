package Part_1;
import javafx.scene.control.Alert;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;


public class Parse  {


    private Set<String> stopWords = new HashSet<>();
    private String[] sums = {"Dollars","million","billion","trillion","m","bn"};
    private String[] months = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec","January",
            "February","March","April","May","June","July","August","September","October","November","December"};
    boolean iSstemmer ;
    Stemmer stemmer = new Stemmer();
    DecimalFormat df = new DecimalFormat("#.###");

    public static boolean stopIndexer = false;

    private boolean isQuery;
    LinkedList<Document> queryParse  = new LinkedList<>();


    public Parse(boolean stemmer , String stopWordPath, boolean query){

        this.iSstemmer = stemmer;
        this.isQuery = query;
        df.setRoundingMode(RoundingMode.CEILING);
        setStopWord(stopWordPath);
    }

    public LinkedList<Document> parseDocs(LinkedList<Document> listDocument) {

        Stemmer stemm = new Stemmer();
        String currToken = "";
        LinkedList<Document>afterParse  = new LinkedList<>();
        while (!listDocument.isEmpty()) {
            Document newDoc = listDocument.poll();
            String text = newDoc.getText();
            if(text == null)
                continue;
            handleEntity(newDoc);
            text = text.replaceAll("[^ -~]|\\${2,}|\\.{2,}|\\,|\\`","");
            String[] allTokens = text.split("(?!,[0-9])[\",\\/?>~<&=@!\\[\\]:;}{;|*#'+)_(\\s]+");
            newDoc.clear();
            int index = 0;
            newDoc.setSize(allTokens.length);
            this.setTitle(newDoc,stemm);
            //String entity = "";
            while (index < allTokens.length) {
                int add;
                boolean havaAllSpace = true;
                String[] afterIndex = new String[3];
                for(int i =1 ; i<4 ; i++){
                    if(index+i < allTokens.length)
                        afterIndex[i-1] = allTokens[index+i];
                    else {
                        afterIndex[i - 1] = "";
                        havaAllSpace = false;
                    }
                }
                currToken = allTokens[index].trim();

                String currTokenStartEnd = startEndWord(currToken);
                if(isBetween(currToken,havaAllSpace,afterIndex)){

                    insertTermDic(currToken +" "+ allTokens[index + 1] +" "+ allTokens[index + 2] +" "+ allTokens[index + 3],newDoc,index);
                    index = index + 4;
                    continue;
                }
                if (currToken.equals("") || currToken.length() == 1 || currToken.equals("\n") || stopWord(currTokenStartEnd)) {
                    if (!currToken.equals("May")) {

                        index++;
                        continue;
                    }
                }
                boolean containNum = currTokenStartEnd.matches(".*\\d.*");
                boolean containLettersAndNum =containNum && currTokenStartEnd.matches("[a-zA-Z]+");

                if (!containNum && !isMonths(currTokenStartEnd)) {
                    if (isLine(currTokenStartEnd)) {
                        index+=handleLine(currToken,afterIndex[0], newDoc,index);
                    } else {
                        handleWords(currTokenStartEnd, newDoc, currTokenStartEnd, index, stemm);
                        index++;
                    }
                }
                else if (!containLettersAndNum &&(add =isDate(currTokenStartEnd,afterIndex,newDoc,index)) != -1) {
                    index +=add;
                    continue;
                } else if (!containLettersAndNum && isPrice(currToken,afterIndex[0])) {
                    index+=handlePrice(currToken,afterIndex,newDoc, index);
                } else if (!containLettersAndNum && isPercent(currToken ,afterIndex[0])) {
                    index+=handlePercent(currToken ,newDoc,index);
                } else if (!containLettersAndNum &&isNumericDouble(currToken)) {
                    index+=handleNum(currToken,afterIndex[0] , newDoc , index);
                } else if (isLine(currTokenStartEnd)) {
                    index+=handleLine(currToken , afterIndex[0],newDoc,index);

                } else if(containLettersAndNum) {
                    index++;
                    continue;
                }else{
                    handleWords(currToken,newDoc,currTokenStartEnd,index,stemm);
                    index++;
                }
            }

            afterParse.add(newDoc);

        }
        return  afterParse;

    }
    private String secondTermEntity(String term){

        if(term == null || term.length()<2 || stopWord(term))
            return null;
       if(!Character.isLetter(term.charAt(term.length()-1)))
            term= term.substring(0,term.length()-1);
        if(term.matches("^[A-Z][a-z]*$"))
            return term;

        return null;
    }

    private String firstTermEntity(String term){

        if(term == null || term.length()<2 || stopWord(term))
            return null;
        if(!Character.isLetterOrDigit(term.charAt(0)))
            term= term.substring(1);
        if(term.matches("^[A-Z][a-z]*$"))
            return term;

        return null;
    }

    private void handleEntity(Document newDoc){

        String[] split = newDoc.getText().split("[\\s-]+");
        String entity = "";
        int isEntity = 0;
        String term = null;

            for(String word:split){
                if(isEntity == 0) {
                    term = firstTermEntity(word);
                    if(term !=null){
                        isEntity++;
                        entity =term;
                    }else{
                        entity = "";
                    }

                }
                else{
                    term = secondTermEntity(word);
                    if(term!=null){
                        isEntity++;
                        entity =entity+" "+term;
                        if(!Character.isLetter(word.charAt(term.length()-1))){
                            insertEntity(entity.toUpperCase(),newDoc);
                            entity = "";
                            isEntity = 0;
                        }
                    }
                    else if(isEntity>1){
                        insertEntity(entity.toUpperCase(),newDoc);
                        entity = "";
                        isEntity = 0;
                    }else{
                        entity = "";
                        isEntity = 0;
                    }
                }
        }
    }

    private void insertEntity(String entity,Document newDoc){
        if(newDoc.entitys.containsKey(entity))
            newDoc.entitys.replace(entity,newDoc.entitys.get(entity)+1);
        else
            newDoc.entitys.put(entity,1);
    }


    private void insertFirstOccur(String term,Document newDoc, int index){

        if(term != null && !term.equals("") && term.length()>1) {
            int[] data = new int[4];
            data[0] = 1;
            double start = index/newDoc.size;
            if(start<0.13)
                data[1] = 1;
            else
                data[1] = 0;
            if(newDoc.title != null &&newDoc.title.contains(term.toLowerCase()))
                data[2] = 1;
            else
                data[2] = 0;

            newDoc.termDic.put(term, data);

            //newDoc.termPlacesInDoc.put(term, String.valueOf(index));
        }
    }

    private void insertTermDic(String term , Document newDoc,int index){

        if(term != null && !term.equals("") && term.length()>1) {

            int[] newData = newDoc.termDic.get(term);
            if(newData == null)
                insertFirstOccur(term , newDoc, index);
            else {
                newData[0]++;

                newDoc.termDic.replace(term, newData);
                //newDoc.termPlacesInDoc.replace(term, newDoc.termPlacesInDoc.get(term) + "," + index);
            }
        }
    }

    private void changeUpperCaseToLowerCase(String term , Document newDoc){

        if(term != null && !term.equals("")) {
            int[] newData = newDoc.termDic.get(term.toUpperCase());
            newData[0]++;
            newDoc.termDic.remove(term.toUpperCase());
            newDoc.termDic.put(term , newData);

        }
    }

    private String numericToPrice(String num ,String sum,String fraction ,boolean sign, boolean Dollars , boolean US ){
        //String price = num.replace("," , "");
        try {
            String price = num;
            if (sign)
                price = price.substring(1);
            if (lessThanMillion(price) && !sum.equals("trillion") && !sum.equals("million") && !sum.equals("billion") && !sum.equals("m") && !sum.equals("bn")) {
                if (!fraction.equals(""))
                    price = price + " " + fraction + " Dollars";
                else if (price.charAt(0) == '.')
                    price = "0" + price + " Dollars";
                else
                    price = price + " Dollars";
            } else {  ///greater then M
                price = price.replace(",", "");
                if (sum.equals("billion") || sum.equals("bn"))
                    price = new BigDecimal(price).movePointRight(3).toString(); //adds 3 zeroes. B ==> M.
                else if (sum.equals("trillion"))
                    price = new BigDecimal(price).movePointRight(6).toString(); //adds 6 ??? zeroes. T ==> M.

                price = price + " M Dollars";
            }

            return price;
        }
        catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    private boolean equalToSum(String word){
        if(word == null)
            return false;
        for(String sum : sums ){
            if(sum.equals(word.toLowerCase()))
                return true;
        }
        return false;
    }

    private boolean isPrice(String price , String afterTerm){

        if(price.charAt(0) == '$' && isNumericDouble(price.substring(1)))
            return true;
        if(!afterTerm.equals("") && isNumericDouble(price)){
            String word = afterTerm;
            if(equalToSum(word))
                return true;
        }

        return false;
    }

    private int handlePrice(String currToken , String[] afterIndex,Document document,int index ){
        String price = "";
        boolean sign ;
        int indexAdd = 0;

        String[] priceTerms = new String[4];
        priceTerms[0] = currToken;
        priceTerms[1] = afterIndex[0];
        priceTerms[2] = afterIndex[1];
        priceTerms[3] = afterIndex[2];

        sign =(priceTerms[0].charAt(0) == '$');

        if(equalToSum(priceTerms[1])){
            priceTerms[1] = priceTerms[1].toLowerCase();
            if(priceTerms[1].equals("Dollars")){
                price = numericToPrice(priceTerms[0],priceTerms[1],"", sign, true , false);
                indexAdd= 2;
            }else if(priceTerms[1].equals("million") || priceTerms[1].equals("billion") ||priceTerms[1].equals("trillion") ){
                if(priceTerms[2].equals("U.S.") && priceTerms[3].equals("dollars")) {
                    price = numericToPrice(priceTerms[0], priceTerms[1],"",  sign, true, true);
                    indexAdd= 4;
                }else { // $100 million
                    price = numericToPrice(priceTerms[0], priceTerms[1],"",  sign, false, false);
                    indexAdd= indexAdd +2;
                }
            } else if (priceTerms[1].equals("m") || priceTerms[1].equals("bn")) {
                if(priceTerms[2].equals("Dollars")) {
                    price = numericToPrice(priceTerms[0], priceTerms[1],"",  sign, true, false);
                    indexAdd= indexAdd +3;
                }else {
                    price = numericToPrice(priceTerms[0], priceTerms[1],"",  sign, false, false);
                    indexAdd++;
                }
            }else if(priceTerms[2].equals("U.S.") && priceTerms[3].equals("dollars")) {
                //trillion
                price = numericToPrice(priceTerms[0], priceTerms[1],"",  sign, true, true);
                indexAdd = indexAdd + 4;
            }else if(isFraction(priceTerms[1]) && priceTerms[2].equals("Dollars")){
                price = numericToPrice(priceTerms[0],"",priceTerms[1],  sign, true , false);
                indexAdd= indexAdd + 3;
            }else{
                price = numericToPrice(priceTerms[0],"","",  sign, false , false);
                indexAdd++;
            }
        }else{
            price = numericToPrice(priceTerms[0],"","",  sign, false , false);
            indexAdd++;
        }
        insertTermDic(price , document, index);
        return indexAdd;
    }

    private boolean isNumericDouble (String docToken){ //checks if the token is a number

        try {

            String afterReplace = docToken.replace(",","");
            Double.parseDouble(afterReplace);
            return true;
        } catch(NumberFormatException e){

            return false;
        }
    }

    private boolean isFraction (String docToken){ //checks if the token is a number with a fraction
        if (docToken.contains("/"))
            return true;
        return false;
    }

    private boolean lessThanMillion (String numToken){

        String notEsrony = numToken;
        if (numToken.charAt(0) == '.')
            notEsrony = "0"+numToken ;
        if (Double.parseDouble(notEsrony.replace(",", "")) < 1000000)
            return true;
        return false;
    }

    private boolean isThousand (String numToken){
        String num  = numToken.replace("," , "");
        if (Double.parseDouble(num) >= 1000 && (Double.parseDouble(num) < 1000000))
            return true;
        return false;
    }

    private boolean isMillion (String numToken){

        if (Double.parseDouble(numToken) >= 1000000 && (Double.parseDouble(numToken) < 1000000000))
            return true;
        return false;
    }

    private boolean lessThenThousand(String numToken){

        try {
            if (Double.parseDouble(numToken) < 1000)
                return true;
        }catch (NumberFormatException e){
            return false;
        }
        return false;
    }

    private boolean isBillion (String numToken){
        if (Double.parseDouble(numToken) >= 1000000000 )
            return true;
        return false;
    }

    private boolean isMonths(String token){
        for (String month : months)
            if ((token.toLowerCase()).equals(month.toLowerCase()))
                return true;
        return false;
    }

    /**
     *  function that get a number and remove
     *  all number after the 3 char after the "."
     * @param numToken - number
     * @return correct num.
     */
    private String returnDouble(String numToken){

        if(numToken.contains(".")){
            try{

                Double test_d =Double.parseDouble(numToken);
                return df.format(test_d);
            }catch (Exception e){
                e.printStackTrace();
                return numToken;
            }
        }else
            return numToken;
    }

    /**
     * function that shift the char "." number of times.
     * @param numToken - number
     * @param shift - the number of shift for "."
     * @return shift number
     */
    private String shiftLeft(String numToken ,int shift ){

        try {
            return df.format(new BigDecimal(numToken).movePointLeft(shift));
        }
        catch(Exception e){
            System.out.println(numToken);
          //  e.printStackTrace();
        }
        return "";
    }

    private boolean isNumericDate(String numToken){
        if(numToken.length()>4 || numToken.length() <2)
            return false;
        String numTerm = numToken;
        try {
            int num = Integer.parseInt(numTerm) ;
            if(numTerm.length() == 2 && num < 32 && num > 0) {
                return true;
            }else if(numTerm.length() == 4)
                return true;
        } catch(NumberFormatException e){
            return false;
        }
        return false;
    }

    private boolean isNumericDayDate(String numTerm){

        try {
            int num = Integer.parseInt(numTerm) ;
            if(numTerm.length() == 2 && num < 32 && num > 0)
                return true;
            else
                return false;
        } catch(NumberFormatException e){
            return false;
        }
    }

    private boolean isNumericYearDate(String numTerm){

        try {
            int num = Integer.parseInt(numTerm) ;
            if(numTerm.length() == 4 )
                return true;
            else
                return false;
        } catch(NumberFormatException e){
            return false;
        }
    }

    private int isDate (String docToken ,String[] afterIndex ,Document doc , int index) { //checks if the token is a month (date)

        boolean year = false;
        String yearTerm = "";
        int indexAdd = 0;
        if (!afterIndex[0].equals("")) {
            String nextTerm = startEndWord(afterIndex[0]);
            if ((isNumericDayDate(docToken) && isMonths(nextTerm))) {
                if (!afterIndex[1].equals("")) {
                    yearTerm = startEndWord(afterIndex[1]);
                    if (isNumericYearDate(yearTerm))
                        year = true;
                }
                if (year) { //20 june 1984
                    insertTermDic(yearTerm+"-"+turnMonthToNumber(nextTerm) + "-" + docToken, doc,index);
                    return 3;
                } else  ///20 June
                    insertTermDic(turnMonthToNumber(nextTerm) + "-" +docToken, doc,index);
                return 2;
            } else if ((isNumericDate(nextTerm) && isMonths(docToken))) {
                ///June 4 or May 1994
                if(nextTerm.length() == 4)
                    insertTermDic(nextTerm+"-"+turnMonthToNumber(docToken) ,doc,index);
                else
                    insertTermDic(turnMonthToNumber(docToken)+"-"+nextTerm , doc,index);
                return 2;

            }
        } else {
            return -1;
        }

        return -1;
    }

    private boolean isPercent(String term , String afterTerm){
        String secondTerm= "";
        if(!afterTerm.equals("")) {
            secondTerm = startEndWord(afterTerm);
            if ((isNumericDouble(term)) && secondTerm.equals("%"))
                return true;
            else if ((isNumericDouble(term)) && (secondTerm.toLowerCase().equals("percent") || secondTerm.toLowerCase().equals("percentage")))
                return true;

        }
        else
            return false;
        return false;
    }

    private boolean isBetween(String term ,boolean haveSpace, String[] afterIndex){

        if(term.toLowerCase().equals("between") && haveSpace ){
            if(afterIndex[1].equals("and")){
                boolean isNumber_1 = isNumericDouble(afterIndex[0]);
                boolean isNumber_2 = isNumericDouble(afterIndex[2]);
                return isNumber_1 && isNumber_2;
            }
        }
        return false;
    }

    private static String turnMonthToNumber (String docMonth){ //turns the month name to number

        if (docMonth.toLowerCase().equals("jan") || docMonth.toLowerCase().equals("january"))
            return "01";
        else if (docMonth.toLowerCase().equals("feb") || docMonth.toLowerCase().equals("february"))
            return "02";
        else if (docMonth.toLowerCase().equals("mar") || docMonth.toLowerCase().equals("march"))
            return "03";
        else if (docMonth.toLowerCase().equals("apr") || docMonth.toLowerCase().equals("april"))
            return "04";
        else if (docMonth.toLowerCase().equals("may"))
            return "05";
        else if (docMonth.toLowerCase().equals("jun") || docMonth.toLowerCase().equals("june"))
            return "06";
        else if (docMonth.toLowerCase().equals("jul") || docMonth.toLowerCase().equals("july"))
            return "07";
        else if (docMonth.toLowerCase().equals("aug") || docMonth.toLowerCase().equals("august"))
            return "08";
        else if (docMonth.toLowerCase().equals("sep") || docMonth.toLowerCase().equals("september"))
            return "09";
        else if (docMonth.toLowerCase().equals("oct") || docMonth.toLowerCase().equals("october"))
            return "10";
        else if (docMonth.toLowerCase().equals("nov") || docMonth.toLowerCase().equals("november"))
            return "11";
        else if (docMonth.toLowerCase().equals("dec") || docMonth.toLowerCase().equals("december"))
            return "12";
        return "-1";
    }

    private boolean stopWord(String word){
        if(stopWords != null && stopWords.size()>0){
            if(stopWords.contains(word) || stopWords.contains(word.toLowerCase()) || stopWords.contains(word.toUpperCase()))
                return true;
        }
        return false;
    }

    public String startEndWord(String word){

        if(word.equals("U.S."))
            return word;


        int size = word.length();
        for(int startindex =0 ; startindex  < size ; startindex ++){
            if(word.charAt(0 ) == '.' ||  word.charAt(0) == ',' || word.charAt(0 ) == '-' ||word.charAt(0 ) == '\'' ) {
                word = word.substring(1);
            }
            else
                break;
        }


        for(int endIndex = word.length()-1 ; endIndex  >= 1 ; endIndex --){
            if(word.charAt(word.length()-1 ) == '.' ||  word.charAt(word.length()-1) == ',' || word.charAt(word.length()-1 ) == '-'|| word.charAt(word.length()-1 ) == '\'' ) {

                word = word.substring(0,word.length()-1);
            }
            else
                break;
        }

        if(word.length() == 1)
            return "";
        return word;
    }

    private String handleWords(String word ,Document newDoc,String currTokenStartEnd ,int index , Stemmer stem){
        word = currTokenStartEnd;
        word.replace("%" ,"");
        if(iSstemmer) {
            word = stem.getStermTerm(currTokenStartEnd);
        }



        if(word != null && word.length() > 1) {
            /// check if word is in lower letters
            if(newDoc.termDic.containsKey(word)){
                insertTermDic(word, newDoc , index);
            }
            else if (word.equals(word.toLowerCase())) {
                /// word is save in the Dic

                if(newDoc.termDic.containsKey(word.toUpperCase())){
                    //check if the word is save as upper case
                    changeUpperCaseToLowerCase(word, newDoc);

                }
                else{
                    //first occur
                    insertFirstOccur(word, newDoc  , index);
                }
            }
            ///check if the word is all in upper letter.
            else if (word.equals(word.toUpperCase())){

                if(newDoc.termDic.containsKey(word.toLowerCase())){
                    insertTermDic(word.toLowerCase() , newDoc  , index);
                }
                else{
                    //first occur
                    insertFirstOccur(word , newDoc  , index);
                }
            }
            /// check if the first char in the word is upper letter
            else if (word.charAt(0) == word.toUpperCase().charAt(0)){
                if(newDoc.termDic.containsKey(word.toUpperCase())){
                    insertTermDic(word.toUpperCase(), newDoc  , index);
                }
                else if(newDoc.termDic.containsKey(word.toLowerCase())){
                    insertFirstOccur(word.toLowerCase() , newDoc  , index);
                }else{
                    insertFirstOccur(word.toUpperCase() , newDoc  , index);
                    return word;
                }
            }
            return "";
        }
        return "";

    }


    private int handlePercent(String num , Document doc,int index) {

        insertTermDic(num + "%" , doc, index);
        return 2;

    }

    private int handleNum(String intNum , String afteIndex  , Document doc ,int index) {

        if(intNum.charAt(intNum.length()-1) == '.')
            intNum = intNum.substring(0,intNum.length()-1);
        String sum = "";
        String zero = "";
        String num = "";
        intNum = intNum.replace(",", "");
        boolean twoTerm = !afteIndex.equals("");
        if (twoTerm) {
            if (afteIndex.toLowerCase().equals("thousand")) {
                sum = "K";
                zero = "000";
            } else if (afteIndex.toLowerCase().equals("million")) {
                sum = "M";
                zero = "000000";
            } else if (afteIndex.toLowerCase().equals("billion")) {
                sum = "B";
                zero = "000000000";
            }
        }
        if (isFraction(intNum) && !sum.equals("")) {
            int fraction = intNum.charAt('.');
            intNum = intNum.substring(0, fraction) + zero + intNum.substring(fraction);
        }
        if (sum.equals("") && lessThenThousand(intNum)) {
            if (twoTerm && isFraction(afteIndex)) {
                num = intNum + " " + afteIndex;

            } else {
                num = intNum;
            }
        }
        else if (isThousand(intNum) || sum.equals("K")) {//only 100,123 (K)
            num = shiftLeft(returnDouble(intNum), 3)+"K";

        } else if (isMillion(intNum) || sum.equals("M")) { // only 100,123,333 (M)
            num = shiftLeft(returnDouble(intNum), 6) + "M";

        } else if (isBillion(intNum) || sum.equals("B")){ // only 100,123,333,000 (B)
            num = shiftLeft(returnDouble(intNum), 9) + "B";
        }

        if(!num.equals(""))
            insertTermDic(num , doc , index);

        if(sum.equals(""))
            return 1;
        else
            return 2;

    }

    private boolean isLine(String currTokenStartEnd) {

        if(currTokenStartEnd.contains("-"))
            return true;
        return false;
    }

    private int handleLine(String line , String afterIndex , Document doc, int index){

        String[] lines = line.split("-");
        if(lines == null || lines.length<2)
            return 0;
        else if(lines.length == 2 && !afterIndex.equals("")){
            try{ /// 23-27 Feb
                lines[0]= startEndWord(lines[0]);
                lines[1]= startEndWord(lines[1]);
                int firstNumDate = Integer.parseInt(lines[0]);
                int SecondNumDate =Integer.parseInt(lines[1]);
                if(firstNumDate >0 && firstNumDate <32 && SecondNumDate > 0 && SecondNumDate <32 && isMonths(afterIndex)) {
                    if(lines[0].length() == 1)

                        insertTermDic(turnMonthToNumber(afterIndex) + "-0"+lines[0],doc , index);
                    else
                        insertTermDic(turnMonthToNumber(afterIndex) + "-" + lines[0],doc, index);
                    if(lines[1].length() == 1)
                        insertTermDic(turnMonthToNumber(afterIndex) + "-0"+lines[1],doc, index);
                    else
                        insertTermDic(turnMonthToNumber(afterIndex) + "-" + lines[1],doc , index);

                    return 2;
                }else{
                    String addWords = "";
                    int counter = 0;
                    for(String term: lines){
                        term = startEndWord(term);
                        if(!term.equals(""))
                            if(counter == 0) {
                                addWords = term;
                                counter++;
                            }else
                                addWords = addWords+"-"+term;
                    }
                    insertTermDic(addWords,doc , index);
                    return 1;
                }
            }
            catch (Exception e){
                String addWords = "";
                int counter = 0;
                for(String term: lines){
                    term = startEndWord(term);
                    if(!term.equals(""))
                        if(counter == 0) {
                            addWords = term;
                            counter++;
                        }else
                            addWords = addWords+"-"+term;
                }
                insertTermDic(addWords,doc,index);
                return 1;
            }
        }else{
            String addWords = "";
            int counter = 0;
            for(String term: lines){
                term = startEndWord(term);
                if(!term.equals(""))
                    if(counter == 0) {
                        addWords = term;
                        counter++;
                    }else
                        addWords = addWords+"-"+term;
            }
            insertTermDic(addWords,doc, index);
            return 1;
        }
    }

    private void setStopWord(String path){

        File rootDirectory= null;

        if(path == null || path.length() == 0)
            rootDirectory = new File("Resources\\stop_words.txt");
        else
            rootDirectory = new File(path+"\\stop_words.txt");
        if(rootDirectory != null) {
            try {
                BufferedReader myBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(rootDirectory)));
                for (String currLine; (currLine = myBufferedReader.readLine()) != null; )
                    stopWords.add(currLine.trim());
                myBufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error in folder path");
            alert.show();
        }
    }

    public void parseQuery(String query) {

        if (query == null) {
            ////// DOC OF QUERIES
        }
        else{ //STRINGGGG , input query.

        }
    }

    private void setTitle(Document newDoc ,Stemmer stemm) {


        if (newDoc.title != null &&!newDoc.title.equals("")) {
            String title = newDoc.title.replaceAll("[^ -~]|\\${2,}|\\.{2,}|\\,|\\`", "");
            String[] allTitle = title.split("(?!,[0-9])[\",\\/?>~<&=@!\\[\\]:;}{;|*#'+)_(\\s]+");
            String afterParse = "";
            for (String currToken : allTitle) {
                String currTokenStartEnd = startEndWord(currToken);
                currToken = currToken.toLowerCase();
                if (currToken.equals("") || currToken.length() == 1 || currToken.equals("\n") || stopWord(currTokenStartEnd)) {
                    if (!currToken.equals("May")) {
                        continue;
                    }
                }
                if (iSstemmer)
                    currToken = stemm.getStermTerm(currTokenStartEnd);
                currToken += " ";
                afterParse += currToken;

            }

            afterParse = afterParse.substring(0, afterParse.length() - 1); // delete space
            newDoc.setTitle(afterParse);

        }
    }

}