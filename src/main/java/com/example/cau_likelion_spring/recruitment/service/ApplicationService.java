package com.example.cau_likelion_spring.recruitment.service;

import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import com.example.cau_likelion_spring.global.util.S3Uploader;
import com.example.cau_likelion_spring.recruitment.domain.Applicant;
import com.example.cau_likelion_spring.recruitment.domain.ApplicantInterviewTime;
import com.example.cau_likelion_spring.recruitment.domain.Application;
import com.example.cau_likelion_spring.recruitment.domain.InterviewAnswer;
import com.example.cau_likelion_spring.recruitment.domain.InterviewTime;
import com.example.cau_likelion_spring.recruitment.domain.Round;
import com.example.cau_likelion_spring.recruitment.domain.RoundPart;
import com.example.cau_likelion_spring.recruitment.dto.ApplicationRequest;
import com.example.cau_likelion_spring.recruitment.dto.ApplicationSubmitResponse;
import com.example.cau_likelion_spring.recruitment.dto.FormOptionsResponse;
import com.example.cau_likelion_spring.recruitment.dto.MyApplicationResponse;
import com.example.cau_likelion_spring.recruitment.dto.RoundPartResponse;
import com.example.cau_likelion_spring.recruitment.dto.StudentNumberCheckRequest;
import com.example.cau_likelion_spring.recruitment.dto.StudentNumberCheckResponse;
import com.example.cau_likelion_spring.recruitment.repository.ApplicantInterviewTimeRepository;
import com.example.cau_likelion_spring.recruitment.repository.ApplicantRepository;
import com.example.cau_likelion_spring.recruitment.repository.ApplicationRepository;
import com.example.cau_likelion_spring.recruitment.repository.InterviewAnswerRepository;
import com.example.cau_likelion_spring.recruitment.repository.InterviewTimeRepository;
import com.example.cau_likelion_spring.recruitment.repository.RoundPartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final RoundPartRepository roundPartRepository;
    private final ApplicantRepository applicantRepository;
    private final InterviewTimeRepository interviewTimeRepository;
    private final ApplicantInterviewTimeRepository applicantInterviewTimeRepository;
    private final InterviewAnswerRepository interviewAnswerRepository;
    private final CurrentRoundFinder currentRoundFinder;
    private final PartQuestionLoader partQuestionLoader;
    private final AnswerValidator answerValidator;
    private final S3Uploader s3Uploader;

    /**
     * 지원서 1단계 선택지(학년, 학적, 지원 경로, 현재 회차의 지원 파트)를 조회한다.
     * 등록된 파트가 없으면 parts는 빈 목록이다.
     */
    public FormOptionsResponse getFormOptions() {
        Round round = currentRoundFinder.find();
        List<RoundPartResponse> parts = roundPartRepository.findAllByRoundOrderByIdAsc(round).stream()
                .map(RoundPartResponse::from)
                .toList();
        return FormOptionsResponse.of(parts);
    }

    /**
     * 이번 회차에 같은 학번으로 제출된 지원서가 있는지 확인한다 (7.4.1 [다음] 클릭 시).
     * 조회만 하므로 접수 기간은 확인하지 않는다 (기간 판정은 제출 API에서).
     */
    public StudentNumberCheckResponse checkStudentNumber(StudentNumberCheckRequest request) {
        Round round = currentRoundFinder.find();
        boolean exists = applicationRepository.existsByRoundPart_RoundAndStudentNumber(
                round, request.getStudentNumber());
        return StudentNumberCheckResponse.of(exists);
    }

    /**
     * 지원서를 제출한다 (7.4.1~7.4.3). 1단계 정보, 면접 가능 시간, 2단계 답변을 한 트랜잭션에 저장한다.
     * 지각 여부는 저장하지 않고 제출 일시와 마감 시각으로 계산한다.
     * 학번 중복은 DB 유니크 없이 조회로 막으므로, 거의 동시에 들어온 같은 학번은 둘 다 저장될 수 있다.
     */
    @Transactional
    public ApplicationSubmitResponse submit(Long applicantId, ApplicationRequest request) {
        LocalDateTime now = LocalDateTime.now();

        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUITMENT_APPLICANT_NOT_FOUND));

        Round round = currentRoundFinder.find();
        validateSubmitTime(round, applicant, now);

        RoundPart roundPart = currentRoundFinder.findRoundPart(round, request.getRoundPartId());
        List<InterviewTime> interviewTimes = findInterviewTimes(round, request.getInterviewTimeIds());
        validateNotDuplicated(round, applicant, request.getStudentNumber());

        if (!s3Uploader.isBucketUrl(request.getCapInfo())) {
            throw new CustomException(ErrorCode.RECRUITMENT_INVALID_ANSWER, "포탈 캡처가 업로드된 파일 주소가 아닙니다.");
        }

        List<AnswerValidator.ValidatedAnswer> answers = answerValidator.validate(
                partQuestionLoader.load(roundPart), request.getAnswers());

        Application application = applicationRepository.save(Application.builder()
                .applicant(applicant)
                .roundPart(roundPart)
                .studentNumber(request.getStudentNumber())
                .department(request.getDepartment())
                .grade(request.getGrade())
                .semester(request.getSemester())
                .enrollment(request.getEnrollment())
                .capInfo(request.getCapInfo())
                .source(request.getSource())
                .submitAt(now)
                .build());

        applicantInterviewTimeRepository.saveAll(interviewTimes.stream()
                .map(interviewTime -> ApplicantInterviewTime.builder()
                        .applicant(applicant)
                        .interviewTime(interviewTime)
                        .build())
                .toList());

        interviewAnswerRepository.saveAll(answers.stream()
                .map(answer -> InterviewAnswer.builder()
                        .interviewQuestion(answer.question())
                        .roundPart(roundPart)
                        .round(round)
                        .application(application)
                        .applicant(applicant)
                        .answer(answer.value())
                        .build())
                .toList());

        return ApplicationSubmitResponse.from(application);
    }

    /**
     * 로그인한 지원자의 이번 회차 제출 내역을 조회한다 (7.3.3).
     * 미제출이면 접수 중인지만 알려준다 (지각 제출 가능 여부는 반영하지 않는다).
     */
    public MyApplicationResponse getMyApplication(Long applicantId) {
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUITMENT_APPLICANT_NOT_FOUND));
        Round round = currentRoundFinder.find();

        LocalDateTime now = LocalDateTime.now();
        boolean applyOpen = round.getStartDate() != null && round.getDeadLine() != null
                && !now.isBefore(round.getStartDate()) && !now.isAfter(round.getDeadLine());

        return applicationRepository.findByApplicantAndRoundPart_Round(applicant, round)
                .map(application -> MyApplicationResponse.submitted(application, applyOpen))
                .orElseGet(() -> MyApplicationResponse.notSubmitted(applyOpen));
    }

    /**
     * 접수 시작 전이면 차단, 마감 전이면 정상 제출,
     * 마감 후라도 폼 진입 시각이 마감 전이면 지각 제출로 허용한다 (7.4.3).
     */
    private void validateSubmitTime(Round round, Applicant applicant, LocalDateTime now) {
        if (round.getStartDate() == null || round.getDeadLine() == null || now.isBefore(round.getStartDate())) {
            throw new CustomException(ErrorCode.RECRUITMENT_NOT_IN_APPLY_PERIOD);
        }
        if (!now.isAfter(round.getDeadLine())) {
            return;
        }
        LocalDateTime formEnteredAt = applicant.getFormEnteredAt();
        if (formEnteredAt == null || formEnteredAt.isAfter(round.getDeadLine())) {
            throw new CustomException(ErrorCode.RECRUITMENT_NOT_IN_APPLY_PERIOD, "서류 접수 기간이 종료되었습니다.");
        }
    }

    /** 면접 시간이 전부 있고, 이번 회차 것이고, 중복이 없는지 확인한다 */
    private List<InterviewTime> findInterviewTimes(Round round, List<Long> interviewTimeIds) {
        Set<Long> distinctIds = new HashSet<>(interviewTimeIds);
        if (distinctIds.size() != interviewTimeIds.size()) {
            throw new CustomException(ErrorCode.RECRUITMENT_INVALID_INTERVIEW_TIME, "면접 가능 시간이 중복되었습니다.");
        }

        List<InterviewTime> interviewTimes = interviewTimeRepository.findAllById(distinctIds);
        boolean allInRound = interviewTimes.stream()
                .allMatch(interviewTime -> interviewTime.getRound() != null
                        && round.getId().equals(interviewTime.getRound().getId()));
        if (interviewTimes.size() != distinctIds.size() || !allInRound) {
            throw new CustomException(ErrorCode.RECRUITMENT_INVALID_INTERVIEW_TIME);
        }
        return interviewTimes;
    }

    /** 이 지원자가 이번 회차에 이미 냈는지, 같은 학번이 이번 회차에 있는지 확인한다 */
    private void validateNotDuplicated(Round round, Applicant applicant, String studentNumber) {
        if (applicationRepository.existsByApplicantAndRoundPart_Round(applicant, round)) {
            throw new CustomException(ErrorCode.RECRUITMENT_ALREADY_APPLIED);
        }
        if (applicationRepository.existsByRoundPart_RoundAndStudentNumber(round, studentNumber)) {
            throw new CustomException(ErrorCode.RECRUITMENT_DUPLICATE_STUDENT_NUMBER);
        }
    }
}
