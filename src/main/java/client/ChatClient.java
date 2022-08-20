package client;

import files.ActiveUsersFiles;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;

import static utils.ConsoleDetail.*;

public class ChatClient {
    private Socket socket;
    private ClientModel client;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    private boolean isServerOn;

    public ChatClient(Socket socket, ClientModel client) {
        try {
            this.socket = socket;
            this.client = client;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.isServerOn = true;
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessage() {
        final String colon = CYAN_BOLD_BRIGHT + ": " + RESET;

        try {
            writeWithBuffered(client.getColoredUsername());

            Scanner scanner = new Scanner(System.in);
            while (isServerOn && socket.isConnected()) {
                if (!isServerOn)
                    break;

                System.out.print(client.getColoredUsername() + colon);

                if (isServerOn && scanner.hasNext()) {
                    System.out.println();
                    if (!isServerOn)
                        break;

                    String messageToSend = scanner.nextLine();

                    if (messageToSend != null) {
                        if (messageToSend.equals("/exit")) {
                            clientLeaving();
                            break;
                        }

                        if (messageToSend.equals("")) {
                            continue;
                        }

                        System.out.printf("\033[%dA", 1); // Move up
                        System.out.print("\033[2K");

                        String messageTime = WHITE_BOLD_BRIGHT + getCurrentTime() + RESET;
                        String indicator = BLUE_BOLD_BRIGHT + " -> " + RESET;

                        messageToSend =
                                messageTime + indicator + client.getColoredUsername() + colon + WHITE_BOLD_BRIGHT + messageToSend + RESET;

                        System.out.println(messageToSend);
                        writeWithBuffered(messageToSend);
                    }
                }
            }
        } catch (IOException e) {
            if (isServerOn)
                closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void listenForMessage() {
        final String colon = CYAN_BOLD_BRIGHT + ": " + RESET;

        new Thread(() -> {
            String messageFromChat;

            while (isServerOn && socket.isConnected()) {
                try {
                    if (isServerOn && bufferedReader.ready()) {
                        messageFromChat = bufferedReader.readLine();

                        if (messageFromChat != null && messageFromChat.length() != 0) {
                            for (int i = 0; i < client.getUsername().length() + 2; i++)
                                System.out.print("\b");

                            if (messageFromChat.equals(RED_BOLD_BRIGHT + "SERVER WAS SHUTDOWN BY THE ADMINISTRATOR." + RESET)) {
                                isServerOn = false;
                                System.out.println(messageFromChat);

                                ChatClientCLI.removeActiveUsers(client.getUsername());
                                closeEverything(socket, bufferedReader, bufferedWriter);
                                break;
                            }

                            System.out.println(messageFromChat);
                            System.out.print(client.getColoredUsername() + colon);
                        }
                    }
                } catch (IOException e) {
                    if (isServerOn)
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }
            }
        }).start();
    }

    public void clientLeaving() {
        try {
            String leftChatMessage = RED_BOLD_BRIGHT + "SERVER: " + RESET +
                    client.getColoredUsername() + RED_BOLD_BRIGHT + " has left the chatroom." + RESET;

            writeWithBuffered(leftChatMessage);

            System.out.println(RED_BOLD_BRIGHT + "You have left the chatroom. Goodbye." + RESET);

            closeEverything(socket, bufferedReader, bufferedWriter);

            System.out.println("""
                    \033[1;97m
                    1. Return to main menu
                    2. Exit
                    \033[0m""");

            label:
            while (true) {
                System.out.print(CYAN_BOLD_BRIGHT + ">" + RESET);

                String choice = new Scanner(System.in).nextLine();

                switch (choice) {
                    case "1":
                        ChatClientCLI.startMenu();
                        break label;
                    case "2":
                        System.out.print(RED_BOLD_BRIGHT + "\nGoodbye." + RESET);
                        break label;
                    case "":
                        continue;
                    default:
                        System.out.println(RED_BOLD_BRIGHT + "Please choose correctly." + RESET);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeWithBuffered(String text) throws IOException {
        bufferedWriter.write(text);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        ChatClientCLI.removeActiveUsers(client.getUsername());

        try {
            if (socket != null)
                socket.close();

            if (bufferedWriter != null)
                bufferedWriter.close();

            if (bufferedReader != null)
                bufferedReader.close();

            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCurrentTime() {
        return dateFormat.format(new Date());
    }

    public Socket getSocket() {
        return socket;
    }

    public BufferedReader getBufferedReader() {
        return bufferedReader;
    }

    public BufferedWriter getBufferedWriter() {
        return bufferedWriter;
    }
}