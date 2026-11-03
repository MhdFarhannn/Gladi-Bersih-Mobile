# API Yang Dipakai

Base URL debug default: `http://10.0.2.2:5257/`.

Semua endpoint selain login dan registrasi memakai header `Authorization: Bearer <token>`. Respons JSON normal memakai wrapper `{ success, message, data }`.

| Method | Endpoint | Parameter utama | Konsumen layar | Role |
|---|---|---|---|---|
| POST | `/api/auth/login` | Body username, password | Login | Publik |
| POST | `/api/auth/register` | Body data Peminjam | Registrasi | Publik |
| POST | `/api/auth/logout` | Tanpa body | Profil | Semua role |
| GET | `/api/users` | page, pageSize, search | Kelola User | Admin |
| GET | `/api/users/{id}` | id | Form User | Admin |
| POST | `/api/users` | Body user | Form User | Admin |
| PUT | `/api/users/{id}` | id, body user | Form User | Admin |
| DELETE | `/api/users/{id}` | id | Kelola User | Admin |
| GET | `/api/kategori` | page, pageSize, search | Kelola Kategori, referensi Form Model | Semua role untuk baca |
| GET | `/api/kategori/{id}` | id | Form Model dan Form Kategori | Semua role untuk baca |
| POST | `/api/kategori` | Body nama | Form Kategori | Admin |
| PUT | `/api/kategori/{id}` | id, body nama | Form Kategori | Admin |
| DELETE | `/api/kategori/{id}` | id | Kelola Kategori | Admin |
| GET | `/api/alat` | page, pageSize, search | Katalog, pilihan sewa, Kelola Model | Semua role |
| GET | `/api/alat/{id}` | id | Detail HP, Form Model, pilihan sewa | Semua role |
| POST | `/api/alat` | Body model HP | Form Model | Admin |
| PUT | `/api/alat/{id}` | id, body model HP | Form Model | Admin |
| DELETE | `/api/alat/{id}` | id | Kelola Model | Admin |
| GET | `/api/unit-alat` | page, pageSize, search | Kelola Unit | Admin, Petugas |
| GET | `/api/unit-alat/{id}` | id | Form Unit | Admin, Petugas |
| POST | `/api/unit-alat` | Body unit | Form Unit | Admin |
| PUT | `/api/unit-alat/{id}` | id, body unit | Form Unit | Admin |
| DELETE | `/api/unit-alat/{id}` | id | Kelola Unit | Admin |
| GET | `/api/peminjaman` | page, pageSize, search, status | Riwayat, antrean, serah-terima, data Admin | Semua role, data disaring backend |
| GET | `/api/peminjaman/{id}` | id | Detail Peminjaman, form pengembalian | Semua role, ownership diperiksa backend |
| POST | `/api/peminjaman` | Body tanggal dan items | Pengajuan Peminjaman | Peminjam |
| PUT | `/api/peminjaman/{id}` | id, body tanggal dan items | Ubah Pengajuan | Peminjam, Admin |
| POST | `/api/peminjaman/{id}/setujui` | id | Detail Antrean | Petugas |
| POST | `/api/peminjaman/{id}/tolak` | id, body alasan | Detail Antrean | Petugas |
| POST | `/api/peminjaman/{id}/serahkan` | id | Serah Terima | Petugas |
| DELETE | `/api/peminjaman/{id}` | id | Detail Data Peminjaman | Admin |
| GET | `/api/pengembalian` | page, pageSize, search | Monitor dan Data Pengembalian | Semua role, data disaring backend |
| GET | `/api/pengembalian/{id}` | id | Detail Pengembalian dan Verifikasi | Semua role, ownership diperiksa backend |
| POST | `/api/pengembalian` | Body peminjaman, tanggal, kondisi unit | Form Pengembalian | Peminjam |
| PUT | `/api/pengembalian/{id}/verifikasi` | id, body tanggal, catatan, kondisi unit | Verifikasi Pengembalian | Petugas, Admin |
| DELETE | `/api/pengembalian/{id}` | id | Detail Data Pengembalian | Admin |
| GET | `/api/laporan/peminjaman` | from, to | Laporan | Petugas, Admin |
| GET | `/api/laporan/pengembalian` | from, to | Laporan | Petugas, Admin |
| GET | `/api/log-aktivitas` | page, pageSize, search | Log Aktivitas | Admin |

Endpoint CSV tidak dipakai aplikasi. Layar laporan memakai respons JSON agar dapat ditampilkan langsung.
