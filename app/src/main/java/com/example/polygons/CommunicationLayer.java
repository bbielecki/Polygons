package com.example.polygons;

/**
 * Created by Bartłomiej on 14.04.2017.
 */

/*
class implementing adapter design patern to allow an application creating routs, checking user location,
drawing points of user presence and so on...
TODO: napisz to ladnie
 */
public final class CommunicationLayer {
    private static PolyActivity polyActivity;
    private MyLocation myLocation;

    public CommunicationLayer(){
    }

    public void registerPolyActivity(PolyActivity pa){
        polyActivity = pa;
    }

    public void drawRoute(){

    }
}
