import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    scenarios: {
        abuse: {
            executor: 'constant-arrival-rate',
            rate: 50, timeUnit: '1s', duration: '60s', preAllocatedVUs: 50, maxVUs: 200,
        }
    },
    thresholds: {
        http_req_failed: ['rate<0.05'],            // 일부 429는 "실패"로 잡히지만 5% 이내 유지 목표(용량/보충에 맞춰 조절)
        'http_reqs{status:429}': ['count>0'],     // 429 반드시 발생
    }
};

const BASE = __ENV.BASE || 'http://localhost:8080';
const KEY  = __ENV.KEY  || 'alice-key';

export default function () {
    const h = { headers: { 'Content-Type': 'application/json', 'X-Api-Key': KEY } };
    // enqueue만 때려서 레이트리밋 유발
    const vid = 1; // 테스트용(존재한다고 가정)
    const r = http.post(`${BASE}/videos/${vid}/enqueue-transcode`, JSON.stringify({ variants: ['480p'] }), h);
    check(r, { 'got 200/429': resp => resp.status === 200 || resp.status === 429 });
    sleep(0.02);
}
