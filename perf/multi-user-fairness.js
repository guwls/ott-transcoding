import http from 'k6/http';
import { check } from 'k6';

export const options = {
    scenarios: {
        u1: { executor: 'constant-arrival-rate', rate: 20, timeUnit: '1s', duration: '60s' },
        u2: { executor: 'constant-arrival-rate', rate: 20, timeUnit: '1s', duration: '60s', startTime: '0s' },
        u3: { executor: 'constant-arrival-rate', rate: 20, timeUnit: '1s', duration: '60s', startTime: '0s' },
    }
};

const BASE = __ENV.BASE || 'http://localhost:8080';
const KEYS = (__ENV.KEYS || 'alice-key,bob-key,charlie-key').split(',');

export default function () {
    const key = KEYS[__VU % KEYS.length];
    const h = { headers: { 'Content-Type': 'application/json', 'X-Api-Key': key } };
    const vid = 1;
    const r = http.post(`${BASE}/videos/${vid}/enqueue-transcode`, JSON.stringify({ variants: ['480p'] }), h);
    check(r, { '200 or 429': resp => resp.status === 200 || resp.status === 429 });
}
