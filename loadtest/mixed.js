// ⑦ 혼합 부하 테스트: 여러 공개 API를 실제 방문 비율과 비슷하게 섞어서 동시에 호출한다.
// 목적: 무거운 API(/api/projects)가 DB 커넥션·스레드를 오래 붙잡아서
//       가벼운 API(/api/admin/faqs)까지 같이 느려지는지 확인한다.
//       → 결과에서 http_req_duration{api:/api/admin/faqs}를 단일 테스트 결과와 비교한다.
//
// 실행: k6 run loadtest/mixed.js
import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, buildOptions, buildSummary, VU_LEVELS } from './lib/common.js';

const THINK_TIME = Number(__ENV.THINK_TIME || 1);

// 누적 비율: FAQ 50%, 프로젝트 목록 30%, 블로그 목록 20%
const MIX = [
    { path: '/api/admin/faqs', weight: 0.5 },
    { path: '/api/projects', weight: 0.3 },
    { path: '/api/blogs', weight: 0.2 },
];

export const options = buildOptions('hit');

// API별 응답 시간을 결과 요약에 따로 출력하기 위한 threshold
for (const { path } of MIX) {
    options.thresholds[`http_req_duration{api:${path}}`] = ['p(95)<1000'];
}

function pick() {
    let r = Math.random();
    for (const item of MIX) {
        if (r < item.weight) return item.path;
        r -= item.weight;
    }
    return MIX[MIX.length - 1].path;
}

export function hit() {
    const path = pick();
    const res = http.get(`${BASE_URL}${path}`, { tags: { api: path } });
    check(res, { 'status 200': (r) => r.status === 200 });
    sleep(THINK_TIME);
}

export function handleSummary(data) {
    return buildSummary(data, 'mixed');
}
