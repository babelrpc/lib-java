package com.concur.babel.exception;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static com.concur.babel.test.IsMapWithSize.*;

/**
 * BabelExceptionTest is the unit test class for BabelException.
 */
@RunWith(JUnit4.class)
public class BabelExceptionTest {

    @Test
    public void addContextWithNameValuePair() {

        BabelException e = new BabelException("9999", "Error Message");
        e.addContext("mykey", "myvalue");
        assertThat(e.getServiceError().getContext(), allOf(
            is(aMapWithSize(1)),
            hasKey("mykey")
        ));
        assertThat(e.getServiceError().getContext().get("mykey"), allOf(
            is(aMapWithSize(1)),
            hasEntry("value", "myvalue")
        ));

    }

    @Test
    public void addContextWithNameValuePairUsingSuppliedMapOfMapKeyValue() {

        BabelException e = new BabelException("9999", "Error Message");
        e.addContext("mykey", "subname", "myvalue");
        assertThat(e.getServiceError().getContext(), allOf(
            is(aMapWithSize(1)),
            hasKey("mykey")
        ));
        assertThat(e.getServiceError().getContext().get("mykey"), allOf(
            is(aMapWithSize(1)),
            hasEntry("subname", "myvalue")
        ));

    }

    @Test
    public void addContextWithKeyAndMapOfValues() {

        BabelException e = new BabelException("9999", "Error Message");
        e.addContext("mykey", new HashMap<String, String>(){{put("subname", "myvalue");}});
        assertThat(e.getServiceError().getContext(), allOf(
            is(aMapWithSize(1)),
            hasKey("mykey")
        ));
        assertThat(e.getServiceError().getContext().get("mykey"), allOf(
            is(aMapWithSize(1)),
            hasEntry("subname", "myvalue")
        ));

    }

    @Test
    public void getMessageShouldReturnExpectedValueWhenThereIsOneErrorAndNotErrorCodes() {
        BabelException e = new BabelException("Msg1");
        assertThat(e.getMessage(), is(equalTo("Msg1")));
    }

    @Test
    public void getMessageShouldReturnExpectedValueWhenThereAreManyErrorsAndNotErrorCodes() {
        BabelException e = new BabelException("Msg1");
        e.addError("Msg2");

        assertThat(e.getMessage(), is(equalTo("Msg1\nMsg2")));
    }

    @Test
    public void getMessageShouldReturnExpectedValueWhenThereIsOneErrorAndErrorCodesExist() {
        BabelException e = new BabelException("Code100", "Msg1");
        assertThat(e.getMessage(), is(equalTo("ErrorCode: Code100 - Msg1")));
    }

    @Test
    public void getMessageShouldReturnExpectedValueWhenThereAreManyErrorsAndErrorCodesExist() {
        BabelException e = new BabelException("Code100", "Msg1");
        e.addError("Msg2", "Code200");

        assertThat(e.getMessage(), is(equalTo("ErrorCode: Code100 - Msg1\nErrorCode: Code200 - Msg2")));
    }

    @Test
    public void getMessageShouldReturnExpectedValueWhenThereAreManyErrorsWithAMixOfErrorCodes() {
        BabelException e = new BabelException("Code100", "Msg1");
        e.addError("Msg2");
        e.addError("Msg3", "Code300");

        assertThat(e.getMessage(), is(equalTo("ErrorCode: Code100 - Msg1\nMsg2\nErrorCode: Code300 - Msg3")));
    }

    @Test
    public void getLocalizedMessageShouldReturnExpectedValueWhenThereIsOneErrorAndNotErrorCodes() {
        BabelException e = new BabelException("Msg1");
        assertThat(e.getLocalizedMessage(), is(equalTo("Msg1")));
    }

    @Test
    public void getLocalizedMessageShouldReturnExpectedValueWhenThereAreManyErrorsAndNotErrorCodes() {
        BabelException e = new BabelException("Msg1");
        e.addError("Msg2");

        assertThat(e.getLocalizedMessage(), is(equalTo("Msg1\nMsg2")));
    }

    @Test
    public void getLocalizedMessageShouldReturnExpectedValueWhenThereIsOneErrorAndErrorCodesExist() {
        BabelException e = new BabelException("Code100", "Msg1");
        assertThat(e.getLocalizedMessage(), is(equalTo("ErrorCode: Code100 - Msg1")));
    }

    @Test
    public void getLocalizedMessageShouldReturnExpectedValueWhenThereAreManyErrorsAndErrorCodesExist() {
        BabelException e = new BabelException("Code100", "Msg1");
        e.addError("Msg2", "Code200");

        assertThat(e.getLocalizedMessage(), is(equalTo("ErrorCode: Code100 - Msg1\nErrorCode: Code200 - Msg2")));
    }

    @Test
    public void getLocalizedMessageShouldReturnExpectedValueWhenThereAreManyErrorsWithAMixOfErrorCodes() {
        BabelException e = new BabelException("Code100", "Msg1");
        e.addError("Msg2");
        e.addError("Msg3", "Code300");

        assertThat(e.getLocalizedMessage(), is(equalTo("ErrorCode: Code100 - Msg1\nMsg2\nErrorCode: Code300 - Msg3")));
    }

}
