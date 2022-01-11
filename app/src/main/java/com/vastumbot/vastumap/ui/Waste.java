package com.vastumbot.vastumap.ui;

import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import java.util.Date;

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
}
