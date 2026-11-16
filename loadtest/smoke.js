// ④ 스모크 테스트: 가상 사용자 1명이 테스트 대상 공개 API를 한 번씩 호출해서
// 스크립트와 API가 정상 동작하는지(200 응답) 확인한다. 부하 측정용이 아님.
//
// 실행: k6 run loadtest/smoke.js
import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL } from './lib/common.js';

export const options = {
    vus: 1,
    duration: '30s',
    thresholds: {
        checks: ['rate==1.0'],
    },
};

// /api/health는 SecurityConfig에 permitAll로만 등록돼 있고 실제 컨트롤러가 없어서 500이 난다 → 대상에서 제외
const TARGETS = [
    '/api/admin/faqs',
    '/api/projects',
    '/api/blogs',
    '/api/gallery/projects',
];

export default function () {
    for (const path of TARGETS) {
        const res = http.get(`${BASE_URL}${path}`, { tags: { api: path } });
        check(res, { [`${path} 200`]: (r) => r.status === 200 });
    }
    sleep(1);
}
