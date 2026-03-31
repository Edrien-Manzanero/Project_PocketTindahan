import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Items(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "item_name") val itemName: String?,
    @ColumnInfo(name = "item_stock") val itemStock: Int?,
    @ColumnInfo(name = "item_currentstock") val itemCurrentStock: Int?,
    @ColumnInfo(name = "item_category") val itemCategory: String?,
    @ColumnInfo(name = "item_originalprice") val itemOriginalPrice: Int?,
    @ColumnInfo(name = "item_retailprice") val itemRetailPrice: Int?,



)