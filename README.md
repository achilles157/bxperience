# ![Byebeli Experience Admin System](https://yourdomain.com/logo.png)

Byebeli Experience Admin System adalah aplikasi berbasis Java yang dirancang untuk mengelola penyewaan alat gaming, pemantauan aset, dan pengalaman pelanggan. Sistem ini menyediakan fitur lengkap untuk administrasi penyewaan, manajemen aset, dan pelaporan.

---

## 🧩 Fitur Utama

- 🎮 **Penyewaan Play At Home**  
  Kelola penyewaan alat gaming seperti konsol, PC, dan VR set. Termasuk fitur pemilihan barang, perhitungan harga otomatis, dan pelacakan status.

- 📋 **Form Booking Experience**  
  Formulir pemesanan pengalaman bermain dengan validasi dan perhitungan biaya berdasarkan durasi.

- 📦 **Manajemen Aset**  
  Kelola inventaris alat gaming beserta status ketersediaan dan harga sewanya.

- 🧠 **Monitoring & Dashboard**  
  Laporan visual dan statistik penyewaan serta ketersediaan aset.

- 🔐 **Login Admin**  
  Sistem login dengan proteksi akses berbasis hak admin.

---

## 🛠️ Instalasi & Menjalankan Proyek

### Persyaratan

- Java JDK 8 atau lebih baru
- NetBeans IDE (direkomendasikan karena proyek menggunakan `nbproject`)
- Apache Ant (opsional untuk build manual)

### Cara Menjalankan

1. **Clone atau Extract Repository**
   ```bash
   unzip bxperience.zip
   cd bxperience/bxperience
   ```

2. **Import ke NetBeans**
   - Buka NetBeans
   - Pilih `File > Open Project`
   - Arahkan ke folder `bxperience/bxperience`

3. **Jalankan Proyek**
   - Klik kanan pada project > `Run`

---

## 📁 Struktur Proyek

```
bxperience/
├── manifest.mf
├── build.xml                # Skrip build dengan Ant
├── nbproject/               # Metadata proyek NetBeans
│   ├── project.xml
│   └── build-impl.xml
├── src/                     # Kode sumber aplikasi Java
│   └── ... (form, backend, logic)
├── dist/                    # Folder output setelah build
└── README.md
```

---

## 📚 Library & Dependensi Eksternal

Proyek ini sebagian besar menggunakan Java SE (Standard Edition), namun tergantung modul tertentu, dapat menggunakan:

- **Swing** – Untuk pembuatan UI desktop
- **JavaBeans Binding (org.jdesktop.beansbinding)** – Untuk binding antar komponen UI dan data
- **JDatePicker atau JCalendar** – Jika digunakan, untuk input tanggal
- **JConnector** – Untuk koneksi ke database
- **Apache Commons (opsional)** – Untuk utilitas string, IO, dll.

> *Catatan:* Pastikan semua `.jar` tambahan diletakkan di folder `lib/` (jika tersedia) dan di-link melalui project properties di NetBeans.

---

## 🧪 Testing

Testing manual dilakukan melalui antarmuka aplikasi. Belum tersedia integrasi unit test otomatis. Disarankan untuk menggunakan:

- **JUnit** – Untuk membuat unit test jika proyek akan dikembangkan lebih lanjut.

---

## 📌 Catatan Tambahan

- Disarankan menjalankan aplikasi dengan resolusi layar minimal 1366x768.
- Aplikasi ini bersifat *offline*, tidak terhubung ke database cloud atau API eksternal.

---

## 👥 Kontributor

- Project Manager   : Falah Fahrurozi & Maisa Suciyanti
- Desainer UI/UX    : Selvia Destri Andani, Dewi Sri Rahayu, & Arief Fachrie Rachman
- DB Developer      : Septio Yasin Tiaratomo
- System Developer  : Falah Fahrurozi

---
