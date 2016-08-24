package th.co.krungthaiaxa.api.elife.utils;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MongoExportDataUtil {
    /*
public static void main(String[] args) throws ParseException {
		
		String dbIp = "10.22.248.52";
		int dbPort = 27117;
		String dbName = "elife";
		String uName = "elifeuser";
		String uPass = "28$Jp7$tsld7nZ";

		try {
			
			MongoClient mongo = new MongoClient(dbIp, dbPort);
			DB db = mongo.getDB(dbName);
			boolean auth = db.authenticate(uName, uPass.toCharArray());		
			
			if (auth) {		
				
				DBCollection policyNumber = db.getCollection("policyNumber");
				BasicDBObject policyNumberQuery = new BasicDBObject();				
				DBCursor policyNumberCursor = policyNumber.find(policyNumberQuery);
				
				System.out.println("policyNumber count : "+policyNumberCursor.count());

				int availablePolicyNumber = 0;
				while (policyNumberCursor.hasNext()) {
					
					DBObject policyNumberResultElement = policyNumberCursor.next();
				    Map<String, Object> policyNumberResultElementMap = policyNumberResultElement.toMap();
				    
				    if(policyNumberResultElementMap.toString().indexOf("_class")==-1){
				    	availablePolicyNumber++;
				    }
				    
				    
				}		
				
				System.out.println("availablePolicyNumber is : "+availablePolicyNumber);
				
				System.out.println("<----- Login is successful! ----->");
			} else {
				System.out.println("!----- Login is failed! -----!");
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} 

	}
	*/

    /**
     * This is just a temporary code!
     *
     * @param args
     * @throws ParseException
     */
    public static void test(String[] args) throws ParseException {

        String dbIp = "localhost";
        int dbPort = 27117;
        String dbName = "elife";
        String uName = "elifeuser";
        String uPass = "password";

        try {

            MongoClient mongo = new MongoClient(dbIp, dbPort);
            DB db = mongo.getDB(dbName);
            boolean auth = db.authenticate(uName, uPass.toCharArray());

            if (auth) {

                DBCollection quote = db.getCollection("quote");
                BasicDBObject quoteQuery = new BasicDBObject();
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
                quoteQuery.put("creationDateTime", BasicDBObjectBuilder.start("$gte", format.parse("2016-08-01T00:00:00.000Z")).add("$lte", format.parse("2016-08-17T23:59:59.999Z")).get());
                quoteQuery.put("commonData.productId", "10EC");
                DBCursor quoteCursor = quote.find(quoteQuery);

                System.out.println("quote count : " + quoteCursor.count());

                List<String> userList = new ArrayList<>();

                while (quoteCursor.hasNext()) {

                    DBObject quoteResultElement = quoteCursor.next();
                    Map<String, Object> quoteResultElementMap = quoteResultElement.toMap();
                    String quoteOId = quoteResultElementMap.get("_id").toString();
                    DBCollection sessionQuote = db.getCollection("sessionQuote");
                    BasicDBObject sessionQuoteQuery = new BasicDBObject();
                    DBCursor sessionQuoteCursor = sessionQuote.find(sessionQuoteQuery);

                    while (sessionQuoteCursor.hasNext()) {

                        DBObject sessionQuoteResultElement = sessionQuoteCursor.next();
                        Map<String, Object> sessionQuoteResultElementMap = sessionQuoteResultElement.toMap();
                        String sessionId = sessionQuoteResultElementMap.get("sessionId").toString();
                        String compareText = sessionQuoteResultElementMap.toString();
                        if (compareText.indexOf(quoteOId) != -1) {
                            if (userList.size() == 0) {
                                userList.add(sessionId);
                            } else {
                                boolean addValue = true;
                                for (int a = 0; a < userList.size(); a++) {
                                    if (userList.get(a).equals(sessionId)) {
                                        addValue = false;
                                        a = userList.size();
                                    }
                                }
                                if (addValue == true) {
                                    userList.add(sessionId);
                                }
                            }
                        }

                    }

                }

                System.out.println("user count : " + userList.size());
                System.out.println("<----- Login is successful! ----->");
            } else {
                System.out.println("!----- Login is failed! -----!");
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

}
