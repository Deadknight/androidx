import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.execSQL
import java.util.HashMap
import java.util.HashSet
import javax.`annotation`.processing.Generated
import kotlin.Any
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION"])
public class MyDatabase_Impl : MyDatabase() {
  private val _myDao: Lazy<MyDao> = lazy {
    MyDao_Impl(this)
  }


  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1,
        "195d7974660177325bd1a32d2c7b8b8c") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `MyEntity` (`pk` INTEGER NOT NULL, PRIMARY KEY(`pk`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '195d7974660177325bd1a32d2c7b8b8c')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `MyEntity`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsMyEntity: HashMap<String, TableInfo.Column> =
            HashMap<String, TableInfo.Column>(1)
        _columnsMyEntity.put("pk", TableInfo.Column("pk", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMyEntity: HashSet<TableInfo.ForeignKey> = HashSet<TableInfo.ForeignKey>(0)
        val _indicesMyEntity: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(0)
        val _infoMyEntity: TableInfo = TableInfo("MyEntity", _columnsMyEntity, _foreignKeysMyEntity,
            _indicesMyEntity)
        val _existingMyEntity: TableInfo = read(connection, "MyEntity")
        if (!_infoMyEntity.equals(_existingMyEntity)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |MyEntity(MyEntity).
              | Expected:
              |""".trimMargin() + _infoMyEntity + """
              |
              | Found:
              |""".trimMargin() + _existingMyEntity)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: HashMap<String, String> = HashMap<String, String>(0)
    val _viewTables: HashMap<String, Set<String>> = HashMap<String, Set<String>>(0)
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "MyEntity")
  }

  public override fun clearAllTables() {
    super.assertNotMainThread()
    val _db: SupportSQLiteDatabase = super.openHelper.writableDatabase
    try {
      super.beginTransaction()
      _db.execSQL("DELETE FROM `MyEntity`")
      super.setTransactionSuccessful()
    } finally {
      super.endTransaction()
      _db.query("PRAGMA wal_checkpoint(FULL)").close()
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM")
      }
    }
  }

  protected override fun getRequiredTypeConverterClasses():
      Map<KClass<out Any>, List<KClass<out Any>>> {
    val _typeConvertersMap: MutableMap<KClass<out Any>, List<KClass<out Any>>> = mutableMapOf()
    _typeConvertersMap.put(MyDao::class, MyDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun getDao(): MyDao = _myDao.value
}
