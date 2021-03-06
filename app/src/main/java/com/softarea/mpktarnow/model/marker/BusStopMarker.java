package com.softarea.mpktarnow.model.marker;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.softarea.mpktarnow.R;
import com.softarea.mpktarnow.adapters.MapScheduleAdapter;
import com.softarea.mpktarnow.data.remote.dao.BusDAO;
import com.softarea.mpktarnow.model.BusStopInfoMapBox;
import com.softarea.mpktarnow.model.BusStopMapItem;

public class BusStopMarker implements MainMarker {
  private static final int MARKER_ZINDEX = 0;

  private Marker marker;
  private FragmentActivity activity;
  private GoogleMap map;
  private MapScheduleAdapter mapScheduleAdapter;
  private View view;
  private boolean canOpen = true;
  private BusStopMapItem busStop;

  public BusStopMarker(View view, FragmentActivity activity, GoogleMap map, BusStopMapItem busStop) {
    this.view = view;
    this.activity = activity;
    this.map = map;
    this.marker = map.addMarker(new MarkerOptions()
      .position(busStop.getPosition())
      .anchor(0.5f, 0.5f)
      .zIndex(MARKER_ZINDEX));
    this.busStop = busStop;
    this.marker.setIcon(BitmapDescriptorFactory.fromResource(getBusStopPin()));

    marker.setTag(this);
  }

  @Override
  public void onClick() {
    if(busStop.getId() == 0) {
      canOpen = false;
    }
    if (canOpen) {
      BusDAO.getBusStopInfo(handlerBusStopDetails, busStop.getId(), marker, busStop);

      mapScheduleAdapter = new MapScheduleAdapter(activity);
      map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
      map.setInfoWindowAdapter(mapScheduleAdapter);
    }
  }

  @Override
  public void onItemWindowClick() {
    if (busStop.getId() != 0) {
      Bundle result = new Bundle();
      result.putInt("id", busStop.getId());
      Navigation.findNavController(view).navigate(R.id.navigation_bus_stop_details, result);
    }
  }

  @Override
  public void setObject(Object object) {

  }

  @Override
  public boolean isOpen() {
    return false;
  }

  private int getBusStopPin() {
    if (busStop.getId() == 0) {
      return R.drawable.ic_buspoint_red;
    } else if (busStop.getIsCity() == 0) {
      return R.drawable.ic_buspoint;
    } else {
      return R.drawable.ic_buspoint_yellow;
    }
  }

  @Override
  public Marker getMarker() {
    return marker;
  }

  @SuppressLint("HandlerLeak")
  private final Handler handlerBusStopDetails = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      BusStopInfoMapBox busStopInfoMapBox = (BusStopInfoMapBox) msg.obj;
      mapScheduleAdapter.setRemoteDepartues(busStopInfoMapBox.getDepartues().getDepartueList());
      mapScheduleAdapter.setBusStopName(busStopInfoMapBox.getBusStop().getName());
      busStopInfoMapBox.getMarker().showInfoWindow();
    }
  };
}
