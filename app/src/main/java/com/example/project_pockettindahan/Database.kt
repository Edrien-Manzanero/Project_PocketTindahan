import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.project_pockettindahan.DebtDao
import com.example.project_pockettindahan.ItemsDao
import com.example.project_pockettindahan.SalesDao


@Database(entities = [Items::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ItemsDao(): ItemsDao
    abstract fun SalesDao(): SalesDao
    abstract fun DebtDao(): DebtDao

}
