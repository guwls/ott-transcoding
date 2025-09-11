import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    scenarios: {
        smoke: {
            executor: 'constant-arrival-rate',
            rate: __ENV.RATE ? Number(__ENV.RATE) : 50, // req/sec
            timeUnit: '1s',
            duration: __ENV.DUR || '2m',
            preAllocatedVUs: 50,
            maxVUs: 200,
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(50)<150', 'p(95)<300'],
    },
    summaryTrendStats: ['avg','p(50)','p(95)','max'],
};

const BASE = __ENV.BASE || 'http://localhost:8080';

export default function () {
    const trace = `${__VU}-${Date.now()}-${Math.random().toString(36).slice(2,8)}`;
    const params = { headers: { 'Content-Type': 'application/json', 'X-Trace-Id': trace } };

    const r1 = http.post(`${BASE}/videos`, JSON.stringify({ title: 'k6', filesize: 1000 }), params);
    check(r1, { 'create 200/201': r => r.status === 200 || r.status === 201 });
    const vid = r1.json('videoId');

    const r2 = http.post(`${BASE}/videos/${vid}/enqueue-transcode`,
        JSON.stringify({ variants: ['480p','720p'] }), params);
    check(r2, { 'enqueue 200': r => r.status === 200 });

    const r3 = http.get(`${BASE}/videos/${vid}`, { headers: { 'X-Trace-Id': trace } });
    check(r3, { 'get 200': r => r.status === 200 });

    sleep(0.1); // 살짝 간격
}
