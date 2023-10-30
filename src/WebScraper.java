import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.simple.JSONObject;

public class WebScraper {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws InterruptedException {

		String searchQuery = "fantasy football";
		String baseUrl = "https://nitter.net/search?f=tweets&q=";
		WebClient client = new WebClient();
		client.getOptions().setCssEnabled(false);
		client.getOptions().setJavaScriptEnabled(false);

		int count = 0;
		while (count<10) {
			try {
				String searchUrl = baseUrl + URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
				HtmlPage page = client.getPage(searchUrl);

				List<HtmlElement> items = page.getByXPath("//div[@class='timeline-item ']");
				if (items.isEmpty()) {
					System.out.println("No items found !");
				} else {
					for (HtmlElement htmlItem : items) {

						HtmlElement spanBody = htmlItem.getFirstByXPath(".//div[@class='tweet-body']");

						Item item = new Item();

						item.setBody(spanBody.asNormalizedText());

						ObjectMapper mapper = new ObjectMapper();
						String jsonString = mapper.writeValueAsString(item);
						// Filter tweets that aren't retweets
						Pattern pattern = Pattern.compile("(.*)retweet(.*)");
						Matcher matcher = pattern.matcher(jsonString);
						if (!(matcher.find())) {
							insertTweet(jsonString);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("duplicate tweet");
			}
			count ++;
			if (count==10){
				System.out.println("Data storage is complete.");
				// Creating a JSONObject object
				JSONObject jsonObject = new JSONObject();

				FindIterable<Document> iterDoc = collection.find();
				Iterator it = iterDoc.iterator();

				// Put (tweet, string) pair into jsonObject
				while (it.hasNext()) {
					jsonObject.put("Tweet",it.next());

					// Append tweet to existing file name
					try {
						FileWriter file = new FileWriter("fantasy_tweets.json", true);
						file.write(jsonObject.toJSONString());
						file.close();
					} catch (IOException e) {               
						e.printStackTrace();
					}
				}
				System.out.println("JSON file created: " + jsonObject);
				System.out.println("END");
				System.exit(0);
			}
			Thread.sleep(10000);
		}

	}

	static MongoUtil mongoUtil = new MongoUtil();
	static MongoDatabase database = mongoUtil.getDB();
	static MongoCollection<Document> collection = database.getCollection("body");

	public static void insertTweet(String obj) throws InterruptedException {
		Document document = new Document("body", obj);
		collection.insertOne(document);
	}
}