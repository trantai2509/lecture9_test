package ute.udn.bt_lecture9.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ute.udn.bt_lecture9.databinding.ItemStudentRoomBinding
import ute.udn.bt_lecture9.model.Student
import java.util.Locale

/**
 * ListAdapter tối ưu hiệu năng RecyclerView nhờ DiffUtil tự tính toán sai khác trên luồng nền.
 * Không lưu trữ Activity Context để tránh rò rỉ bộ nhớ.
 */
class StudentAdapter(
    private val onStudentClick: (Student) -> Unit,
    private val onDeleteClick: (Student) -> Unit
) : ListAdapter<Student, StudentAdapter.StudentViewHolder>(StudentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = ItemStudentRoomBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StudentViewHolder(
        private val binding: ItemStudentRoomBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(student: Student) {
            binding.tvFullName.text = student.fullName
            binding.tvStudentCode.text = "MSSV: ${student.studentCode}"
            binding.tvEmail.text = student.email
            binding.tvGpa.text = "GPA: ${String.format(Locale.US, "%.2f", student.gpa)}"
            binding.tvAvatar.text = student.fullName.trim().take(1).uppercase().ifEmpty { "?" }

            binding.root.setOnClickListener { onStudentClick(student) }
            binding.btnDelete.setOnClickListener { onDeleteClick(student) }
        }
    }

    class StudentDiffCallback : DiffUtil.ItemCallback<Student>() {
        override fun areItemsTheSame(oldItem: Student, newItem: Student): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Student, newItem: Student): Boolean =
            oldItem == newItem
    }
}
