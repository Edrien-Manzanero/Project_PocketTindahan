import android.R
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import kotlin.Int


@Entity
data class Sales(
    @PrimaryKey(autoGenerate = true) val salesID: Int = 0,
    @ColumnInfo(name = "sales_date") val salesDate: String?,
    @ColumnInfo(name = "sales_totalsales") val salesTotalSales: Int?,
    @ColumnInfo(name = "sales_totalsold") val sales_totalsold: Int?,
    @ColumnInfo(name = "sales_transactions") val sales_transactions: Int?,

    )