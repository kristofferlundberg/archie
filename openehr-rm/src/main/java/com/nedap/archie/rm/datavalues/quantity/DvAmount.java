package com.nedap.archie.rm.datavalues.quantity;

import com.nedap.archie.rm.datatypes.CodePhrase;
import com.nedap.archie.rm.datavalues.quantity.datetime.DvDuration;
import com.nedap.archie.rminfo.Invariant;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.Objects;

/**
 * Created by pieter.bos on 04/11/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DV_AMOUNT", propOrder = {
        "accuracy",
        "accuracyIsPercent"
})
@XmlSeeAlso({
        DvProportion.class,
        DvCount.class,
        DvDuration.class,
        DvQuantity.class
})
public abstract class DvAmount<MagnitudeType extends Comparable> extends DvQuantified<Double, MagnitudeType> {
    @Nullable
    private Double accuracy;
    @Nullable
    @XmlElement(name = "accuracy_is_percent")
    private Boolean accuracyIsPercent;

    public DvAmount() {
    }

    public DvAmount(@Nullable List<ReferenceRange> otherReferenceRanges, @Nullable DvInterval normalRange, @Nullable CodePhrase normalStatus, @Nullable Double accuracy, @Nullable Boolean accuracyIsPercent, @Nullable String magnitudeStatus) {
        super(otherReferenceRanges, normalRange, normalStatus, magnitudeStatus);
        this.accuracy = accuracy;
        this.accuracyIsPercent = accuracyIsPercent;
    }

    @Nullable
    public Boolean getAccuracyIsPercent() {
        return accuracyIsPercent;
    }

    public void setAccuracyIsPercent(@Nullable Boolean accuracyIsPercent) {
        this.accuracyIsPercent = accuracyIsPercent;
    }

    @Override
    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DvAmount<?> dvAmount = (DvAmount<?>) o;
        return Objects.equals(accuracy, dvAmount.accuracy) &&
                Objects.equals(accuracyIsPercent, dvAmount.accuracyIsPercent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), accuracy, accuracyIsPercent);
    }

    @Invariant("Accuracy_is_percent_validity")
    public boolean accuracyIsPercentValidity() {
        if (accuracy != null && accuracyIsPercent != null && accuracyIsPercent) {
            return accuracy != 0;
        }
        return true;
    }

    @Invariant("Accuracy_valid")
    public boolean accuracyValid() {
        if(accuracyIsPercent != null && accuracy != null && accuracyIsPercent) {
            return accuracy <= 100 && accuracy >= 0;
        }
        return true;
    }

}
