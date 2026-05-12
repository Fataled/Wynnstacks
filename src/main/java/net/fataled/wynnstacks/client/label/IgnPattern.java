package net.fataled.wynnstacks.client.label;

import net.fataled.wynnstacks.client.util.ClientUtils;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class IgnPattern {

    private static final Pattern NO_MATCH = Pattern.compile("(?!)");
    private IgnPattern(){

    }

    public static final IgnPattern INSTANCE = new IgnPattern();

    private final AtomicReference<Pattern> pattern = new AtomicReference<>(NO_MATCH);

    private Set<String> lastRoster = Collections.emptySet();

    public Pattern getPattern() { return pattern.get(); }

    public void resetPattern() {
        lastRoster = Collections.emptySet();
        pattern.set(NO_MATCH);
    }

    public void refreshIfChanged(){
        Set<String> current = ClientUtils.getOnlinePlayerNames();
        if(current.equals(lastRoster)) return ;

        lastRoster = current;
        if(current.isEmpty()){pattern.set(NO_MATCH); return ;}

        String alt = current.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        pattern.set(Pattern.compile(alt, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
    }

}
