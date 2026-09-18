/* SHENX HEALTH — proxy pencarian kalori & nutrisi.
   Menyimpan GEMINI_API_KEY di sisi server (Worker secret) supaya key
   tidak pernah muncul di kode app yang publik. App cuma manggil endpoint
   ini, bukan Gemini langsung. */

const GEMINI_MODEL = 'gemini-3.5-flash-lite';

function corsHeaders() {
  return {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type, X-App-Secret'
  };
}

function jsonResponse(obj, status) {
  return new Response(JSON.stringify(obj), {
    status,
    headers: { 'Content-Type': 'application/json', ...corsHeaders() }
  });
}

// Hitung jumlah install: app kirim ping anonim sekali per device (tanpa nama/identitas
// apa pun), cuma menambah satu angka di KV. Dilihat lewat /install-count?key=ADMIN_KEY.
async function handleInstallPing(request, env) {
  if (env.APP_SECRET && request.headers.get('X-App-Secret') !== env.APP_SECRET) {
    return jsonResponse({ error: 'Unauthorized' }, 401);
  }
  const current = parseInt((await env.INSTALLS.get('count')) || '0', 10) || 0;
  await env.INSTALLS.put('count', String(current + 1));
  return jsonResponse({ ok: true }, 200);
}

async function handleInstallCount(request, env) {
  const url = new URL(request.url);
  if (!env.ADMIN_KEY || url.searchParams.get('key') !== env.ADMIN_KEY) {
    return jsonResponse({ error: 'Unauthorized' }, 401);
  }
  const count = parseInt((await env.INSTALLS.get('count')) || '0', 10) || 0;
  return jsonResponse({ count }, 200);
}

export default {
  async fetch(request, env) {
    if (request.method === 'OPTIONS') {
      return new Response(null, { headers: corsHeaders() });
    }

    const path = new URL(request.url).pathname;
    if (path === '/install-ping' && request.method === 'POST') return handleInstallPing(request, env);
    if (path === '/install-count' && request.method === 'GET') return handleInstallCount(request, env);

    if (request.method !== 'POST') {
      return jsonResponse({ error: 'Method not allowed' }, 405);
    }

    // Pengecekan ringan: cuma request dari app kita yang punya header ini.
    // Bukan proteksi sempurna (kode app publik), tapi menahan bot pemindai kasar.
    if (env.APP_SECRET && request.headers.get('X-App-Secret') !== env.APP_SECRET) {
      return jsonResponse({ error: 'Unauthorized' }, 401);
    }

    let body;
    try {
      body = await request.json();
    } catch (e) {
      return jsonResponse({ error: 'Invalid JSON body' }, 400);
    }

    const query = String(body.query || '').trim().slice(0, 100);
    if (!query) return jsonResponse({ error: 'Query kosong' }, 400);

    const prompt =
      'Kamu adalah database nutrisi makanan Indonesia & internasional. ' +
      'Untuk permintaan makanan berikut: "' + query + '", berikan HANYA JSON valid ' +
      '(tanpa markdown, tanpa penjelasan tambahan) dengan format persis:\n' +
      '{"results":[{"name":"nama makanan spesifik","kal":angka,"karbo":angka,"protein":angka,"lemak":angka}]}\n' +
      'Semua angka (kal dalam kkal, karbo/protein/lemak dalam gram) dihitung PER 100 GRAM bahan. ' +
      'Berikan 1 sampai 6 hasil paling relevan (variasi jenis/varian/merek jika masuk akal). ' +
      'Kalau makanan sama sekali tidak dikenali, kembalikan {"results":[]}.';

    let geminiRes;
    try {
      geminiRes = await fetch(
        'https://generativelanguage.googleapis.com/v1beta/models/' + GEMINI_MODEL + ':generateContent?key=' + env.GEMINI_API_KEY,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            contents: [{ parts: [{ text: prompt }] }],
            generationConfig: { responseMimeType: 'application/json', temperature: 0.2 }
          })
        }
      );
    } catch (e) {
      return jsonResponse({ error: 'Gagal menghubungi Gemini API' }, 502);
    }

    if (!geminiRes.ok) {
      return jsonResponse({ error: 'Gemini API error', status: geminiRes.status }, 502);
    }

    const data = await geminiRes.json();
    const text = data && data.candidates && data.candidates[0] && data.candidates[0].content &&
      data.candidates[0].content.parts && data.candidates[0].content.parts[0] &&
      data.candidates[0].content.parts[0].text;

    let parsed;
    try {
      parsed = JSON.parse(text || '{"results":[]}');
    } catch (e) {
      parsed = { results: [] };
    }
    if (!parsed || !Array.isArray(parsed.results)) parsed = { results: [] };

    return jsonResponse(parsed, 200);
  }
};
