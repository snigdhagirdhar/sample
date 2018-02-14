package com.sample.aop;

import com.google.common.collect.Iterables;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Aspect
public class AccessibleCountriesVerification {

    public static final Logger LOG = LoggerFactory.getLogger(AccessibleCountriesVerification.class);

    private Entitlements entitlements;

    private Authentication authentication;

    @Autowired
    public AccessibleCountriesVerification(Entitlements entitlements, Authentication authentication) {
        this.entitlements = entitlements;
        this.authentication = authentication;
    }

    @Pointcut("execution(* *(.., @com.sample.aop.Countries (*), ..))")
    public void controllerMethodsTakingCountries() {}

    @Pointcut("@within(org.springframework.stereotype.Controller)")
    public void allControllerMethods() {}

    @Around("controllerMethodsTakingCountries()")
    public Object verifyAndPassAccessibleCountries(ProceedingJoinPoint joinPoint) throws Throwable {
        String user = authentication.getUserId();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final Method method = signature.getMethod();

        Object[] args = joinPoint.getArgs();
        Parameter[] parameters = method.getParameters();

        List<Object> proceedingArgs = IntStream.range(0, parameters.length)
                .boxed()
                .map(idx -> getAccessibleCountriesOrOriginalValue(user, parameters[idx], args[idx]))
                .collect(Collectors.toList());

        return joinPoint.proceed(proceedingArgs.toArray(new Object[proceedingArgs.size()]));
    }

    private Object getAccessibleCountriesOrOriginalValue(String user, Parameter param, Object argValue) {
        return isCountriesParam(param) ? getAccessibleCountries(user, argValue) : argValue;
    }

    private boolean isCountriesParam(Parameter param) {
        return param.isAnnotationPresent(Countries.class);
    }

    private Object getAccessibleCountries(String user, Object countries) {
        if(isNullOrEmpty((Iterable<?>) countries)) {
            final Set<String> countriesSet = entitlements.getAccessibleCountries(user);
            LOG.debug("No countriesprovided by user {}, using all Accessible countries: {}", user, countriesSet);
            return new ArrayList(countriesSet);
        }
        LOG.debug("Checking privileges for user {} and contries {}", user, countries);
        if (entitlements.isAccessibleForCountries(user, (Collection<String>) countries)) {
            LOG.debug("Sap legal entity check passed for user {} and countries{}", user, countries);
            return new ArrayList<>((Collection<? extends String>) countries);
        }
        LOG.debug("User {} does not have entitlements for all requested countries", user, countries);
        throw new RuntimeException("User does not have privileges for all requested Countries");
    }

    private boolean isNullOrEmpty(Iterable<?> items) {
        return items == null || Iterables.isEmpty(items);
    }

}
