package com.sample.aop;

import java.util.Collection;
import java.util.Set;

public interface Entitlements {

    Set<String> getAccessibleCountries(String user);

    boolean isAccessibleForCountries(String user, Collection<String> countries);
}
