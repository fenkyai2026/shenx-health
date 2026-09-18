# Nutrition Proxy

Cloudflare Worker kecil yang menjembatani fitur "Info Kalori & Nutrisi" di app
ke Gemini API (`gemini-3.5-flash-lite`). API key Gemini disimpan sebagai
**Worker secret** di server Cloudflare — tidak pernah ada di kode app (repo ini
publik, jadi key tidak boleh ikut ter-commit).

## Setup (sekali saja)

Dari folder ini (`nutrition-proxy/`):

```bash
npx wrangler login
npx wrangler secret put GEMINI_API_KEY
```
Saat diminta, paste API key Gemini dari https://aistudio.google.com/apikey
(format `AIzaSy...`).

Opsional tapi disarankan — set kata sandi aplikasi sederhana (dicek di header
`X-App-Secret`, dicocokkan di `index.html`) supaya bukan siapa saja yang
menemukan URL Worker ini bisa memanggilnya:
```bash
npx wrangler secret put APP_SECRET
```

Lalu deploy:
```bash
npx wrangler deploy
```

Wrangler akan menampilkan URL Worker-nya, contoh:
`https://shenx-nutrition-proxy.<subdomain-kamu>.workers.dev`

Salin URL itu ke `NUTRISI_PROXY_URL` di `www/index.html` (cari komentar
`GANTI_URL_PROXY_DI_SINI`), dan kalau kamu set `APP_SECRET`, isi juga
`NUTRISI_APP_SECRET` dengan nilai yang sama.

## Update setelah ganti kode worker.js

```bash
npx wrangler deploy
```

Tidak perlu ulangi langkah `secret put` — secret tetap tersimpan di Cloudflare.

## Hitung jumlah install (anonim)

Worker ini juga menyimpan satu angka hitungan install di Cloudflare KV
(namespace `INSTALLS`) — app mengirim ping sekali per device (tanpa nama,
tanpa identitas apa pun) lewat `POST /install-ping` saat pertama kali dibuka.

Untuk melihat angkanya, buka di browser (termasuk dari HP):
```
https://shenx-nutrition-proxy.<subdomain-kamu>.workers.dev/install-count?key=<ADMIN_KEY>
```

`ADMIN_KEY` adalah secret terpisah, di-set sekali lewat:
```bash
npx wrangler secret put ADMIN_KEY
```
Simpan nilainya sendiri — siapa pun yang tahu key ini bisa lihat hitungannya.
