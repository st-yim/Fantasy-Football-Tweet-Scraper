import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.*;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoUtil {

	private static MongoClient mClient;

	private MongoClient getMongoClient() {
		if (MongoUtil.mClient == null) {
			String connectionString = "mongodb+srv://styim:mvRdmUud28ISr6PZ@cluster0.th5c3zi.mongodb.net/?retryWrites=true&w=majority";
			ServerApi serverApi = ServerApi.builder()
					.version(ServerApiVersion.V1)
					.build();

			MongoClientSettings settings = MongoClientSettings.builder()
					.applyConnectionString(new ConnectionString(connectionString))
					.serverApi(serverApi)
					.build();

			// Create a new client and connect to the server
			MongoClient mongoClient = MongoClients.create(settings);
			MongoUtil.mClient = mongoClient;

			try {
				// Send a ping to confirm a successful connection
				MongoDatabase database = mongoClient.getDatabase("admin");
				database.runCommand(new Document("ping", 1));
				System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
			} catch (MongoException e) {
				e.printStackTrace();
			}
		}
		return mClient;

	}
	// Utility method to get database instance
	public MongoDatabase getDB() {
		return getMongoClient().getDatabase("nitterscraper");
	}
	public MongoCollection<Document> getUserCollection() {
		return getDB().getCollection("body");
	}
}