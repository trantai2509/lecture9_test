package ute.udn.bt_lecture9.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ute.udn.bt_lecture9.model.Student

/**
 * Lớp RoomDatabase trừu tượng, áp dụng Singleton Pattern:
 * toàn bộ ứng dụng chỉ dùng một kết nối SQLite duy nhất.
 */
@Database(entities = [Student::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao

    companion object {
        private const val DATABASE_NAME = "ute_student_room.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Kiểm tra lại trong synchronized để tránh tạo 2 instance khi nhiều luồng gọi cùng lúc
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext, // Dùng applicationContext để không rò rỉ Activity
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(DatabaseCallback())
                    .build()
                    .also { INSTANCE = it }
            }
        }

        /**
         * Callback chỉ chạy onCreate() MỘT LẦN khi file database được tạo mới,
         * nên dữ liệu mẫu không bị chèn lặp ở những lần mở ứng dụng sau.
         */
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    // Chèn dữ liệu mẫu trên luồng nền (Dispatchers.IO)
                    CoroutineScope(Dispatchers.IO).launch {
                        val initialList = listOf(
                            Student(0, "Nguyễn Văn An", "20110001", "an.nv@ute.udn.vn", 3.85, "Công Nghệ Thông Tin"),
                            Student(0, "Trần Thị Mai", "20110002", "mai.tt@ute.udn.vn", 3.92, "Kỹ Thuật Phần Mềm"),
                            Student(0, "Lê Hoàng Phúc", "20110003", "phuc.lh@ute.udn.vn", 3.45, "An Toàn Thông Tin"),
                            Student(0, "Phạm Quốc Dũng", "20110004", "dung.pq@ute.udn.vn", 3.60, "Mạng Máy Tính")
                        )
                        database.studentDao().insertAll(initialList)
                    }
                }
            }
        }
    }
}
