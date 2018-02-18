package com.sample.messagededupe.aspect;

import com.sample.messagededupe.DeDuplicationService;
import com.sample.messagededupe.annotation.DeDupe;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DeDuplicationAspectTest {

    @InjectMocks
    private DeDuplicationAspect uut;

    @Mock
    private DeDuplicationService deDuplicationService;
    @Mock
    private JoinPoint jointPoint;
    @Mock
    private MethodSignature signature;

    private MockHttpServletRequest request = new MockHttpServletRequest();

    @Before
    public void setUp() throws Exception {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    public void deDupeMessage() {
        given(jointPoint.getSignature()).willReturn(signature);
        given(signature.getMethod()).willReturn(ReflectionUtils.findMethod(DeDupeMessageReceiver.class, "doStuff"));
        request.addHeader("cheese", "123");
        request.addHeader("wine", "graphite");

        uut.deDupeMessage(jointPoint);

        verify(deDuplicationService).deDupe("123", "graphite");
    }

    public static class DeDupeMessageReceiver {
        @DeDupe(message = "cheese", messageSource = "wine")
        public void doStuff() {

        }
    }

    @Test
    public void deDupeMessageDefault() {
        given(jointPoint.getSignature()).willReturn(signature);
        given(signature.getMethod()).willReturn(ReflectionUtils.findMethod(DeDupeDefaultMessageReceiver.class, "doStuff"));
        request.addHeader("message", "123");
        request.addHeader("message-source", "graphite");

        uut.deDupeMessage(jointPoint);

        verify(deDuplicationService).deDupe("123", "graphite");
    }

    public static class DeDupeDefaultMessageReceiver {
        @DeDupe()
        public void doStuff() {

        }
    }
}