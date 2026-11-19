package ute.udn.bt_lecture9

import android.content.Context
import android.view.View
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import ute.udn.bt_lecture9.data.AppDatabase
import ute.udn.bt_lecture9.data.StudentDao
import ute.udn.bt_lecture9.model.Student
import ute.udn.bt_lecture9.network.StudentApiService
import ute.udn.bt_lecture9.network.StudentDto
import ute.udn.bt_lecture9.repository.StudentRepository
import ute.udn.bt_lecture9.ui.MainActivity
import java.io.IOException

/**
 * Kiểm thử tích hợp Room DAO, Repository và luồng UI MVVM end-to-end trên thiết bị/Emulator.
 */
@RunWith(AndroidJUnit4::class)
class StudentAppInstrumentedTest {

    @Test
    fun roomDao_crudAndFlowOrder_workCorrectly() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val dao: StudentDao = db.studentDao()

            val id1 = dao.insertStudent(
                Student(0, "Sinh Viên Một", "20111001", "sv1@ute.udn.vn", 3.20)
            ).toInt()
            dao.insertAll(
                listOf(
                    Student(0, "Sinh Viên Hai", "20111002", "sv2@ute.udn.vn", 3.50),
                    Student(0, "Sinh Viên Ba", "20111003", "sv3@ute.udn.vn", 3.80)
                )
            )

            val listAfterInsert = dao.getAllStudents().first()
            assertEquals(3, listAfterInsert.size)
            // Truy vấn ORDER BY id DESC nên phần tử mới nhất đứng đầu
            assertEquals("20111003", listAfterInsert.first().studentCode)

            val found = dao.getStudentByCode("20111001")
            assertNotNull(found)
            dao.updateStudent(found!!.copy(gpa = 3.95))
            assertEquals(3.95, dao.getStudentByCode("20111001")!!.gpa, 0.001)

            dao.deleteStudent(dao.getStudentByCode("20111002")!!)
            assertNull(dao.getStudentByCode("20111002"))

