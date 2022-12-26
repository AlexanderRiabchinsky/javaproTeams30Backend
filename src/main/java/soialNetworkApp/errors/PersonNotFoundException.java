package soialNetworkApp.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.function.Supplier;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PersonNotFoundException extends  RuntimeException implements Supplier<Exception> {
    private static final long serialVersionUID = 7428051251365675318L;

    public PersonNotFoundException(String message) {
        super(message);
    }

    @Override
    public Exception get() {
        return this;
    }
}
