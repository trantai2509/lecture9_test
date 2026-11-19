package ute.udn.bt_lecture9

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ute.udn.bt_lecture9.data.StudentDao
import ute.udn.bt_lecture9.model.Student
import ute.udn.bt_lecture9.network.StudentApiService
import ute.udn.bt_lecture9.network.StudentDto
import ute.udn.bt_lecture9.network.isValid
import ute.udn.bt_lecture9.network.toEntity
import ute.udn.bt_lecture9.repository.StudentRepository
import ute.udn.bt_lecture9.viewmodel.StudentViewModel
import ute.udn.bt_lecture9.viewmodel.StudentViewModelFactory

/**
 * Unit test kiểm tra logic ánh xạ DTO, kiểm tra tính hợp lệ và ViewModelFactory trên JVM.
 */
class StudentUnitTest {

    @Test
    fun studentDto_toEntity_mapsAllFieldsWhenPresent() {
        val dto = StudentDto(
            id = 99,
            fullName = "  Nguyễn Minh Khoa  ",
            studentCode = " 20110050 ",
            email = " khoa.nm@ute.udn.vn ",
            gpa = 3.72,
            major = " Khoa Học Dữ Liệu "
        )

        val entity = dto.toEntity(localId = 5)

        assertEquals(5, entity.id)
        assertEquals("Nguyễn Minh Khoa", entity.fullName)
        assertEquals("20110050", entity.studentCode)
        assertEquals("khoa.nm@ute.udn.vn", entity.email)
        assertEquals(3.72, entity.gpa, 0.001)
        assertEquals("Khoa Học Dữ Liệu", entity.major)
    }

    @Test
    fun studentDto_toEntity_appliesDefaultsWhenOptionalFieldsMissing() {
        val dto = StudentDto(
            fullName = "Lê Thị Lan",
            studentCode = "2011ABC",
            email = "   ",
            gpa = null,
            major = null
        )

        val entity = dto.toEntity()

        assertEquals(0, entity.id)
        assertEquals("2011abc@ute.udn.vn", entity.email)
        assertEquals(0.0, entity.gpa, 0.001)
        assertEquals("Công Nghệ Thông Tin", entity.major)
    }

    @Test
    fun studentDto_isValid_checksRequiredFields() {
        assertTrue(StudentDto(fullName = "An", studentCode = "20110001").isValid())
        assertFalse(StudentDto(fullName = "   ", studentCode = "20110001").isValid())
        assertFalse(StudentDto(fullName = "An", studentCode = "").isValid())
        assertFalse(StudentDto(fullName = null, studentCode = null).isValid())
    }

    @Test
    fun viewModelFactory_createsStudentViewModelAndRejectsUnknownClass() {
        val fakeDao = object : StudentDao {
            override suspend fun insertStudent(student: Student): Long = 1L
            override suspend fun insertAll(students: List<Student>) = Unit
            override suspend fun updateStudent(student: Student) = Unit
            override suspend fun deleteStudent(student: Student) = Unit
            override suspend fun deleteStudentById(id: Int) = Unit
            override fun getAllStudents(): Flow<List<Student>> = flowOf(emptyList())
            override suspend fun getStudentByCode(code: String): Student? = null
        }
        val fakeApi = object : StudentApiService {
            override suspend fun getStudents(): List<StudentDto> = emptyList()
        }
        val factory = StudentViewModelFactory(StudentRepository(fakeDao, fakeApi))

        val vm = factory.create(StudentViewModel::class.java)
        assertNotNull(vm)

        class DummyViewModel : ViewModel()
        var threw = false
        try {
            factory.create(DummyViewModel::class.java)
        } catch (e: IllegalArgumentException) {
            threw = true
        }
        assertTrue(threw)
    }
}
