package com.example.project_pockettindahan

import Debt
import Items
import Sales
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ItemsDao {


    @Query("SELECT * FROM Items")
    fun getAll(): List<Items>



    @Query("SELECT * FROM Items WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<Items>

    @Query("SELECT * FROM Items WHERE item_name LIKE :first AND " +
            "item_category LIKE :last LIMIT 1")

    fun findByName(first: String, last: String): Items

    @Insert
    fun insertAll(vararg users: Items)

    @Delete
    fun delete(user: Items)
}

@Dao
interface SalesDao{


    @Query("SELECT * FROM Sales")
    fun getAll(): List<Items>



    @Query("SELECT * FROM Sales WHERE salesID IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<Items>

    @Query("SELECT * FROM Sales WHERE salesID LIKE :first AND " +
            "sales_totalsales LIKE :last LIMIT 1")

    fun findByName(first: String, last: String): Sales

    @Insert
    fun insertAll(vararg sales: Sales)

    @Delete
    fun delete(sales: Sales)
}

@Dao
interface DebtDao {

    @Query("SELECT * FROM Debt")
    fun getAll(): List<Debt>

    @Query("SELECT * FROM Debt WHERE debtID IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<Debt>

    @Query("SELECT * FROM Debt WHERE debt_name LIKE :first AND debt_date LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): Debt

    @Insert
    fun insertAll(vararg debts: Debt)

    @Delete
    fun delete(debt: Debt)
}