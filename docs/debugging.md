# Catatan Debugging

| Masalah | Penyebab | Perbaikan | Verifikasi |
|---|---|---|---|
| Namespace template tidak sesuai | Proyek awal memakai `com.app.gladimobille` | Namespace, applicationId, source package, dan test diubah menjadi `com.contoh.rentalhp` | `assembleDebug` berhasil |
| Referensi `menu_home` gagal dikompilasi | Menu placeholder dihapus setelah menu role lengkap dibuat | Routing placeholder dan referensi ID lama dihapus | Build berikutnya berhasil |
| Token berpotensi disimpan plaintext | EncryptedSharedPreferences sebelumnya memiliki fallback SharedPreferences biasa | Fallback plaintext dihapus, storage rusak dihapus lalu dibuat ulang secara terenkripsi, backup aplikasi dinonaktifkan | Build dan inspeksi source |
| Pagination dapat mencampur query | Halaman berikut membaca teks search yang belum dikirim | Query aktif dibekukan saat tombol Cari ditekan, request lama dibatalkan saat reset, respons stale diabaikan, ID dideduplikasi | Unit kompilasi dan inspeksi source |
| Fragment dapat menyentuh binding setelah dibatalkan | `Throwable` menangkap CancellationException dan blok finally memakai binding | CancellationException diteruskan dan akses binding pada finally dibuat nullable | Build berhasil |
| Autocomplete dapat memilih ID salah | Posisi item berasal dari adapter terfilter tetapi digunakan pada list asal | Item dicocokkan kembali berdasarkan label respons dan debounce menjalankan request yang dapat dibatalkan | Build berhasil |
| Konten dapat tertutup system bar atau keyboard | Target SDK 37 memaksa edge-to-edge | BaseActivity menerapkan inset system bar, cutout, navigation bar, dan IME pada root activity | Build berhasil, runtime belum diuji |
| Test instrumentasi template salah | Test lama masih mengharapkan package template | Test dipindah dan assertion diubah ke `com.contoh.rentalhp` | Kompilasi unit test berhasil; instrumentasi belum dijalankan |
| Backend lokal tidak dapat dihubungi | Tidak ada proses pada port `5257` saat pemeriksaan | Tidak ada perubahan backend; hasil dicatat sebagai batas pengujian | `curl` menghasilkan HTTP `000` |
| Emulator tidak tersedia | `adb devices` tidak menampilkan device | Tidak membuat klaim uji UI | Dicatat di laporan pengujian |
| Peringatan lint aplikasi | Lint awal menemukan resource template, label dropdown, dan resource plural | Resource lama dihapus, label aksesibilitas ditambahkan, dan jumlah memakai plurals | Lint akhir 0 error; 9 warning hanya versi dependency |

## Cara Mendiagnosis API

- Pastikan backend aktif pada port yang sama dengan `BuildConfig.BASE_URL`.
- Emulator memakai `10.0.2.2`, bukan `localhost`.
- Perangkat fisik memakai IP LAN laptop dan backend harus bind ke `0.0.0.0` atau IP LAN.
- Buka `/swagger/v1/swagger.json` saat backend memakai environment Development.
- Logging OkHttp hanya level BASIC pada debug. Header Authorization dan body password tidak dicatat.
