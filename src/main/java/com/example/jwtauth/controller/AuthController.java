package com.example.jwtauth.controller;

import com.example.jwtauth.jwtutil.JwtUtil;
import com.example.jwtauth.repository.UserRepository;
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

@CrossOrigin(origins = "http://localhost:3000")
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




    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody User user) { //Userin her seyini gotururuk
        User existingUser = userRepository.findByEmail(user.getEmail()); //userin emailini axtaririg
        if (existingUser == null) { //eger email yoxdusa istifadeci tapilmadi yazir
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "İstifadəçi tapılmadı"));
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
        user.setPassword(passwordEncoder.encode(user.getPassword())); //usere parolun hashini set edirik
        user.setRole(Role.USER); //usere rolu set edirik default olarag USER save olunmalidi
   //heleki tesdig etmeyib deye false qaytaririg
    //yuxarida hashmap yaratmisigki muveqqeti olarag datalarimizi oraya add eliyek.Hashmap yaratmagimizin megsedi Key Value sohbetidir yeniki email ve userimin otpsi

        userRepository.save(user);
        return ResponseEntity.ok("OTP email ünvanınıza göndərildi. Qeydiyyatı tamamlamaq üçün təsdiqləyin.");

    }



}
