package ute.udn.bt_lecture9.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import ute.udn.bt_lecture9.data.AppDatabase
import ute.udn.bt_lecture9.databinding.DialogAddStudentBinding
import ute.udn.bt_lecture9.model.Student
import ute.udn.bt_lecture9.repository.StudentRepository
import ute.udn.bt_lecture9.viewmodel.StudentViewModel
import ute.udn.bt_lecture9.viewmodel.StudentViewModelFactory

/**
 * Hộp thoại nhập liệu thêm sinh viên mới vào Room Database.
 * Kiểm tra dữ liệu hợp lệ trước khi đóng dialog để tránh đóng mất nội dung đang nhập.
 */
class AddStudentDialogFragment : DialogFragment() {

    private var _binding: DialogAddStudentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StudentViewModel by activityViewModels {
        val dao = AppDatabase.getDatabase(requireContext().applicationContext).studentDao()
        StudentViewModelFactory(StudentRepository(dao))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddStudentBinding.inflate(LayoutInflater.from(requireContext()))

        // Truyền null cho listener nút Positive để tự kiểm soát đóng dialog sau khi validate
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Thêm Sinh Viên Mới")
            .setView(binding.root)
            .setPositiveButton("Lưu vào Room", null)
            .setNegativeButton("Hủy", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                handleSave(dialog)
            }
        }

        return dialog
    }

    private fun handleSave(dialog: AlertDialog) {
        binding.tilFullName.error = null
        binding.tilStudentCode.error = null
        binding.tilGpa.error = null

        val name = binding.etFullName.text?.toString()?.trim().orEmpty()
        val code = binding.etStudentCode.text?.toString()?.trim().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val gpaText = binding.etGpa.text?.toString()?.trim().orEmpty()

        if (name.isEmpty() || code.isEmpty()) {
            if (name.isEmpty()) binding.tilFullName.error = "Họ tên không được để trống"
            if (code.isEmpty()) binding.tilStudentCode.error = "MSSV không được để trống"
            Toast.makeText(
                requireContext(),
                "Họ tên và MSSV không được để trống!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val gpa = gpaText.replace(',', '.').toDoubleOrNull()
        if (gpaText.isEmpty() || gpa == null || gpa < 0.0 || gpa > 4.0) {
            binding.tilGpa.error = "GPA phải từ 0.0 đến 4.0"
            Toast.makeText(
                requireContext(),
                "Điểm GPA không hợp lệ (0.0 - 4.0)!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Nếu email để trống thì tạo email mặc định theo MSSV
        val finalEmail = if (email.isNotEmpty()) email else "${code.lowercase()}@ute.udn.vn"
        val student = Student(
            fullName = name,
            studentCode = code,
            email = finalEmail,
            gpa = gpa
        )
        viewModel.insert(student)
        dialog.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
