package com.sample.messagededupe.aspect;

import com.sample.messagededupe.DeDuplicationService;
import com.sample.messagededupe.annotation.DeDupe;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
public class DeDuplicationAspect {

    private static final Logger LOG = LoggerFactory.getLogger(DeDuplicationAspect.class);
    private DeDuplicationService deDuplicationService;

    @Autowired
    public DeDuplicationAspect(DeDuplicationService deDuplicationService) {
        this.deDuplicationService = deDuplicationService;
    }

    @Pointcut("@annotation(com.sample.messagededupe.annotation.DeDupe) && execution(* *(..))")
    public void controllerMethodsForDeDupe() {
        //Used to facilitate the point cut only, adds description to the point cut for readability
    }

    @Pointcut("within(@org.springframework.stereotype.Controller * || @org.springframework.web.bind.annotation.RestController *)")
    public void controllerAnnotations() {
        //Used to facilitate the point cut only, adds description to the point cut for readability
    }

    @Before("controllerMethodsForDeDupe() && (controllerAnnotations())")
    public void deDupeMessage(JoinPoint joinPoint) {
        DeDupe deDupe = getDeDupeSpec(joinPoint);

        HttpServletRequest request = getRequest();

        String message = request.getHeader(deDupe.message());
        String messageSource = request.getHeader(deDupe.messageSource());
        LOG.debug("Received message [{}] from message source [{}]", message, messageSource);
        saveReceivedMessage(message, messageSource);
    }

    private DeDupe getDeDupeSpec(JoinPoint joinPoint) {
        joinPoint.getArgs();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        return method.getAnnotation(DeDupe.class);
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes());
        return servletRequestAttributes.getRequest();
    }

    private void saveReceivedMessage(String message, String source) {
        deDuplicationService.deDupe(message, source);
    }
}
