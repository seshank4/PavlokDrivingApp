package edu.bu.cs591.ateam.pavlokdrivingapp;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

/**
 * Created by sesha on 11/28/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TomTomResponse {

    private int buildingNumber;
    private int streetNumber;
    private String street;
    private String speedLimit;
    private String streetName;
    private String countyCode;
    private String countySubdivision;
    private String postalCode;
    private String freeformAddress;
    private String municipalitySubdivision;
    public int getBuildingNumber() {
        return buildingNumber;
    }

    public String getMunicipalitySubdivision() {
        return municipalitySubdivision;
    }

    public void setMunicipalitySubdivision(String municipalitySubdivision) {
        this.municipalitySubdivision = municipalitySubdivision;
    }

    public void setBuildingNumber(int buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    public int getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(int streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getSpeedLimit() {
        return speedLimit;
    }

    public void setSpeedLimit(String speedLimit) {
        this.speedLimit = speedLimit;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getCountyCode() {
        return countyCode;
    }

    public void setCountyCode(String countyCode) {
        this.countyCode = countyCode;
    }

    public String getCountySubdivision() {
        return countySubdivision;
    }

    public void setCountySubdivision(String countySubdivision) {
        this.countySubdivision = countySubdivision;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getFreeformAddress() {
        return freeformAddress;
    }

    public void setFreeformAddress(String freeformAddress) {
        this.freeformAddress = freeformAddress;
    }

}

