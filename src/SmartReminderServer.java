import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class SmartReminderServer {
    private static final int PORT = 12345;
    private static ConcurrentHashMap<String, List<Reminder>> clientReminders = new ConcurrentHashMap<>();
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) {
        System.out.println("Smart Reminder Server started...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            scheduler.scheduleAtFixedRate(SmartReminderServer::checkReminders, 0, 10, TimeUnit.SECONDS);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                 ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

                String clientId = (String) in.readObject();
                Reminder reminder = (Reminder) in.readObject();

                clientReminders.computeIfAbsent(clientId, k -> new ArrayList<>()).add(reminder);

                out.writeObject("Reminder added successfully!");

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    private static void checkReminders() {
        long currentTime = System.currentTimeMillis();

        for (Map.Entry<String, List<Reminder>> entry : clientReminders.entrySet()) {
            String clientId = entry.getKey();
            List<Reminder> reminders = entry.getValue();

            reminders.removeIf(reminder -> {
                if (currentTime >= reminder.getReminderTime()) {
                    System.out.println("Triggering reminder for client " + clientId + ": " + reminder.getMessage());
                    return true; 
                }
                return false;
            });
        }
    }
}

class Reminder implements Serializable {
    private String message;
    private long reminderTime;

    public Reminder(String message, long reminderTime) {
        this.message = message;
        this.reminderTime = reminderTime;
    }

    public String getMessage() {
        return message;
    }

    public long getReminderTime() {
        return reminderTime;
    }
}
