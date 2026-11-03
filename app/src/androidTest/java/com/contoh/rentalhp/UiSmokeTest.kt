package com.contoh.rentalhp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.net.Uri
import android.os.LocaleList
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.contoh.rentalhp.data.api.ApiService
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.data.model.*
import com.contoh.rentalhp.databinding.ActivityAlatFormBinding
import com.contoh.rentalhp.ui.admin.AlatFormActivity
import com.contoh.rentalhp.ui.auth.LoginActivity
import com.contoh.rentalhp.ui.auth.RegisterActivity
import com.contoh.rentalhp.ui.common.DatePickerHelper
import com.contoh.rentalhp.util.SessionManager
import com.contoh.rentalhp.util.StatusUtil
import com.contoh.rentalhp.util.ThemePreference
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.Matchers.`is`
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class UiSmokeTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private lateinit var originalApi: ApiService
    private lateinit var preferences: SharedPreferences
    private var sessionSnapshot: Map<String, *> = emptyMap<String, Any>()
    private var originalTheme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    private lateinit var backend: UiTestBackend

    @Before
    fun setUp() {
        val session = SessionManager.getInstance(context)
        preferences = SessionManager::class.java.getDeclaredField("preferences").apply {
            isAccessible = true
        }.get(session) as SharedPreferences
        sessionSnapshot = preferences.all.toMap()
        session.clearSession()
        originalTheme = ThemePreference.currentMode(context)
        onMain { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) }
        originalApi = RetrofitClient.apiService
        backend = UiTestBackend()
        setApi(backend.api)
    }

    @After
    fun tearDown() {
        setApi(originalApi)
        val editor = preferences.edit().clear()
        sessionSnapshot.forEach { (key, value) ->
            when (value) {
                is String -> editor.putString(key, value)
                is Long -> editor.putLong(key, value)
                is Int -> editor.putInt(key, value)
                is Boolean -> editor.putBoolean(key, value)
                is Float -> editor.putFloat(key, value)
                is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toSet())
            }
        }
        editor.commit()
        onMain { ThemePreference.setMode(context, originalTheme) }
    }

    @Test
    fun layouts_renderInBothThemesAtPhoneAndTabletWidths() {
        val layouts = listOf(
            R.layout.activity_login, R.layout.activity_register, R.layout.activity_main,
            R.layout.nav_header, R.layout.fragment_profile, R.layout.fragment_paged_list,
            R.layout.item_entity, R.layout.activity_alat_detail, R.layout.activity_alat_form,
            R.layout.activity_peminjaman_form, R.layout.activity_peminjaman_detail,
            R.layout.activity_pengembalian_form, R.layout.activity_pengembalian_detail,
            R.layout.item_rental_selection, R.layout.item_rental_detail, R.layout.item_return_unit,
            R.layout.item_return_detail, R.layout.fragment_report, R.layout.dialog_reject,
            R.layout.activity_user_form, R.layout.activity_category_form, R.layout.activity_unit_form
        )
        for (night in listOf(false, true)) {
            for (widthDp in listOf(320, 600)) {
                val configuration = Configuration(context.resources.configuration).apply {
                    uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                        if (night) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
                    screenWidthDp = widthDp
                    smallestScreenWidthDp = widthDp
                    fontScale = if (widthDp == 320) 2f else 1f
                    setLocales(LocaleList.forLanguageTags("id"))
                }
                val themed = ContextThemeWrapper(context.createConfigurationContext(configuration), R.style.Theme_GladiMobille)
                for (layout in layouts) {
                    onMain {
                        val root = LayoutInflater.from(themed).inflate(layout, FrameLayout(themed), false)
                        prepareLayout(root)
                        val density = themed.resources.displayMetrics.density
                        val width = (widthDp * density).toInt()
                        val height = (960 * density).toInt()
                        val name = themed.resources.getResourceEntryName(layout)
                        val contentSized = name.startsWith("item_") || name == "nav_header" || name == "dialog_reject"
                        root.measure(
                            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(height, if (contentSized) View.MeasureSpec.AT_MOST else View.MeasureSpec.EXACTLY)
                        )
                        root.layout(0, 0, root.measuredWidth, root.measuredHeight)
                        checkHorizontalBounds(root, name)
                        val bitmap = Bitmap.createBitmap(root.measuredWidth, root.measuredHeight, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(MaterialColors.getColor(root, android.R.attr.colorBackground))
                        root.draw(canvas)
                        val folder = File(context.cacheDir, "ui-checks").apply { mkdirs() }
                        try {
                            File(folder, "$name-${if (night) "dark" else "light"}-$widthDp.png").outputStream().use {
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                            }
                        } finally {
                            bitmap.recycle()
                        }
                    }
                }
            }
        }
    }

    @Test
    fun authValidationPasswordToggleAndRegistrationNavigation_work() {
        ActivityScenario.launch(LoginActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.button_login).performClick()
                assertEquals(activity.getString(R.string.required_field), activity.findViewById<TextInputLayout>(R.id.layout_username).error)
                assertEquals(activity.getString(R.string.required_field), activity.findViewById<TextInputLayout>(R.id.layout_password).error)
                val password = activity.findViewById<TextView>(R.id.input_password)
                password.text = "kata-sandi-uji"
                activity.findViewById<TextInputLayout>(R.id.layout_password).error = null
                val before = password.transformationMethod
                activity.findViewById<TextInputLayout>(R.id.layout_password)
                    .findViewById<View>(com.google.android.material.R.id.text_input_end_icon).performClick()
                assertTrue(before != password.transformationMethod)
                activity.findViewById<View>(R.id.button_register).performClick()
            }
            waitUntil { resumedActivity() is RegisterActivity }
            onMain {
                val activity = resumedActivity() as RegisterActivity
                activity.findViewById<View>(R.id.button_register).performClick()
                assertEquals(activity.getString(R.string.required_field), activity.findViewById<TextInputLayout>(R.id.layout_name).error)
                assertEquals(activity.getString(R.string.required_field), activity.findViewById<TextInputLayout>(R.id.layout_phone).error)
            }
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            waitUntil { resumedActivity() is LoginActivity }
        }
    }

    @Test
    fun everyRoleDestinationDrawerAndDateDialog_work() {
        val roleMenus = listOf(
            SessionManager.ROLE_PEMINJAM to listOf(R.id.menu_catalog, R.id.menu_rental_history, R.id.menu_profile),
            SessionManager.ROLE_PETUGAS to listOf(R.id.menu_request_queue, R.id.menu_handover, R.id.menu_return_monitor, R.id.menu_reports, R.id.menu_profile),
            SessionManager.ROLE_ADMIN to listOf(R.id.menu_users, R.id.menu_categories, R.id.menu_phones, R.id.menu_units, R.id.menu_rental_data, R.id.menu_return_data, R.id.menu_reports, R.id.menu_activity_log, R.id.menu_profile)
        )
        for ((role, menus) in roleMenus) {
            signIn(role)
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                for (menu in menus) {
                    scenario.onActivity { it.openDestination(menu) }
                    waitUntil {
                        onMain {
                            val activity = resumedActivity() as? MainActivity ?: return@onMain false
                            activity.supportFragmentManager.findFragmentById(R.id.fragment_container)?.view != null
                        }
                    }
                    scenario.onActivity { activity ->
                        val navigation = activity.findViewById<NavigationView>(R.id.navigation_view)
                        assertTrue(navigation.menu.findItem(menu).isChecked)
                        assertTrue(activity.findViewById<View>(R.id.toolbar).top >= 0)
                    }
                }
                scenario.onActivity { activity ->
                    val drawer = activity.findViewById<DrawerLayout>(R.id.drawer_layout)
                    drawer.openDrawer(GravityCompat.START, false)
                    assertTrue(drawer.isDrawerOpen(GravityCompat.START))
                    drawer.closeDrawer(GravityCompat.START, false)
                    assertFalse(drawer.isDrawerOpen(GravityCompat.START))
                    DatePickerHelper.show(activity.supportFragmentManager, activity.getString(R.string.start_date), LocalDate.now().toString()) {}
                }
                waitUntil { onMain { (resumedActivity() as? MainActivity)?.supportFragmentManager?.findFragmentByTag("date_picker") != null } }
                instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            }
        }
    }

    @Test
    fun listErrorRetryAndEmptyStates_work() {
        signIn(SessionManager.ROLE_PEMINJAM)
        backend.mode = UiTestBackend.Mode.ERROR
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            waitUntil { onMain { (resumedActivity() as? MainActivity)?.findViewById<View>(R.id.button_retry)?.visibility == View.VISIBLE } }
            scenario.onActivity { activity ->
                assertEquals(UiTestBackend.ERROR_MESSAGE, activity.findViewById<TextView>(R.id.text_state_message).text.toString())
                backend.mode = UiTestBackend.Mode.NORMAL
                activity.findViewById<View>(R.id.button_retry).performClick()
            }
            waitUntil { onMain { ((resumedActivity() as? MainActivity)?.findViewById<RecyclerView>(R.id.recycler_view)?.adapter?.itemCount ?: 0) > 0 } }
            scenario.onActivity { activity ->
                backend.mode = UiTestBackend.Mode.EMPTY
                activity.findViewById<View>(R.id.button_search).performClick()
            }
            waitUntil { onMain { (resumedActivity() as? MainActivity)?.findViewById<View>(R.id.layout_state)?.visibility == View.VISIBLE } }
            scenario.onActivity { activity ->
                assertEquals(activity.getString(R.string.empty_catalog_title), activity.findViewById<TextView>(R.id.text_state_title).text.toString())
                assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.image_state).visibility)
            }
        }
    }

    @Test
    fun themeSelector_recreatesProfileInLightAndDarkMode() {
        signIn(SessionManager.ROLE_PEMINJAM)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.openDestination(R.id.menu_profile) }
            for ((label, nightMode) in listOf(
                R.string.theme_dark to Configuration.UI_MODE_NIGHT_YES,
                R.string.theme_light to Configuration.UI_MODE_NIGHT_NO
            )) {
                waitUntil { onMain { (resumedActivity() as? MainActivity)?.findViewById<View>(R.id.input_theme) != null } }
                onMain {
                    val activity = resumedActivity() as MainActivity
                    val fragmentView = activity.supportFragmentManager.findFragmentById(R.id.fragment_container)?.view
                    (fragmentView as? androidx.core.widget.NestedScrollView)?.fullScroll(View.FOCUS_DOWN)
                }
                instrumentation.waitForIdleSync()
                onMain {
                    val activity = resumedActivity() as MainActivity
                    val input = activity.findViewById<MaterialAutoCompleteTextView>(R.id.input_theme)
                    input.requestRectangleOnScreen(Rect(0, 0, input.width, input.height), true)
                    input.showDropDown()
                }
                val text = context.getString(label)
                onData(`is`(text)).inRoot(isPlatformPopup()).perform(click())
                waitUntil {
                    onMain {
                        val activity = resumedActivity() as? MainActivity ?: return@onMain false
                        activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == nightMode &&
                            activity.findViewById<TextView>(R.id.text_name) != null
                    }
                }
            }
        }
    }

    @SdkSuppress(minSdkVersion = 29)
    @Test
    fun photoPickerLaunchesWithoutStoragePermissions() {
        signIn(SessionManager.ROLE_ADMIN)
        val permissions = context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.GET_PERMISSIONS).requestedPermissions.orEmpty()
        assertFalse(permissions.any { it.contains("READ_MEDIA") || it.contains("READ_EXTERNAL_STORAGE") || it.contains("WRITE_EXTERNAL_STORAGE") })
        ActivityScenario.launch<AlatFormActivity>(AlatFormActivity.createIntent(context, 1)).use { scenario ->
            waitUntil { onMain { (resumedActivity() as? AlatFormActivity)?.findViewById<View>(R.id.content)?.visibility == View.VISIBLE } }
            scenario.onActivity { it.findViewById<View>(R.id.button_pick_image).performClick() }
            waitUntil {
                val packageName = instrumentation.uiAutomation.rootInActiveWindow?.packageName?.toString()
                packageName != null && packageName != context.packageName && !packageName.contains("permissioncontroller")
            }
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            waitUntil { resumedActivity() is AlatFormActivity }
        }
    }

    @SdkSuppress(minSdkVersion = 29)
    @Test
    fun uploadFailure_showsServerMessagePreventsDoubleTapAndDeletesTempFile() {
        signIn(SessionManager.ROLE_ADMIN)
        val uri = GalleryTestPhoto.create(context, 2400, 1600, 6)
        val before = uploadFiles()
        try {
            backend.rejectUpload = true
            ActivityScenario.launch<AlatFormActivity>(AlatFormActivity.createIntent(context, 1)).use { scenario ->
                waitUntil { onMain { (resumedActivity() as? AlatFormActivity)?.findViewById<View>(R.id.content)?.visibility == View.VISIBLE } }
                scenario.onActivity { activity ->
                    AlatFormActivity::class.java.getDeclaredMethod("selectImage", Uri::class.java).apply {
                        isAccessible = true
                    }.invoke(activity, uri)
                }
                waitUntil { onMain { (resumedActivity() as? AlatFormActivity)?.findViewById<TextView>(R.id.text_image_message)?.text?.toString() == context.getString(R.string.gallery_image_selected) } }
                scenario.onActivity { activity ->
                    val binding = AlatFormActivity::class.java.getDeclaredField("binding").apply { isAccessible = true }.get(activity) as ActivityAlatFormBinding
                    assertEquals("125000.5", binding.inputPrice.text.toString().replace(',', '.'))
                    binding.buttonSave.performClick()
                    assertFalse(binding.buttonSave.isEnabled)
                    binding.buttonSave.performClick()
                }
                waitUntil { backend.uploadCount == 1 && uploadFiles() == before && onMain { (resumedActivity() as? AlatFormActivity)?.findViewById<View>(R.id.button_save)?.isEnabled == true } }
                scenario.onActivity { activity ->
                    assertEquals(UiTestBackend.UPLOAD_ERROR, activity.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).text.toString())
                }
                assertTrue(backend.lastBody.contains("name=\"image\""))
                assertTrue(backend.lastBody.contains("Content-Type: image/jpeg"))
                assertTrue(backend.lastBody.contains("125000.5"))
                assertFalse(backend.lastBody.contains("imagePath"))
                assertEquals(1, backend.uploadCount)
            }
        } finally {
            context.contentResolver.delete(uri, null, null)
        }
    }

    private fun signIn(role: String) {
        SessionManager.getInstance(context).saveSession(AuthResponse(
            "token-uji", Instant.now().plusSeconds(3600).toString(),
            UserResponse(1, "akun-uji", "Pengguna uji", "081234567890", "Alamat uji", role, Instant.now().toString())
        ))
    }

    private fun setApi(api: ApiService) {
        RetrofitClient::class.java.getDeclaredField("apiService").apply { isAccessible = true }.set(null, api)
    }

    private fun uploadFiles(): Set<String> = context.cacheDir.listFiles().orEmpty()
        .filter { it.name.startsWith("alat_") }.map { it.name }.toSet()

    private fun resumedActivity(): Activity? = onMain {
        ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).firstOrNull()
    }

    private fun waitUntil(condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + 8000
        while (!condition()) {
            if (System.currentTimeMillis() > deadline) throw AssertionError("UI did not reach the expected state")
            Thread.sleep(40)
        }
        instrumentation.waitForIdleSync()
    }

    private fun <T> onMain(action: () -> T): T {
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) return action()
        val result = AtomicReference<T>()
        instrumentation.runOnMainSync { result.set(action()) }
        return result.get()
    }

    private fun prepareLayout(root: View) {
        listOf(R.id.progress, R.id.layout_error, R.id.layout_empty, R.id.layout_state).forEach {
            root.findViewById<View>(it)?.visibility = View.GONE
        }
        root.findViewById<View>(R.id.content)?.visibility = View.VISIBLE
        fun populate(view: View) {
            if (view is Chip && view.text.isNullOrBlank()) StatusUtil.apply(view, "tersedia")
            if (view is TextView && view !is MaterialButton && view !is Chip && view.text.isNullOrBlank() && view.id != View.NO_ID) {
                view.text = "Data uji"
            }
            if (view is ImageView && view.drawable == null && !view.contentDescription.isNullOrBlank()) {
                view.setImageResource(R.drawable.ic_phone_placeholder)
            }
            if (view is ViewGroup) for (index in 0 until view.childCount) populate(view.getChildAt(index))
        }
        populate(root)
    }

    private fun checkHorizontalBounds(view: View, layout: String) {
        if (view.visibility != View.VISIBLE) return
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                val child = view.getChildAt(index)
                if (child.visibility != View.VISIBLE) continue
                assertTrue("Horizontal overflow in $layout", child.left >= -1 && child.right <= view.width + 1)
                checkHorizontalBounds(child, layout)
            }
        }
        if (view is MaterialButton && view.visibility == View.VISIBLE) {
            val minimum = (48 * view.resources.displayMetrics.density).toInt()
            assertTrue("Small button in $layout", view.height >= minimum)
        }
    }
}

