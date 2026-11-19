package ute.udn.bt_lecture9.network

import com.google.gson.annotations.SerializedName
import ute.udn.bt_lecture9.model.Student

/**
 * Data Transfer Object: mô tả cấu trúc JSON nhận về từ REST API.
 * Tách riêng với Entity để thay đổi API không ảnh hưởng tới bảng Room.
 *
 * Ví dụ JSON mong đợi:
 * { "id": 1, "fullName": "Nguyễn Văn A", "studentCode": "20110010",
 *   "email": "a@ute.udn.vn", "gpa": 3.5, "major": "Công Nghệ Thông Tin" }
 */
data class StudentDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("studentCode") val studentCode: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("gpa") val gpa: Double? = null,
    @SerializedName("major") val major: String? = null
)

/**
 * Chuyển DTO (mạng) sang Entity (Room).
 * @param localId id của bản ghi cục bộ trùng MSSV (0 = thêm mới, Room tự tăng id).
 */
fun StudentDto.toEntity(localId: Int = 0): Student = Student(
    id = localId,
    fullName = fullName.orEmpty().trim(),
    studentCode = studentCode.orEmpty().trim(),
    email = email?.trim().takeUnless { it.isNullOrEmpty() }
        ?: "${studentCode.orEmpty().trim().lowercase()}@ute.udn.vn",
    gpa = gpa ?: 0.0,
    major = major?.trim().takeUnless { it.isNullOrEmpty() } ?: "Công Nghệ Thông Tin"
)

/** DTO hợp lệ khi có đủ họ tên và MSSV. */
fun StudentDto.isValid(): Boolean =
    !fullName.isNullOrBlank() && !studentCode.isNullOrBlank()
