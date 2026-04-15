import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SalesItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "parent_sale_id") val parentSaleId: Int, // Links to Sales.sales_id
    @ColumnInfo(name = "item_name") val itemName: String,
    @ColumnInfo(name = "quantity") val quantity: Int,
    @ColumnInfo(name = "price_per_unit") val pricePerUnit: Int
)