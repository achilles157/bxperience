-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Waktu pembuatan: 12 Nov 2025 pada 05.32
-- Versi server: 10.4.32-MariaDB
-- Versi PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `bxperience`
--

-- --------------------------------------------------------

--
-- Struktur dari tabel `aset`
--

CREATE TABLE `aset` (
  `id_aset` varchar(20) NOT NULL,
  `nama_barang` varchar(100) NOT NULL,
  `kode_barang` varchar(20) NOT NULL,
  `kategori` varchar(50) NOT NULL,
  `deskripsi` text DEFAULT NULL,
  `harga_sewa_menit` decimal(10,2) NOT NULL CHECK (`harga_sewa_menit` >= 0),
  `harga_sewa_hari` decimal(10,2) NOT NULL CHECK (`harga_sewa_hari` >= 0),
  `status_tersedia` tinyint(1) DEFAULT 0,
  `status_disewakan` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Struktur dari tabel `booking`
--

CREATE TABLE `booking` (
  `id_booking` int(11) NOT NULL,
  `nama` varchar(100) DEFAULT NULL,
  `instagram` varchar(100) DEFAULT NULL,
  `no_hp` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `tanggal` date DEFAULT NULL,
  `jam` time DEFAULT NULL,
  `durasi_menit` int(11) DEFAULT NULL,
  `total_harga` decimal(10,2) DEFAULT NULL,
  `diskon` decimal(10,2) DEFAULT 0.00,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `status` enum('pending','confirmed','completed','cancelled') DEFAULT 'pending'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Struktur dari tabel `booking_detail`
--

CREATE TABLE `booking_detail` (
  `id_detail` int(11) NOT NULL,
  `id_booking` int(11) DEFAULT NULL,
  `id_aset` varchar(20) DEFAULT NULL,
  `jumlah` int(11) DEFAULT NULL,
  `add_on` tinyint(1) DEFAULT NULL,
  `subtotal` decimal(10,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Struktur dari tabel `daily_revenue`
--

CREATE TABLE `daily_revenue` (
  `id` int(11) NOT NULL,
  `date` date NOT NULL,
  `total_revenue` decimal(15,2) NOT NULL DEFAULT 0.00,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Struktur dari tabel `playathome`
--

CREATE TABLE `playathome` (
  `id_playhome` int(11) NOT NULL,
  `nama` varchar(100) DEFAULT NULL,
  `alamat_lengkap` text DEFAULT NULL,
  `no_telp` varchar(20) DEFAULT NULL,
  `tgl_mulai` date DEFAULT NULL,
  `tgl_selesai` date DEFAULT NULL,
  `metode_pengambilan` varchar(20) DEFAULT NULL,
  `alamat_antar` text DEFAULT NULL,
  `alamat_kembali` text DEFAULT NULL,
  `keperluan` text DEFAULT NULL,
  `nama_kurir` varchar(100) DEFAULT NULL,
  `no_telp_kurir` varchar(20) DEFAULT NULL,
  `ongkir` decimal(10,2) DEFAULT NULL,
  `diskon` decimal(10,2) DEFAULT 0.00,
  `total_harga` decimal(10,2) DEFAULT NULL,
  `status` enum('aktif','selesai') DEFAULT 'aktif'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Struktur dari tabel `playathome_detail`
--

CREATE TABLE `playathome_detail` (
  `id_detail` int(11) NOT NULL,
  `id_playhome` int(11) DEFAULT NULL,
  `id_aset` varchar(20) DEFAULT NULL,
  `jumlah` int(11) DEFAULT NULL,
  `subtotal` decimal(10,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Struktur dari tabel `user_login`
--

CREATE TABLE `user_login` (
  `id_user` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Indexes for dumped tables
--

--
-- Indeks untuk tabel `aset`
--
ALTER TABLE `aset`
  ADD PRIMARY KEY (`id_aset`),
  ADD UNIQUE KEY `kode_barang` (`kode_barang`);

--
-- Indeks untuk tabel `booking`
--
ALTER TABLE `booking`
  ADD PRIMARY KEY (`id_booking`);

--
-- Indeks untuk tabel `booking_detail`
--
ALTER TABLE `booking_detail`
  ADD PRIMARY KEY (`id_detail`),
  ADD KEY `id_booking` (`id_booking`),
  ADD KEY `id_aset` (`id_aset`);

--
-- Indeks untuk tabel `daily_revenue`
--
ALTER TABLE `daily_revenue`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_date` (`date`);

--
-- Indeks untuk tabel `playathome`
--
ALTER TABLE `playathome`
  ADD PRIMARY KEY (`id_playhome`);

--
-- Indeks untuk tabel `playathome_detail`
--
ALTER TABLE `playathome_detail`
  ADD PRIMARY KEY (`id_detail`),
  ADD KEY `id_playhome` (`id_playhome`),
  ADD KEY `id_aset` (`id_aset`);

--
-- Indeks untuk tabel `user_login`
--
ALTER TABLE `user_login`
  ADD PRIMARY KEY (`id_user`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT untuk tabel yang dibuang
--

--
-- AUTO_INCREMENT untuk tabel `booking`
--
ALTER TABLE `booking`
  MODIFY `id_booking` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT untuk tabel `booking_detail`
--
ALTER TABLE `booking_detail`
  MODIFY `id_detail` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT untuk tabel `daily_revenue`
--
ALTER TABLE `daily_revenue`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT untuk tabel `playathome`
--
ALTER TABLE `playathome`
  MODIFY `id_playhome` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT untuk tabel `playathome_detail`
--
ALTER TABLE `playathome_detail`
  MODIFY `id_detail` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT untuk tabel `user_login`
--
ALTER TABLE `user_login`
  MODIFY `id_user` int(11) NOT NULL AUTO_INCREMENT;

--
-- Ketidakleluasaan untuk tabel pelimpahan (Dumped Tables)
--

--
-- Ketidakleluasaan untuk tabel `booking_detail`
--
ALTER TABLE `booking_detail`
  ADD CONSTRAINT `booking_detail_ibfk_1` FOREIGN KEY (`id_booking`) REFERENCES `booking` (`id_booking`),
  ADD CONSTRAINT `booking_detail_ibfk_2` FOREIGN KEY (`id_aset`) REFERENCES `aset` (`id_aset`);

--
-- Ketidakleluasaan untuk tabel `playathome_detail`
--
ALTER TABLE `playathome_detail`
  ADD CONSTRAINT `playathome_detail_ibfk_1` FOREIGN KEY (`id_playhome`) REFERENCES `playathome` (`id_playhome`),
  ADD CONSTRAINT `playathome_detail_ibfk_2` FOREIGN KEY (`id_aset`) REFERENCES `aset` (`id_aset`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
