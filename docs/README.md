# Rental HP Android

Aplikasi Android untuk sistem rental HP dengan tiga role: `Admin`, `Petugas`, dan `Peminjam`. Aplikasi memakai REST API dari proyek ASP.NET Core di `/home/hanverseee/Projects/Gladi-Bersih`.

## Kebutuhan

- Android Studio dengan Android SDK 37
- JDK 17 atau lebih baru
- Emulator Android atau perangkat fisik dengan minimum Android 7.0 (API 24)
- Backend Gladi-Bersih dan MySQL

## Menjalankan Backend

1. Buat database dan jalankan `query.sql`.
2. Jalankan `database/query_objek.sql` setelah schema utama. Prosedur, function, trigger, dan view di file ini wajib untuk persetujuan, pengembalian, denda, dan laporan.
3. Atur connection string dan JWT backend melalui konfigurasi lokal yang aman.
4. Jalankan backend pada port `5257`.

Untuk emulator, alamat default aplikasi adalah `http://10.0.2.2:5257/`.

Untuk perangkat fisik, jalankan backend agar mendengar pada alamat LAN, izinkan port pada firewall, lalu build dengan IP laptop:

```bash
dotnet run --urls http://0.0.0.0:5257
./gradlew assembleDebug -PAPI_BASE_URL=http://192.168.1.10:5257/
```

`API_BASE_URL` disimpan menjadi satu `BuildConfig.BASE_URL`. URL tidak tersebar di source Kotlin.

## Build Dan Test

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

APK debug berada di `app/build/outputs/apk/debug/app-debug.apk`.

## Akun Uji

Backend tidak menanam akun atau password di database.

- Admin pertama dibuat melalui `POST /api/bootstrap/admin` saat backend memakai environment Development.
- Peminjam dibuat melalui layar registrasi atau `POST /api/auth/register`.
- Petugas dibuat oleh Admin melalui menu Kelola User.
- Kredensial tidak disimpan di source Android maupun dokumentasi ini.

## Layar Peminjam

- Login dan registrasi
- Katalog HP berpaginasi, pencarian, stok, harga, dan gambar
- Detail HP
- Pengajuan peminjaman multi-model dan jumlah per model
- Riwayat dan detail peminjaman
- Ubah pengajuan selama status masih `diajukan`
- Pengembalian per unit dengan kondisi dan catatan
- Detail keterlambatan dan denda dari server
- Profil dan logout

## Layar Petugas

- Antrean pengajuan `diajukan`
- Detail, setujui, dan tolak pengajuan
- Daftar serah-terima `disetujui`
- Monitor dan detail pengembalian
- Verifikasi kondisi pengembalian
- Laporan peminjaman dan pengembalian per periode
- Profil dan logout

## Layar Admin

- CRUD user
- CRUD kategori
- CRUD model HP
- CRUD unit fisik
- Data dan detail peminjaman
- Koreksi pengajuan yang masih `diajukan` dan soft delete sesuai aturan backend
- Data, verifikasi, dan soft delete pengembalian
- Laporan per periode
- Log aktivitas berpaginasi
- Profil dan logout

## Catatan Kontrak

- Tanggal request memakai `yyyy-MM-dd`.
- Mata uang dikirim sebagai angka decimal dan ditampilkan sebagai Rupiah.
- `imagePath` harus berupa URL absolut `http` atau `https`. Backend tidak menyediakan upload atau static file hosting.
- Registrasi selalu menghasilkan role `Peminjam` dan tidak langsung login.
- Token tidak memiliki refresh token. Pengguna harus login ulang setelah masa berlaku habis.
- Debug mengizinkan HTTP untuk backend lokal. Gunakan HTTPS sebelum distribusi produksi.
