package com.example.jwtauth.controller;

import com.example.jwtauth.authentrypoint.OTPCode;
import com.example.jwtauth.jwtutil.JwtUtil;
import com.example.jwtauth.repository.UserRepository;
import com.example.jwtauth.service.EmailService;
import com.example.jwtauth.statusenum.Role;
import com.example.jwtauth.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@CrossOrigin(origins = "http://10.0.2.2")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final Map<String, User> pendingUsers = new ConcurrentHashMap<>();
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtils;

    @Autowired
    private EmailService emailService;
    @Autowired
    private OTPCode otpCode;


    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody User user) { //Userin her seyini gotururuk
        User existingUser = userRepository.findByEmail(user.getEmail()); //userin emailini axtaririg
        if (existingUser == null) { //eger email yoxdusa istifadeci tapilmadi yazir
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "İstifadəçi tapılmadı"));
        }
        if (!existingUser.isVerified()) { //Eger email tesdiqlenmiyibse Emaili tesdiqle deyir
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Emaili təsdiqləyin, sonra giriş edin!"));
        }
        try {
            Authentication authentication = authenticationManager.authenticate( //ve try edib email ve passwordu gotururuk
                    new UsernamePasswordAuthenticationToken(
                            user.getEmail(),
                            user.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication); //email passwordu add edirik contextholdere
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtils.generateToken(userDetails.getUsername()); //email ucun token yaradirig
            return ResponseEntity.ok(Map.of("token", token,"username",existingUser.getUsername())); //token ile username response edirik jsona

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Email və ya şifrə yanlışdır")); //eger olmadisa email sifre sehvdir
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@RequestBody User user) { //Useri gotururuk
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {  //Username nulldursa ve yaxud username arasinda boslug varsa ve bosdursa
            return ResponseEntity.badRequest().body("İstifadəçi adı boş ola bilməz!");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) { //email nulldursa ve yaxud email arasinda boslug varsa ve bosdursa
            return ResponseEntity.badRequest().body("Email boş ola bilməz!");
        }
        if (user.getPassword() == null || user.getPassword().length() < 4) { //parol nulldursa ve parolun uzunlugu 4 den kicik olarsa
            return ResponseEntity.badRequest().body("Şifrə ən az 5 simvoldan ibarət olmalıdır!");
        }

        if (userRepository.existsByUsername(user.getUsername())) { //Repositoryde bu usernameni axtar ve movcuddursa error qaytar
            return ResponseEntity.status(HttpStatus.CONFLICT).body("İstifadəçi adı artıq mövcuddur!");
        }

        String otp = otpCode.generateOTPCode(); //generate otp metodunun interfacecini cagiririg ve orda otp yaradir
        user.setOtp(otp); //sonra otpni usere set edirik
        user.setPassword(passwordEncoder.encode(user.getPassword())); //usere parolun hashini set edirik
        user.setRole(Role.USER); //usere rolu set edirik default olarag USER save olunmalidi
        user.setVerified(false); //heleki tesdig etmeyib deye false qaytaririg
        pendingUsers.put(user.getEmail(), user); //yuxarida hashmap yaratmisigki muveqqeti olarag datalarimizi oraya add eliyek.Hashmap yaratmagimizin megsedi Key Value sohbetidir yeniki email ve userimin otpsi
        try {
            emailService.sendOtpEmail(user.getEmail(), otp); //email service ile emaile otp gondermeyi yoxla
        } catch (Exception e) {
            pendingUsers.remove(user.getEmail()); //eger gondermek mumkun olmasa hashmapa add etdiyimiz emaili silirik
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("OTP göndərmək mümkün olmadı!");
        }

        return ResponseEntity.ok("OTP email ünvanınıza göndərildi. Qeydiyyatı tamamlamaq üçün təsdiqləyin."); //Gonderildise response 200 alirik
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam String otp) { //RequestParam ile email ve otpmizi alirig
        User pendingUser = pendingUsers.get(email); //Userin emailini hashmapdaki emaile beraber edirik
        if (pendingUser == null) { //Eger nulldusa email
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bu email ilə gözlənilən qeydiyyat tapılmadı!");
        }
        if (pendingUser.getOtp().equals(otp)) { //Ve emaildeki otp yazdigimiz otp ye beraberdise
            pendingUser.setOtp(null); //otpni null ele
            pendingUser.setVerified(true); //tesdigle hesabi
            userRepository.save(pendingUser); //database elave ele
            pendingUsers.remove(email); //hashmapdan sil
            return ResponseEntity.ok("Email uğurla təsdiqləndi və qeydiyyat tamamlandı!");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OTP yanlışdır!");
        }
    }
    @PostMapping("/change-email-request")
    public ResponseEntity<String> changeEmailRequest(
            @RequestHeader("Authorization") String token, //Headerdan tokeni alirig ve map ile body gonderirik
            @RequestBody Map<String, String> body) {
        String newEmail = body.get("newEmail"); //bodye teze email elave edirik
        String jwt = token.replace("Bearer ", ""); //jwtden tokenin bearer hissesini cixariri*
        String currentUserEmail = jwtUtils.getUsernameFromToken(jwt);
        if (newEmail == null || newEmail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Yeni e-posta adresi geçersiz.");
        }
        User user = userRepository.findByEmail(currentUserEmail); //dbdan emaili tapirig
        if (user == null) { //eger null dursa
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("İstifadəçi tapılmadı.");
        }
        String otp = otpCode.generateOTPCode(); //null deyilse otp kod generate ele
        user.setOtp(otp); //otp kodu usere elave et
        pendingUsers.put(newEmail, user); //hashmapa emailnen useri elave et
        try {
            emailService.sendOtpEmail(newEmail, otp); //email service ile emaile otp gonder
            return ResponseEntity.ok("Yeni e-posta adresine OTP gönderildi.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("OTP gönderilemedi.");
        }
    }

    @PutMapping("/change-email")
    public ResponseEntity<String> changeEmail(
            @RequestHeader("Authorization") String token, // Headerden tokeni alirig
            @RequestBody Map<String, String> body) { //bodyni gotururuk map ile string
        String newEmail = body.get("newEmail"); //teze emaili bodyden gotururuk
        String otp = body.get("otp");  //teze otpni gotururuk
        String jwt = token.replace("Bearer ", ""); //bearer hissesini cixaririg
        String currentUserEmail = jwtUtils.getUsernameFromToken(jwt); //
        if (newEmail == null || newEmail.isEmpty()) { // eger teze email nulldursa ve yaxud bosdursa
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Yeni e-posta adresi geçersiz.");
        }
        User user = userRepository.findByEmail(currentUserEmail); // evvelki emailimizi tapirig birinci
        if (user == null) { // eger teze email nulldursa ve yaxud bosdursa
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("İstifadəçi tapılmadı.");
        }
        User pendingUser = pendingUsers.get(newEmail); //hashmapdan teze emaili getir
        if (pendingUser == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Yeni e-posta adresi doğrulama beklemiyor.");
        }
        if (!pendingUser.getOtp().equals(otp)) { //eger userimin daxil etdiyi otp otpye beraber olmazsa
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Geçersiz OTP.");
        }
        user.setEmail(newEmail); //userin emailine teze email ile deyis
        user.setOtp(null);  // Otpni sifirlayirig
        userRepository.save(user); //db ya save edirik
        pendingUsers.remove(newEmail); //ve hashmapdan silirik emaili
        return ResponseEntity.ok("E-posta adresiniz başarıyla güncellendi.");
    }
}
