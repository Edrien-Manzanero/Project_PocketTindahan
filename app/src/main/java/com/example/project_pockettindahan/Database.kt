import androidx.room.Database
import androidx.room.RoomDatabase

// 1. Added SalesItem::class to entities
// 2. Changed version to 2
@Database(entities = [Items::class, Sales::class, Debt::class, SalesItem::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ItemsDao(): ItemsDao
    abstract fun SalesDao(): SalesDao
    abstract fun DebtDao(): DebtDao

    // 3. Registered the new Dao
    abstract fun SalesItemDao(): SalesItemDao
}