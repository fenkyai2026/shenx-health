/* SHENX HEALTH — service worker (khusus versi web/PWA).
   Cache-first supaya aplikasi tetap jalan tanpa internet. */
const CACHE = 'shenx-health-v7';
const ASSETS = [
  './',
  './index.html',
  './manifest.webmanifest',
  './icon-192.png',
  './icon-512.png',
  './icon-512-maskable.png',
  './images/bg-greeting.jpg',
  './images/bg-bmi.jpg',
  './images/bg-water.jpg',
  './images/bg-kalori.jpg',
  './images/bg-exercise.jpg',
  './images/bg-jalan.jpg',
  './images/bg-napas.jpg',
  './images/bg-setting.jpg',
  './images/bg-bodyweight.jpg',
  './images/bg-tidur.jpg',
  './images/bg-positif.jpg',
  './images/bg-nutrisi.jpg',
  './videos/ex-pushup.mp4',
  './videos/ex-squat.mp4',
  './videos/ex-plank.mp4',
  './videos/ex-situp.mp4',
  './videos/ex-lunges.mp4',
  './videos/ex-jumpingjack.mp4',
  './videos/ex-mountainclimber.mp4',
  './videos/ex-burpee.mp4',
  './videos/ex-highknees.mp4',
  './videos/ex-russiantwist.mp4',
  './videos/ex-legraise.mp4',
  './videos/ex-bicyclecrunch.mp4',
  './videos/ex-squatbarbel.mp4',
  './videos/ex-deadlift.mp4',
  './videos/ex-benchpress.mp4',
  './videos/ex-overheadpress.mp4',
  './videos/ex-bentoverrow.mp4',
  './videos/ex-frontkick.mp4',
  './videos/ex-calfstretch.mp4',
  './videos/ex-clapjacks.mp4',
  './videos/ex-shouldercircles.mp4',
  './videos/ex-jumpinplace.mp4',
  './videos/ex-childspose.mp4',
  './videos/ex-clapcrossjacks.mp4',
  './videos/ex-hipopeners.mp4',
  './videos/ex-lowerbackstretch.mp4',
  './videos/ex-lungestretch.mp4',
  './videos/ex-shoulderstretch.mp4',
  './videos/ex-hamstringstretch.mp4',
  './videos/ex-quadstretch.mp4',
  './videos/ex-overheadtricepstretch.mp4'
];

self.addEventListener('install', e => {
  e.waitUntil(caches.open(CACHE).then(c => c.addAll(ASSETS)).then(() => self.skipWaiting()));
});

self.addEventListener('activate', e => {
  e.waitUntil(
    caches.keys()
      .then(keys => Promise.all(keys.filter(k => k !== CACHE).map(k => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', e => {
  if (e.request.method !== 'GET') return;
  e.respondWith(
    caches.match(e.request).then(hit => hit || fetch(e.request).then(res => {
      const copy = res.clone();
      caches.open(CACHE).then(c => c.put(e.request, copy)).catch(() => {});
      return res;
    }).catch(() => caches.match('./index.html')))
  );
});
