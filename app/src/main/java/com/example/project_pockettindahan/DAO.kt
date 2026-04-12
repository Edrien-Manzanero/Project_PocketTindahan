import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface ItemsDao {
    @Query("SELECT * FROM Items")
    fun getAll(): Flow<List<Items>>

    @Query("SELECT * FROM Items WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<Items>

    @Query("SELECT * FROM Items WHERE item_name LIKE :first AND " +
            "item_category LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): Items

    @Insert
    fun insertAll(vararg users: Items)

    @Delete
    fun delete(user: Items)

    @Update
    fun update(user: Items)
}

@Dao
interface SalesDao {
    @Query("SELECT * FROM Sales")
    fun getAll(): Flow<List<Sales>>

    // FIX HERE: Change List<Items> to List<Sales>
    @Query("SELECT * FROM Sales WHERE sales_id IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<Sales>

    @Query("SELECT * FROM Sales WHERE sales_date LIKE :first AND " +
            "sales_Time LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): Sales

    @Insert
    fun insertAll(vararg users: Sales)

    @Delete
    fun delete(user: Sales)
}

@Dao
interface DebtDao {
    @Query("SELECT * FROM Debt")
    fun getAll(): Flow<List<Debt>>

    @Query("SELECT * FROM Debt WHERE debt_id IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<Debt>

    // FIX HERE: Change : Sales to : Debt
    @Query("SELECT * FROM Debt WHERE debt_name LIKE :first AND " +
            "debt_date LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): Debt

    @Insert
    fun insertAll(vararg users: Debt)

    @Delete
    fun delete(user: Debt)
}