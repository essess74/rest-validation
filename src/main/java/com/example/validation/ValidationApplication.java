package com.example.validation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;

import javax.validation.*;
import java.lang.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication
public class ValidationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ValidationApplication.class, args);
    }

}


@RestController
class TestController {

    @PostMapping("/hello")
    public @ResponseBody
    String hello(@Valid @RequestBody Person person) {
        System.out.println(person);
        return "";
    }

}


class PhoneValidator implements ConstraintValidator<Phone, PhoneObject> {

    private static Pattern pattern;

    private static Matcher matcher;

    @Override
    public boolean isValid(PhoneObject value, ConstraintValidatorContext context) {
        if (value == null)
            return true;

        if (StringUtils.isEmpty(value.getValue())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("phone is required")
                    .addPropertyNode("value").addConstraintViolation();
            return false;
        }
        matcher = pattern.matcher(value.getValue());
        if (!matcher.matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("phone is invalid")
                    .addPropertyNode("value").addConstraintViolation();
            return false;
        }
        return true;
    }

    @Override
    public void initialize(Phone constraintAnnotation) {
        pattern = Pattern.compile(constraintAnnotation.pattern());
    }
}

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {PhoneValidator.class})
@interface Phone {
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String pattern();
}

class PhoneObject {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

class Person {

    private String firstName, lastName;

    @Phone(pattern = "^(0|\\+33)[1-9]([-. ]?[0-9]{2}){4}$")
    private PhoneObject phone;

    public PhoneObject getPhone() {
        return phone;
    }

    public void setPhone(PhoneObject phone) {
        this.phone = phone;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Person{");
        sb.append("firstName='").append(firstName).append('\'');
        sb.append(", lastName='").append(lastName).append('\'');
        sb.append(", phone='").append(phone).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

@ControllerAdvice
class ExceptionHandling {


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = WebExchangeBindException.class)
    public @ResponseBody
    Map<String, String> handleException(WebExchangeBindException exception) {

        String errorMsg = exception.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse(exception.getMessage());

        final Map<String, String> hello = new HashMap<>();
        hello.putIfAbsent("err", errorMsg);
        return hello;
    }

}