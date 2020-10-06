package com.nedap.archie.aom.primitives;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nedap.archie.ArchieLanguageConfiguration;
import com.nedap.archie.aom.Archetype;
import com.nedap.archie.aom.CObject;
import com.nedap.archie.aom.CPrimitiveObject;
import com.nedap.archie.aom.terminology.ArchetypeTerm;
import com.nedap.archie.aom.terminology.ArchetypeTerminology;
import com.nedap.archie.aom.terminology.TerminologyCodeWithArchetypeTerm;
import com.nedap.archie.aom.terminology.ValueSet;
import com.nedap.archie.aom.utils.AOMUtils;
import com.nedap.archie.aom.utils.CodeRedefinitionStatus;
import com.nedap.archie.aom.utils.ConformanceCheckResult;
import com.nedap.archie.base.terminology.TerminologyCode;
import com.nedap.archie.rminfo.ModelInfoLookup;
import org.openehr.utils.message.I18n;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 *
 * Created by pieter.bos on 15/10/15.
 */
@XmlType(name="C_TERMINOLOGY_CODE")
@XmlAccessorType(XmlAccessType.FIELD)
public class CTerminologyCode extends CPrimitiveObject<String, TerminologyCode> {

    @XmlElement(name="assumed_value")
    private TerminologyCode assumedValue;
    private List<String> constraint = new ArrayList<>();

    private ConstraintStatus constraintStatus;

    @Override
    public TerminologyCode getAssumedValue() {
        return assumedValue;
    }

    @Override
    public void setAssumedValue(TerminologyCode assumedValue) {
        this.assumedValue = assumedValue;
    }

    @Override
    public List<String> getConstraint() {
        return this.constraint;
    }

    @Override
    public void setConstraint(List<String> constraint) {
        this.constraint = constraint;
    }

    @Override
    public void addConstraint(String constraint) {
        this.constraint.add(constraint);
    }

    public ConstraintStatus getConstraintStatus() {
        return constraintStatus;
    }

    public void setConstraintStatus(ConstraintStatus constraintStatus) {
        this.constraintStatus = constraintStatus;
    }

    public boolean isConstraintRequired() {
        return getConstraintStatus() == null || getConstraintStatus() == ConstraintStatus.REQUIRED;
    }

    @Override
    public boolean isValidValue(TerminologyCode value) {
        if(getConstraint().isEmpty()) {
            return true;
        }
        if(isConstraintRequired()) {
            for (String constraint : getConstraint()) {
                if (constraint.startsWith("at")) {
                    if (value.getCodeString() != null && value.getCodeString().equals(constraint)) {
                        return true;
                    }
                } else if (constraint.startsWith("ac")) {
                    if (value.getTerminologyId() != null && value.getTerminologyId().equals(constraint)) {
                        return true;
                    }
                }
            }
        } else {
            return true;
        }
        return false;
    }


    /**
     * Get the ArchetypeTerms in the selected meaning and description language for all the possible options if this is a
     * locally defined terminology.
     * See the ArchieLanguageConfiguration for the language settings.
     *
     * @return
     */
    public List<TerminologyCodeWithArchetypeTerm> getTerms() {
        List<TerminologyCodeWithArchetypeTerm> result = new ArrayList<>();
        Archetype archetype = getArchetype();
        if(archetype == null) {
            //ideally this would not happen, but no reference to archetype exists in leaf constraints in rules so far
            //so for now fix it so it doesn't throw a NullPointerException
            return result;
        }
        ArchetypeTerminology terminology = archetype.getTerminology(this);
        String language = ArchieLanguageConfiguration.getMeaningAndDescriptionLanguage();
        String defaultLanguage = ArchieLanguageConfiguration.getDefaultMeaningAndDescriptionLanguage();
        for(String constraint:getConstraint()) {
            if(constraint.startsWith("at")) {
                ArchetypeTerm termDefinition = terminology.getTermDefinition(language, constraint);
                if(termDefinition == null) {
                    termDefinition = terminology.getTermDefinition(defaultLanguage, constraint);
                }
                if(termDefinition != null) {
                    result.add(new TerminologyCodeWithArchetypeTerm(constraint, termDefinition));
                }
            } else if (constraint.startsWith("ac")) {
                ValueSet acValueSet = terminology.getValueSets().get(constraint);
                if(acValueSet != null) {
                    for(String atCode:acValueSet.getMembers()) {
                        ArchetypeTerm termDefinition = terminology.getTermDefinition(language, atCode);
                        if(termDefinition == null) {
                            termDefinition = terminology.getTermDefinition(defaultLanguage, atCode);
                        }
                        if(termDefinition != null) {
                            result.add(new TerminologyCodeWithArchetypeTerm(atCode, termDefinition));
                        }
                    }
                }
            }
        }
        return result;
    }

