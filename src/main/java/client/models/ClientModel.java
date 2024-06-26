package client.models;

import java.io.Serializable;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import static utils.ConsoleDetail.BOLD_BRIGHTS_COLORS;
import static utils.ConsoleDetail.RESET;

public class ClientModel implements Serializable {
    private final UUID clientId;
    private final String username;
    private final String password;
    private final String coloredUsername;
    private final String CLIENT_COLOR;

    private Date lastLogin;

    private boolean online;
    private boolean banned;

    public ClientModel(String username, String password, UUID clientId) {
        this.username = username;
        this.password = password;
        this.clientId = clientId;
        this.CLIENT_COLOR = BOLD_BRIGHTS_COLORS[new Random().nextInt(BOLD_BRIGHTS_COLORS.length)];
        this.coloredUsername = CLIENT_COLOR + this.username + RESET;

        this.lastLogin = new Date();

        this.online = true;
        this.banned = false;
    }

    public ClientModel(UUID clientId, String username, String password, String CLIENT_COLOR, Date lastLogin, boolean online, boolean banned) {
        this.username = username;
        this.password = password;
        this.clientId = clientId;
        this.CLIENT_COLOR = BOLD_BRIGHTS_COLORS[new Random().nextInt(BOLD_BRIGHTS_COLORS.length)];
        this.coloredUsername = CLIENT_COLOR + this.username + RESET;

        this.lastLogin = new Date();

        this.online = true;
        this.banned = false;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getColoredUsername() {
        return coloredUsername;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public String getCLIENT_COLOR() {
        return CLIENT_COLOR;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public boolean isOnline() {
        return online;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public static ClientModel factory(String username, String password) {
        return new ClientModel(username, password, null);
    }

    public UUID getClientId() {
        return clientId;
    }
}
