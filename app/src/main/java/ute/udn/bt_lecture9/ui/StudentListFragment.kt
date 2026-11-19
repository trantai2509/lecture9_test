package ute.udn.bt_lecture9.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ute.udn.bt_lecture9.R
import ute.udn.bt_lecture9.data.AppDatabase
import ute.udn.bt_lecture9.databinding.FragmentStudentListBinding
import ute.udn.bt_lecture9.model.Student
import ute.udn.bt_lecture9.repository.StudentRepository
import ute.udn.bt_lecture9.ui.adapter.StudentAdapter
import ute.udn.bt_lecture9.viewmodel.StudentViewModel
import ute.udn.bt_lecture9.viewmodel.StudentViewModelFactory
import java.util.Locale

/**
 * Màn hình hiển thị danh sách sinh viên từ Room Database qua ViewModel.
 * Áp dụng Backing Property (_binding) để tránh rò rỉ bộ nhớ vòng đời kép của Fragment.
 */
class StudentListFragment : Fragment() {

    // Backing Property: quản lý an toàn vòng đời ViewBinding của Fragment
    private var _binding: FragmentStudentListBinding? = null
    private val binding get() = _binding!!

    // Dùng chung ViewModel với MainActivity kèm Factory để tránh lỗi khởi tạo
    private val viewModel: StudentViewModel by activityViewModels {
        val dao = AppDatabase.getDatabase(requireContext().applicationContext).studentDao()
        StudentViewModelFactory(StudentRepository(dao))
    }

    private lateinit var adapter: StudentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = StudentAdapter(
            onStudentClick = { student -> showStudentDetailDialog(student) },
            onDeleteClick = { student -> confirmDelete(student) }
        )
        binding.rvStudents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@StudentListFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        // Quan sát danh sách sinh viên tự động từ Room DB qua viewLifecycleOwner
        viewModel.allStudents.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            val isEmpty = list.isEmpty()
            binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvStudents.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.tvStudentCountBadge.text = "${list.size} sinh viên"
        }

        viewModel.statusMessage.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                viewModel.onStatusMessageShown()
            }
        }
    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            AddStudentDialogFragment().show(childFragmentManager, "ADD_STUDENT_DIALOG")
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_sync -> {
                    viewModel.syncFromCloud()
                    true
                }
                else -> false
            }
        }
    }

    private fun confirmDelete(student: Student) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xác Nhận Xóa")
            .setMessage("Bạn có chắc muốn xóa sinh viên ${student.fullName} khỏi CSDL Room?")
            .setPositiveButton("Xóa") { _, _ -> viewModel.delete(student) }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showStudentDetailDialog(student: Student) {
        val message = buildString {
            appendLine("ID: ${student.id}")
            appendLine("Họ Tên: ${student.fullName}")
            appendLine("MSSV: ${student.studentCode}")
            appendLine("Email: ${student.email}")
            appendLine("Điểm GPA: ${String.format(Locale.US, "%.2f", student.gpa)}")
            append("Chuyên ngành: ${student.major}")
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Thông Tin Sinh Viên")
            .setMessage(message)
            .setPositiveButton("Đóng", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Giải phóng binding.rvStudents.adapter và _binding để chống rò rỉ bộ nhớ
        binding.rvStudents.adapter = null
        _binding = null
    }
}
