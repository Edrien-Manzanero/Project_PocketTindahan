import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Sales(
    @PrimaryKey(autoGenerate = true) val sales_id: Int = 0,
    @ColumnInfo(name = "sales_date") val salesDate: String?,
    @ColumnInfo(name = "sales_Time") val salesTime: String?,
    @ColumnInfo(name = "sales_totalsales") val salesTotalSales: Int?,
    @ColumnInfo(name = "sales_itemsold") val salesTotalsold: Int?,
    @ColumnInfo(name = "sales_transaction") val salesTransaction: Int?,
    @ColumnInfo(name = "sales_profit") val salesProfit: Int?,



    )