# SIGMA-48 Mission Management System

SIGMA-48 adalah sebuah aplikasi desktop prototipe yang dirancang untuk manajemen operasi intelijen. Aplikasi ini memfasilitasi berbagai peran dalam sebuah agensi, mulai dari Direktur, Analis, Komandan Operasi, hingga Agen Lapangan, dalam mengelola misi, target, dan pelaporan.

## Cara Menjalankan Aplikasi

### Prasyarat

1.  **Java Development Kit (JDK)**: Versi 21 atau yang lebih baru.
2.  **JavaFX SDK**: Aplikasi ini menggunakan JavaFX. Pastikan Anda telah mengunduh SDK yang sesuai dengan versi JDK dan sistem operasi Anda.
3.  **IDE (Integrated Development Environment)**: **Direkomendasikan** menggunakan IntelliJ IDEA atau Eclipse.
4.  **Dependensi**:
    * **jBCrypt**: Untuk hashing password.
    * **Jackson**: Untuk serialisasi/deserialisasi data dari/ke file JSON.
    * **Lombok**: Untuk mengurangi boilerplate code pada model.

### Langkah-langkah Menjalankan

1.  **Clone Repository**: Unduh atau clone seluruh kode sumber ke komputer lokal Anda.
2.  **Buka di IDE**: Buka proyek sebagai proyek Java di IDE pilihan Anda.
3.  **Konfigurasi JavaFX**:
    * Tambahkan JavaFX SDK sebagai Global Library di IDE Anda.
    * Konfigurasikan *VM Options* untuk *Run Configuration* dari kelas `Main`. Tambahkan argumen berikut (sesuaikan path ke JavaFX SDK Anda):
        ```
        --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml,javafx.media
        ```
4.  **Jalankan Aplikasi**:
    * Temukan file `Main.java` di dalam paket `com.sigma48`.
    * Jalankan method `main` dari kelas tersebut.
5.  **Login**: Aplikasi akan menampilkan layar login. Gunakan salah satu kredensial default yang ada di `data/users.json` untuk masuk sesuai dengan peran yang diinginkan. Contoh user default adalah `4899` dengan password `pass123`.

## Struktur Kode

Struktur kode aplikasi ini dibagi ke dalam beberapa paket utama yang mengikuti arsitektur berlapis (*Layered Architecture*) untuk memisahkan antara presentasi, logika bisnis, dan akses data.

* `com.sigma48`
    * `Main.java`: Titik masuk utama aplikasi JavaFX. Bertugas memuat font, view login awal, dan CSS.
    * `ServiceLocator.java`: Mengimplementasikan pola *Service Locator* untuk menyediakan akses global yang terpusat ke semua kelas *Manager* dan *DAO*.
* `dao`: **Data Access Object (DAO)**. Lapisan ini bertanggung jawab penuh untuk berinteraksi dengan sumber data, yaitu file-file JSON.
    * `base/GenericDao.java`: Kelas abstrak yang menyediakan fungsionalitas dasar CRUD (Create, Read, Update, Delete) untuk file JSON, yang kemudian di-extend oleh DAO spesifik.
    * Contoh: `UserDao.java`, `MissionDao.java`, `TargetDao.java`.
* `manager`: **Logic/Business Layer**. Paket ini berisi logika bisnis aplikasi. Kelas-kelas Manager mengoordinasikan operasi, memvalidasi data, dan menjadi jembatan antara Controller (UI) dan DAO (Data).
    * Contoh: `AuthManager.java` (mengelola login/logout), `MissionManager.java` (mengelola semua logika terkait misi), `UserManager.java` (mengelola pengguna).
* `model`: **Data Model**. Berisi kelas-kelas POJO (Plain Old Java Object) yang merepresentasikan entitas utama dalam aplikasi.
    * Contoh: `User.java`, `Mission.java`, `Target.java`, `Report.java`.
* `ui`
    * `controller`: **Presentation Layer (Controller)**. Kelas-kelas controller yang terhubung dengan file FXML. Bertugas menangani interaksi pengguna, mengambil data dari *Manager*, dan menampilkannya di UI.
    * `dto`: **Data Transfer Object**. Kelas yang digunakan untuk membawa data yang sudah diformat khusus untuk ditampilkan di UI.
