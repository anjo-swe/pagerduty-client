package com.github.dikhan.utils;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.dikhan.PagerDutyEventsClient;
import com.github.dikhan.domain.EventResult;
import com.github.dikhan.domain.Incident;
import com.github.dikhan.exceptions.NotifyEventException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class provided for integration testing purposes
 *
 * @author Daniel I. Khan Ramiro
 */
public class FakePagerDutyEventsClient extends PagerDutyEventsClient {

    private static final Logger log = LoggerFactory.getLogger(FakePagerDutyEventsClient.class);

    private final Set<Incident> openIncidents = new HashSet<>();
    private final Set<Incident> resolvedIncidents = new HashSet<>();
    private final Set<Incident> acknowledgedIncidents = new HashSet<>();
    private final Random random = new Random();

    private FakePagerDutyEventsClient(PagerDutyClientBuilder pagerDutyClientBuilder) {
        super(pagerDutyClientBuilder);
    }

    public static FakePagerDutyEventsClient create() {
        return new FakePagerDutyEventsClient(new PagerDutyClientBuilder());
    }

    @Override
    public EventResult trigger(Incident incident) throws NotifyEventException {
        EventResult eventResult = addIncidentAndCreateEventResult(openIncidents, incident);
        log.debug("Event result {}", eventResult);
        return eventResult;
    }

    @Override
    public EventResult acknowledge(String serviceKey, String incidentKey) throws NotifyEventException {
        Incident ack = Incident.IncidentBuilder.acknowledge(serviceKey, incidentKey);
        EventResult eventResult = addIncidentAndCreateEventResult(acknowledgedIncidents, ack);
        log.debug("Event result {} for acknowledge incident {}", eventResult, ack);
        return eventResult;
    }

    @Override
    public EventResult resolve(String serviceKey, String incidentKey) throws NotifyEventException {
        Incident resolve = Incident.IncidentBuilder.resolve(serviceKey, incidentKey);
        EventResult eventResult = addIncidentAndCreateEventResult(resolvedIncidents, resolve);
        log.debug("Event result {} for resolve incident {}", eventResult, resolve);
        return eventResult;
    }

    public Set<Incident> openIncidents() {
        Set<String> incidentKeysResolved = incidentKeysResolved();
        return openIncidents.stream()
                .filter(incidentKeysResolved::contains)
                .collect(Collectors.toSet());
    }

    public Set<Incident> resolvedIncidents() {
        return resolvedIncidents;
    }

    public Set<Incident> acknowledgedIncidents() {
        return acknowledgedIncidents;
    }

    private EventResult addIncidentAndCreateEventResult(Set<Incident> incidents, Incident incident) {
        incidents.add(incident);
        String incidentKey = StringUtils.isBlank(incident.getIncidentKey()) ? String.valueOf(random.nextLong()) : incident.getIncidentKey();
        return EventResult.successEvent("success-" + incident.getEventType().getEventType(), "Event processed", incidentKey);
    }

    public Set<String> incidentKeysResolved() {
        return resolvedIncidents.stream()
                .map(Incident::getIncidentKey)
                .collect(Collectors.toSet());
    }

}