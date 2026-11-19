package ute.udn.bt_lecture9.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ute.udn.bt_lecture9.R
import ute.udn.bt_lecture9.data.AppDatabase
import ute.udn.bt_lecture9.databinding.ActivityMainBinding
import ute.udn.bt_lecture9.repository.StudentRepository
import ute.udn.bt_lecture9.viewmodel.StudentViewModel
import ute.udn.bt_lecture9.viewmodel.StudentViewModelFactory

/**
 * Single-Activity Host: khởi tạo Room Database, Repository, ViewModelFactory
 * và nạp StudentListFragment vào FragmentContainerView.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { StudentRepository(database.studentDao()) }
    private val viewModel: StudentViewModel by viewModels {
        StudentViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Xử lý khoảng đệm thanh trạng thái & điều hướng trên Android 15/16+ (Edge-to-Edge)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Khởi tạo ViewModel dùng chung trong ViewModelStore của Activity
        viewModel.allStudents

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, StudentListFragment())
                .commit()
        }
    }
}
