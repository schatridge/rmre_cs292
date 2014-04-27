import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;


// Output: known%, unknown%, list of unknown words
public class PassageUserComparator {
	public final int mTextID;
	public final String mUserName;
	
	private double knownPercentage;
	private double unknownPercentage;
	private Map<String, Integer> unknownWords;
	

	// CONSTRUCTOR
	public PassageUserComparator(int textID, String username) {
		mTextID = textID;
		mUserName = username;
		
		knownPercentage = 0;
		unknownPercentage = 0;
		unknownWords = new HashMap<String, Integer>();
	}
	
	// GETTERS
	public double getKnownPercentage() throws UnknownHostException {
		if (knownPercentage == 0) {
			compareTextAndUser();
		}
		return knownPercentage;
	}
	
	public double getUnknownPercentage() throws UnknownHostException {
		if (unknownPercentage == 0) {
			compareTextAndUser();
		}
		return unknownPercentage;
	}
	
	public Map<String, Integer> getUnknownWords() throws UnknownHostException {
		if (unknownWords.isEmpty()) {
			compareTextAndUser();
		}
		return unknownWords;
	}


	// Fetch Text.words_stemmed and output a map<String, Integer> of (stemmed word, count)
	public Map<String, Integer> fetchTextWords() throws UnknownHostException {
		Map<String, Integer> textWords = new HashMap<String, Integer>();
		String unparsedStemmedWords = "";
		
		MongoClient mongoClient = new MongoClient();
		DB db = mongoClient.getDB("Reading Recommender DB");   // !!! --- CHANGE: Database name --- !!!
		DBCollection table = db.getCollection("Text");
		 
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("_id", mTextID);
		BasicDBObject fields = new BasicDBObject();
		fields.put("words_stemmed", 1);
		
		DBCursor cursor = table.find(searchQuery, fields);
		while (cursor.hasNext()) {
			unparsedStemmedWords = cursor.next().get("words_stemmed").toString();
			break;
		}
		
		unparsedStemmedWords = (unparsedStemmedWords.split("["))[1];
		unparsedStemmedWords = (unparsedStemmedWords.split("]"))[0].trim();
		unparsedStemmedWords = unparsedStemmedWords.substring(1, unparsedStemmedWords.length()-2);
		String[] allWordObjects = unparsedStemmedWords.split("}, {");
		for (String obj : allWordObjects) {
			String[] pairs = obj.split(", ");
			String[] stemPair = pairs[0].split(": ");
			String[] countPair = pairs[1].split(": ");
			String stem = stemPair[1].substring(1, stemPair[1].length()-2);
			Integer count = new Integer(countPair[1]);
			
			textWords.put(stem, count);
		}
		
		return textWords;
	}


	// Fetch User.known_words_stemmed and output a map<String, Double> of (stemmed known word, strength)
	public Map<String, Double> fetchUserKnownWords() throws UnknownHostException {
		Map<String, Double> knownWords = new HashMap<String, Double>();
		String unparsedKnownWords = "";
		
		MongoClient mongoClient = new MongoClient();
		DB db = mongoClient.getDB("Reading Recommender DB");   // !!! --- CHANGE: Database name --- !!!
		DBCollection table = db.getCollection("User");
		 
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("_id", mUserName);
		BasicDBObject fields = new BasicDBObject();
		fields.put("known_words_stemmed", 1);
		
		DBCursor cursor = table.find(searchQuery, fields);
		while (cursor.hasNext()) {
			unparsedKnownWords = cursor.next().get("known_words_stemmed").toString();
			break;
		}
		
		unparsedKnownWords = (unparsedKnownWords.split("["))[1];
		unparsedKnownWords = (unparsedKnownWords.split("]"))[0].trim();
		unparsedKnownWords = unparsedKnownWords.substring(1, unparsedKnownWords.length()-2);
		String[] allWordObjects = unparsedKnownWords.split("}, {");
		for (String obj : allWordObjects) {
			String[] pairs = obj.split(", ");
			String[] stemPair = pairs[0].split(": ");
			String[] countPair = pairs[1].split(": ");
			String stem = stemPair[1].substring(1, stemPair[1].length()-2);
			Double count = new Double(countPair[1]);
			
			knownWords.put(stem, count);
		}
		
		return knownWords;
	}
	
	// Compare Text.words_stemmed with User.known_words_stemmed
	public void compareTextAndUser() throws UnknownHostException {
		int knownCount = 0;
		int unknownCount = 0;
		int totalCount = 0;
		
		Map<String, Integer> textWords = fetchTextWords();
		Map<String, Double> knownWords = fetchUserKnownWords();
		for (Map.Entry<String, Integer> textWord : textWords.entrySet()) {
			String word = textWord.getKey();
			Integer count = textWord.getValue();
			if (knownWords.get(word) == null) {   // this word is unknown
				totalCount += count;
				unknownCount += count;
				unknownWords.put(word, count);
			}
			else {   // this word is known
				Double str = knownWords.get(word);
				totalCount += count;
				knownCount += count * str;
				unknownCount += count * (1 - str);
			}
		}
		
		knownPercentage = (double)knownCount/totalCount;
		unknownPercentage = (double)unknownCount/totalCount;
	}
}
