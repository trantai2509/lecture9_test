package ute.udn.bt_lecture9.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Khởi tạo Retrofit theo Singleton (object) cho toàn ứng dụng.
 */
object RetrofitClient {

    /**
     * LƯU Ý: Tài liệu bài giảng không cung cấp máy chủ REST API thật.
     * Đây là địa chỉ giữ chỗ (placeholder) – endpoint "students" KHÔNG tồn tại,
     * nên chức năng Đồng bộ Cloud sẽ báo lỗi và ứng dụng tiếp tục dùng dữ liệu Room.
     * Khi có API thật, chỉ cần thay BASE_URL (phải kết thúc bằng dấu "/").
     */
    const val BASE_URL = "https://example.com/api/"

    // Ghi log request/response ra Logcat để dễ kiểm tra khi học
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    // by lazy: chỉ tạo Retrofit khi lần đầu cần dùng
    val apiService: StudentApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StudentApiService::class.java)
    }
}