private class UiTestBackend {
    enum class Mode { NORMAL, EMPTY, ERROR }
    @Volatile var mode = Mode.NORMAL
    @Volatile var rejectUpload = false
    @Volatile var uploadCount = 0
    @Volatile var lastBody = ""
    private val now = Instant.now().toString()
    private val today = LocalDate.now().toString()
    private val user = UserResponse(1, "akun-uji", "Pengguna uji", "081234567890", "Alamat uji", "Admin", now)
    private val phone = AlatResponse(1, 1, "Kategori uji", "HP uji", "Spesifikasi untuk pemeriksaan tampilan.", BigDecimal("125000.5"), null, 3, now)
    private val category = KategoriResponse(1, "Kategori uji")
    private val unit = UnitAlatResponse(1, 1, "HP uji", "UNIT-UJI-01", "tersedia", "baik", now)
    private val rental = PeminjamanDetailResponse(
        1, 1, "akun-uji", "Peminjam uji", 2, "Petugas uji", "diajukan", today,
        LocalDate.now().plusDays(1).toString(), BigDecimal("250000"), now,
        listOf(DetailPeminjamanResponse(1, 1, "HP uji", 1, BigDecimal("125000.5"), listOf(UnitPeminjamanResponse(1, "UNIT-UJI-01"))))
    )
    private val returned = PengembalianDetailResponse(
        1, 1, 1, "akun-uji", "Peminjam uji", 2, "Petugas uji", "selesai", today, today,
        today, 0, BigDecimal.ZERO, "Catatan uji",
        listOf(DetailPengembalianResponse(1, 1, "UNIT-UJI-01", "HP uji", "baik", BigDecimal.ZERO, "Catatan uji"))
    )
    val api: ApiService = Retrofit.Builder().baseUrl("http://localhost/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder().addInterceptor { chain -> respond(chain.request()) }.build())
        .build().create(ApiService::class.java)

    private fun respond(request: Request): Response {
        val path = request.url.encodedPath
        val upload = path.startsWith("/api/alat") && request.method in listOf("POST", "PUT")
        if (upload) {
            uploadCount += 1
            lastBody = Buffer().also { request.body?.writeTo(it) }.readUtf8()
        }
        val failure = mode == Mode.ERROR || (upload && rejectUpload)
        val data: Any? = when {
            failure -> null
            path == "/api/alat" && request.method == "GET" -> page(listOf(phone))
            path.startsWith("/api/alat") -> phone
            path == "/api/kategori" -> page(listOf(category))
            path.startsWith("/api/kategori/") -> category
            path == "/api/users" -> page(listOf(user))
            path.startsWith("/api/users/") -> user
            path == "/api/unit-alat" -> page(listOf(unit))
            path.startsWith("/api/unit-alat/") -> unit
            path == "/api/peminjaman" -> page(listOf(rental))
            path.startsWith("/api/peminjaman/") -> rental
            path == "/api/pengembalian" -> page(listOf(returned))
            path.startsWith("/api/pengembalian/") -> returned
            path == "/api/laporan/peminjaman" -> LaporanPeminjamanResponse(today, today, now, RingkasanPeminjaman(0, 0, BigDecimal.ZERO), emptyList())
            path == "/api/laporan/pengembalian" -> LaporanPengembalianResponse(today, today, now, RingkasanPengembalian(0, 0, BigDecimal.ZERO), emptyList())
            path == "/api/log-aktivitas" -> page(emptyList<Any>())
            else -> user
        }
        val message = if (upload && rejectUpload) UPLOAD_ERROR else if (failure) ERROR_MESSAGE else "Data uji berhasil dimuat"
        val body = Gson().toJson(ApiResponse(!failure, message, data))
        return Response.Builder().request(request).protocol(Protocol.HTTP_1_1)
            .code(if (failure) 400 else 200).message("Test response")
            .body(body.toResponseBody("application/json".toMediaType())).build()
    }

    private fun <T> page(items: List<T>): PagedData<T> {
        val content = if (mode == Mode.EMPTY) emptyList() else items
        return PagedData(content, 1, 20, content.size.toLong())
    }

    companion object {
        const val ERROR_MESSAGE = "Data uji tidak dapat dimuat."
        const val UPLOAD_ERROR = "File gambar tidak valid (uji)."
    }
}
