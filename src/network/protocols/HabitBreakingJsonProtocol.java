package network.protocols;

import database.DatabaseFactory;
import database.habitbreaking.HabitBreakingDatabaseInteractor;
import database.habitbreaking.adt.UserData;
import org.json.simple.JSONObject;

public class HabitBreakingJsonProtocol {

    // Message keys
    private static final String KEY_TYPE = "TYPE";
    private static final String KEY_LOGIN = "LOGIN";
    private static final String KEY_PASSWORD = "PASSWORD";

    // Message types
    private static final String TYPE_REGISTRATION = "REGISTRATION";

    // Module dependencies
    private HabitBreakingDatabaseInteractor habitBreakingDatabaseInteractor;

    ////

    public HabitBreakingJsonProtocol() {
        habitBreakingDatabaseInteractor = DatabaseFactory.provideHabitBreakingDatabaseInteractor();
    }

    ////

    public void handleMessage(JSONObject message) {
        String type = (String) message.get(KEY_TYPE);

        switch (type) {
            case TYPE_REGISTRATION:
                handleRegistration(message);
                break;
        }
    }

    ////

    private void handleRegistration(JSONObject message) {
        String login = (String) message.get(KEY_LOGIN);
        String password = (String) message.get(KEY_PASSWORD);

        UserData userData = new UserData();
        userData.setLogin(login);
        userData.setPassword(password);
        habitBreakingDatabaseInteractor.writeNewUser(userData);

        System.out.println(login + "  " + password);
    }
}
