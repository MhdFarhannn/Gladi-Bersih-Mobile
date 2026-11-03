package com.contoh.rentalhp.data.api

import com.contoh.rentalhp.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<UserResponse>

    @POST("api/auth/logout")
    suspend fun logout(): ApiResponse<Any>

    @GET("api/users")
    suspend fun getUsers(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("search") search: String? = null
    ): ApiResponse<PagedData<UserResponse>>

    @GET("api/users/{id}")
    suspend fun getUser(@Path("id") id: Long): ApiResponse<UserResponse>

    @POST("api/users")
    suspend fun createUser(@Body request: UserCreateRequest): ApiResponse<UserResponse>

    @PUT("api/users/{id}")
    suspend fun updateUser(
        @Path("id") id: Long,
        @Body request: UserUpdateRequest
    ): ApiResponse<UserResponse>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: Long): ApiResponse<Any>

    @GET("api/kategori")
    suspend fun getKategori(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("search") search: String? = null
    ): ApiResponse<PagedData<KategoriResponse>>

    @GET("api/kategori/{id}")
    suspend fun getKategoriDetail(@Path("id") id: Long): ApiResponse<KategoriResponse>

    @POST("api/kategori")
    suspend fun createKategori(@Body request: KategoriRequest): ApiResponse<KategoriResponse>

    @PUT("api/kategori/{id}")
    suspend fun updateKategori(
        @Path("id") id: Long,
        @Body request: KategoriRequest
    ): ApiResponse<KategoriResponse>

    @DELETE("api/kategori/{id}")
    suspend fun deleteKategori(@Path("id") id: Long): ApiResponse<Any>

    @GET("api/alat")
    suspend fun getAlat(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("search") search: String? = null
    ): ApiResponse<PagedData<AlatResponse>>

    @GET("api/alat/{id}")
    suspend fun getAlatDetail(@Path("id") id: Long): ApiResponse<AlatResponse>

    @Multipart
    @POST("api/alat")
    suspend fun createAlat(
        @Part("idKategori") idKategori: RequestBody,
        @Part("nama") nama: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody,
        @Part("hargaSewa") hargaSewa: RequestBody,
        @Part image: MultipartBody.Part? = null
    ): ApiResponse<AlatResponse>

    @Multipart
    @PUT("api/alat/{id}")
    suspend fun updateAlat(
        @Path("id") id: Long,
        @Part("idKategori") idKategori: RequestBody,
        @Part("nama") nama: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody,
        @Part("hargaSewa") hargaSewa: RequestBody,
        @Part image: MultipartBody.Part? = null
    ): ApiResponse<AlatResponse>

    @DELETE("api/alat/{id}")
    suspend fun deleteAlat(@Path("id") id: Long): ApiResponse<Any>

    @GET("api/unit-alat")
    suspend fun getUnitAlat(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("search") search: String? = null
    ): ApiResponse<PagedData<UnitAlatResponse>>

    @GET("api/unit-alat/{id}")
    suspend fun getUnitAlatDetail(@Path("id") id: Long): ApiResponse<UnitAlatResponse>

    @POST("api/unit-alat")
    suspend fun createUnitAlat(@Body request: UnitAlatCreateRequest): ApiResponse<UnitAlatResponse>

    @PUT("api/unit-alat/{id}")
    suspend fun updateUnitAlat(
        @Path("id") id: Long,
        @Body request: UnitAlatUpdateRequest
    ): ApiResponse<UnitAlatResponse>

    @DELETE("api/unit-alat/{id}")
    suspend fun deleteUnitAlat(@Path("id") id: Long): ApiResponse<Any>

    @GET("api/peminjaman")
    suspend fun getPeminjaman(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): ApiResponse<PagedData<PeminjamanResponse>>

    @GET("api/peminjaman/{id}")
    suspend fun getPeminjamanDetail(@Path("id") id: Long): ApiResponse<PeminjamanDetailResponse>

    @POST("api/peminjaman")
    suspend fun createPeminjaman(@Body request: PeminjamanRequest): ApiResponse<PeminjamanDetailResponse>

    @PUT("api/peminjaman/{id}")
    suspend fun updatePeminjaman(
        @Path("id") id: Long,
        @Body request: PeminjamanRequest
    ): ApiResponse<PeminjamanDetailResponse>

    @POST("api/peminjaman/{id}/setujui")
    suspend fun approvePeminjaman(@Path("id") id: Long): ApiResponse<PeminjamanDetailResponse>

    @POST("api/peminjaman/{id}/tolak")
    suspend fun rejectPeminjaman(
        @Path("id") id: Long,
        @Body request: PenolakanPeminjamanRequest
    ): ApiResponse<PeminjamanDetailResponse>

    @POST("api/peminjaman/{id}/serahkan")
    suspend fun handoverPeminjaman(@Path("id") id: Long): ApiResponse<PeminjamanDetailResponse>

    @DELETE("api/peminjaman/{id}")
    suspend fun deletePeminjaman(@Path("id") id: Long): ApiResponse<Any>

    @GET("api/pengembalian")
    suspend fun getPengembalian(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("search") search: String? = null
    ): ApiResponse<PagedData<PengembalianResponse>>

    @GET("api/pengembalian/{id}")
    suspend fun getPengembalianDetail(@Path("id") id: Long): ApiResponse<PengembalianDetailResponse>

    @POST("api/pengembalian")
    suspend fun createPengembalian(@Body request: PengembalianRequest): ApiResponse<PengembalianDetailResponse>

    @PUT("api/pengembalian/{id}/verifikasi")
    suspend fun verifyPengembalian(
        @Path("id") id: Long,
        @Body request: VerifikasiPengembalianRequest
    ): ApiResponse<PengembalianDetailResponse>

    @DELETE("api/pengembalian/{id}")
    suspend fun deletePengembalian(@Path("id") id: Long): ApiResponse<Any>

    @GET("api/laporan/peminjaman")
    suspend fun getLaporanPeminjaman(
        @Query("from") from: String,
        @Query("to") to: String
    ): ApiResponse<LaporanPeminjamanResponse>

    @GET("api/laporan/pengembalian")
    suspend fun getLaporanPengembalian(
        @Query("from") from: String,
        @Query("to") to: String
    ): ApiResponse<LaporanPengembalianResponse>

    @GET("api/log-aktivitas")
    suspend fun getLogAktivitas(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("search") search: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): ApiResponse<PagedData<LogAktivitasResponse>>
}
