// ⑤⑥ 단일 API 부하 테스트: API 하나에 가상 사용자를 단계적으로 늘려가며 요청한다.
// 모든 API에 같은 시나리오를 써서 조건을 똑같이 맞춘다.
//
// 실행 예:
//   k6 run -e TARGET=/api/admin/faqs loadtest/single-api.js    # 기준선 (가벼운 API)
//   k6 run -e TARGET=/api/projects loadtest/single-api.js      # N+1 의심 API
//
// 선택 옵션:
//   -e VU_LEVELS=10,50,100,200   단계별 가상 사용자 수 (기본값)
//   -e STAGE_DURATION=1m         단계별 유지 시간 (기본값)
//   -e THINK_TIME=1              요청 사이 대기 시간(초). 실제 사용자처럼 쉬는 시간 (기본값 1)
import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, buildOptions, buildSummary } from './lib/common.js';

const TARGET = __ENV.TARGET;
if (!TARGET) {
    throw new Error('TARGET을 지정하세요. 예: k6 run -e TARGET=/api/projects loadtest/single-api.js');
}
const THINK_TIME = Number(__ENV.THINK_TIME || 1);

export const options = buildOptions('hit');

export function hit() {
    const res = http.get(`${BASE_URL}${TARGET}`, { tags: { api: TARGET } });
    check(res, { 'status 200': (r) => r.status === 200 });
    sleep(THINK_TIME);
}

export function handleSummary(data) {
    // /api/gallery/projects → api-gallery-projects
    const name = TARGET.replace(/^\//, '').replace(/[^a-zA-Z0-9]+/g, '-');
    return buildSummary(data, `single-${name}`);
}
