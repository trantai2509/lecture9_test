package ute.udn.bt_lecture9.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ute.udn.bt_lecture9.model.Student

/**
 * DAO (Data Access Object) định nghĩa các thao tác CRUD trên bảng students_table.
 * Các hàm ghi dùng suspend để chạy trong Coroutine (không chặn Main Thread).
 */
@Dao
interface StudentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(students: List<Student>)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM students_table WHERE id = :id")
    suspend fun deleteStudentById(id: Int)

    // Trả về Flow để tự động phát luồng dữ liệu khi CSDL thay đổi
    @Query("SELECT * FROM students_table ORDER BY id DESC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students_table WHERE student_code = :code LIMIT 1")
    suspend fun getStudentByCode(code: String): Student?
}
