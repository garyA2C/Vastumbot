package com.vastumbot.vastumap.ui;

import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import java.util.Date;
import java.util.Objects;

public class Waste {
    private int id;
    private LatLng coord;
    private Date date;
    private String type;
    private String status;
    private int id_user;
    private GroundOverlay groundOverlay;

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
}
