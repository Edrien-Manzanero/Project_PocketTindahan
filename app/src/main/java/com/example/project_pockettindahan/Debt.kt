import android.R
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class Debt(
    @PrimaryKey(autoGenerate = true) val debtID: Int = 0,
    @ColumnInfo(name = "debt_name") val debtName: String?,
    @ColumnInfo(name = "debt_amount") val debtAmount: Int?,
    @ColumnInfo(name = "debt_date") val debtDate: String?,



    )