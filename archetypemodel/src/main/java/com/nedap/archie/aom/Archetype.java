package com.nedap.archie.aom;

import com.nedap.archie.aom.terminology.ArchetypeTerminology;

import java.util.ArrayList;
import java.util.List;

/**
 * Note: this Archetype is not 100% correct, as it should not be an AuthoredResource, but this makes things SO much easier
 * than implementing lots of interfaces
 * Created by pieter.bos on 15/10/15.
 */
public class Archetype extends AuthoredResource {

    private String parentArchetypeId;
    private Boolean differential;
    private ArchetypeHRID archetypeId;

    private CComplexObject definition;
    private ArchetypeTerminology terminology;
    private List<RuleStatement> rules = new ArrayList<>();

    public String getParentArchetypeId() {
        return parentArchetypeId;
    }

    public void setParentArchetypeId(String parentArchetypeId) {
        this.parentArchetypeId = parentArchetypeId;
    }

    public Boolean getDifferential() {
        return differential;
    }

    public void setDifferential(Boolean differential) {
        this.differential = differential;
    }

    public ArchetypeHRID getArchetypeId() {
        return archetypeId;
    }

    public void setArchetypeId(ArchetypeHRID archetypeId) {
        this.archetypeId = archetypeId;
    }

    public CComplexObject getDefinition() {
        return definition;
    }

    public void setDefinition(CComplexObject definition) {
        this.definition = definition;
    }

    public List<RuleStatement> getRules() {
        return rules;
    }

    public void setRules(List<RuleStatement> rules) {
        this.rules = rules;
    }

    public ArchetypeTerminology getTerminology() {
        return terminology;
    }

    public void setTerminology(ArchetypeTerminology terminology) {
        this.terminology = terminology;
    }
}