# Laporan Implementasi

## Fitur Yang Selesai

- Setup Kotlin, XML, ViewBinding, Material Components, RecyclerView, Retrofit, OkHttp, Gson, coroutines, Glide, dan SwipeRefreshLayout.
- Session terenkripsi, Bearer interceptor, pemeriksaan expiry, redirect 401, pesan 403, dan logout lokal walaupun API gagal.
- Login, registrasi Peminjam, dan drawer menu berbeda untuk setiap role.
- Komponen daftar berpaginasi dengan loading, empty, error, retry, pencarian, pull-to-refresh, dan load-more.
- Format Rupiah dan tanggal Indonesia serta warna status terpusat.
- Seluruh alur Peminjam yang dapat dipenuhi kontrak backend.
- Antrean, persetujuan, penolakan, serah-terima, monitor pengembalian, verifikasi, dan laporan untuk Petugas.
- CRUD master, data transaksi, laporan, log aktivitas, profil, dan logout untuk Admin.
- Konfirmasi soft delete dan penerusan pesan HTTP 409 dari backend.
- Build debug dan unit test JVM berhasil.

## Asumsi

- Folder Android yang dimaksud adalah `/home/hanverseee/AndroidStudioProjects/GladiMobille`.
- Port backend debug adalah `5257` berdasarkan `launchSettings.json`.
- Tiga file contoh gaya tidak tersedia, jadi struktur mengikuti spesifikasi pengguna dan konvensi Kotlin Android umum.
- Field `deskripsi` dipakai sebagai spesifikasi HP karena DTO tidak memiliki field spesifikasi lain.
- Profil non-Admin menampilkan data login yang tersimpan karena tidak ada endpoint profil diri.
- `imagePath` dianggap URL absolut. Path relatif memakai placeholder karena backend tidak melayani static file.
- Tema terang dipilih untuk keterbacaan meja layanan dan penggunaan inventaris. Aplikasi tidak mengiklankan dark mode.

## Batasan Backend

- Tidak ada status pengembalian `belum diverifikasi`. Pembuatan pengembalian langsung menyelesaikan peminjaman dan mengubah status unit.
- Verifikasi pengembalian dapat diulang. Verifikasi lama berpotensi mengubah status unit yang sudah dialokasikan lagi.
- Menghapus pengembalian tidak memulihkan status peminjaman atau unit dan pengembalian pengganti tidak dapat dibuat.
- Logout, perubahan role, perubahan password, dan soft delete user tidak mencabut JWT yang sudah terbit.
- Persetujuan dapat sudah commit tetapi tetap menghasilkan HTTP 500 jika pencatatan log berikutnya gagal.
- Alasan penolakan tidak ada pada DTO peminjaman dan hanya masuk log Admin.
- Tidak ada upload atau static image endpoint.
- Laporan JSON tidak berpaginasi. Aplikasi membatasi periode interaktif maksimal 366 hari untuk mengurangi risiko data berlebihan.

## Batasan Mobile

- Alur runtime belum diuji karena backend dan emulator tidak aktif saat pemeriksaan.
- Filter kategori katalog tidak ditampilkan karena endpoint alat tidak menerima `idKategori`.
- Filter `belum diverifikasi` tidak ditampilkan karena backend tidak memiliki field atau status tersebut.
- CSV laporan tidak diunduh oleh aplikasi; laporan memakai endpoint JSON.
- Ikon launcher memakai placeholder geometris berbentuk HP dan perlu identitas final dari pemilik produk.
- Aplikasi ditujukan untuk build debug HTTP lokal. Deployment produksi memerlukan base URL HTTPS.

## Keputusan Desain

- Palet hijau hutan dipakai untuk identitas inventaris dan hierarki tindakan utama, bukan dekorasi.
- Amber hanya dipakai pada status menunggu agar perhatian tidak tersebar ke seluruh layar.
- Layout daftar dibuat linear dan padat karena tugas utama pengguna adalah memindai dan memproses data.
- Typeface sistem Android dipakai untuk keterbacaan, konsistensi platform, dan dukungan ukuran teks.
- Skala jarak berbasis 8dp menjaga ritme, sementara semua target sentuh utama minimal 48dp.
- Kartu hanya dipakai untuk unit data atau produk yang memang perlu dikelompokkan; state dan form tetap datar.
- Motion dibatasi pada feedback komponen Material karena proses operasional tidak membutuhkan animasi dekoratif.
- Dial visual: ENERGY 2, RHYTHM 1, MOTION 1.

## Roadmap

1. Jalankan backend, siapkan akun tiap role, lalu eksekusi seluruh skenario integrasi.
2. Uji pada emulator 320dp, perangkat fisik, font besar, TalkBack, keyboard, dan kondisi jaringan lambat.
3. Perbaiki state machine pengembalian dan revokasi JWT di backend sebelum penggunaan nyata.
4. Tambahkan endpoint media resmi atau kontrak URL gambar HTTPS.
5. Tambahkan pagination laporan jika backend menyediakannya.
6. Ganti launcher placeholder setelah pemilik menentukan logo final.
