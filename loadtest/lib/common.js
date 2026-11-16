// 부하 테스트 공통 설정
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.1.0/index.js';

export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// 가상 사용자 수 단계. 각 단계를 별도 scenario로 순서대로 실행해서
// 결과에서 단계별 응답 시간·실패율을 따로 볼 수 있게 한다.
export const VU_LEVELS = (__ENV.VU_LEVELS || '10,50,100,200').split(',').map(Number);
export const STAGE_DURATION = __ENV.STAGE_DURATION || '1m';
// 단계 사이 휴식 시간 (이전 단계의 영향이 다음 단계에 남지 않도록)
const GAP_SECONDS = Number(__ENV.GAP_SECONDS || 10);

// 판정 기준: p95 1초 이하, 실패율 1% 미만
const P95_LIMIT_MS = 1000;
const FAIL_RATE_LIMIT = 0.01;

function toSeconds(duration) {
    const m = duration.match(/^(\d+)(s|m)$/);
    if (!m) throw new Error(`STAGE_DURATION 형식 오류: ${duration} (예: 30s, 1m)`);
    return m[2] === 'm' ? Number(m[1]) * 60 : Number(m[1]);
}

function levelName(vus) {
    return `vu${String(vus).padStart(3, '0')}`;
}

/**
 * VU_LEVELS의 단계마다 constant-vus scenario를 만들고, 단계별 threshold를 건다.
 * threshold를 단계별로 걸어두면 k6 결과 요약에 단계별 수치가 따로 출력된다.
 */
export function buildOptions(execName) {
    const stageSeconds = toSeconds(STAGE_DURATION);
    const scenarios = {};
    const thresholds = {
        http_req_duration: [`p(95)<${P95_LIMIT_MS}`],
        http_req_failed: [`rate<${FAIL_RATE_LIMIT}`],
    };

    VU_LEVELS.forEach((vus, i) => {
        const name = levelName(vus);
        scenarios[name] = {
            executor: 'constant-vus',
            exec: execName,
            vus,
            duration: STAGE_DURATION,
            startTime: `${i * (stageSeconds + GAP_SECONDS)}s`,
            gracefulStop: '30s',
        };
        thresholds[`http_req_duration{scenario:${name}}`] = [`p(95)<${P95_LIMIT_MS}`];
        thresholds[`http_req_failed{scenario:${name}}`] = [`rate<${FAIL_RATE_LIMIT}`];
        thresholds[`http_reqs{scenario:${name}}`] = ['count>=0']; // 단계별 요청 수 출력용
    });

    return {
        scenarios,
        thresholds,
        summaryTrendStats: ['avg', 'med', 'p(90)', 'p(95)', 'max'],
    };
}

/** 콘솔에 요약을 출력하고, loadtest/results/ 아래에 JSON으로도 저장한다. */
export function buildSummary(data, resultName) {
    const stamp = new Date().toISOString().replace(/[:.]/g, '-');
    const file = `loadtest/results/${resultName}-${stamp}.json`;
    return {
        stdout: textSummary(data, { indent: ' ', enableColors: true }),
        [file]: JSON.stringify(data, null, 2),
    };
}
