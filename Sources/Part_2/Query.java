package Part_2;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * This class represents a query that needs to be parsed
 */
public class Query {

    private HashMap<String, int[]> queryTermDic;
    private HashMap<String, String[]> queryDocsDic;
    private String text;

    public Query(String queryText) {
        text = queryText;
        queryDocsDic = new LinkedHashMap<>();
    }


    public String getQueryText() {
        return text;
    }


    public void insertIntoQueryTermDic(String[] toInsert) {

        for (int i = 0; i < toInsert.length; i++) {
            queryDocsDic.put(toInsert[i], null);
        }
    }




    public void setQueryTermDic(HashMap<String, int[]> queryTermDic) {
        this.queryTermDic = queryTermDic;
    }


    public HashMap<String, int[]> getQueryTermDic() {
        return queryTermDic;
    }

}
