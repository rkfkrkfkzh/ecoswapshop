package shop.ecoswapshop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.ecoswapshop.domain.Address;
import shop.ecoswapshop.domain.Member;
import shop.ecoswapshop.domain.MemberStatus;
import shop.ecoswapshop.domain.UserType;
import shop.ecoswapshop.repository.MemberRepository;

import javax.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true) //조회만 하고 수정은 하지 않는다는 의미
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // 회원 가입
    @Transactional(readOnly = false) // 회원 가입은 읽기 전용이 아닌 트랜잭션에서 실행
    public Long registerMember(Member member) { // 회원 객체를 저장하고 생성된 회원의 ID를 반환
        validateDuplicateMember(member); //중복 회원 검출
        String encodedPassword = passwordEncoder.encode(member.getPassword());
        member.setPassword(encodedPassword); // 인코딩된 비밀번호 설정
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        List<Member> findMembers = memberRepository.findByFullName(member.getFullName());// DB 유니크제약조건으로 설정 권장
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    // 회원 조회
    public Optional<Member> findMemberById(Long memberId) {
        return memberRepository.findById(memberId);
    }

    // 전체 회원 조회
    public List<Member> findAllMembers() {
        return memberRepository.findAll();
    }

    // 회원 이름으로 조회
    public List<Member> findMembersByFullName(String fullName) {
        return memberRepository.findByFullName(fullName);
    }

    // 회원 이메일로 조회
    public Optional<Member> findMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    // 회원 전화번호로 조회
    public Optional<Member> findMemberByPhoneNumber(String phoneNumber) {
        return memberRepository.findByPhoneNumber(phoneNumber);
    }

    // 특정 등급의 회원들 조회
    public List<Member> findMembersByType(UserType type) {
        return memberRepository.findByType(type);
    }

    // 회원 등록일 기준으로 조회 (특정 날짜 이후의 회원들)
    public List<Member> findMembersRegisteredAfter(LocalDateTime registrationDate) {
        return memberRepository.findByRegistrationDateAfter(registrationDate);
    }

    // 회원 등록일과 등급 기준으로 조회 (특정 날짜 이후, 특정 등급 이상의 회원들)
    public List<Member> findMembersRegisteredAfterAndType(LocalDateTime registrationDate, UserType type) {
        return memberRepository.findByRegistrationDateAfterAndType(registrationDate, type);
    }

    // 회원 등록일과 등급, 이름으로 복합 조건으로 조회
    public List<Member> findMembersRegisteredAfterAndTypeAndName(LocalDateTime registrationDate, UserType type, String fullName) {
        return memberRepository.findByRegistrationDateAfterAndTypeAndFullName(registrationDate, type, fullName);
    }

    // 회원 정보 수정
    @Transactional
    public void updateMember(Long memberId, String username, String password, String email,
                             String fullName, String phoneNumber, Address address) {
        Member existingMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("No member found with id: " + memberId));

        if (username != null && !username.isEmpty()) existingMember.setUsername(username);
        if (password != null && !password.isEmpty()) {
            String encodedPassword = passwordEncoder.encode(password);
            existingMember.setPassword(encodedPassword);
        }
        if (email != null && !email.isEmpty()) existingMember.setEmail(email);
        if (fullName != null && !fullName.isEmpty()) existingMember.setFullName(fullName);
        if (phoneNumber != null && !phoneNumber.isEmpty()) existingMember.setPhoneNumber(phoneNumber);
        if (address != null) existingMember.setAddress(address);
    }

// ... 기존의 코드 ...


    // 회원 삭제
    @Transactional
    public void deleteMemberById(Long memberId) {
        memberRepository.deleteById(memberId);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 로그 추가
        System.out.println("loadUserByUsername called with username: " + username);
        Member member = memberRepository.findByUsername(username);
        if (member == null) {
            throw new UsernameNotFoundException("이런 유저 없습니다잉");
        }
        // 여기서 회원의 상태를 체크하여 DEACTIVATED 상태인 경우 로그인을 방지합니다.
        if (member.getStatus() == MemberStatus.DEACTIVATED) {
            throw new UsernameNotFoundException("비활성화된 회원입니다.");
        }
        return new User(
                member.getUsername(), member.getPassword(), Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")));
    }

    // id 중복체크
    public boolean existsByUsername(String username) {
        return memberRepository.existsByUsername(username);
    }

    public Optional<Long> findLoggedInMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // 로그인한 사용자의 username 가져오기

        Member member = memberRepository.findByUsername(username); // username으로 Member 조회
        if (member != null) {
            return Optional.of(member.getId()); // memberId 반환
        } else {
            return Optional.empty();
        }
    }

    // 회원 비활성화
    @Transactional
    public void deactivateMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("No member found with id: " + memberId));

        member.setStatus(MemberStatus.DEACTIVATED); // 회원 상태를 DEACTIVATED로 변경
    }

    // 비밀번호 검증
    public boolean validatePassword(Member member, String rawPassword) {
        return passwordEncoder.matches(rawPassword, member.getPassword());
    }

    // 아이디 찾기
    public Optional<String> findUsernameByEmailOrPhoneNumber(String email,String phoneNumber) {
        Member member = null;

        if (email != null && !email.isEmpty()) {
            Optional<Member> emailMember = memberRepository.findByEmail(email);
            if (emailMember.isPresent()) {
                member = emailMember.get();
            }
        }
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            Optional<Member> phoneMemmber = memberRepository.findByPhoneNumber(phoneNumber);
            if (phoneMemmber.isPresent()) {
                member = phoneMemmber.get();
            }
        }
        return Optional.ofNullable(member).map(Member::getUsername);
    }

    // 비밀번호 찾기: 임시 비밀번호 발급 및 이메일 전송 (비밀번호 재설정 링크 전송도 고려해볼 수 있습니다.)
    @Transactional
    public void resetPassword(String username, String email) {
        Member member = memberRepository.findByUsername(username);
        if (member == null || !member.getEmail().equals(email)) {
            throw new IllegalStateException("제공된 정보와 일치하는 회원을 찾을 수 없습니다.");
        }
        String tempPassword = generateTempPassword(); // 임시 비밀번호 생성
        String encodedPassword = passwordEncoder.encode(tempPassword);
        member.setPassword(encodedPassword);

        // 임시 비밀번호를 해당 이메일로 전송
        emailService.sendEmail(email, "임시 비밀번호 발금", "귀하의 임시 비밀번호는 " + tempPassword + "입니다.");
    }

    private String generateTempPassword() {
        // 임의의 문자열을 생성하여 임시 비밀번호로 사용. 실제 구현에서는 더 안전한 방식을 고려
        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }
}