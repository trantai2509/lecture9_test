package ute.udn.bt_lecture9.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ute.udn.bt_lecture9.repository.StudentRepository

/**
 * Factory khởi tạo StudentViewModel kèm Repository
 * (ViewModel có tham số constructor nên không thể tạo bằng mặc định).
 */
class StudentViewModelFactory(private val repository: StudentRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
