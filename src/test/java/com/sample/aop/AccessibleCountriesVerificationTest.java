package com.sample.aop;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.util.ReflectionUtils;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.collection.IsArrayContainingInOrder.arrayContaining;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AccessibleCountriesVerificationTest {

    @InjectMocks
    private AccessibleCountriesVerification uut;

    @Mock
    private Authentication authentication;

    @Mock
    private Entitlements entitlements;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private UltimateTarget ultimateTarget;

    @Captor
    private ArgumentCaptor<Object[]> proceedingArgs;

    private TargetedObject target;

    @Before
    public void setUp() {
        target = new TargetedObject(ultimateTarget);

        given(joinPoint.getSignature()).willReturn(methodSignature);
        given(methodSignature.getMethod()).willReturn(ReflectionUtils.findMethod(TargetedObject.class, "methodWhichTakesCountries", String.class, List.class));
        given(authentication.getUserId()).willReturn("user");
    }

    @Test
    public void verifyAndPassEntitledCountries() throws Throwable {

        List<String> requestedCountries = Lists.newArrayList("country1", "country2");
        givenIRequestCountries(requestedCountries);

        givenIHaveAccessFor(requestedCountries);

        uut.verifyAndPassAccessibleCountries(joinPoint);

        verifyThatTargetMethodIsCalledWith(requestedCountries);
    }

    @Test
    public void verifyAndPassEntitledCountries_usesAllEntitledWhenNoneProvided() throws Throwable {

        Set<String> entitledCountries = Sets.newHashSet("country1", "country2");

        givenIRequestCountries(null);
        givenIHaveAccessFor(entitledCountries);

        uut.verifyAndPassAccessibleCountries(joinPoint);

        verifyThatTargetMethodIsCalledWith(entitledCountries);
    }

    @Test
    public void verifyAndPassEntitledCountries_emptyCollectionProvided() throws Throwable {

        Set<String> entitledCountries = Sets.newHashSet("country1", "country2");

        givenIRequestCountries(Collections.emptySet());
        givenIHaveAccessFor(entitledCountries);

        uut.verifyAndPassAccessibleCountries(joinPoint);

        verifyThatTargetMethodIsCalledWith(entitledCountries);
    }

    @Test(expected = RuntimeException.class)
    public void verifyAndPassEntitledCountries_noAccess() throws Throwable {
        Set<String> requestedCountries = Sets.newHashSet("sle");

        givenIRequestCountries(requestedCountries);
        givenIDoNotHaveEntitlementsFor(requestedCountries);

        uut.verifyAndPassAccessibleCountries(joinPoint);
    }

    private void givenIDoNotHaveEntitlementsFor(Set<String> countries) {
        given(entitlements.isAccessibleForCountries(eq("user"), eq(countries))).willReturn(false);
    }

    public void verifyThatTargetMethodIsCalledWith(Collection<String> requestedCountries) throws Throwable {
        verify(joinPoint).proceed(proceedingArgs.capture());
        assertThat(proceedingArgs.getValue(), arrayWithSize(2));
        final ArrayList<String> operand = Lists.newArrayList(requestedCountries);
        assertThat(proceedingArgs.getValue(), arrayContaining(equalTo("something"), equalTo(operand)));
    }

    public void givenIHaveAccessFor(Collection<String> countries) {
        given(entitlements.isAccessibleForCountries("user", countries)).willReturn(true);
        given(entitlements.getAccessibleCountries("user")).willReturn(Sets.newHashSet(countries));
    }

    public void givenIRequestCountries(Iterable<String> requestedCountries) {
        given(joinPoint.getArgs()).willReturn(new Object[]{"something", requestedCountries});
    }

    public static class TargetedObject {

        private UltimateTarget ultimateTarget;

        public TargetedObject(UltimateTarget ultimateTarget) {
            this.ultimateTarget = ultimateTarget;
        }

        public void methodWhichTakesCountries(String something, @Countries List<String> Countries) {
            ultimateTarget.methodWhichTakesCountries(something, Countries);
        }
    }

    private interface UltimateTarget {
        void methodWhichTakesCountries(String something, List<String> Countries);
    }
}