* `util`: Berisi kelas-kelas utilitas untuk fungsionalitas umum seperti hashing password (`PasswordUtils.java`), manajemen zoom gambar (`ImageViewZoomManager.java`), dan memutar suara (`SoundUtils.java`).
* `resources`
    * `com/sigma48/fxml`: Berisi file-file FXML yang mendefinisikan tata letak antarmuka pengguna.
    * `com/sigma48/css`: Berisi file CSS (`theme.css`) untuk styling aplikasi.
    * `com/sigma48/images`, `sounds`, `fonts`: Aset-aset lain yang digunakan oleh aplikasi.

## Penerapan Pilar OOP

Aplikasi ini dibangun dengan menerapkan empat pilar utama Pemrograman Berorientasi Objek (OOP):

### 1. Enkapsulasi (Encapsulation)

Enkapsulasi adalah konsep menyembunyikan detail implementasi dan hanya mengekspos fungsionalitas yang diperlukan.

* **Model Data**: Semua kelas di paket `model` (seperti `User`, `Mission`, dan `Target`) mendeklarasikan *field* mereka sebagai `private`. Akses ke *field* ini dikontrol melalui metode *getter* dan *setter* publik (sebagian besar diotomatisasi oleh anotasi Lombok seperti `@Data`).
* **Manager Classes**: Kelas seperti `AuthManager` menyembunyikan detail sesi pengguna (`currentUser`). Kelas lain tidak dapat mengubah `currentUser` secara langsung, melainkan harus melalui metode publik seperti `login()` dan `logout()`. Ini memastikan bahwa keadaan sesi tetap konsisten.

### 2. Pewarisan (Inheritance)

Pewarisan memungkinkan sebuah kelas (subclass) untuk mewarisi properti dan metode dari kelas lain (superclass).

* **`Agent extends User`**: Kelas `Agent` adalah turunan dari kelas `User`. `Agent` mewarisi semua atribut dasar pengguna (seperti `id`, `username`, `passwordHash`) dan menambahkan fungsionalitas spesifiknya sendiri, yaitu `spesialisasi`.
* **`BaseController`**: Sejumlah besar controller UI (misalnya, `AnalisOverviewController`, `KomandanOverviewController`, `AgentMissionsViewController`) mewarisi kelas `BaseController`. Ini memungkinkan mereka untuk mewarisi referensi ke `MainDashboardController` dan metode utilitas umum seperti `showStatus()`, mengurangi duplikasi kode.

### 3. Polimorfisme (Polymorphism)

Polimorfisme memungkinkan objek dari kelas yang berbeda untuk merespons pesan (pemanggilan metode) yang sama dengan cara yang berbeda.

* **Deserialisasi JSON dengan `@JsonSubTypes`**: Di kelas `User`, anotasi `@JsonTypeInfo` dan `@JsonSubTypes` digunakan. Ini memungkinkan library Jackson untuk membuat objek yang benar (`User` atau `Agent`) saat membaca data dari `users.json`, meskipun semuanya dibaca sebagai bagian dari satu daftar `User`. Ini adalah contoh polimorfisme saat runtime.
* **Penggunaan `BaseController`**: `MainDashboardController` memiliki metode yang berinteraksi dengan berbagai jenis controller melalui referensi `BaseController`. Misalnya, saat memuat view, ia memanggil `setMainDashboardController()` pada controller yang baru dimuat, terlepas dari apa pun kelas konkret controller tersebut, selama itu adalah turunan dari `BaseController`.

### 4. Abstraksi (Abstraction)

Abstraksi berfokus pada penyembunyian detail implementasi yang kompleks dan hanya menampilkan fungsionalitas esensial kepada pengguna.

* **`GenericDao<T>`**: Kelas abstrak `GenericDao` menyediakan "kontrak" untuk semua kelas DAO. Ia mendefinisikan metode umum seperti `getAll()`, `save()`, dan `delete()`, menyembunyikan detail rumit dari operasi file I/O dan manipulasi JSON. Subkelas seperti `UserDao` dan `MissionDao` hanya perlu menyediakan implementasi spesifik tanpa perlu menulis ulang logika pembacaan/penulisan file.
* **Lapisan *Manager***: Lapisan ini secara keseluruhan adalah bentuk abstraksi. Misalnya, ketika sebuah controller UI memanggil `missionManager.approveDraftAndAssignCommander()`, ia tidak perlu tahu langkah-langkah detail seperti "cari misi", "validasi status", "ubah status", "set komandan ID", dan "simpan kembali ke file". Semua kompleksitas itu diabstraksikan di dalam metode *manager*.