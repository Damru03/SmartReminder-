import java.io.*;
import java.net.*;
import java.util.Scanner;

public class SmartReminderClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            Scanner scanner = new Scanner(System.in);

            // Get client ID (mock for user identification)
            System.out.print("Enter your client ID: ");
            String clientId = scanner.nextLine();

            // Get reminder message and time
            System.out.print("Enter your reminder message: ");
            String message = scanner.nextLine();

            System.out.print("Enter time delay (in seconds) for the reminder: ");
            long delayInSeconds = scanner.nextLong();

            // Calculate the future trigger time
            long reminderTime = System.currentTimeMillis() + (delayInSeconds * 1000);

            // Send the reminder to the server
            Reminder reminder = new Reminder(message, reminderTime);

            out.writeObject(clientId);
            out.writeObject(reminder);

            // Get confirmation from the server
            String response = (String) in.readObject();
            System.out.println(response);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
