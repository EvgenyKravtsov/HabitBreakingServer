package database;

import database.habitbreaking.HabitBreakingDatabaseInteractor;
import database.habitbreaking.HabitBreakingDatabaseInteractorMysql;

public class DatabaseFactory {

    public static HabitBreakingDatabaseInteractor provideHabitBreakingDatabaseInteractor() {
        return HabitBreakingDatabaseInteractorMysql.getInstance();
    }
}
