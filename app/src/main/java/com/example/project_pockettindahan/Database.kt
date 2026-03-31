import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [Items::class, Sales::class, Debt::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ItemsDao(): ItemsDao
    abstract fun SalesDao(): SalesDao
    abstract fun DebtDao(): DebtDao
}