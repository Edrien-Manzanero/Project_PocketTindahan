import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Debt(
    @PrimaryKey(autoGenerate = true) val debt_id: Int = 0,
    @ColumnInfo(name = "debt_name") val debtName: String?,
    @ColumnInfo(name = "debt_amount") val debtAmount: Int?,
    @ColumnInfo(name = "debt_date") val debtDate: String?,
    @ColumnInfo(name = "debt_status") val debtStatus: String?


    )