            dao.deleteStudentById(id1)
            assertEquals(1, dao.getAllStudents().first().size)
        } finally {
            db.close()
        }
    }

    @Test
    fun repository_syncFromCloud_handlesSuccessAndFailureSafely() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val dao = db.studentDao()
            dao.insertStudent(Student(0, "Sinh Viên Cũ", "20110001", "cu@ute.udn.vn", 3.5))

            val successRepo = StudentRepository(dao, object : StudentApiService {
                override suspend fun getStudents(): List<StudentDto> = listOf(
                    StudentDto(null, "Sinh Viên Cập Nhật", "20110001", null, 3.88, "CNTT"),
                    StudentDto(null, "Sinh Viên Mới Cloud", "20110009", null, 3.70, null),
                    StudentDto(null, "", "20119999", null, 3.0, null) // Không hợp lệ -> bỏ qua
                )
            })
            val okResult = successRepo.syncFromCloud()
            assertTrue(okResult.isSuccess)
            assertEquals(2, okResult.getOrNull())

            val stored = dao.getAllStudents().first()
            assertEquals(2, stored.size)
            assertEquals("Sinh Viên Cập Nhật", dao.getStudentByCode("20110001")?.fullName)
            assertEquals("20110009@ute.udn.vn", dao.getStudentByCode("20110009")?.email)

            val failingRepo = StudentRepository(dao, object : StudentApiService {
                override suspend fun getStudents(): List<StudentDto> {
                    throw IOException("Mạng không khả dụng")
                }
            })
            val failResult = failingRepo.syncFromCloud()
            assertTrue(failResult.isFailure)
            // Dữ liệu cục bộ vẫn được bảo toàn khi đồng bộ thất bại
            assertEquals(2, dao.getAllStudents().first().size)
        } finally {
            db.close()
        }
    }

    @Test
    fun endToEnd_studentManagementFlow_worksWithRoomAndMvvm() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // 1 & 2. Khởi động lần đầu, hiển thị 4 sinh viên mẫu từ DatabaseCallback
            waitUntilMatches(withId(R.id.tvStudentCountBadge), withText("4 sinh viên"))
            onView(withText("Nguyễn Văn An")).check(matches(isDisplayed()))
            onView(withText("Trần Thị Mai")).check(matches(isDisplayed()))
            onView(withText("Lê Hoàng Phúc")).check(matches(isDisplayed()))
            onView(withText("Phạm Quốc Dũng")).check(matches(isDisplayed()))

            // Kiểm tra tệp CSDL ute_student_room.db và bảng students_table thực tế
            assertTrue(context.getDatabasePath("ute_student_room.db").exists())
            val cursor = AppDatabase.getDatabase(context).openHelper.readableDatabase
                .query("SELECT COUNT(*) FROM students_table")
            cursor.use {
                assertTrue(it.moveToFirst())
                assertEquals(4, it.getInt(0))
            }

            // 3, 4, 5. Mở dialog thêm, kiểm tra validation thiếu dữ liệu và GPA sai không đóng dialog
            onView(withId(R.id.fabAdd)).perform(click())
            onView(withText("Lưu vào Room")).perform(click())
            onView(withId(R.id.etFullName)).check(matches(isDisplayed()))

            onView(withId(R.id.etFullName)).perform(replaceText("Võ Minh Tuấn"), closeSoftKeyboard())
            onView(withId(R.id.etStudentCode)).perform(replaceText("20110099"), closeSoftKeyboard())
            onView(withId(R.id.etGpa)).perform(replaceText("5.5"), closeSoftKeyboard())
            onView(withText("Lưu vào Room")).perform(click())
            onView(withId(R.id.etGpa)).check(matches(isDisplayed()))

            // Nhập GPA hợp lệ, để trống email -> tự sinh 20110099@ute.udn.vn
            onView(withId(R.id.etGpa)).perform(replaceText("3.75"), closeSoftKeyboard())
            onView(withText("Lưu vào Room")).perform(click())

            waitUntilMatches(withId(R.id.tvStudentCountBadge), withText("5 sinh viên"))
            onView(withText("Võ Minh Tuấn")).check(matches(isDisplayed()))
            onView(withText("20110099@ute.udn.vn")).check(matches(isDisplayed()))

            // 6. Chạm xem chi tiết sinh viên
            onView(withText("Võ Minh Tuấn")).perform(click())
            onView(withText("Thông Tin Sinh Viên")).check(matches(isDisplayed()))
            onView(withText("Đóng")).perform(click())

            // 7 & 8. Thử xóa rồi bấm Hủy -> danh sách giữ nguyên; xóa thật -> còn 4 sinh viên
            onView(deleteButtonForStudent("Võ Minh Tuấn")).perform(click())
            onView(withText("Hủy")).perform(click())
            onView(withId(R.id.tvStudentCountBadge)).check(matches(withText("5 sinh viên")))

            onView(deleteButtonForStudent("Võ Minh Tuấn")).perform(click())
            onView(withText("Xóa")).perform(click())
            waitUntilMatches(withId(R.id.tvStudentCountBadge), withText("4 sinh viên"))

            // 11. Tái tạo Activity mô phỏng xoay màn hình (Configuration Change)
            scenario.recreate()
            waitUntilMatches(withId(R.id.tvStudentCountBadge), withText("4 sinh viên"))
            onView(withText("Phạm Quốc Dũng")).check(matches(isDisplayed()))

            // 13. Thử đồng bộ Cloud (endpoint giữ chỗ trả lỗi -> không crash, giữ nguyên dữ liệu)
            onView(withId(R.id.action_sync)).perform(click())
            onView(withId(R.id.tvStudentCountBadge)).check(matches(withText("4 sinh viên")))

            // 12. Xóa toàn bộ để kiểm tra Empty State
            val remainingNames = listOf("Phạm Quốc Dũng", "Lê Hoàng Phúc", "Trần Thị Mai", "Nguyễn Văn An")
            remainingNames.forEachIndexed { index, name ->
                onView(deleteButtonForStudent(name)).perform(click())
                onView(withText("Xóa")).perform(click())
                val expectedRemaining = remainingNames.size - 1 - index
                waitUntilMatches(
                    withId(R.id.tvStudentCountBadge),
                    withText("$expectedRemaining sinh viên")
                )
                // Chờ DefaultItemAnimator hoàn tất hiệu ứng dịch chuyển dòng trước khi bấm dòng kế tiếp
                Thread.sleep(350)
            }
            onView(withId(R.id.layoutEmpty)).check(matches(isDisplayed()))
        }
    }

    private fun deleteButtonForStudent(fullName: String): Matcher<View> =
        allOf(withId(R.id.btnDelete), hasSibling(hasDescendant(withText(fullName))))

    private fun waitUntilMatches(
        viewMatcher: Matcher<View>,
        assertionMatcher: Matcher<View>,
        timeoutMs: Long = 5000L
    ) {
        val deadline = System.currentTimeMillis() + timeoutMs
        var lastError: Throwable? = null
        while (System.currentTimeMillis() < deadline) {
            try {
                onView(viewMatcher).check(matches(assertionMatcher))
                return
            } catch (e: AssertionError) {
                lastError = e
            } catch (e: NoMatchingViewException) {
                lastError = e
            }
            Thread.sleep(100)
        }
        throw AssertionError("Hết thời gian chờ điều kiện UI khớp", lastError)
    }
}
