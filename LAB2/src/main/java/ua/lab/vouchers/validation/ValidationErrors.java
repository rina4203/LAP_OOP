package ua.lab.vouchers.validation;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

class ValidationErrors implements ErrorHandler {

    private final List<String> messages = new ArrayList<>();

    @Override
    public void warning(SAXParseException exception) {
        add(exception);
    }

    @Override
    public void error(SAXParseException exception) {
        add(exception);
    }

    @Override
    public void fatalError(SAXParseException exception)
            throws SAXParseException {
        add(exception);
        throw exception;
    }

    List<String> getMessages() {
        return List.copyOf(messages);
    }

    private void add(SAXParseException exception) {
        messages.add("рядок " + exception.getLineNumber() + ": "
                + exception.getMessage());
    }
}
