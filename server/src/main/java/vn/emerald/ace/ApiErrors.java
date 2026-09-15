package vn.emerald.ace;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import java.util.Map;
@RestControllerAdvice
public class ApiErrors {
 @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class) ResponseEntity<?> duplicate(Exception e){return ResponseEntity.status(409).body(Map.of("code","DUPLICATE","message","Already exists"));}
 @ExceptionHandler(GameService.ApiError.class) ResponseEntity<?> api(GameService.ApiError e){return ResponseEntity.status(e.status).body(Map.of("code",e.code,"message",e.getMessage()));}
 @ExceptionHandler({MethodArgumentNotValidException.class,HttpMessageNotReadableException.class}) ResponseEntity<?> invalid(Exception e){return ResponseEntity.badRequest().body(Map.of("code","INVALID_REQUEST","message","Yêu cầu không hợp lệ."));}
}
