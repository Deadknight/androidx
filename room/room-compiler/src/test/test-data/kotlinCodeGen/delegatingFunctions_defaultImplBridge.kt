import android.database.Cursor
import androidx.room.RoomDatabase
import androidx.room.RoomSQLiteQuery
import androidx.room.RoomSQLiteQuery.Companion.acquire
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.query
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.jvm.JvmStatic
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION"])
public class MyDao_Impl(
  __db: RoomDatabase,
) : MyDao {
  private val __db: RoomDatabase
  init {
    this.__db = __db
  }

  public override fun getEntity(): MyEntity {
    val _sql: String = "SELECT * FROM MyEntity"
    val _statement: RoomSQLiteQuery = acquire(_sql, 0)
    __db.assertNotSuspendingTransaction()
    val _cursor: Cursor = query(__db, _statement, false, null)
    try {
      val _cursorIndexOfPk: Int = getColumnIndexOrThrow(_cursor, "pk")
      val _result: MyEntity
      if (_cursor.moveToFirst()) {
        val _tmpPk: Long
        _tmpPk = _cursor.getLong(_cursorIndexOfPk)
        _result = MyEntity(_tmpPk)
      } else {
        error("The query result was empty, but expected a single row to return a NON-NULL object of type <MyEntity>.")
      }
      return _result
    } finally {
      _cursor.close()
      _statement.release()
    }
  }

  public companion object {
    @JvmStatic
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
