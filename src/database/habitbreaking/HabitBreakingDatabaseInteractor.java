package database.habitbreaking;

import database.habitbreaking.adt.UserData;

public interface HabitBreakingDatabaseInteractor {

    void writeNewUser(UserData userData);
}
