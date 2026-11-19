package ute.udn.bt_lecture9.repository

import kotlinx.coroutines.flow.Flow
import ute.udn.bt_lecture9.data.StudentDao
import ute.udn.bt_lecture9.model.Student
import ute.udn.bt_lecture9.network.RetrofitClient
import ute.udn.bt_lecture9.network.StudentApiService
import ute.udn.bt_lecture9.network.isValid
import ute.udn.bt_lecture9.network.toEntity

/**
 * Tầng điều phối dữ liệu giữa Room (cục bộ) và Retrofit (mạng).
 * Room là Single Source of Truth: UI chỉ quan sát Flow từ Room,
 * dữ liệu mạng phải được ghi vào Room trước rồi mới hiển thị.
 */
class StudentRepository(
    private val studentDao: StudentDao,
    private val apiService: StudentApiService = RetrofitClient.apiService
) {
    // Single Source of Truth: UI chỉ quan sát Room Flow
    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()

    suspend fun insert(student: Student): Long = studentDao.insertStudent(student)

    suspend fun delete(student: Student) = studentDao.deleteStudent(student)

    // Tìm sinh viên theo MSSV (dùng để kiểm tra trùng mã trước khi thêm)
    suspend fun getStudentByCode(code: String): Student? = studentDao.getStudentByCode(code)

    /**
     * Tải danh sách từ REST API rồi ghi vào Room.
     * - Thành công: Result.success(số sinh viên đã đồng bộ).
     * - Thất bại (mất mạng, HTTP lỗi, JSON sai...): Result.failure, dữ liệu cục bộ giữ nguyên.
     */
    suspend fun syncFromCloud(): Result<Int> {
        return try {
            val remoteList = apiService.getStudents()
            val entities = remoteList
                .filter { it.isValid() } // Bỏ qua bản ghi thiếu họ tên/MSSV
                .map { dto ->
                    // Nếu MSSV đã có trong Room thì giữ id cũ để cập nhật, tránh tạo bản ghi trùng
                    val localId = studentDao.getStudentByCode(dto.studentCode!!.trim())?.id ?: 0
                    dto.toEntity(localId)
                }
            studentDao.insertAll(entities)
            Result.success(entities.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