    private void setTerms(List<TerminologyCodeWithArchetypeTerm> terms) {
        //hack for jackson to work
    }

    @JsonIgnore
    public List<String> getValueSetExpanded() {
        List<String> result = new ArrayList<>();
        Archetype archetype = getArchetype();
        if(archetype == null) {
            //ideally this would not happen, but no reference to archetype exists in leaf constraints in rules so far
            //so for now fix it so it doesn't throw a NullPointerException
            return result;
        }
        ArchetypeTerminology terminology = archetype.getTerminology(this);
        for(String constraint:getConstraint()) {
            if(constraint.startsWith("at")) {
                result.add(constraint);
            } else if (constraint.startsWith("ac")) {
                ValueSet acValueSet = terminology.getValueSets().get(constraint);
                if(acValueSet != null) {
                    result.addAll(acValueSet.getMembers());
                }
            }
        }
        return result;
    }

    @Override
    public ConformanceCheckResult cConformsTo(CObject other, BiFunction<String, String, Boolean> rmTypesConformant) {
        ConformanceCheckResult superResult = super.cConformsTo(other, rmTypesConformant);
        if(!superResult.doesConform()) {
            return superResult;
        }
        //now guaranteed to be the same class
        CTerminologyCode otherCode = (CTerminologyCode) other;
        List<String> valueSet = getValueSetExpanded();
        List<String> otherValueSet = otherCode.getValueSetExpanded();
        if(constraint.size() != 1) {
            return ConformanceCheckResult.fails(I18n.t("child CTerminology code contains more than one constraint, that is not valid. Constraints are: {0}", constraint));
        }
        if(otherCode.constraint.size() != 1) {
            return ConformanceCheckResult.fails(I18n.t("parent CTerminology code contains more than one constraint, that is not valid. Constraints are: {0}", constraint));
        }
        String thisConstraint = constraint.get(0);
        String otherConstraint = otherCode.constraint.get(0);
        Archetype archetype = this.getArchetype();
        int archetypeSpecialisationDepth = archetype == null ? 0 : archetype.specializationDepth();
        if(AOMUtils.isValidValueSetCode(thisConstraint) && AOMUtils.isValidValueSetCode(otherConstraint)) {
            if (otherValueSet.isEmpty()) {
                return ConformanceCheckResult.conforms();
            }
            if (!AOMUtils.codesConformant(thisConstraint, otherConstraint)) {
                return ConformanceCheckResult.fails(I18n.t("child terminology constraint value set code {0} does not conform to parent constraint with value set code {0}", thisConstraint, otherConstraint));
            }
            if(otherCode.isConstraintRequired()) {
                //if required, codes can be:
                // - reused directly
                // - specialized
                for (String value : valueSet) {
                    if( !valueSetContainsCodeOrSpecialization(otherValueSet, value)) {
                        return ConformanceCheckResult.fails(I18n.t("child terminology constraint value code {0} is not contained in {1}, or a direct specialization of one of its values", value, otherValueSet));
                    }
                }
            } else {
                //if not required, codes can be:
                // - reused directly
                // - specialized
                // - added
                //everything goes here.
                return ConformanceCheckResult.conforms();
//                for (String value : valueSet) {
//                    if(valueSetContainsCodeOrSpecialization(otherValueSet, value) ||
//                            AOMUtils.getSpecialisationStatusFromCode(value, archetypeSpecialisationDepth) == CodeRedefinitionStatus.ADDED) {
//                        return false;
//                    }
//                }
            }
            return ConformanceCheckResult.conforms();
        } else {
            if(!AOMUtils.codesConformant(thisConstraint, otherConstraint)) {
                return ConformanceCheckResult.fails(I18n.t("child terminology constraint value code {0} does not conform to parent constraint with value code {0}", thisConstraint, otherConstraint));
            }
            return ConformanceCheckResult.conforms();
        }
    }

    private boolean valueSetContainsCodeOrSpecialization(List<String> otherValueSet, String code) {
        for(String value:otherValueSet) {
            if(AOMUtils.codesConformant(code, value)) {
                return true;
            }
        }
        return false;
    }

}
