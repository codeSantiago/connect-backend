package com.connect.services;

import com.connect.entities.redis.RedisQrLoginRequest;
import com.connect.entities.User;
import com.connect.exceptions.applicationexceptions.QrScannedEventSendException;
import com.connect.models.QrLoginRequest;
import com.connect.models.QrScannedEvent;
import com.connect.repositories.redis.RedisQrLoginRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final RedisQrLoginRequestRepository redisQrLoginRequestRepository;

    public Optional<User> getAuthenticatedUser(String username){
        return userService.findByUsername(username);
    }

    public void registerQrCodeRequest(QrLoginRequest request){
        String qrId = String.format(
                "Qr-%s-%s",
                request.getMobileSignature(),
                request.getWebSignature()
        );

        RedisQrLoginRequest newQrRequest = new RedisQrLoginRequest(qrId, request);
        redisQrLoginRequestRepository.save(newQrRequest);

        /*
        try{
            String qrId = String.format(
                    "Qr-%s-%s",
                    request.getMobileSignature(),
                    request.getWebSignature()
            );
            String stringifyQrCodeRequest = mapper.writeValueAsString(request);

            redisTemplate.opsForValue()
                .set(
                     qrId,
                     stringifyQrCodeRequest,
                     10L,
                     TimeUnit.MINUTES
                );
        }catch (IOException e){
            e.printStackTrace();
            throw new QrCodeRequestParseException("QrCodeRequest conversion to json failed", e);
        }
         */
    }

    public void sendQrScannedEventToBrowser(SseEmitter emitter, QrScannedEvent eventData){
        try{
            emitter.send(
               SseEmitter.event()
                   .name("onQrScanned")
                   .data(eventData)
            );
        }catch (IOException e){
            e.printStackTrace();
            String errorMessage = "Qr data could not get sent to the the browser";
            throw new QrScannedEventSendException(errorMessage, e);
        }
    }

    // Some data needs to be send otherwise the sse callback listener on Js will not be triggered
    public void sendQrLoginEvent(SseEmitter emitter){
        try{
            emitter.send(
                SseEmitter.event()
                    .name("onQrLogin")
                    .data("Placeholder data")
            );
        }catch (IOException e){
            e.printStackTrace();
            String errorMessage = "QrLogin event could not be sent to it's respective emitter";
            throw new QrScannedEventSendException(errorMessage, e);
        }
    }

    public void sendQrCancelEvent(SseEmitter emitter){
        try{
            emitter.send(
                    SseEmitter.event()
                            .name("onQrCancel")
                            .data("Placeholder data")
            );
        }catch (IOException e){
            e.printStackTrace();
            String errorMessage = "QrLogin event could not be sent to it's respective emitter";
            throw new QrScannedEventSendException(errorMessage, e);
        }
    }

}
