# Pengujian Mobile

Tanggal pemeriksaan: 3 November 2026.

Lingkungan saat pemeriksaan:

- `./gradlew assembleDebug`: berhasil.
- `./gradlew testDebugUnitTest`: berhasil, 7 test lulus.
- `./gradlew compileDebugAndroidTestKotlin`: berhasil.
- `./gradlew lintDebug`: berhasil dengan 0 error dan 9 peringatan versi dependency.
- Backend `127.0.0.1:5257`: tidak aktif, `curl` menghasilkan HTTP `000`.
- `adb devices`: tidak menemukan emulator atau perangkat.

| Kasus | Langkah | Hasil yang diharapkan | Hasil aktual | Status |
|---|---|---|---|---|
| Build debug | Jalankan `./gradlew assembleDebug` | APK debug terbentuk tanpa error kompilasi | Build berhasil dan APK terbentuk | Lulus |
| Unit test | Jalankan `./gradlew testDebugUnitTest` | Test format, telepon, dan serialisasi lulus | 7 test lulus | Lulus |
| Lint debug | Jalankan `./gradlew lintDebug` | Tidak ada error lint | 0 error; 9 warning hanya memberi tahu versi dependency yang lebih baru | Lulus |
| Kompilasi test instrumentasi | Jalankan `./gradlew compileDebugAndroidTestKotlin` | Source androidTest dapat dikompilasi | Kompilasi berhasil; test tidak dijalankan karena tidak ada device | Lulus kompilasi |
| Login benar | Masukkan username dan password valid | Sesi tersimpan dan menu sesuai role terbuka | Belum dijalankan karena backend dan emulator tidak aktif | Belum diuji |
| Login salah | Masukkan password salah | Pesan backend tampil dan sesi tidak dibuat | Belum dijalankan karena backend dan emulator tidak aktif | Belum diuji |
| Menu berbeda per role | Login sebagai Admin, Petugas, dan Peminjam | Drawer hanya berisi menu role terkait | Routing dan resource menu diverifikasi dari source, tetapi belum diklik pada perangkat | Diverifikasi statis |
| Sesi kedaluwarsa 401 | Gunakan token kedaluwarsa lalu buka data | Sesi dibersihkan dan kembali ke login dengan pesan `Session expired` | Interceptor dan event sesi diverifikasi dari source, belum diuji terhadap API | Belum diuji |
| Pengajuan valid | Pilih HP tersedia, jumlah valid, dan tanggal valid | Server membuat peminjaman `diajukan` | Belum dijalankan karena backend dan emulator tidak aktif | Belum diuji |
| Pengajuan melebihi stok | Pilih jumlah di atas stok | UI mencegah jumlah dan server tetap menjadi otoritas akhir | Kontrol jumlah dan validasi source tersedia, belum diuji dengan data API | Belum diuji |
| Setujui lalu serahkan | Petugas menyetujui pengajuan lalu menyerahkan unit | Status berubah `diajukan` ke `disetujui` lalu `dipinjam` | Belum dijalankan karena backend dan emulator tidak aktif | Belum diuji |
| Pengembalian terlambat | Kirim tanggal kembali setelah rencana | Detail menampilkan hari terlambat dan denda dari server | Belum dijalankan karena backend dan emulator tidak aktif | Belum diuji |
| Penghapusan ditolak 409 | Hapus kategori, model, unit, atau data yang masih dipakai | Pesan alasan dari backend tampil apa adanya | Parsing pesan 409 diverifikasi dari source, respons nyata belum tersedia | Belum diuji |
| Tidak ada koneksi | Matikan backend lalu buka daftar | UI menampilkan `Cannot connect to server` dan tombol coba lagi | Backend dipastikan tidak dapat dihubungi dengan HTTP `000`; UI tidak dapat dijalankan karena tidak ada emulator | Sebagian |
| Registrasi Peminjam | Isi data valid dan kirim | Akun Peminjam dibuat lalu pengguna kembali ke login | Belum dijalankan karena backend dan emulator tidak aktif | Belum diuji |
| Pagination dan refresh | Gulir lebih dari 20 data lalu tarik untuk refresh | Halaman berikut ditambahkan tanpa duplikasi dan refresh kembali ke halaman satu | Implementasi diverifikasi dari source, belum diuji dengan data API | Belum diuji |

Status `Diverifikasi statis` dan `Sebagian` bukan klaim bahwa alur runtime berhasil.
