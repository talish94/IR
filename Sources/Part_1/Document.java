package Part_1;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Document {

    private String id;
    private String  docText;
    private int tfMax;
    public HashMap<String, int[]> termDic ;
    public HashMap<String,Integer> entitys;
    public String title;

    public int size;

    public Document(){

        termDic = new HashMap<>();
        entitys = new HashMap<>();

    }

//    public String entitysToString(){
//
//        String ans = "";
//        for(Map.Entry<String,Integer> entry: entitys.entrySet()){
//
//            String key = entry.getKey();
//
//        }
//    }

    public int uniqueTerm(){
        return termDic.size();
    }

    public int getTfMax() {
        return calMaxTf();
    }

    public String getText() { return docText; }


    public String getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.docText = text;
    }

    public void setSize(int size){
        this.size = size;
    }

    private int calMaxTf(){

        int Max =0 ;
        for(Map.Entry<String, int[]> entry : termDic.entrySet()){

            if(entry.getValue()[0] > Max)
                Max = entry.getValue()[0];
        }
        tfMax = Max;
        return Max;

    }

    public int getLength(){
        int length =0 ;
        for(Map.Entry<String, int[]> entry : termDic.entrySet()){
            length = length + entry.getValue()[0];
        }
        return length;
    }

    public void clear(){
        this.docText = null;
    }

    @Override
    public String toString(){
        return id;
    }

    public String getTermDicAsString() {

        String ans = "";
        //String[] stringArrayReturn = new String[termDic.size()];
        //Set<String> toReturn = termDic.keySet();

        for (String key : termDic.keySet())
            ans = ans + " " + key;

        // stringArrayReturn = (String[])toReturn.toArray();

 /*       for(int i=0; i<termDic.size(); i++)
            stringArrayReturn[i] = toReturn.*/

        return ans;
    }

    public String[] getArrayOfString(){


        String[] list = new String[termDic.size()];
        int index =0;
        for (String key : termDic.keySet()){
            list[index] = key;
            index++;
        }

        return list;

    }



}
