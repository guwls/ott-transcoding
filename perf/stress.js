import http from 'k6/http';
import { check } from 'k6';

export const options = {
    scenarios: {
        step1: { executor: 'constant-arrival-rate', rate: 200, timeUnit: '1s', duration: '2m', preAllocatedVUs: 200, maxVUs: 500 },
        step2: { executor: 'constant-arrival-rate', rate: 300, timeUnit: '1s', duration: '2m', startTime: '2m', preAllocatedVUs: 300, maxVUs: 800 },
        step3: { executor: 'constant-arrival-rate', rate: 400, timeUnit: '1s', duration: '2m', startTime: '4m', preAllocatedVUs: 400, maxVUs: 1000 },
        step4: { executor: 'constant-arrival-rate', rate: 500, timeUnit: '1s', duration: '2m', startTime: '6m', preAllocatedVUs: 600, maxVUs: 1500 },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<350'], // API 레벨 목표
    },
    summaryTrendStats: ['avg','p(50)','p(95)','max'],
};

const BASE = __ENV.BASE || 'http://localhost:8080';

export default function () {
    const t = `${__VU}-${Date.now()}`;
    const h = { headers: { 'Content-Type': 'application/json', 'X-Trace-Id': t } };
    const r1 = http.post(`${BASE}/videos`, JSON.stringify({ title: 'k6', filesize: 1000 }), h);
    check(r1, { 'create ok': r => r.status === 200 || r.status === 201 });
    const vid = r1.json('videoId');
    const r2 = http.post(`${BASE}/videos/${vid}/enqueue-transcode`, JSON.stringify({ variants:['480p'] }), h);
    check(r2, { 'enqueue ok': r => r.status === 200 });
    http.get(`${BASE}/videos/${vid}`, { headers: { 'X-Trace-Id': t } });
}