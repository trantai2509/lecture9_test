package ute.udn.bt_lecture9.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ute.udn.bt_lecture9.model.Student
import ute.udn.bt_lecture9.repository.StudentRepository

/**
 * ViewModel quản lý trạng thái UI, sống sót qua xoay màn hình.
 * Mọi thao tác CSDL/mạng chạy trong viewModelScope trên Dispatchers.IO.
 */
class StudentViewModel(private val repository: StudentRepository) : ViewModel() {

    // Chuyển đổi Flow sang LiveData trên Dispatchers.IO
    val allStudents: LiveData<List<Student>> = repository.allStudents.asLiveData(Dispatchers.IO)

    // Thông báo trạng thái (Toast) gửi lên UI
    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> get() = _statusMessage

    fun insert(student: Student) = viewModelScope.launch(Dispatchers.IO) {
        try {
            // Không cho phép trùng MSSV
            if (repository.getStudentByCode(student.studentCode) != null) {
                _statusMessage.postValue("MSSV ${student.studentCode} đã tồn tại!")
                return@launch
            }
            repository.insert(student)
            _statusMessage.postValue("Đã thêm sinh viên: ${student.fullName}")
        } catch (e: Exception) {
            _statusMessage.postValue("Lỗi khi thêm: ${e.message}")
        }
    }

    fun delete(student: Student) = viewModelScope.launch(Dispatchers.IO) {
        try {
            repository.delete(student)
            _statusMessage.postValue("Đã xóa sinh viên: ${student.fullName}")
        } catch (e: Exception) {
            _statusMessage.postValue("Lỗi khi xóa: ${e.message}")
        }
    }

    fun syncFromCloud() = viewModelScope.launch(Dispatchers.IO) {
        _statusMessage.postValue("Đang đồng bộ dữ liệu từ máy chủ...")
        val result = repository.syncFromCloud()
        if (result.isSuccess) {
            _statusMessage.postValue("Đã đồng bộ ${result.getOrNull()} sinh viên từ máy chủ!")
        } else {
            _statusMessage.postValue("Chưa thể kết nối máy chủ Cloud, sử dụng dữ liệu cục bộ.")
        }
    }

    /**
     * Gọi sau khi UI đã hiển thị thông báo, tránh Toast cũ hiện lại
     * khi Fragment quan sát lại LiveData (ví dụ lúc xoay màn hình).
     */
    fun onStatusMessageShown() {
        _statusMessage.value = ""
    }
}
