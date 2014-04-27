import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;


public class TextRecommender {

	/**
	 * @param args
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) throws UnknownHostException {
		String username = "";   // !!! --- CHANGE: User name --- !!!
		
		// GET USER DATA
		Double meanWordLevel = 0.0;
		
		MongoClient mongoClient = new MongoClient();
		DB db = mongoClient.getDB("Reading Recommender DB");   // !!! --- CHANGE: Database name --- !!!
		DBCollection userTable = db.getCollection("User");
		 
		BasicDBObject userQuery = new BasicDBObject();
		userQuery.put("_id", username);
		BasicDBObject userFields = new BasicDBObject();
		userFields.put("mean_word_level", 1);
		
		DBCursor userCursor = userTable.find(userQuery, userFields);
		while (userCursor.hasNext()) {
			meanWordLevel = new Double(userCursor.next().get("mean_word_level").toString());
			break;
		}
		
		
		// GET TEXT IDS
		List<Integer> textIDs = new ArrayList<Integer>();
		
		Double minLevel = meanWordLevel * 0.9;
		Double maxLevel = meanWordLevel * 1.1;
		
		DBCollection textTable = db.getCollection("Text");
		BasicDBObject textQuery = new BasicDBObject();
		textQuery.put("mean_word_level", new BasicDBObject("$gt", minLevel).append("$lt", maxLevel));
		BasicDBObject textFields = new BasicDBObject();
		textFields.put("_id", 1);
		
		DBCursor textCursor = textTable.find(textQuery, textFields);
		while (textCursor.hasNext()) {
			textIDs.add(new Integer(textCursor.next().get("_id").toString()));
		}
		
		
		// COMPARE USER AND TEXTS
		Map<Integer, Double> unknownPercentages = new HashMap<Integer, Double>();
		Multimap<Double, Integer> unknownReverse = ArrayListMultimap.create();
		Map<Integer, Double> knownPercentages = new HashMap<Integer, Double>();
		Map<Integer, Map<String, Integer>> unknownWords = new HashMap<Integer, Map<String, Integer>>();
		
		for (int i = 0; i < textIDs.size(); ++i) {
			Integer textID = textIDs.get(i);
			PassageUserComparator comparator = new PassageUserComparator(textID, username);
			
			double known = comparator.getKnownPercentage();
			double unknown = comparator.getUnknownPercentage();
			Map<String, Integer> words = comparator.getUnknownWords();
			
			unknownPercentages.put(textID, unknown);
			unknownReverse.put(unknown, textID);
			knownPercentages.put(textID, known);
			unknownWords.put(textID, words);
		}
		
		
		// FIND EASIEST TEXTS
		List<Double> sortedPercentages = new ArrayList<Double>();
		sortedPercentages.addAll(unknownReverse.keySet());
		Collections.sort(sortedPercentages);
		
		int count = 0;
		for (int i = 0; count <= 20 && i < sortedPercentages.size(); ++ i) {
			Collection<Integer> IDs = unknownReverse.get(sortedPercentages.get(i));
			for (Integer id : IDs) {
				System.out.print("TextID: " + id);
				System.out.print("\tUnknown: " + unknownPercentages.get(id) * 100 + "%");
				System.out.print("\tKnown: " + knownPercentages.get(id) * 100 + "%");
				System.out.println();
			}
			
			++ count;
		}
	}

}
