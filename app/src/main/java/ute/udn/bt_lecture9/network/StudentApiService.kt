package ute.udn.bt_lecture9.network

import retrofit2.http.GET

/**
 * Interface Retrofit định nghĩa các Endpoint HTTP.
 * Hàm suspend: Retrofit tự chạy request trên luồng nền.
 */
interface StudentApiService {

    // GET {BASE_URL}students -> trả về mảng JSON sinh viên
    @GET("students")
    suspend fun getStudents(): List<StudentDto>
}
