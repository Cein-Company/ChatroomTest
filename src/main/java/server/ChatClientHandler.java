package server;

import client.ClientModel;
import files.ChatMessagesFiles;
import server.commandclient.CommandHandlerClient;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import static client.ChatClientCLI.getUsersFromFile;
import static utils.ConsoleDetail.RED_BOLD_BRIGHT;
import static utils.ConsoleDetail.RESET;

public class ChatClientHandler implements Runnable {
    private static final ArrayList<ChatClientHandler> clients = new ArrayList<>();

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private ClientModel clientModel;

    public ChatClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String clientColoredUsername = bufferedReader.readLine();
            for( ClientModel clientModel : getUsersFromFile().values())
                if (clientModel.getColoredUsername().equals(clientColoredUsername))
                    this.clientModel = clientModel;

            clients.add(this);

            String enteredChatMessage = RED_BOLD_BRIGHT + "SERVER: " + RESET +
                    clientModel.getColoredUsername() + RED_BOLD_BRIGHT + " has entered the chat." + RESET;

            readMessages();
            saveMessages(enteredChatMessage);

            System.out.println(enteredChatMessage);
            broadcastMessageToAll(enteredChatMessage);
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while (ChatServer.isServerOn() && socket.isConnected()) {
            try {
                if (ChatServer.isServerOn() && bufferedReader.ready()) {
                    messageFromClient = bufferedReader.readLine();

                    if (messageFromClient != null && messageFromClient.length() != 0) {
                        if (messageFromClient.charAt(messageFromClient.indexOf(": ") + 13) == '/') {
                            String commandRespond = CommandHandlerClient.commandHandler(messageFromClient);

                            if (commandRespond.contains("SERVER: ")) {
                                sendMessageToClient(commandRespond);
                            } else {
                                String[] arr = commandRespond.split(" ", 2);

                                String target = arr[0];
                                String messageToBeSent = arr[1];

                                messagingAClient(target, messageToBeSent);
                            }
                        } else {
                            if (messageFromClient.contains("has left the chatroom"))
                                closeEverything(socket, bufferedReader, bufferedWriter);

                            saveMessages(messageFromClient);

                            System.out.println(messageFromClient);
                            broadcastMessageToOthers(messageFromClient);
                        }
                    }
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void broadcastMessageToAll(String messageToSend) {
        for (ChatClientHandler client : clients) {
            try {
                client.getBufferedWriter().write(messageToSend);
                client.getBufferedWriter().newLine();
                client.getBufferedWriter().flush();
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void broadcastMessageToOthers(String messageToSend) {
        for (ChatClientHandler client : clients) {
            try {
                if (!client.equals(this)) {
                    client.getBufferedWriter().write(messageToSend);
                    client.getBufferedWriter().newLine();
                    client.getBufferedWriter().flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void sendMessageToClient(String messageToSend) {
        try {
            bufferedWriter.write(messageToSend);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void messagingAClient(String clientUsername, String messageToSend) {
        if (clientUsername.equals("server")) {
            System.out.println(messageToSend);
            return;
        }

        for (ChatClientHandler client : clients) {
            try {
                if (client.clientModel.getColoredUsername().equals(clientUsername)) {
                    client.getBufferedWriter().write(messageToSend);
                    client.getBufferedWriter().newLine();
                    client.getBufferedWriter().flush();
                    break;
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void removeClientHandler() {
        clients.remove(this);
        //        broadcastMessage(RED_BOLD_BRIGHT + "SERVER: " + RESET +
        //                clientUsername + RED_BOLD_BRIGHT + " has left the chat." + RESET);
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();

        if (!ChatServer.isServerOn()) {
            String shutdownMessage = RED_BOLD_BRIGHT + "SERVER WAS SHUTDOWN BY THE ADMINISTRATOR." + RESET;

            try {
                bufferedWriter.write(shutdownMessage);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            if (socket != null)
                socket.close();

            if (bufferedWriter != null)
                bufferedWriter.close();

            if (bufferedReader != null)
                bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveMessages(String messageToSave) {
        ArrayList<String> tempMessages = ChatMessagesFiles.readChatMessages();
        if (tempMessages != null) {
            ChatServer.getChatMessages().clear();
            ChatServer.getChatMessages().addAll(tempMessages);
        }

        ChatServer.getChatMessages().add(messageToSave);
        ChatMessagesFiles.writeChatMessages(ChatServer.getChatMessages());
    }

    public void readMessages() {
        ArrayList<String> tempMessages = ChatMessagesFiles.readChatMessages();
        if (tempMessages != null) {
            ChatServer.getChatMessages().clear();
            ChatServer.getChatMessages().addAll(tempMessages);
        }

        for (String oldMessage : ChatServer.getChatMessages()) {
            try {
                bufferedWriter.write(oldMessage);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
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

    public ClientModel getClientModel() {
        return clientModel;
    }

    public static ArrayList<ChatClientHandler> getClients() {
        return clients;
    }
}
