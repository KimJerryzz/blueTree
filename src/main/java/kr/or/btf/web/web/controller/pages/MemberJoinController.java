package kr.or.btf.web.web.controller.pages;

import kr.or.btf.web.common.Constants;
import kr.or.btf.web.common.exceptions.ValidCustomException;
import kr.or.btf.web.domain.web.Account;
import kr.or.btf.web.domain.web.MobileAuthLog;
import kr.or.btf.web.services.web.MemberService;
import kr.or.btf.web.services.web.MobileAuthLogService;
import kr.or.btf.web.utils.AESEncryptor;
import kr.or.btf.web.web.controller.BaseCont;
import kr.or.btf.web.web.form.MemberForm;
import kr.or.btf.web.web.form.MobileAuthLogForm;
import kr.or.btf.web.web.validator.MemberFormValidator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberJoinController extends BaseCont {

    private final MemberService memberService;
    private final MemberFormValidator memberFormValidator;
    private final MobileAuthLogService mobileAuthLogService;

    @GetMapping({"/pages/member/idFind"})
    public String idFind(Model model,
                         HttpSession session) {

        model.addAttribute("mc", "memberJoin");
        return "/pages/member/idFind";
    }

    @GetMapping({"/pages/member/pwFind"})
    public String pwFind(Model model,
                         HttpSession session) {

        model.addAttribute("mc", "memberJoin");
        return "/pages/member/pwFind";
    }

    @GetMapping({"/pages/member/joinAgree"})
    public String joinAgree(Model model,
                            HttpSession session) {

        model.addAttribute("mc", "memberJoin");
        return "/pages/member/joinAgree";
    }

    @GetMapping({"/pages/member/register"})
    public String register(Model model,
                           MemberForm memberForm,
                           HttpSession session) {

        model.addAttribute("mc", "memberJoin");
        return "/pages/member/register";
    }

    @PostMapping("/api/member/insert")
    public ResponseEntity insert(Model model,
                                 @ModelAttribute @Valid MemberForm memberForm,
                                 Errors errors,
                                 BindingResult bindingResult) {
        boolean result = false;

        String msg = "";
        /*if (memberService.existsSpace(memberForm.getLoginId())) {
            bindingResult.rejectValue("loginId", "invalid ID", new Object[]{memberForm.getLoginId()}, "??????????????? ????????? ?????? ??? ??? ????????????.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bindingResult.getFieldError("loginId").getDefaultMessage());
        }*/
        String year = memberForm.getBrthdy().substring(0, 4);
        Calendar cal = Calendar.getInstance();
        if ((cal.get(Calendar.YEAR) - Integer.parseInt(year)) < 14) { //??????????????? ??????
            if (memberForm.getPrtctorNm() == null || "".equals(memberForm.getPrtctorNm()) ||
                    memberForm.getPrtctorBrthdy() == null || "".equals(memberForm.getPrtctorBrthdy()) ||
                    memberForm.getPrtctorEmail() == null || "".equals(memberForm.getPrtctorEmail()) ||
                    memberForm.getPrtctorEmail().split("@").length > 2 || StringUtils.countMatches(memberForm.getEmail(), " ") > 0 ||
                    StringUtils.countMatches(memberForm.getLoginId(), " ") > 0 || StringUtils.countMatches(memberForm.getPwd(), " ") > 0 ||
                    StringUtils.countMatches(memberForm.getPrtctorEmail(), " ") > 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors.getFieldErrors());
            }
        } else { //????????? ????????????
            if (memberForm.getEmail().split("@").length > 2 || StringUtils.countMatches(memberForm.getEmail(), " ") > 0 ||
                    StringUtils.countMatches(memberForm.getLoginId(), " ") > 0 || StringUtils.countMatches(memberForm.getPwd(), " ") > 0
            ) {//validation
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors.getFieldErrors());
            }

            if (!errors.hasErrors()) {//???????????? validation
                //????????? ?????? ?????? ??????
                MobileAuthLogForm mobileAUthLogForm = new MobileAuthLogForm();
                mobileAUthLogForm.setDmnNo(memberForm.getSRequestNumber());
                mobileAUthLogForm.setRspNo(memberForm.getSResponseNumber());
                mobileAUthLogForm.setMbtlnum(memberForm.getMoblphon());
                MobileAuthLog load = mobileAuthLogService.load(mobileAUthLogForm);
                if (load == null){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors.getFieldErrors());
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors.getFieldErrors());
            }
        }
        if (memberForm.getId() == null) {
            try {
                result = memberService.insert(memberForm);
            } catch (ValidCustomException ve) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ve);
            }
        }

        if (result) {
            msg = "???????????? ?????? ????????? ?????????????????????. ?????? ?????? ??????(1??????)????????? ????????? ?????? ??? ????????? ????????????.";
            return ResponseEntity.ok(msg);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors.getFieldErrors());
        }
    }

    @PostMapping("/api/member/update")
    public ResponseEntity update(Model model,
                                 @ModelAttribute @Valid MemberForm memberForm,
                                 Errors errors) {
        boolean result = false;
        String msg = "";
        if (!errors.hasErrors()) {
            if (memberForm.getId() != null) {
                try {
                    result = memberService.update(memberForm);
                } catch (ValidCustomException ve) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ve.getErrors()[0]);
                }
            } else {
                msg = "???????????? ?????? ??? ????????????.";
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ValidCustomException(msg, "altmsg"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors.getFieldErrors());
        }

        if (result) {
            msg = "??????????????? ?????????????????????.";
            return ResponseEntity.ok(msg);
        } else {
            msg = "??????????????????. ??????????????? ??????????????????.";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ValidCustomException(msg, "altmsg"));
        }
    }

    @PostMapping("/pages/member/pwdUpdate")
    public ResponseEntity pwdUpdate(Model model,
                                    @ModelAttribute @Valid MemberForm memberForm,
                                    Errors errors) {
        boolean result = false;
        String msg = "";
        List<FieldError> errList = new ArrayList<>();
        if (memberForm.getId() != null) {
            if (!errors.hasFieldErrors("pwd") && !errors.hasFieldErrors("pwdChg") && !errors.hasFieldErrors("pwdChgChk")) {
                try {
                    result = memberService.update(memberForm);
                } catch (ValidCustomException ve) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ve.getErrors());
                }
            } else {
                if (errors.hasFieldErrors("pwd")) {
                    errList.addAll(errors.getFieldErrors("pwd"));
                }
                if (errors.hasFieldErrors("pwdChg")) {
                    errList.addAll(errors.getFieldErrors("pwdChg"));
                }
                if (errors.hasFieldErrors("pwdChgChk")) {
                    errList.addAll(errors.getFieldErrors("pwdChgChk"));
                }
                if (!memberForm.getPwdChg().equals(memberForm.getPwdChgChk())) {
                    msg = "?????? ??????????????? ?????? ???????????? ????????? ???????????? ????????????.";
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ValidCustomException(msg, "altmsg"));
                }

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errList);
            }
        } else {
            msg = "???????????? ?????? ??? ????????????.";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ValidCustomException(msg, "altmsg"));
        }

        if (result) {
            msg = "??????????????? ?????????????????????.";
            return ResponseEntity.ok(msg);
        } else {
            msg = "??????????????????. ??????????????? ??????????????????.";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ValidCustomException(msg, "altmsg"));
        }
    }

    @ResponseBody
    @PostMapping("/api/member/isExistsByLoginId")
    public ResponseEntity isExistsByLoginId(Model model,
                                            @ModelAttribute MemberForm memberForm,
                                            BindingResult bindingResult) {

        if (memberService.existsByLoginId(memberForm.getLoginId())) {
            bindingResult.rejectValue("loginId", "invalid ID", new Object[]{memberForm.getLoginId()}, "?????? ???????????? ??????????????????.");
        }
        if (memberService.existsSpace(memberForm.getLoginId())) {
            bindingResult.rejectValue("loginId", "invalid ID", new Object[]{memberForm.getLoginId()}, "??????????????? ????????? ?????? ??? ??? ????????????.");
        }
        //memberFormValidator.validate(memberForm, bindingResult);
        if (bindingResult.hasFieldErrors("loginId")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bindingResult.getFieldError("loginId").getDefaultMessage());
        } else {
            return ResponseEntity.ok(memberForm);
        }
    }

    @ResponseBody
    @PostMapping("/api/member/isExistsByEmail")
    public ResponseEntity isExistsByEmail(Model model,
                                          @ModelAttribute MemberForm memberForm,
                                          BindingResult bindingResult) {

        if (memberService.existsByEmail(memberForm.getEmail())) {
            bindingResult.rejectValue("email", "invalid EMAIL", new Object[]{memberForm.getEmail()}, "?????? ???????????? ??????????????????.");
        }
        //memberFormValidator.validate(memberForm, bindingResult);
        if (bindingResult.hasFieldErrors("email")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bindingResult.getFieldError("email").getDefaultMessage());
        } else {
            return ResponseEntity.ok(memberForm);
        }
    }

    // id??????
    // ??????, ???????????? member ???????????? ??????.
    // ??????????????? id??? ???????????? ????????????????????? ?????????
    @ResponseBody
    @PostMapping("/api/member/idFind")
    public String idFind(Model model,
                         @RequestBody MemberForm memberForm) {
        String loginId = "";

        Account byNmAndEmail = memberService.findByNmAndEmail(memberForm.getNm(), memberForm.getEmail());
        if (byNmAndEmail != null && !byNmAndEmail.getLoginId().isEmpty()) {
            loginId = byNmAndEmail.getLoginId();
        }

        return loginId;
    }

    // pw ?????? ???????????? ??????
    // ??????, ???????????? member ???????????? ??????.
    // ??????????????? ?????? ???????????? ?????? ???, ????????? ??????
    @ResponseBody
    @PostMapping("/api/member/sendTempPw")
    public Account pwFind(Model model,
                          @RequestBody MemberForm memberForm) {
        boolean result = false;

        Account account = new Account();
        if (!memberForm.getLoginId().isEmpty() && !memberForm.getEmail().isEmpty()) {
            Account check = memberService.loadByLoginId(memberForm.getLoginId());
            if (check == null || check.getLoginId() == null) {
                return null;
            }
            if (!memberForm.getEmail().equals(check.getEmail())) {
                return null;
            }
            result = memberService.updateTempPw(memberForm);
            if (result) {
                Account load = memberService.findByLoginIdAndDelAt(memberForm.getLoginId(), "N");
                account.setId(load.getId());
                account.setLoginId(load.getLoginId());
            }

        }

        return account;
    }

    //???????????? ?????????
    @PostMapping("/api/member/mailAuthResend")
    public String mailAuthResend(Model model,
                                 @ModelAttribute MemberForm memberForm) throws Exception {
        boolean result = false;

        if (memberForm.getLoginId() != null) {
            Account account = memberService.loadByLoginId(memberForm.getLoginId());
            if (account != null) {
                if (account.getEmailAttcAt().equals("Y") && account.getPrtctorAttcAt().equals("Y")) {
                    model.addAttribute("altmsg", "?????? ???????????? ??? ????????? ?????????.");
                    model.addAttribute("locurl", "/login");
                    return "/message";
                }
                memberForm.setEmailAttcAt(account.getEmailAttcAt());
                memberForm.setPrtctorAttcAt(account.getPrtctorAttcAt());
                result = memberService.updateEmailAttc(memberForm);
            }
        }

        if (result) {
            model.addAttribute("altmsg", "????????? ??????????????????. ?????? ??? ?????????????????????.");
        } else {
            model.addAttribute("altmsg", "????????? ??????????????????. ?????? ????????? ????????? ????????????.");
        }
        model.addAttribute("locurl", "/login");
        return "/message";
    }

    //????????????
    @GetMapping("/api/member/mailAuth")
    public String mailAuth(Model model,
                           @RequestParam(name = "authKey") String authKey) throws Exception {
        boolean result = false;

        AESEncryptor aesEncryptor = AESEncryptor.getInstance(Constants.AESEncryptKey);
        String authKeyStr = aesEncryptor.decrypt(authKey);

        String loginId = authKeyStr.substring(0, authKeyStr.indexOf("|"));
        LocalDateTime emailAttcDtm = LocalDateTime.parse(authKeyStr.substring(authKeyStr.indexOf("|") + 1));

        MemberForm memberForm = new MemberForm();
        memberForm.setLoginId(loginId);
        memberForm.setEmailAttcDtm(emailAttcDtm);
        if (memberForm.getLoginId() != null && memberForm.getEmailAttcDtm() != null) {
            Account account = memberService.loadByLoginId(memberForm.getLoginId());
            if (account != null) {
                if (account.getEmailAttcAt().equals("Y")) {
                    model.addAttribute("altmsg", "?????? ???????????? ??? ????????? ?????????.");
                    model.addAttribute("locurl", "/login");
                    return "/message";
                } else {
                    LocalDateTime limitLdt = LocalDateTime.now();
                    LocalDateTime reqLdt = memberForm.getEmailAttcDtm().plusHours(1L);
                    if (limitLdt.isBefore(reqLdt)) { // ?????? ?????? 1?????? ??????
                        memberForm.setEmailAttcAt("Y");
                        result = memberService.updateEmailAttc(memberForm);
                    } else {
                        model.addAttribute("altmsg", "?????? ?????? ??????(1??????) ?????????, ?????? ??????????????????. ?????? ????????? ????????? ????????????.");
                        model.addAttribute("locurl", "/login");
                        return "/message";
                    }
                }
            }
        }

        if (result) {
            model.addAttribute("altmsg", "?????? ??????????????????. ?????????????????????.");
        } else {
            model.addAttribute("altmsg", "?????? ??????????????????. ??????????????? ??????????????????.");
        }
        model.addAttribute("locurl", "/login");
        return "/message";
    }

    @GetMapping("/api/member/parentMailAuth")
    public String parentMailAuth(Model model,
                                 @RequestParam(name = "authKey") String authKey) throws Exception {
        boolean result = false;

        AESEncryptor aesEncryptor = AESEncryptor.getInstance(Constants.AESEncryptKey);
        String authKeyStr = aesEncryptor.decrypt(authKey);

        String loginId = authKeyStr.substring(0, authKeyStr.indexOf("|"));
        LocalDateTime emailAttcDtm = LocalDateTime.parse(authKeyStr.substring(authKeyStr.indexOf("|") + 1));

        MemberForm memberForm = new MemberForm();
        memberForm.setLoginId(loginId);
        memberForm.setPrtctorAttcDtm(emailAttcDtm);
        if (memberForm.getLoginId() != null && memberForm.getPrtctorAttcDtm() != null) {
            Account account = memberService.loadByLoginId(memberForm.getLoginId());
            if (account != null) {
                if (account.getPrtctorAttcAt().equals("Y")) {
                    model.addAttribute("altmsg", "?????? ???????????? ??? ????????? ?????????.");
                    model.addAttribute("locurl", "/login");
                    return "/message";
                } else {
                    LocalDateTime limitLdt = LocalDateTime.now();
                    LocalDateTime reqLdt = memberForm.getPrtctorAttcDtm().plusHours(1L);
                    if (limitLdt.isBefore(reqLdt)) { // ?????? ?????? 1?????? ??????
                        memberForm.setPrtctorAttcAt("Y");
                        result = memberService.updateEmailAttc(memberForm);
                    } else {
                        model.addAttribute("altmsg", "?????? ?????? ??????(1??????) ?????????, ?????? ??????????????????. ?????? ????????? ????????? ????????????.");
                        model.addAttribute("locurl", "/login");
                        return "/message";
                    }
                }
            }
        }

        if (result) {
            model.addAttribute("altmsg", "?????? ??????????????????. ?????????????????????.");
        } else {
            model.addAttribute("altmsg", "?????? ??????????????????. ??????????????? ??????????????????.");
        }
        model.addAttribute("locurl", "/login");
        return "/message";
    }
}
