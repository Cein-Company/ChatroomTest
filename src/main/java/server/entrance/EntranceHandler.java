package server.entrance;

import client.models.ClientModel;
import files.MyActiveUsersFiles;
import files.MyUsersFiles;

import java.util.UUID;

import static utils.ConsoleDetail.RED_BOLD_BRIGHT;
import static utils.ConsoleDetail.RESET;

public class EntranceHandler {

    public static final String USERNAME_TAKEN = RED_BOLD_BRIGHT+"Username taken. Try again."+RESET;
    public static final String NO_SUCH_CLIENT =  RED_BOLD_BRIGHT + "No such username was found. Try again." + RESET;
    public static final String CLIENT_BANNED = RED_BOLD_BRIGHT + "This user was banned from the chatroom." + RESET;
    public static final String ACTIVE_CLIENT = RED_BOLD_BRIGHT + "User is already in the chatroom." + RESET;
    private static final String INCORRECT_PASSWORD = RED_BOLD_BRIGHT + "Password incorrect. Try again." + RESET;

    public static boolean register(ClientModel newClient) throws Exception {
        String result;
        result = checkUsername(newClient.getUsername());
        result = checkPassword(newClient.getPassword());
        if (result != null)
           throw new Exception(result);
        newClient = new ClientModel(newClient.getUsername(),newClient.getPassword(), UUID.randomUUID());
        MyUsersFiles.save(newClient);
        return true;
    }

    public static boolean login(ClientModel client) throws Exception {
        String result = null;
        if (!MyUsersFiles.contains(client.getUsername())) {
           result = NO_SUCH_CLIENT;
        } else if (MyUsersFiles.getUserByName(client.getUsername()).isBanned()) {
            result = CLIENT_BANNED ;
        }else if (MyActiveUsersFiles.contains(client.getUsername())) {
            result = ACTIVE_CLIENT;
        } else if (!MyUsersFiles.getUserByName(client.getUsername()).getPassword().equals(client.getPassword())) {
            result = INCORRECT_PASSWORD;
        }
        if (result != null)
            throw new Exception(result);
        return true;
    }

    private static String checkUsername(String username) {
        if (MyUsersFiles.contains(username)) {
            return USERNAME_TAKEN;
        }
        return null;
    }

    private static String checkPassword(String password) {
        return null;
    }

}
