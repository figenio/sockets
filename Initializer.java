import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Initializer {

    public static void main(String args[]) {
        // Profile setup by user
        Scanner scanner = new Scanner(System.in); // Create input reader
        System.out.println("Enter profile to use (1-4):"); // Read input
        int option = scanner.nextInt();  // Assign profile

        try {
            // Read JSON with profile options
            JSONParser parser = new JSONParser();
            JSONArray profiles = (JSONArray) parser.parse(new FileReader("profiles.json"));
            System.out.println("Chosen profile" + ((JSONObject) profiles.get(option - 1)));

            // Creates an initiates new process
            Process p = new Process(
                    Integer.parseInt(((JSONObject) profiles.get(option - 1)).get("id").toString()),
                    ((JSONObject) profiles.get(option - 1)).get("unicastAddress").toString(),
                    ((JSONObject) profiles.get(option - 1)).get("multicastAddress").toString(),
                    Integer.parseInt(((JSONObject) profiles.get(option - 1)).get("unicastSocket").toString()),
                    Integer.parseInt(((JSONObject) profiles.get(option - 1)).get("multicastSocket").toString())
            );

        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}