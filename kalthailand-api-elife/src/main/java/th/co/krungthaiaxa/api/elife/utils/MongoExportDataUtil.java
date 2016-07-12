package th.co.krungthaiaxa.api.elife.utils;

import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

public class MongoExportDataUtil {
	
	public static void main(String[] args) throws ParseException {
		
		String dbIp = "10.22.248.52";
		int dbPort = 27117;
		String dbName = "elife";
		String uName = "elifeuser";
		String uPass = "28$Jp7$tsld7nZ";
		

		try {

			/**** Connect to MongoDB ****/
			// Since 2.10.0, uses MongoClient
			MongoClient mongo = new MongoClient(dbIp, dbPort);

			/**** Get database ****/
			// if database doesn't exists, MongoDB will create it for you
			DB db = mongo.getDB(dbName);
			
			/**** Get authentication ****/
			boolean auth = db.authenticate(uName, uPass.toCharArray());
			
			
			if (auth) {		
				
				/**** Get collection / table from 'testdb' ****/
				// if collection doesn't exists, MongoDB will create it for you
				DBCollection quote = db.getCollection("quote");
				
				/**** Find and display ****/
				BasicDBObject searchQuery = new BasicDBObject();
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
				searchQuery.put("creationDateTime", BasicDBObjectBuilder.start("$gte", format.parse("2016-07-04T00:00:00.000Z")).add("$lte", format.parse("2016-07-10T23:59:59.999Z")).get());

				DBCursor cursor = quote.find(searchQuery);
				
				System.out.println("count : "+cursor.count());

				while (cursor.hasNext()) {
					
					DBObject resultElement = cursor.next();
				    Map<String, Object> resultElementMap = resultElement.toMap();
				    String quoteId = (String) resultElementMap.get("quoteId");
				    
				    //find sessionquote
				    DBCollection sessionQuote = db.getCollection("sessionQuote");
				    BasicDBObject query = new BasicDBObject();
				    query.put("creationDateTime", BasicDBObjectBuilder.start("$gte", format.parse("2016-07-04T00:00:00.000Z")).add("$lte", format.parse("2016-07-10T23:59:59.999Z")).get());
				    
					System.out.println("________________________________-");
				}
			
				System.out.println("<----- Login is successful! ----->");
			} else {
				System.out.println("!----- Login is failed! -----!");
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} 

	}

}
