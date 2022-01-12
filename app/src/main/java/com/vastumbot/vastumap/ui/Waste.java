package com.vastumbot.vastumap.ui;

import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.Date;
import java.util.Objects;

public class Waste {
    public int id;
    public LatLng coord;
    public Date date;
    public String type;
    public String status;
    public int id_user;
    public GroundOverlay groundOverlay;
    public Marker marker;

    public Waste(int id, LatLng coord, Date date, String type, String status, int id_user) {
        this.id = id;
        this.coord = coord;
        this.date = date;
        this.type = type;
        this.status = status;
        this.id_user = id_user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Waste waste = (Waste) o;
        return id == waste.id && id_user == waste.id_user && Objects.equals(coord, waste.coord) && date.equals(waste.date) && type.equals(waste.type) && status.equals(waste.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, coord, date, type, status, id_user);
    }

    public boolean isSameType(String otherType){
        return this.type.equals(otherType);
    }
}
