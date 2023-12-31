package com.seb45_pre_036.stackoverflow.member.controller;

import com.seb45_pre_036.stackoverflow.dto.MultiResponseDto;
import com.seb45_pre_036.stackoverflow.dto.SingleResponseDto;
import com.seb45_pre_036.stackoverflow.member.dto.MemberDto;
import com.seb45_pre_036.stackoverflow.member.entity.Member;
import com.seb45_pre_036.stackoverflow.member.mapper.MemberMapper;
import com.seb45_pre_036.stackoverflow.member.service.MemberService;
import com.seb45_pre_036.stackoverflow.utils.UriCreator;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/members")
@Validated
public class MemberController {
    private final MemberService memberService;
    private final MemberMapper mapper;
    private final static String MEMBER_DEFAULT_URL = "/members";

    public MemberController(MemberService memberService, MemberMapper mapper) {
        this.memberService = memberService;
        this.mapper = mapper;
    }

    @PostMapping("/signup")
    public ResponseEntity postMember(@RequestBody @Valid MemberDto.Post memberPostDto){

        Member member = mapper.memberPostDtoToMember(memberPostDto);
        Member createdMember = memberService.createMember(member);

        URI location = UriCreator.createUri(MEMBER_DEFAULT_URL, createdMember.getMemberId());

        return ResponseEntity.created(location).build();

    }

    @PostMapping("/renewAccessToken")
    public ResponseEntity renewAccessToken(@RequestHeader("Refresh") String refreshToken){

        String accessToken = memberService.renewAccessToken(refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", accessToken);

        return new ResponseEntity(headers, HttpStatus.OK);
    }

    @PatchMapping("{member-id}")
    public ResponseEntity patchMember(@RequestBody @Valid MemberDto.Patch memberPatchDto,
                                      @PathVariable("member-id") @Positive long memberId,
                                      @RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken){
        memberPatchDto.setMemberId(memberId);

        Member member = memberService.updateMember(mapper.memberPatchDtoToMember(memberPatchDto), accessToken);

        return new ResponseEntity(
                new SingleResponseDto<>(mapper.memberToMyPageResponseDto(member)), HttpStatus.OK);
    }

    @GetMapping("/myPage/{member-id}")
    public ResponseEntity getMember(@PathVariable("member-id") @Positive long memberId,
                                    @RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken){

        Member member = memberService.findMember(memberId, accessToken);

        return new ResponseEntity(new SingleResponseDto<>(mapper.memberToMyPageResponseDto(member)), HttpStatus.OK);

    }

    @GetMapping
    public ResponseEntity getMembers(@RequestParam @Positive int page,
                                     @RequestParam @Positive int size){

        Page<Member> pageMembers = memberService.findMembers(page-1, size);
        List<Member> members = pageMembers.getContent();
        return new ResponseEntity(
                new MultiResponseDto<>(mapper.membersToMemberResponseDtos(members), pageMembers), HttpStatus.OK);
    }


    @DeleteMapping("/{member-id}")
    public ResponseEntity deleteMember(@PathVariable("member-id") @Positive long memberId,
                                       @RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken){
        memberService.deleteMember(memberId, accessToken);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

}
