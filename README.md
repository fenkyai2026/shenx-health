# SHENX HEALTH — Android App

Aplikasi kesehatan harian (kalori, BMI, latihan jalan, latihan pernapasan) —
dibungkus jadi APK Android pakai [Capacitor](https://capacitorjs.com).

Aplikasinya tetap satu file HTML (`www/index.html`), jadi bisa dipakai
sebagai web/PWA **dan** sebagai APK dari sumber yang sama.

---

## 1. Cara tercepat dapat APK (tanpa install apa-apa)

1. Bikin repo baru di GitHub, upload seluruh isi folder ini.
2. Buka tab **Actions** → jalankan workflow **Build APK** (atau otomatis jalan setiap push ke `main`).
3. Tunggu ±4–6 menit → buka run-nya → bagian **Artifacts** → download `shenx-health-apk`.
4. Ekstrak ZIP-nya, kirim APK ke HP, install (aktifkan "Install dari sumber tidak dikenal").

APK dari sini adalah **debug build** — bisa langsung dipasang, tapi jangan dipakai untuk Play Store.

## 2. Build di komputer sendiri

Butuh: Node 20+, JDK 21, Android Studio (atau Android SDK + `ANDROID_HOME`).

```bash
npm install
npm run sync            # salin www/ ke project Android
npm run build:apk       # hasil: android/app/build/outputs/apk/debug/app-debug.apk
```

Atau buka di Android Studio: `npm run open`, lalu tekan Run.

## 3. Build release (untuk Play Store / distribusi)

Bikin keystore sekali saja — **simpan baik-baik, kalau hilang tidak bisa update aplikasi**:

```bash
keytool -genkey -v -keystore shenx-release.jks -keyalg RSA \
        -keysize 2048 -validity 10000 -alias shenx
```

**Build lokal:**

```bash
cp shenx-release.jks android/
export SHENX_KEYSTORE_PASSWORD=xxx SHENX_KEY_ALIAS=shenx SHENX_KEY_PASSWORD=xxx
npm run build:release
```

**Build lewat GitHub Actions:** masukkan 4 secret di repo
(Settings → Secrets and variables → Actions):

| Secret | Isi |
|---|---|
| `SHENX_KEYSTORE_BASE64` | hasil `base64 -w0 shenx-release.jks` |
| `SHENX_KEYSTORE_PASSWORD` | password keystore |
| `SHENX_KEY_ALIAS` | `shenx` |
| `SHENX_KEY_PASSWORD` | password key |

Lalu Actions → Build APK → Run workflow → pilih **release**.

Untuk Play Store, ganti `assembleRelease` jadi `bundleRelease` (hasilnya `.aab`).

---

## Struktur

```
www/index.html      ← aplikasinya. Edit di sini, lalu `npm run sync`
www/manifest.webmanifest, sw.js, icon-*.png   ← versi web/PWA
capacitor.config.json                          ← appId, nama app
android/                                       ← project Android (boleh dibuka di Android Studio)
  app/src/main/java/id/shenx/health/ShenxPlugin.java   ← plugin kecil: layar tetap nyala
.github/workflows/build-apk.yml                ← build otomatis di cloud
```

## Yang disesuaikan untuk versi APK

| Hal | Sebelum | Sekarang |
|---|---|---|
| Ikon | link ke `placehold.co` (butuh internet) | PNG lokal, semua kerapatan layar + adaptive icon |
| Suara panduan | `speechSynthesis` — **tidak jalan** di Android WebView | plugin TTS native, fallback ke browser otomatis |
| Aba-aba latihan jalan | "Jalan Santai" bunyi 1 detik *setelah* fase cepat mulai (salah hitung), dan emoji di label ikut dibacakan | aba-aba tepat di tiap pergantian fase, teks dibersihkan dulu, plus "sepuluh detik lagi" & getar hitung mundur 3-2-1 |
| Matikan suara | tidak ada | tombol 🔊 di tab Jalan & Napas, tersimpan |
| Layar mati saat latihan | timer terganggu | layar dijaga nyala selama timer jalan, mati lagi saat Jeda/Selesai |
| Getar | butuh izin | `VIBRATE` sudah ada di AndroidManifest |
| Service worker | dari Blob URL (tidak berfungsi) | `sw.js` nyata, cache-first — hanya untuk versi web |
| Daftar makanan | hilang saat app ditutup | tersimpan, reset otomatis ganti hari |
| Animasi lingkaran napas | tidak membesar (inline style menimpa CSS) | mengembang/mengempis mengikuti durasi fase |
| Riwayat | tercatat "dihentikan" tiap pindah tab | hanya dicatat kalau latihan benar-benar berjalan |

## Ganti identitas aplikasi

- Nama & ID: `capacitor.config.json` (`appId`, `appName`) → lalu `npm run sync`
- Nama di layar HP: `android/app/src/main/res/values/strings.xml`
- Ikon: ganti `www/icon-*.png` dan `android/app/src/main/res/mipmap-*/`
- Versi: otomatis dari nomor run CI, atau `VERSION_NAME` / `VERSION_CODE`

## Catatan

- Minimum Android 7.0 (API 24).
- Suara panduan pakai TTS bawaan HP — bahasa Indonesia perlu terpasang di
  *Setelan → Bahasa → Text-to-speech*. Kalau tidak ada, dibacakan dengan aksen default.
- Semua data disimpan lokal di HP (localStorage). Tidak ada server, tidak ada internet.
