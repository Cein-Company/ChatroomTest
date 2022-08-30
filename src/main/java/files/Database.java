package files;

import client.models.ClientModel;
import server.models.ServerMessageMode;
import server.models.ServerMessageModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Database {
    protected static Map<String, ClientModel> users = new HashMap<>();
    protected static ArrayList<ServerMessageModel> messages = new ArrayList<>();

    private Connection connection;

//----------------------------------------------------------------------------------------------------------------------

    public Connection getConnection() throws SQLException {
        if (connection != null)
            return connection;

        final String url = "jdbc:mysql://localhost/chatroom";
        final String username = "root";
        final String password = "";

        this.connection = DriverManager.getConnection(url, username, password);

        System.out.println("Connected to chatroom database.");

        return this.connection;
    }

    public void initializeDatabase() throws SQLException {
        createUsersTable();
        createMessagesTable();
    }

    public void readTables() throws SQLException {
        readUsers();
        readMessages();
    }

//----------------------------------------------------------------------------------------------------------------------

    private void createUsersTable() throws SQLException {
        Statement usersStatement = getConnection().createStatement();

        String create = "CREATE TABLE IF NOT EXISTS users " +
                "(clientID varchar(36) PRIMARY KEY, " +
                "username TEXT, " +
                "password TEXT, " +
                "CLIENT_COLOR TEXT, " +
                "lastLogin DATETIME, " +
                "online BIT, " +
                "banned BIT)";

        usersStatement.execute(create);
        usersStatement.close();
    }

    public void readUsers() throws SQLException {
        users.clear();

        String select = "SELECT * FROM users";
        PreparedStatement statement = getConnection().prepareStatement(select);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            String clientID = resultSet.getString(1);
            String username = resultSet.getString(2);
            String password = resultSet.getString(3);
            String CLIENT_COLOR = resultSet.getString(4);
            Date lastLogin = resultSet.getDate(5);
            boolean online = resultSet.getBoolean(6);
            boolean banned = resultSet.getBoolean(7);

            ClientModel client = new ClientModel(UUID.fromString(clientID), username, password, CLIENT_COLOR, lastLogin, online, banned);
            users.put(username, client);
        }
    }

    public void saveUser(ClientModel client) throws SQLException {
        users.put(client.getUsername(), client);

        String add = "INSERT INTO users(clientID, username, password, CLIENT_COLOR, lastLogin, online, banned) VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement statement = getConnection().prepareStatement(add);

        statement.setString(1, client.getClientId().toString());
        statement.setString(2, client.getUsername());
        statement.setString(3, client.getPassword());
        statement.setString(4, client.getCLIENT_COLOR());
        statement.setDate(5, new Date(client.getLastLogin().getTime()));
        statement.setBoolean(6, client.isOnline());
        statement.setBoolean(7, client.isBanned());

        statement.executeUpdate();
        statement.close();
    }

    public void updateUser(ClientModel client) throws SQLException {
        users.replace(client.getUsername(), client);

        String add = "UPDATE users SET lastLogin = ?, online = ?, banned = ? WHERE clientID = ?";

        PreparedStatement statement = getConnection().prepareStatement(add);

        statement.setDate(1, new Date(client.getLastLogin().getTime()));
        statement.setBoolean(2, client.isOnline());
        statement.setBoolean(3, client.isBanned());
        statement.setString(4, client.getClientId().toString());

        statement.executeUpdate();
        statement.close();
    }

    public ClientModel getUserByUsername(String username) throws SQLException {
        String get = "SELECT * FROM users WHERE username = ?";

        PreparedStatement statement = getConnection().prepareStatement(get);

        statement.setString(1, username);

        ResultSet resultSet = statement.executeQuery();

        ClientModel client;
        if (resultSet.next()) {
            client = new ClientModel(
                    UUID.fromString(resultSet.getString("clientID")),
                    resultSet.getString("username"),
                    resultSet.getString("password"),
                    resultSet.getString("CLIENT_COLOR"),
                    resultSet.getDate("lastLogin"),
                    resultSet.getBoolean("online"),
                    resultSet.getBoolean("banned"));

            statement.close();
            return client;
        }

        statement.close();
        return null;
    }

//----------------------------------------------------------------------------------------------------------------------

    private void createMessagesTable() throws SQLException {
        Statement messagesStatement = getConnection().createStatement();

        String create = "CREATE TABLE IF NOT EXISTS messages " +
                "(messageID varchar(36) PRIMARY KEY, " +
                "message varchar(1000), " +
                "messageMode ENUM('ServerShutdownMsg', " +
                                    "'ServerKickMsg', " +
                                    "'FromSerer', " +
                                    "'FromServerAboutClient', " +
                                    "'FromClient', " +
                                    "'ToAdminister', " +
                                    "'ToAdministerAboutAClient', " +
                                    "'PMFromServerToClient', " +
                                    "'PMFromClientToClient', " +
                                    "'PMFromClientToServer', " +
                                    "'ListFromServer', " +
                                    "'SignInteract'), " +
                "clientID varchar(36), " +
                "clientModelSenderID varchar(36), " +
                "clientModelReceiverID varchar(36), " +
                "messageTime TEXT)";
        ;

        messagesStatement.execute(create);
        messagesStatement.close();
    }

    public void readMessages() throws SQLException {
        messages.clear();

        String select = "SELECT * FROM messages";
        PreparedStatement statement = getConnection().prepareStatement(select);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            String messageID = resultSet.getString(1);
            String message = resultSet.getString(2);
            String messageMode = resultSet.getString(3);
            String clientID = resultSet.getString(4);
            String clientModelSenderID = resultSet.getString(5);
            String clientModelReceiverID = resultSet.getString(6);
            String messageTime = resultSet.getString(7);

            ServerMessageModel messageModel = new ServerMessageModel(
                                                    ServerMessageMode.valueOf(messageMode),
                                                    UUID.fromString(messageID),
                                                    message,
                                                    getUserByUsername(clientID),
                                                    getUserByUsername(clientModelSenderID),
                                                    getUserByUsername(clientModelReceiverID),
                                                    messageTime);

            messages.add(messageModel);
        }
    }

    public void saveMessage(ServerMessageModel messageModel) throws SQLException {
        messages.add(messageModel);

        String add = "INSERT INTO messages(messageID, message, messageMode, clientID, clientModelSenderID, clientModelReceiverID, messageTime) VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement statement = getConnection().prepareStatement(add);

        statement.setString(1, messageModel.getMessageID().toString());
        statement.setString(2, messageModel.getMessage());
        statement.setString(3, messageModel.getMessageMode().toString());
        statement.setString(4, messageModel.getClient().getClientId().toString());
        statement.setString(5, messageModel.getClientModelSender().getClientId().toString());
        statement.setString(6, messageModel.getClientModelReceiver().getClientId().toString());
        statement.setString(7, messageModel.getMessageTime());

        statement.executeUpdate();
        statement.close();
    }

//----------------------------------------------------------------------------------------------------------------------
}
